package com.example.api;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.example.domain.GroupMembership;
import com.example.domain.SecretSantaGroup;
import com.example.domain.SecretSantaPairing;
import com.example.security.AppUser;
import com.example.security.SecurityUtils;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/groups")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupResource {

    @Inject
    SecurityUtils securityUtils;

    @Inject
    Mailer mailer;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static class CreateMembershipRequest {
        public String participantName;
        public String participantEmail;
        public String wishlist;
    }

    @POST
    @RolesAllowed("user")
    @Transactional
    public Response createGroup(CreateGroupRequest request) {
        String currentEmail = securityUtils.currentUsername();
        AppUser owner = AppUser.find("email", currentEmail).firstResult();
        if (owner == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        SecretSantaGroup group = new SecretSantaGroup();
        group.name = request.name;
        group.inviteCode = generateInviteCode();
        group.owner = owner;
        group.persist();

        return Response.status(Response.Status.CREATED).entity(group).build();
    }

    @POST
    @Path("{groupId}/members")
    @RolesAllowed("user")
    @Transactional
    public Response addMember(@PathParam("groupId") Long groupId,
            CreateMembershipRequest request) {
        SecretSantaGroup group = SecretSantaGroup.findById(groupId);
        if (group == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        GroupMembership member = new GroupMembership();
        member.group = group;
        member.participantName = request.participantName;
        member.participantEmail = request.participantEmail;
        member.wishlist = request.wishlist;
        member.persist();

        GroupMembershipResponse response = new GroupMembershipResponse(
            member.id,
            group.id,
            group.name,
            member.participantName,
            member.participantEmail,
            member.wishlist
        );

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("{groupId}/pairings")
    @RolesAllowed("user")
    @Transactional
    public Response generatePairings(@PathParam("groupId") Long groupId) {
        SecretSantaGroup group = SecretSantaGroup.findById(groupId);
        if (group == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<GroupMembership> members = GroupMembership.list("group", group);
        if (members.size() < 2) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Need at least 2 members to generate pairings").build();
        }

        // Clear existing pairings for this group
        SecretSantaPairing.delete("group", group);

        List<GroupMembership> givers = new ArrayList<>(members);
        List<GroupMembership> receivers = new ArrayList<>(members);

        List<SecretSantaPairing> pairings = createDerangement(group, givers, receivers);

        // send emails (in a real app, move to async)
        for (SecretSantaPairing p : pairings) {
            String subject = "Your Secret Santa assignment for group " + group.name;
            String body = """

                    Ho ho ho %s!

                    You are the Secret Santa for: %s

                    Wishlist:
                    %s

                    Please keep it secret and have fun!
                    """.formatted(
                    p.giver.participantName,
                    p.receiver.participantName,
                    Optional.ofNullable(p.receiver.wishlist).orElse("(no wishlist provided)"));

            mailer.send(
                    Mail.withText(p.giver.participantEmail, subject, body));
        }

        // Convert to DTOs to avoid lazy initialization issues
        List<PairingResponse> response = pairings.stream()
                .map(p -> new PairingResponse(
                        p.id,
                        p.group.id,
                        p.group.name,
                        p.giver.id,
                        p.giver.participantName,
                        p.giver.participantEmail,
                        p.receiver.id,
                        p.receiver.participantName,
                        p.receiver.participantEmail,
                        p.receiver.wishlist
                ))
                .toList();

        return Response.ok(response).build();
    }

    private static List<SecretSantaPairing> createDerangement(SecretSantaGroup group,
            List<GroupMembership> givers,
            List<GroupMembership> receivers) {
        List<SecretSantaPairing> result = new ArrayList<>();
        boolean valid;
        int attempts = 0;

        do {
            Collections.shuffle(receivers, RANDOM);
            valid = true;
            for (int i = 0; i < givers.size(); i++) {
                if (Objects.equals(givers.get(i).id, receivers.get(i).id)) {
                    valid = false;
                    break;
                }
            }
            attempts++;
        } while (!valid && attempts < 1000);

        if (!valid) {
            throw new IllegalStateException("Could not generate valid pairings");
        }

        for (int i = 0; i < givers.size(); i++) {
            SecretSantaPairing pairing = new SecretSantaPairing();
            pairing.group = group;
            pairing.giver = givers.get(i);
            pairing.receiver = receivers.get(i);
            pairing.persist();
            result.add(pairing);
        }

        return result;
    }

    private String generateInviteCode() {
        return Integer.toHexString(RANDOM.nextInt()).substring(0, 6)
                .toUpperCase(Locale.ROOT);
    }
}
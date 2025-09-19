package com.example.poll;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/ui/polls")
public class PollPage {

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance list(java.util.List<Poll> polls);

        public static native TemplateInstance detail(Poll poll);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list() {
        return Templates.list(Poll.listAll());
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance detail(@PathParam("id") Long id) {
        Poll poll = Poll.findById(id);
        if (poll == null)
            throw new NotFoundException();
        return Templates.detail(poll);
    }

    @POST
    @Path("/{id}/slots/{slotId}/vote")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response vote(@PathParam("id") Long pollId,
            @PathParam("slotId") Long slotId,
            @FormParam("participant") String participant) {
        TimeSlot slot = TimeSlot.findById(slotId);
        if (slot == null || !slot.poll.id.equals(pollId)) {
            throw new NotFoundException();
        }
        Vote vote = new Vote();
        vote.participant = participant;
        vote.timeSlot = slot;
        vote.persist();

        PollSocket.broadcastUpdate(pollId);

        return Response.seeOther(java.net.URI.create("/ui/polls/" + pollId)).build();
    }
}
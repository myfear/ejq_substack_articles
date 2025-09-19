package com.example.poll;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/polls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PollResource {

    @GET
    public List<Poll> allPolls() {
        return Poll.listAll();
    }

    @POST
    @Transactional
    public Poll createPoll(Poll poll) {
        // Set the poll reference for each slot
        if (poll.slots != null) {
            for (TimeSlot slot : poll.slots) {
                slot.poll = poll;
            }
        }
        poll.persist();
        return poll;
    }

    @POST
    @Path("/{id}/slots/{slotId}/vote")
    @Transactional
    public Vote vote(@PathParam("id") Long pollId,
            @PathParam("slotId") Long slotId,
            Vote vote) {
        TimeSlot slot = TimeSlot.findById(slotId);
        if (slot == null || !slot.poll.id.equals(pollId)) {
            throw new NotFoundException();
        }
        vote.timeSlot = slot;
        vote.persist();

        PollSocket.broadcastUpdate(pollId);
        return vote;
    }
}
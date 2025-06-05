package org.acme;

import java.util.Optional;

import org.acme.model.CreditLine;
import org.acme.service.CreditLineService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/credit-lines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreditLineResource {

    @Inject
    CreditLineService creditLineService;

    public static class CreditLineRequest {
        public String customerName;
        public String customerEmail;
        public double requestedAmount;
    }

    @POST
    public Response initiateCreditLine(CreditLineRequest request) {
        CreditLine creditLine = creditLineService.initiateCreditLine(
                request.customerName,
                request.customerEmail,
                request.requestedAmount);
        // In a real application, you might queue the approval process for asynchronous
        // execution For simplicity, we'll call it directly here.
        creditLineService.processApproval(creditLine.id);
        return Response.status(Response.Status.CREATED).entity(creditLine).build();
    }

    @GET
    @Path("/{id}")
    public Response getCreditLineStatus(@PathParam("id") Long id) {
        Optional<CreditLine> creditLine = creditLineService.findById(id);
        if (creditLine.isPresent()) {
            return Response.ok(creditLine.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}

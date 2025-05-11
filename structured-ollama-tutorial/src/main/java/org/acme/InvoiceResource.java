package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/invoice")
public class InvoiceResource {

    @Inject
    InvoiceParser parser;

    @POST
    @Path("/parse")
    @Consumes("text/plain")
    @Produces("application/json")
    public Invoice parse(String text) {
        return parser.parseInvoice(text, CurrentDate.CURRENT_DATE_STR);
    }
}

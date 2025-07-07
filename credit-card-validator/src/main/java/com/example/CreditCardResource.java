package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class CreditCardResource {

    @GET
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public ValidationResult validate(@QueryParam("cardNumber") String cardNumber) {
        boolean valid = LuhnAlgorithm.isValid(cardNumber);
        String brand = CardBrandDetector.detect(cardNumber).name();
        return new ValidationResult(valid, brand);
    }

    public record ValidationResult(boolean valid, String brand) {
    }

    @Inject
    CreditCardAiService aiService;

    @POST
    @Path("/withAi")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String chatWithValidator(String message) {
        return aiService.chat(message);
    }

}
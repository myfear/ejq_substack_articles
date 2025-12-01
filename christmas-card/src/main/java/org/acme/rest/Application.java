package org.acme.rest;

import org.acme.model.CardData;

import io.quarkiverse.renarde.Controller;
import io.quarkiverse.renarde.pdf.Pdf;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class Application extends Controller {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance christmasCard(CardData card);
    }

    @GET
    @Path("/christmasPngCard")
    @Produces(Pdf.IMAGE_PNG)
    public TemplateInstance christmasCard() {

        CardData card = new CardData(
            "Dear Readers of The Main Thread",
            "Thank you for being part of this journey. Your time, curiosity, and constant support mean more than you know. Wishing you peace, joy, and a warm home full of good memories this festive season.",
            "Markus",
            "2025"
        );

        return Templates.christmasCard(card);
    }

    @GET
    @Path("/christmasHtmlCard")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance christmasCardHTML() {

        CardData card = new CardData(
            "Dear Readers of The Main Thread",
            "Thank you for being part of this journey. Your time, curiosity, and constant support mean more than you know. Wishing you peace, joy, and a warm home full of good memories this festive season.",
            "Markus",
            "2025"
        );

        return Templates.christmasCard(card);
    }
}

package com.example;

import java.net.URI;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

@Path("/")
public class UiResource {

    @Inject
    Template index;
    @Inject
    ShortenerService shortener;
    @Inject
    QRCodeService qrs;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String home() {
        return index.data("result", null).render();
    }

    @GET
    @Path("/shorten")
    public Response redirectToRoot() {
        return Response.status(Response.Status.FOUND)
                .header("Location", "/")
                .build();
    }

    @POST
    @Path("/shorten")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String create(@FormParam("originalUrl") String url,
            @Context UriInfo uriInfo) {
        ShortLink link = shortener.createShortLink(url);
        String shortUrl = uriInfo.getBaseUriBuilder().path(link.key).build().toString();
        var result = new Result(link.originalUrl, shortUrl, "/qr/" + link.key);
        return index.data("result", result).render();
    }

    @GET
    @Path("/{key}")
    public Response redirect(@PathParam("key") String key) {
        return shortener.getOriginalUrl(key)
                .map(u -> Response.status(Response.Status.FOUND).location(URI.create(u)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/qr/{key}")
    @Produces("image/svg+xml")
    public Response qr(@PathParam("key") String key, @Context UriInfo uriInfo) {

        if (key == null || key.isBlank()) {
            return Response
                    .status(Status.BAD_REQUEST)
                    .entity("QR Code data cannot be null or empty")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        String shortUrl = uriInfo.getBaseUriBuilder().path(key).build().toString();
        String svg = QRCodeService.toSvgString(qrs.generateQrCode(shortUrl), 4, "#FFFFFF", "#000000", true);

        return Response.ok(svg)
                .build();

    }

    public record Result(String originalUrl, String shortUrl, String qrCodeUrl) {
    }
}
package org.acme.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

import org.acme.domain.Product;
import org.acme.repo.ProductRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductRepo repo;

    @Context
    Request request;

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") String id) {
        Product p = repo.find(id);
        if (p == null)
            throw new NotFoundException("No product " + id);

        EntityTag etag = new EntityTag(strongEtag(p));
        // Handle conditional GET: If-None-Match -> 304
        Response.ResponseBuilder precond = request.evaluatePreconditions(etag);
        if (precond != null) {
            return precond
                    .cacheControl(cacheOneMinute())
                    .build(); // 304
        }

        return Response.ok(p)
                .tag(etag)
                .cacheControl(cacheOneMinute())
                .build();
    }

    @PUT
    @Path("{id}/stock/{qty}")
    public Response updateStock(@PathParam("id") String id, @PathParam("qty") int qty) {
        Product updated = repo.updateStock(id, qty);
        if (updated == null)
            throw new NotFoundException();
        EntityTag etag = new EntityTag(strongEtag(updated));
        return Response.ok(updated)
                .tag(etag)
                .cacheControl(noStore()) // writes are not cacheable
                .build();
    }

    private static CacheControl cacheOneMinute() {
        CacheControl cc = new CacheControl();
        cc.setMaxAge((int) Duration.ofMinutes(1).getSeconds());
        cc.setPrivate(false);
        return cc;
    }

    private static CacheControl noStore() {
        CacheControl cc = new CacheControl();
        cc.setNoStore(true);
        return cc;
    }

    // Strong ETag based on immutable fields that change on update
    private static String strongEtag(Product p) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String payload = p.id() + "|" + p.name() + "|" + p.stock() + "|" + p.lastUpdated().toEpochMilli();
            byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
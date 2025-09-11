package org.acme.api;

import org.acme.domain.Product;
import org.acme.repo.ProductRepo;

import io.quarkiverse.bucket4j.runtime.RateLimited;
import io.quarkiverse.bucket4j.runtime.resolver.IpResolver;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

@Path("/limited-products")
@Produces(MediaType.APPLICATION_JSON)
public class RateLimitedProductResource {

    @Inject
    ProductRepo repo;

    @Context
    Request request;

    @GET
    @Path("{id}")
    @RateLimited(bucket = "api", identityResolver = IpResolver.class)
    public Response getLimited(@PathParam("id") String id) {
        Product p = repo.find(id);
        if (p == null)
            throw new NotFoundException();
        EntityTag etag = new EntityTag(Integer.toHexString(p.hashCode()));
        Response.ResponseBuilder precond = request.evaluatePreconditions(etag);
        if (precond != null)
            return precond.build();
        return Response.ok(p).tag(etag).build();
    }
}
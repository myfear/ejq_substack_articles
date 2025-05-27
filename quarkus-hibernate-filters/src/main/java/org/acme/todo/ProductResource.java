package org.acme.todo;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductService service;

    @POST
    public Response add(Product p) {
        service.addProduct(p);
        return Response.status(Response.Status.CREATED).entity(p).build();
    }

    @GET
    @Path("/active")
    public List<Product> getActive() {
        return service.getActiveProducts();
    }

    @GET
    @Path("/deleted")
    public List<Product> getDeleted() {
        return service.getDeletedProducts();
    }

    @GET
    @Path("/all")
    public List<Product> getAll() {
        return service.getAllProductsIncludingDeleted();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id,
            @QueryParam("includeDeleted") @DefaultValue("false") boolean include) {
        Product p = service.findById(id, include);
        if (p == null || (!include && p.deleted))
            return Response.status(Response.Status.NOT_FOUND).entity("Product not found.").build();
        return Response.ok(p).build();
    }

    @DELETE
    @Path("/{id}")
    public Response softDelete(@PathParam("id") Long id) {
        return service.softDeleteProduct(id)
                ? Response.ok("Soft-deleted.").build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{id}/restore")
    public Response restore(@PathParam("id") Long id) {
        return service.restoreProduct(id)
                ? Response.ok("Restored.").build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
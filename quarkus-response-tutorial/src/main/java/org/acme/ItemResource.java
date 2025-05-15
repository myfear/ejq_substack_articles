package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
public class ItemResource {

    private static final Map<String, Item> items = new ConcurrentHashMap<>();

    static {
        items.put("1", new Item("1", "Sample Item", "This is a sample item."));
        items.put("2", new Item("2", "Another Item", "This is another item."));
    }

    @GET
    @Path("/simple/{id}")
    public Response getItem(@PathParam("id") String id) {
        Item item = items.get(id);
        return (item != null)
                ? Response.ok(item).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/simple-list")
    public Response getAllItems() {
        return Response.ok(new ArrayList<>(items.values())).build();
    }

    @GET
    @Path("/direct/{id}")
    public Item getDirect(@PathParam("id") String id) {
        Item item = items.get(id);
        if (item == null) {
            throw new NotFoundException("Item with id " + id + " not found.");
        }
        return item;
    }

    @GET
    @Path("/empty")
    public Response getEmpty() {
        return Response.noContent().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createItem(Item item, @Context UriInfo uriInfo) {
        // Simple ID generation for the example
        item.id = UUID.randomUUID().toString();
        items.put(item.id, item);

        // Build URI for the newly created resource for the Location header
        URI location = uriInfo.getAbsolutePathBuilder().path(item.id).build();
        // 201 Created with location header and the created item in the body
        return Response.created(location).entity(item).build();
    }

    @GET
    @Path("/notfound-example/{id}")
    public Response getItemByIdWithManualNotFound(@PathParam("id") String id) {
        Item item = items.get(id);
        if (item != null) {
            return Response.ok(item).build();
        } else {
            // Explicitly return 404 Not Found
            // The entity here is a simple String, but it will be wrapped as JSON
            // because of the class-level @Produces. For a plain text error,
            // you'd override .type(MediaType.TEXT_PLAIN).
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Item with id " + id + " not found.\"}")
                    // .type(MediaType.APPLICATION_JSON) // Redundant if class @Produces is JSON and
                    // entity is complex enough or a String that looks like JSON
                    .build();
        }
    }

    @GET
    @Path("/with-headers")
    public Response getItemWithCustomHeaders() {
        Item item = new Item("id789", "Header Item", "Item with custom headers.");
        return Response.ok(item) // Start with 200 OK and the item body
                .header("X-Custom-Header", "MyValue123")
                .header("X-Another-Header", "AnotherCoolValue")
                .cookie(new NewCookie.Builder("session-id") // Example: setting a cookie
                        .value("abcxyz789")
                        .path("/items")
                        .domain("localhost") // Be careful with domain in real apps
                        .maxAge(3600) // 1 hour
                        .secure(false) // true in production over HTTPS
                        .httpOnly(true)
                        .build())
                .build();
    }

    @GET
    @Path("/{id}") // This will be our main GET by ID endpoint
    public Response getItemById(@PathParam("id") String id) {
        Item item = items.get(id);

        if (item != null) {
            return Response.ok(item).build();
        } else {
            ErrorResponse error = new ErrorResponse("E404_ITEM_NOT_FOUND", "Item with id '" + id + "' not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error) // Jackson will serialize this to JSON
                    .build();
        }
    }

    @GET
    @Path("/mapped/{id}") // Ensure this is the active method for /items/{id}
    public Response getItemByIdMappedException(@PathParam("id") String id) {
        Item item = items.get(id);

        if (item != null) {
            return Response.ok(item).build();
        } else {
            // Instead of building the Response here, throw the custom exception
            throw new ItemNotFoundException(id);
        }
    }

}

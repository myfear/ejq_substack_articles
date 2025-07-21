package com.example;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("/products")
public class MyPageResource {

    @Inject
    Template detail;

    // Handle the simple case with no categories: /products/{product}/details
    @GET
    @Path("/{product}/details")
    @Produces(MediaType.TEXT_HTML)
    public String showSimpleProductDetails(@PathParam("product") String product, @Context UriInfo uriInfo) {
        return detail.data("uriInfo", uriInfo)
                .data("product", product)
                .data("additional", null)
                .data("cat1", null)
                .data("cat2", null)
                .data("cat3", null)
                .data("categories", "")
                .data("categorySegments", new String[0])
                .render();
    }

    // Single wildcard pattern to handle all URL variations with categories
    @GET
    @Path("/{product}/{categories: .+}/details")
    @Produces(MediaType.TEXT_HTML)
    public String showProductDetails(
            @PathParam("product") String product,
            @PathParam("categories") String categories,
            @Context UriInfo uriInfo) {

        // Parse the categories path into individual segments
        String[] categorySegments = categories.split("/");

        // Extract individual category variables (null if not present)
        String additional = categorySegments.length > 0 ? categorySegments[0] : null;
        String cat1 = categorySegments.length > 1 ? categorySegments[1] : null;
        String cat2 = categorySegments.length > 2 ? categorySegments[2] : null;
        String cat3 = categorySegments.length > 3 ? String.join("/",
                java.util.Arrays.copyOfRange(categorySegments, 3, categorySegments.length)) : null;

        return detail.data("uriInfo", uriInfo)
                .data("product", product)
                .data("additional", additional)
                .data("cat1", cat1)
                .data("cat2", cat2)
                .data("cat3", cat3)
                .data("categories", categories)
                .data("categorySegments", categorySegments)
                .render();
    }
}
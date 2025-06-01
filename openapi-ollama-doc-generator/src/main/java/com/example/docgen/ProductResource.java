package com.example.docgen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// Basic OpenAPI Definition for the whole API
@OpenAPIDefinition(info = @Info(title = "Product Catalog API", version = "1.0.0", description = "A simple API for managing products in an inventory."))
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Product Management", description = "Operations related to product catalog.")
public class ProductResource {

    private static ConcurrentHashMap<String, Product> products = new ConcurrentHashMap<>();

    static {
        Product laptop = new Product(UUID.randomUUID().toString(), "Laptop Pro", 1899.99f,
                "High-performance laptop for professional use.");
        Product mouse = new Product(UUID.randomUUID().toString(), "Ergo Mouse", 49.99f,
                "Ergonomic wireless mouse with advanced tracking.");
        products.put(laptop.getId(), laptop);
        products.put(mouse.getId(), mouse);
    }

    @GET
    @Operation(summary = "Retrieve all products", description = "Returns a comprehensive list of all products currently in the catalog.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully retrieved list of products", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Product[].class)))
    })
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Retrieve a product by ID", description = "Fetches a single product's details using its unique identifier.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Product found and returned", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Product.class))),
            @APIResponse(responseCode = "404", description = "Product with the specified ID not found")
    })
    public Response getProductById(
            @Parameter(description = "Unique ID of the product to retrieve", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef") @PathParam("id") String id) {
        return products.values().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(p -> Response.ok(p).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Operation(summary = "Create a new product", description = "Adds a new product to the catalog with a generated ID.")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Product.class))),
            @APIResponse(responseCode = "400", description = "Invalid product data provided")
    })
    public Response createProduct(
            @RequestBody(description = "Product object to be created", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Product.class))) Product product) {
        if (product.getName() == null || product.getName().isEmpty() || product.getPrice() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Product name and price are required and valid.")
                    .build();
        }
        product.setId(UUID.randomUUID().toString()); // Assign a new ID
        products.put(product.getId(), product);
        System.out.println("Creating product: " + product);
        return Response.status(Response.Status.CREATED).entity(product).build();
    }
}
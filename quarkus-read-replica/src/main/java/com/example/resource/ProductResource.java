package com.example.resource;

import java.math.BigDecimal;
import java.util.List;

import com.example.entity.Product;
import com.example.service.ProductService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
    ProductService productService;

    @GET
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") Long id) {
        return productService.getProductById(id).map(product -> Response.ok(product).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/category/{category}")
    public List<Product> getProductsByCategory(@PathParam("category") String category) {
        return productService.getProductsByCategory(category);
    }

    @GET
    @Path("/price-range")
    public List<Product> getProductsByPriceRange(@QueryParam("min") BigDecimal minPrice,
            @QueryParam("max") BigDecimal maxPrice) {
        return productService.getProductsByPriceRange(minPrice, maxPrice);
    }

    @GET
    @Path("/count")
    public Response getProductCount() {
        long count = productService.getProductCount();
        return Response.ok().entity("{\"count\": " + count + "}").build();
    }

    @POST
    public Response createProduct(Product product) {
        Product created = productService.createProduct(product);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateProduct(@PathParam("id") Long id, Product product) {
        product.id = id;
        Product updated = productService.updateProduct(product);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/{id}/stock")
    public Response updateStock(@PathParam("id") Long id, @QueryParam("stock") Integer stock) {
        Product updated = productService.updateProductStock(id, stock);
        if (updated != null) {
            return Response.ok(updated).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") Long id) {
        productService.deleteProduct(id);
        return Response.noContent().build();
    }
}

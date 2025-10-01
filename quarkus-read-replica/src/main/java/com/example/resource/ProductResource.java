package com.example.resource;

import java.math.BigDecimal;
import java.util.List;

import com.example.entity.Product;
import com.example.repository.ProductRepository;
import com.example.tenant.ReadWrite;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
    ProductRepository productRepository;

    @GET
    public List<Product> getAllProducts() {
        return productRepository.findAllForRead();
    }

    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") Long id) {
        return productRepository.findByIdForRead(id).map(product -> Response.ok(product).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/category/{category}")
    public List<Product> getProductsByCategory(@PathParam("category") String category) {
        return productRepository.findByCategoryForRead(category);
    }

    @GET
    @Path("/price-range")
    public List<Product> getProductsByPriceRange(@QueryParam("min") BigDecimal minPrice,
            @QueryParam("max") BigDecimal maxPrice) {
        return productRepository.findByPriceRangeForRead(minPrice, maxPrice);
    }

    @GET
    @Path("/count")
    public Response getProductCount() {
        long count = productRepository.countForRead();
        return Response.ok().entity("{\"count\": " + count + "}").build();
    }

    @ReadWrite
    @POST
    public Response createProduct(Product product) {
        Product created = productRepository.save(product);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateProduct(@PathParam("id") Long id, Product product) {
        product.id = id;
        Product updated = productRepository.save(product);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/{id}/stock")
    public Response updateStock(@PathParam("id") Long id, @QueryParam("stock") Integer stock) {
        Product updated = productRepository.updateStock(id, stock);
        if (updated != null) {
            return Response.ok(updated).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") Long id) {
        productRepository.deleteProduct(id);
        return Response.noContent().build();
    }
}
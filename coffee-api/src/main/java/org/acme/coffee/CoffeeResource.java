package org.acme.coffee;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
//import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
//import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

//@SecurityScheme(securitySchemeName = "coffee-oauth", type = org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType.OAUTH2, description = "OAuth2 flow for Coffee API")
@Path("/coffee")
@Tag(name = "Coffee", description = "Endpoints related to coffee drinks")
@ExternalDocumentation(description = "Coffee brewing guide", url = "https://example.com/docs/brewing")
// @SecurityRequirement(name = "coffee-oauth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoffeeResource {

    @GET
    @Operation(summary = "List all coffees", description = "Returns the full coffee menu including Espresso, Latte, etc.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Coffee list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Coffee.class)))
    })
    public List<Coffee> listCoffees() {
        return List.of(
                new Coffee(1L, "Espresso", "Strong and bold"),
                new Coffee(2L, "Latte", "Smooth with milk"),
                new Coffee(3L, "Cappuccino", "Foamy delight"),
                new Coffee(4L, "Macchiato", "Espresso with a dollop of foam"),
                new Coffee(5L, "Americano", "Espresso diluted with hot water"),
                new Coffee(6L, "Mocha", "Rich chocolate and espresso blend"),
                new Coffee(7L, "Flat White", "Silky microfoam with double shot"),
                new Coffee(8L, "Cortado", "Equal parts espresso and warm milk"),
                new Coffee(9L, "Affogato", "Espresso poured over vanilla gelato"),
                new Coffee(10L, "Turkish Coffee", "Traditional unfiltered brewing method"),
                new Coffee(11L, "Cold Brew", "Smooth, less acidic cold extraction"),
                new Coffee(12L, "Nitro Coffee", "Cold brew infused with nitrogen"),
                new Coffee(13L, "Frappuccino", "Blended ice coffee with whipped cream"),
                new Coffee(14L, "Irish Coffee", "Coffee with Irish whiskey and cream"),
                new Coffee(15L, "Vienna Coffee", "Espresso with whipped cream and chocolate"));
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Find coffee by ID", description = "Returns a coffee if it exists")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Coffee found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Coffee.class))),
            @APIResponse(responseCode = "404", description = "Coffee not found")
    })
    public Coffee getCoffee(@PathParam("id") Long id) {
        return new Coffee(id, "Filter Coffee", "Simple classic");
    }

    @POST
    @Operation(summary = "Add a new coffee", description = "Creates a new coffee entry")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Coffee created"),
            @APIResponse(responseCode = "400", description = "Invalid input")
    })
    public Response addCoffee(@Valid Coffee coffee) {
        // In a real app, persist the coffee
        return Response.status(Response.Status.CREATED).entity(coffee).build();
    }

    @GET
    @Path("/export")
    @Produces("text/csv")
    @Operation(summary = "Export coffee list", description = "Download coffee menu in CSV format")
    @APIResponse(responseCode = "200", description = "CSV file with all coffees", content = @Content(mediaType = "text/csv"))
    public Response exportCoffeesAsCsv() {
        List<Coffee> coffees = listCoffees();
        StringBuilder csv = new StringBuilder();
        csv.append("id,name,description\n");

        for (Coffee coffee : coffees) {
            csv.append(coffee.id).append(",")
                    .append("\"").append(coffee.name).append("\",")
                    .append("\"").append(coffee.description).append("\"\n");
        }

        return Response.ok(csv.toString())
                .type("text/csv")
                .header("Content-Disposition", "attachment; filename=coffees.csv")
                .build();
    }

}
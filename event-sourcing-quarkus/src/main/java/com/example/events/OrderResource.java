package com.example.events;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.example.events.Commands.AddItemCommand;
import com.example.events.Commands.CancelOrderCommand;
import com.example.events.Commands.PlaceOrderCommand;
import com.example.events.Commands.ShipOrderCommand;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderCommandHandler handler;

    @Inject
    EventStore eventStore;

    @POST
    public Response placeOrder(PlaceOrderRequest request) {
        var cmd = new PlaceOrderCommand(request.customerEmail());
        CommandResult result = handler.placeOrder(cmd);

        return switch (result) {
            case CommandResult.Success s -> Response
                    .created(URI.create("/orders/" + s.aggregateId()))
                    .entity(s)
                    .build();
            case CommandResult.ValidationError v -> Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(v)
                    .build();
            default -> Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(result)
                    .build();
        };
    }

    @POST
    @Path("/{id}/items")

    public Response addItem(@PathParam("id") UUID orderId, AddItemRequest request) {
        var cmd = new AddItemCommand(
                orderId,
                request.productName(),
                request.quantity(),
                request.price());
        CommandResult result = handler.addItem(cmd);

        return switch (result) {
            case CommandResult.Success s -> Response.ok(s).build();
            case CommandResult.NotFound n -> Response.status(Response.Status.NOT_FOUND).entity(n).build();
            case CommandResult.InvalidState i -> Response.status(Response.Status.CONFLICT).entity(i).build();
            case CommandResult.ValidationError v -> Response.status(Response.Status.BAD_REQUEST).entity(v).build();
        };
    }

    @POST
    @Path("/{id}/ship")

    public Response ship(@PathParam("id") UUID orderId, ShipOrderRequest request) {
        var cmd = new ShipOrderCommand(orderId, request.trackingNumber());
        CommandResult result = handler.shipOrder(cmd);

        return switch (result) {
            case CommandResult.Success s -> Response.ok(s).build();
            case CommandResult.NotFound n -> Response.status(Response.Status.NOT_FOUND).entity(n).build();
            case CommandResult.InvalidState i -> Response.status(Response.Status.CONFLICT).entity(i).build();
            default -> Response.status(Response.Status.BAD_REQUEST).entity(result).build();
        };
    }

    @POST
    @Path("/{id}/cancel")

    public Response cancel(@PathParam("id") UUID orderId, CancelOrderRequest request) {
        var cmd = new CancelOrderCommand(orderId, request.reason());
        CommandResult result = handler.cancelOrder(cmd);

        return switch (result) {
            case CommandResult.Success s -> Response.ok(s).build();
            case CommandResult.NotFound n -> Response.status(Response.Status.NOT_FOUND).entity(n).build();
            case CommandResult.InvalidState i -> Response.status(Response.Status.CONFLICT).entity(i).build();
            default -> Response.status(Response.Status.BAD_REQUEST).entity(result).build();
        };
    }

    @GET
    @Path("/{id}")

    public Response getState(@PathParam("id") UUID orderId) {
        OrderState state = handler.loadCurrentState(orderId);
        if (state == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(state).build();
    }

    @GET
    @Path("/{id}/events")

    public List<OrderEvent> getEvents(@PathParam("id") UUID orderId) {
        return eventStore.loadEvents(orderId);
    }

    @GET
    @Path("/{id}/read-model")
    public Response getReadModel(@PathParam("id") UUID orderId) {
        OrderReadModel readModel = OrderReadModel.findByOrderId(orderId);
        if (readModel == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(readModel).build();
    }

    // DTOs for the REST layer

    public record PlaceOrderRequest(
            String customerEmail) {
    }

    public record AddItemRequest(
            String productName,
            int quantity,
            BigDecimal price) {
    }

    public record ShipOrderRequest(
            String trackingNumber) {
    }

    public record CancelOrderRequest(
            String reason) {
    }
}
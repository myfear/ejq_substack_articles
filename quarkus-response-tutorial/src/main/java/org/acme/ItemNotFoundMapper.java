package org.acme;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider // This annotation registers the mapper with JAX-RS
public class ItemNotFoundMapper implements ExceptionMapper<ItemNotFoundException> {

    @Override
    public Response toResponse(ItemNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
            "ITEM_NOT_FOUND_MAPPED",
            exception.getMessage() // Message from the exception
        );
        return Response.status(Response.Status.NOT_FOUND)
                       .entity(error)
                       .build();
    }
}

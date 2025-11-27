package com.example.api;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import com.example.adapter.AdapterRegistry;
import com.example.adapter.RequestAdapter;
import com.example.adapter.ResponseAdapter;
import com.example.api.v1.PaymentV1;
import com.example.api.v2.PaymentV2;
import com.example.api.v3.PaymentV3Response;
import com.example.domain.CanonicalPayment;
import com.example.service.PaymentService;
import com.example.version.VersionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/payments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    VersionContext versionContext;
    @Inject
    AdapterRegistry registry;
    @Inject
    PaymentService service;
    @Inject
    ObjectMapper mapper;

    @POST
    public Response createPayment(
        @RequestBody(
            description = "Versioned payment request",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "V1",
                        value = """
                        {
                          "amount": 10.00
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "V2",
                        value = """
                        {
                          "amount": 10.00,
                          "method": "CARD"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "V3",
                        value = """
                        {
                          "amount": 10.00,
                          "method": "SEPA"
                        }
                        """
                    )
                }
            )
        )
        @Valid JsonNode body) throws Exception {
        String version = versionContext.get();

        // Pick the best request adapter for this version.
        // We support V1 and V2 request shapes across all versions.
        RequestAdapter<?> reqAdapter = pickRequestAdapter(version);

        // Deserialize into the adapter’s expected request type
        Object typedReq = mapper.treeToValue(body, reqAdapter.requestType());
        CanonicalPayment canonical = adaptToCanonical(reqAdapter, typedReq);

        // Business logic
        CanonicalPayment created = service.create(canonical);

        // Transform response for the same version
        Object versionedResponse = pickAndTransformResponse(version, created);

        return Response.ok(versionedResponse).build();
    }

    // ---- Helpers

    private RequestAdapter<?> pickRequestAdapter(String version) {
        // Try V2 first (has method), fallback to V1
        try {
            return registry.requestAdapterFor(version, PaymentV2.class);
        } catch (Exception ignore) {
            /* fallback */ }
        return registry.requestAdapterFor(version, PaymentV1.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private CanonicalPayment adaptToCanonical(RequestAdapter adapter, Object typedReq) {
        return adapter.toCanonical(typedReq);
    }

    private Object pickAndTransformResponse(String version, CanonicalPayment created) {
        // V3 returns richer response type; otherwise V2 or V1
        try {
            ResponseAdapter<PaymentV3Response> v3 = registry.responseAdapterFor(version, PaymentV3Response.class);
            return v3.fromCanonical(created);
        } catch (Exception ignore) {
        }

        try {
            ResponseAdapter<PaymentV2> v2 = registry.responseAdapterFor(version, PaymentV2.class);
            return v2.fromCanonical(created);
        } catch (Exception ignore) {
        }

        ResponseAdapter<PaymentV1> v1 = registry.responseAdapterFor(version, PaymentV1.class);
        return v1.fromCanonical(created);
    }
}
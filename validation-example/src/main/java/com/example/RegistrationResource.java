package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class RegistrationResource {

    @Inject
    Template registration;

    @Inject
    Validator validator;

    @Path("/registration")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showForm() {
        return registration.data("registration", new UserRegistration())
                .data("validation", Map.of(
                        "username", "",
                        "email", "",
                        "phone", ""));
    }

    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance handleForm(@BeanParam UserRegistration registrationDto) {
        Set<ConstraintViolation<UserRegistration>> violations = validator.validate(registrationDto);

        Map<String, String> validationMessages = new HashMap<>();
        // Initialize with empty messages for all fields
        validationMessages.put("username", "");
        validationMessages.put("email", "");
        validationMessages.put("phone", "");

        if (!violations.isEmpty()) {
            // Override with actual validation messages where there are violations
            violations.forEach(violation -> validationMessages.put(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()));

            return registration
                    .data("registration", registrationDto)
                    .data("validation", validationMessages);
        }

        // Normally you would persist user here
        return registration
                .data("registration", registrationDto)
                .data("validation", validationMessages);
    }
}

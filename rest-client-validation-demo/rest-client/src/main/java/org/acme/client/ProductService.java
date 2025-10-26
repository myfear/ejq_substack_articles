package org.acme.client;

import org.acme.client.interceptor.ValidatedResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;

@ApplicationScoped
public class ProductService {

    @Inject
    @RestClient
    ProductClient client;

    @Inject
    Validator validator;

    @ValidatedResponse
    public Product getValidatedProduct() {
        Product product = client.getProduct();

        /*
         * var violations = validator.validate(product);
         * if (!violations.isEmpty()) {
         * throw new ConstraintViolationException(violations);
         * }
         */
        return product;
    }
}
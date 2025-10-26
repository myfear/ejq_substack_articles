package org.acme.client.interceptor;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

@Interceptor
@ValidatedResponse
public class ResponseValidationInterceptor {

    @Inject
    Validator validator;

    @AroundInvoke
    Object validateResponse(InvocationContext ctx) throws Exception {
        Object result = ctx.proceed();
        if (result != null) {
            var violations = validator.validate(result);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
        return result;
    }
}
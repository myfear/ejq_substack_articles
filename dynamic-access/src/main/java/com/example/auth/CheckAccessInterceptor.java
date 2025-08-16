package com.example.auth;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;

@CheckAccess
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class CheckAccessInterceptor {

    @Inject
    AccessControlService acs;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        CheckAccess ann = ctx.getMethod().getAnnotation(CheckAccess.class);

        if (ann != null && !acs.canPerformAction(ann.resourceType(), ann.action())) {
            throw new ForbiddenException("You do not have permission to perform this action.");
        }

        return ctx.proceed();
    }
}
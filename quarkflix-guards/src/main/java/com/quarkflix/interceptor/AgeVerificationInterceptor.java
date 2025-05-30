package com.quarkflix.interceptor;

import org.jboss.logging.Logger;

import com.quarkflix.annotation.RequiresAgeVerification;
import com.quarkflix.exception.ContentRestrictionException;
import com.quarkflix.model.User;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@RequiresAgeVerification(minimumAge = 0) // Default minimumAge for the binding, actual value comes from annotation
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 100) // Higher priority (lower number) means it runs before AuditLog
public class AgeVerificationInterceptor {

    private static final Logger LOG = Logger.getLogger(AgeVerificationInterceptor.class);

    @AroundInvoke
    Object verifyAge(InvocationContext context) throws Exception {
        RequiresAgeVerification ageVerificationAnnotation = context.getMethod()
                .getAnnotation(RequiresAgeVerification.class);
        int requiredAge = ageVerificationAnnotation.minimumAge();
        LOG.info("XXXXXX");
        LOG.info(requiredAge);
        User user = null;
        // Attempt to find a User object in the method parameters
        for (Object param : context.getParameters()) {
            if (param instanceof User) {
                user = (User) param;
                break;
            }
        }

        if (user == null) {
            LOG.warnf(
                    "Method %s.%s is annotated with @RequiresAgeVerification but no User parameter was found. Skipping check.",
                    context.getTarget().getClass().getSimpleName(), context.getMethod().getName());
            return context.proceed();
        }

        LOG.infof("AGE_VERIFY: User %s (age %d) attempting to access content requiring age %d.",
                user.getUsername(), user.getAge(), requiredAge);

        if (user.getAge() < requiredAge) {
            String message = String.format("User %s (age %d) does not meet minimum age requirement of %d for %s.",
                    user.getUsername(), user.getAge(), requiredAge, context.getMethod().getName());
            LOG.warn(message);
            throw new ContentRestrictionException(message);
        }

        LOG.infof("AGE_VERIFY: User %s meets age requirement for %s.", user.getUsername(),
                context.getMethod().getName());
        return context.proceed();
    }
}
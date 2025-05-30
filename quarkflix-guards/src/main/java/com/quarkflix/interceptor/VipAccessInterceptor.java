package com.quarkflix.interceptor;

import org.eclipse.microprofile.config.inject.ConfigProperty; // For config injection
import org.jboss.logging.Logger;

import com.quarkflix.annotation.VipAccessRequired;
import com.quarkflix.exception.FeatureAccessException;
import com.quarkflix.model.User;

import jakarta.annotation.Priority;
import jakarta.inject.Inject; // For CDI injection
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@VipAccessRequired
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 50) // Highest priority, runs first
public class VipAccessInterceptor {

    private static final Logger LOG = Logger.getLogger(VipAccessInterceptor.class);

    @Inject
    @ConfigProperty(name = "quarkflix.features.vip-premiere.enabled", defaultValue = "false")
    boolean vipFeatureEnabled; // Inject configuration

    @AroundInvoke
    Object checkVipAccess(InvocationContext context) throws Exception {
        if (!vipFeatureEnabled) {
            LOG.info("VIP_ACCESS: VIP Premiere feature check is disabled. Allowing access.");
            return context.proceed();
        }

        User user = null;
        for (Object param : context.getParameters()) {
            if (param instanceof User) {
                user = (User) param;
                break;
            }
        }

        if (user == null) {
            LOG.warnf(
                    "VIP_ACCESS: Method %s.%s is annotated with @VipAccessRequired but no User parameter was found. Skipping check.",
                    context.getTarget().getClass().getSimpleName(), context.getMethod().getName());
            return context.proceed();
        }

        LOG.infof("VIP_ACCESS: User %s attempting to access VIP content. VIP Status: %s", user.getUsername(),
                user.isVip());

        if (!user.isVip()) {
            String message = String.format("User %s is not a VIP. Access to premiere content %s denied.",
                    user.getUsername(), context.getMethod().getName());
            LOG.warn(message);
            throw new FeatureAccessException(message);
        }

        LOG.infof("VIP_ACCESS: VIP User %s granted access to %s.", user.getUsername(), context.getMethod().getName());
        return context.proceed();
    }
}
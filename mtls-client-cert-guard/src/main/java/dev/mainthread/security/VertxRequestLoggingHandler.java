package dev.mainthread.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import io.quarkus.vertx.http.runtime.filters.Filters;

@ApplicationScoped
public class VertxRequestLoggingHandler {

    private static final Logger LOG = Logger.getLogger(VertxRequestLoggingHandler.class);

    public void register(@Observes Filters filters) {
        filters.register(rc -> {
            LOG.infof(">>> Vertx Request: %s %s from %s", 
                    rc.request().method(),
                    rc.request().path(),
                    rc.request().remoteAddress());
            LOG.infof(">>> Request headers: %s", rc.request().headers());
            
            // Log response when it ends
            rc.response().endHandler(v -> {
                LOG.infof("<<< Vertx Response: %s %s -> %d", 
                        rc.request().method(),
                        rc.request().path(),
                        rc.response().getStatusCode());
            });
            
            rc.next();
        }, 1);
    }
}


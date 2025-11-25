package com.ibm.developer.quarkus.actuator.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.BuildInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.GitInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.JavaInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.MachineInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.SslInfoProvider;
import io.quarkus.arc.Arc;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class ActuatorInfoEndpoint implements Handler<RoutingContext> {

    private final ActuatorRuntimeConfig config;

    public ActuatorInfoEndpoint(ActuatorRuntimeConfig config) {
        this.config = config;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        if (!routingContext.request().method().equals(io.vertx.core.http.HttpMethod.GET)) {
            routingContext.response().setStatusCode(405).end();
            return;
        }
        try {
            Map<String, Object> out = new LinkedHashMap<>();

            // Git section
            if (config.info().gitEnabled()) {
                GitInfoProvider git = Arc.container().instance(GitInfoProvider.class).get();
                if (git != null) {
                    Map<String, Object> g = git.getGitInfo();
                    if (!g.isEmpty()) {
                        out.put("git", g);
                    } else {
                        org.jboss.logging.Logger.getLogger(ActuatorInfoEndpoint.class)
                                .debug("Git info is enabled but returned empty map");
                    }
                } else {
                    org.jboss.logging.Logger.getLogger(ActuatorInfoEndpoint.class)
                            .warn("GitInfoProvider bean not found");
                }
            }

            // Build section
            // As an alternate approach, most of this information is also available via ConfigProvider.getConfig().getOptionalValue("quarkus.application.name", String.class).orElse("application");
            BuildInfoProvider buildProvider = Arc.container().instance(BuildInfoProvider.class).get();
            if (buildProvider != null) {
                out.put("build", buildProvider.getBuildInfo());
            }

            // Get MachineInfoProvider for OS and process information
            MachineInfoProvider machineProvider = Arc.container().instance(MachineInfoProvider.class).get();

            // OS section
            if (machineProvider != null) {
                Map<String, Object> os = machineProvider.getOsInfo();
                out.put("os", os);

                // Process section (pid, parentPid, owner, cpus)
                Map<String, Object> process = machineProvider.getProcessInfo();
                out.put("process", process);
            } else {
                org.jboss.logging.Logger.getLogger(ActuatorInfoEndpoint.class)
                        .warn("MachineInfoProvider bean not found");
            }

            // Get JavaInfoProvider for Java info
            JavaInfoProvider javaProvider = Arc.container().instance(JavaInfoProvider.class).get();

            // Java section
            if (config.info().javaEnabled()) {
                if (javaProvider != null) {
                    Map<String, Object> java = javaProvider.getJavaInfo();
                    out.put("java", java);
                } else {
                    org.jboss.logging.Logger.getLogger(ActuatorInfoEndpoint.class)
                            .warn("JavaInfoProvider bean not found");
                }
            }

            // SSL section
            if (config.info().sslEnabled()) {
                SslInfoProvider sslProvider = Arc.container().instance(SslInfoProvider.class).get();
                if (sslProvider != null) {
                    Map<String, Object> ssl = sslProvider.getSslInfo();
                    if (!ssl.isEmpty()) {
                        out.put("ssl", ssl);
                    }
                } else {
                    org.jboss.logging.Logger.getLogger(ActuatorInfoEndpoint.class)
                            .warn("SslInfoProvider bean not found");
                }
            }

            ObjectMapper objectMapper = Arc.container().instance(ObjectMapper.class).get();
            HttpServerResponse response = routingContext.response();
            response.putHeader("Content-Type", "application/vnd.quarkus.actuator.v3+json");
            response.end(objectMapper.writeValueAsString(out));
        } catch (Exception e) {
            routingContext.fail(e);
        }
    }
}
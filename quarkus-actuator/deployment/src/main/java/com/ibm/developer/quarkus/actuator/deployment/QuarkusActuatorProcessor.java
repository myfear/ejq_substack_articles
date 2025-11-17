package com.ibm.developer.quarkus.actuator.deployment;

import org.jboss.logging.Logger;

import com.ibm.developer.quarkus.actuator.runtime.ActuatorRecorder;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.GitInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.JavaInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.MachineInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.SslInfoProvider;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.StaticInitConfigBuilderBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;

class QuarkusActuatorProcessor {

    private static final Logger log = Logger.getLogger(QuarkusActuatorProcessor.class);
    private static final String FEATURE = "quarkus-actuator";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerBeans(BuildProducer<AdditionalBeanBuildItem> beans,
            ActuatorBuildTimeConfig cfg) {

        if (!cfg.enabled()) {
            log.debug("Actuator is disabled, skipping bean registration");
            return;
        }

        log.infof("Registering actuator bean: %s", GitInfoProvider.class.getName());
        beans.produce(AdditionalBeanBuildItem.unremovableOf(GitInfoProvider.class));

        log.infof("Registering actuator bean: %s", JavaInfoProvider.class.getName());
        beans.produce(AdditionalBeanBuildItem.unremovableOf(JavaInfoProvider.class));

        log.infof("Registering actuator bean: %s", MachineInfoProvider.class.getName());
        beans.produce(AdditionalBeanBuildItem.unremovableOf(MachineInfoProvider.class));

        log.infof("Registering actuator bean: %s", SslInfoProvider.class.getName());
        beans.produce(AdditionalBeanBuildItem.unremovableOf(SslInfoProvider.class));
    }

    @BuildStep
    void registerNativeResources(BuildProducer<NativeImageResourceBuildItem> resources) {
        log.debug("Registering native resource: git.properties");
        resources.produce(new NativeImageResourceBuildItem("git.properties"));
    }

    @BuildStep
    void loadGitInfoIntoConfig(
            BuildProducer<StaticInitConfigBuilderBuildItem> configBuilder,
            ActuatorBuildTimeConfig buildTimeCfg) {

        if (!buildTimeCfg.enabled() || !buildTimeCfg.infoEnabled()) {
            log.debug("Skipping git info loading - actuator or info endpoint disabled");
            return;
        }

        // Register GitInfoConfigBuilder which will read git.properties at build time
        // Note: The class is in the runtime module but registered from deployment module
        log.debug("Registering GitInfoConfigBuilder");
        configBuilder.produce(new StaticInitConfigBuilderBuildItem(
                com.ibm.developer.quarkus.actuator.runtime.GitInfoConfigBuilder.class));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    RouteBuildItem registerRoutes(
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            ActuatorRecorder recorder,
            ActuatorBuildTimeConfig buildTimeCfg) {

        if (!buildTimeCfg.enabled() || !buildTimeCfg.infoEnabled()) {
            log.debugf("Skipping route registration - enabled: %s, infoEnabled: %s",
                    buildTimeCfg.enabled(), buildTimeCfg.infoEnabled());
            return null;
        }

        // Normalize the base path: remove leading/trailing slashes for relative path
        // Use build-time config for route registration since routes are registered at
        // build time
        String basePath = buildTimeCfg.basePath();
        if (basePath == null || basePath.isEmpty() || basePath.equals("/")) {
            basePath = "";
        } else {
            // Remove leading slash for relative path
            if (basePath.startsWith("/")) {
                basePath = basePath.substring(1);
            }
            // Remove trailing slash
            if (basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
        }

        // Use nestedRoute if we have a base path, otherwise use route directly
        RouteBuildItem.Builder routeBuilder;
        String finalPath;
        if (basePath.isEmpty()) {
            finalPath = "info";
            routeBuilder = nonApplicationRootPathBuildItem.routeBuilder()
                    .route(finalPath);
        } else {
            finalPath = basePath + "/info";
            routeBuilder = nonApplicationRootPathBuildItem.routeBuilder()
                    .nestedRoute(basePath, "info");
        }

        log.infof("Registering actuator route: %s (relative to non-application root)", finalPath);

        // Recorder has RuntimeValue injected via constructor
        return routeBuilder
                .handler(recorder.createInfoHandler())
                .displayOnNotFoundPage()
                .build();
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void initRuntime(ActuatorRecorder recorder) {
        log.debug("Initializing actuator runtime");
        // Recorder has RuntimeValue injected via constructor
        recorder.init();
    }
}
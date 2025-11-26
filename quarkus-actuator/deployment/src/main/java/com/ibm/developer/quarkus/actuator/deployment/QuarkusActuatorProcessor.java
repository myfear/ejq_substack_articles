package com.ibm.developer.quarkus.actuator.deployment;

import com.ibm.developer.quarkus.actuator.runtime.ActuatorRecorder;
import com.ibm.developer.quarkus.actuator.runtime.InfoRecorder;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.BuildInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.GitInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.JavaInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.MachineInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.SslInfoProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.bootstrap.model.ApplicationModel;
import io.quarkus.builder.Version;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.maven.dependency.ResolvedDependency;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

class QuarkusActuatorProcessor {

    private static final Logger log = Logger.getLogger(QuarkusActuatorProcessor.class);
    private static final String FEATURE = "quarkus-actuator";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    void registerBuildTimeBeans(CurateOutcomeBuildItem curateOutcomeBuildItem,
                                BuildProducer<SyntheticBeanBuildItem> beanProducer, InfoRecorder recorder,
                                CombinedIndexBuildItem combinedIndex) {


        ApplicationModel applicationModel = curateOutcomeBuildItem.getApplicationModel();
        IndexView index = combinedIndex.getIndex();

        // Pass through information about the build to the supplier, so processing happens at build time
        // To produce an injectable bean, it needs to be recorded, but we can initialise the recorder with static information
        beanProducer.produce(SyntheticBeanBuildItem.configure(BuildInfoProvider.class)
                .supplier(recorder.buildInfoSupplier(readBuildData(applicationModel, index)))
                .scope(ApplicationScoped.class)
                .done());


        // Pass through the git information to the supplier
        beanProducer.produce(SyntheticBeanBuildItem.configure(GitInfoProvider.class)
                .supplier(recorder.gitInfoSupplier(readGitInfo()))
                .scope(ApplicationScoped.class)
                .done());
    }

    private Map<String, Object> readBuildData(ApplicationModel applicationModel, IndexView index) {
        ResolvedDependency appArtifact = applicationModel.getAppArtifact();
        Map<String, Object> buildData = new LinkedHashMap<>();

        String group = appArtifact.getGroupId();
        buildData.put("group", group);
        String artifact = appArtifact.getArtifactId();
        buildData.put("artifact", artifact);
        String version = appArtifact.getVersion();
        buildData.put("version", version);
        String time = ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now());
        buildData.put("time", time);
        String quarkusVersion = Version.getVersion();
        buildData.put("quarkusVersion", quarkusVersion);

        // Count all known classes in the index
        int count = index.getKnownClasses().size();

        buildData.put("classes", count);

        return buildData;
    }

    // As an alternative approach, the Eclipse jgit library can be used to read the .git folder directly
    private Map<String, Object> readGitInfo() {
        // Read git.properties at build time
        String location = "/git.properties";
        Properties props = new Properties();

        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(location)) {
            if (is == null) {
                // Try without leading slash
                String locationWithoutSlash = location.substring(1);
                try (InputStream is2 = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(locationWithoutSlash)) {
                    if (is2 != null) {
                        props.load(is2);
                        log.debugf("Loaded git.properties from: %s", locationWithoutSlash);
                    } else {
                        log.debugf("git.properties not found at: %s or %s", location, locationWithoutSlash);
                    }
                }
            } else {
                props.load(is);
                log.debugf("Loaded git.properties from: %s", location);
            }
        } catch (IOException e) {
            log.warnf("Failed to load git.properties: %s", e.getMessage());
        }

        // Extract the relevant git properties
        Map<String, String> gitProperties = new HashMap<>();
        if (!props.isEmpty()) {
            String branch = props.getProperty("git.branch");
            String commitId = props.getProperty("git.commit.id.abbrev");
            if (commitId == null || commitId.isEmpty()) {
                commitId = props.getProperty("git.commit.id");
            }
            String commitTime = props.getProperty("git.commit.time");

            if (branch != null && !branch.isEmpty()) {
                gitProperties.put("branch", branch);
            }
            if (commitId != null && !commitId.isEmpty()) {
                gitProperties.put("commit-id", commitId);
            }
            if (commitTime != null && !commitTime.isEmpty()) {
                gitProperties.put("commit-time", commitTime);
            }

            log.infof("Git info loaded at build time - branch: %s, commit: %s, time: %s",
                    branch != null ? branch : "N/A",
                    commitId != null ? commitId : "N/A",
                    commitTime != null ? commitTime : "N/A");
        } else {
            log.debug("No git properties found, git info will be empty");
        }

        return gitProperties;
    }


    @BuildStep
    void registerRuntimeBeans(BuildProducer<AdditionalBeanBuildItem> beans, ActuatorBuildTimeConfig cfg) {

        if (!cfg.enabled()) {
            log.debug("Actuator is disabled, skipping bean registration");
            return;
        }


        log.infof("Registering actuator bean: %s", GitInfoProvider.class.getName());
        beans.produce(AdditionalBeanBuildItem.unremovableOf(GitInfoProvider.class));

        log.infof("Registering actuator bean: %s", BuildInfoProvider.class.getName());
        beans.produce(AdditionalBeanBuildItem.unremovableOf(BuildInfoProvider.class));

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
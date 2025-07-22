package org.example.waf;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.AutoEncoder;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import io.vertx.core.http.HttpServerRequest;
// Import necessary classes...
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
@PreMatching
public class SelfLearningWAFFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(SelfLearningWAFFilter.class.getName());

    // Inject Vert.x request to get client IP robustly
    @Context
    HttpServerRequest vertxRequest;

    // Model and configuration
    private MultiLayerNetwork autoEncoder;
    private final int inputSize = 25; // Size of our feature vector
    private volatile double anomalyThreshold = 0.15;

    // Thread-safe collections for tracking and training
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> lastRequestTime = new ConcurrentHashMap<>();
    private final Queue<double[]> trainingBuffer = new ConcurrentLinkedQueue<>();
    private final Queue<WAFLogEntry> auditLog = new ConcurrentLinkedQueue<>();

    // WAF parameters
    private final int bufferSize = 2000; // Max requests to hold for training
    private final int maxAuditLogSize = 10000;
    private final long rateLimitWindow = 60000; // 1 minute
    private final int maxRequestsPerWindow = 100;

    // Scheduler for background tasks
    private ScheduledExecutorService scheduler;
    private final AtomicInteger trainingCycle = new AtomicInteger(0);

    // Inside SelfLearningWAFFilter class

    @PostConstruct
    public void init() {
        logger.info("Initializing Self-Learning WAF Filter...");
        initializeModel();
        scheduler = Executors.newScheduledThreadPool(2);

        // Schedule periodic training to run every 5 minutes
        scheduler.scheduleAtFixedRate(this::performIncrementalTraining, 5, 5, TimeUnit.MINUTES);

        // Schedule data cleanup to run every hour
        scheduler.scheduleAtFixedRate(this::cleanupOldData, 1, 1, TimeUnit.HOURS);

        logger.info("Self-Learning WAF Filter initialized successfully.");
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Shutting down WAF scheduler.");
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    // Inside SelfLearningWAFFilter class

    @Override
    public void filter(ContainerRequestContext requestContext) {
        long startTime = System.currentTimeMillis();
        try {
            // 1. Extract request info
            RestRequestInfo requestInfo = extractRequestInfo(requestContext);

            // 2. Analyze the request
            WAFDecision decision = analyzeRequest(requestInfo);

            // 3. Log the outcome
            logDecision(requestInfo, decision, System.currentTimeMillis() - startTime);

            // 4. Act on the decision
            switch (decision.action()) {
                case BLOCK -> {
                    logger.warning("BLOCK: " + decision.reason() + " from IP " + requestInfo.clientIP());
                    abortWithSecurityResponse(requestContext, Response.Status.FORBIDDEN, "WAF_BLOCK",
                            "Request blocked by security policy");
                }
                case RATE_LIMIT -> {
                    logger.info("RATE_LIMIT: " + requestInfo.clientIP());
                    abortWithSecurityResponse(requestContext, Response.Status.TOO_MANY_REQUESTS, "RATE_LIMIT",
                            "Rate limit exceeded");
                }
                default -> {
                    // ALLOW or LOG_ONLY, let request proceed
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in WAF filter, allowing request to proceed.", e);
        }
    }

    // Inside SelfLearningWAFFilter class

    private void initializeModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                // Encoder layers
                .layer(new AutoEncoder.Builder().nIn(inputSize).nOut(20).activation(Activation.RELU).build())
                .layer(new AutoEncoder.Builder().nIn(20).nOut(15).activation(Activation.RELU).build())
                .layer(new AutoEncoder.Builder().nIn(15).nOut(8).activation(Activation.RELU).build()) // Bottleneck
                // Decoder layers
                .layer(new AutoEncoder.Builder().nIn(8).nOut(15).activation(Activation.RELU).build())
                .layer(new AutoEncoder.Builder().nIn(15).nOut(20).activation(Activation.RELU).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(20).nOut(inputSize).activation(Activation.SIGMOID).build())
                .build();

        autoEncoder = new MultiLayerNetwork(conf);
        autoEncoder.init();
    }

    private RestRequestInfo extractRequestInfo(ContainerRequestContext context) {
        Map<String, String> headers = new HashMap<>();
        context.getHeaders().forEach((key, values) -> {
            if (!values.isEmpty())
                headers.put(key, values.get(0));
        });

        String contentLength = context.getHeaderString("Content-Length");

        return new RestRequestInfo(
                context.getMethod(),
                context.getUriInfo().getPath(),
                context.getUriInfo().getRequestUri().getQuery(),
                getClientIP(context.getHeaderString("X-Forwarded-For")),
                System.currentTimeMillis(),
                headers,
                context.getMediaType() != null ? context.getMediaType().toString() : "",
                contentLength != null ? Integer.parseInt(contentLength) : 0);
    }

    private String getClientIP(String xForwardedFor) {
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        // Fallback to Vert.x request's remote address
        return vertxRequest.remoteAddress().host();
    }

    private WAFDecision analyzeRequest(RestRequestInfo request) {
        // 1. Check for rate limiting first
        if (isRateLimited(request.clientIP(), request.timestamp())) {
            return new WAFDecision(true, WAFAction.RATE_LIMIT, 1.0, -1, "Rate limit exceeded");
        }

        // 2. Check for obvious, rule-based attack patterns
        if (isObviousAttack(request)) {
            return new WAFDecision(true, WAFAction.BLOCK, 1.0, -1, "Known attack pattern detected");
        }

        // 3. Use the Autoencoder for anomaly detection
        double[] features = extractFeatures(request);
        addToTrainingBuffer(features); // Add to buffer for future training

        INDArray input = Nd4j.create(features).reshape(1, inputSize);
        INDArray reconstructed = autoEncoder.output(input);
        double error = input.sub(reconstructed).mul(input).sumNumber().doubleValue() / inputSize;

        boolean isAnomaly = error > anomalyThreshold;
        double confidence = Math.min(error / anomalyThreshold, 2.0); // Cap confidence at 2.0

        if (isAnomaly) {
            String reason = "High-confidence anomaly. Error: " + String.format("%.4f", error);
            // Block if confidence is very high, otherwise just log it
            WAFAction action = (confidence > 1.5) ? WAFAction.BLOCK : WAFAction.LOG_ONLY;
            return new WAFDecision(true, action, confidence, error, reason);
        }

        // 4. If nothing is suspicious, allow the request
        return new WAFDecision(false, WAFAction.ALLOW, 0.0, error, "Request allowed");
    }

    private double[] extractFeatures(RestRequestInfo request) {
        double[] features = new double[inputSize];
        String fullUrl = request.path() + (request.query() != null ? "?" + request.query() : "");
        Map<String, String> headers = request.headers();

        // Basic features
        features[0] = Math.min(fullUrl.length() / 250.0, 1.0);
        features[1] = Math.min(countParams(request.query()) / 20.0, 1.0);
        features[2] = hasSpecialChars(fullUrl) ? 1.0 : 0.0;
        features[3] = hasSqlKeywords(fullUrl) ? 1.0 : 0.0;
        features[4] = hasXssPatterns(fullUrl) ? 1.0 : 0.0;
        features[5] = hasPathTraversal(fullUrl) ? 1.0 : 0.0;
        features[6] = hasCommandInjection(fullUrl) ? 1.0 : 0.0;
        // Method (one-hot encoding)
        features[7] = request.method().equals("GET") ? 1.0 : 0.0;
        features[8] = request.method().equals("POST") ? 1.0 : 0.0;
        // Header features
        features[12] = Math.min(headers.size() / 30.0, 1.0);
        features[13] = hasSuspiciousUserAgent(headers.get("User-Agent")) ? 1.0 : 0.0;
        // Content features
        features[14] = Math.min(request.contentLength() / 10000.0, 1.0);
        // Advanced features
        features[22] = getEntropyScore(fullUrl);
        features[23] = hasEncodedPayload(fullUrl) ? 1.0 : 0.0;
        features[24] = Math.min(getPathDepth(request.path()) / 10.0, 1.0);

        return features;
    }

    // Inside SelfLearningWAFFilter class

    private boolean isObviousAttack(RestRequestInfo request) {
        String fullUrl = request.path() + (request.query() != null ? "?" + request.query() : "");
        String userAgent = request.headers().get("User-Agent");
        return hasSqlKeywords(fullUrl) || hasXssPatterns(fullUrl) || hasPathTraversal(fullUrl)
                || hasCommandInjection(fullUrl) || hasSuspiciousUserAgent(userAgent);
    }

    private boolean isRateLimited(String clientIP, long timestamp) {
        long now = System.currentTimeMillis();
        lastRequestTime.put(clientIP, new AtomicLong(now));
        AtomicInteger count = requestCounts.computeIfAbsent(clientIP, k -> new AtomicInteger(0));

        // This is a simplified sliding window check
        if (now - lastRequestTime.get(clientIP).get() > rateLimitWindow) {
            count.set(1); // Reset count for new window
            return false;
        }

        return count.incrementAndGet() > maxRequestsPerWindow;
    }

    private void performIncrementalTraining() {
        if (trainingBuffer.size() < 100) {
            logger.info("Skipping training cycle, not enough data.");
            return; // Don't train on too few samples
        }

        try {
            double[][] data = trainingBuffer.toArray(new double[0][]);
            INDArray trainingData = Nd4j.create(data);
            DataSet dataSet = new DataSet(trainingData, trainingData);

            logger.info("Starting incremental training cycle #" + trainingCycle.incrementAndGet() + " with "
                    + data.length + " samples.");
            autoEncoder.fit(dataSet);
            logger.info("Training complete. Current anomaly threshold: " + anomalyThreshold);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during incremental training", e);
        }
    }

    // Utility methods for aborting, logging, cleanup, and pattern matching
    private void abortWithSecurityResponse(ContainerRequestContext context, Response.Status status, String code,
            String message) {
        String jsonPayload = String.format("{\"error\":\"%s\",\"code\":\"%s\"}", message, code);
        Response response = Response.status(status)
                .entity(jsonPayload)
                .type("application/json")
                .build();
        context.abortWith(response);
    }

    private void logDecision(RestRequestInfo request, WAFDecision decision, long processingTime) {
        WAFLogEntry logEntry = new WAFLogEntry(System.currentTimeMillis(), request.clientIP(), request.method(),
                request.path(), decision.action(), decision.reconstructionError(), processingTime, decision.reason());
        if (auditLog.size() >= maxAuditLogSize)
            auditLog.poll();
        auditLog.offer(logEntry);

        if (decision.action() != WAFAction.ALLOW) {
            logger.info("WAF Decision: " + logEntry);
        }
    }

    private void addToTrainingBuffer(double[] features) {
        if (trainingBuffer.size() >= bufferSize)
            trainingBuffer.poll();
        trainingBuffer.offer(features);
    }

    private void cleanupOldData() {
        long oneHourAgo = System.currentTimeMillis() - 3_600_000;
        lastRequestTime.entrySet().removeIf(entry -> entry.getValue().get() < oneHourAgo);
        requestCounts.keySet().retainAll(lastRequestTime.keySet());
        logger.info("Cleanup complete. Tracking " + requestCounts.size() + " active clients.");
    }

    // --- Pattern Matching Helpers ---
    private boolean hasSpecialChars(String s) {
        return s != null && s.matches(".*[<>'\"%;()&+].*");
    }

    private boolean hasSqlKeywords(String s) {
        return s != null && s.toLowerCase().matches(".*(select|insert|union|script|exec|drop).*");
    }

    private boolean hasXssPatterns(String s) {
        return s != null && s.toLowerCase().matches(".*(<script|javascript:|onload=|onerror=).*");
    }

    private boolean hasPathTraversal(String s) {
        return s != null && s.contains("../");
    }

    private boolean hasCommandInjection(String s) {
        return s != null && s.toLowerCase().matches(".*(;|\\|\\|?|&&)\\s*(cat|ls|pwd|whoami).*");
    }

    private boolean hasSuspiciousUserAgent(String ua) {
        return ua != null && ua.toLowerCase().matches(".*(sqlmap|nikto|nessus|burp).*");
    }

    private boolean hasEncodedPayload(String s) {
        return s != null && s.matches(".*(%[0-9a-fA-F]{2}|\\+).*");
    }

    private int countParams(String q) {
        return q == null ? 0 : q.split("&").length;
    }

    private double getPathDepth(String p) {
        return p == null ? 0 : p.chars().filter(ch -> ch == '/').count();
    }

    private double getEntropyScore(String text) {
        if (text == null || text.isEmpty())
            return 0.0;
        Map<Character, Integer> freq = new HashMap<>();
        text.chars().forEach(c -> freq.put((char) c, freq.getOrDefault((char) c, 0) + 1));
        double entropy = freq.values().stream()
                .mapToDouble(count -> (double) count / text.length())
                .map(p -> -p * (Math.log(p) / Math.log(2)))
                .sum();
        return Math.min(entropy / 8.0, 1.0); // Normalize
    }

    // Public accessors for management endpoint
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("trackedClients", requestCounts.size());
        stats.put("trainingBufferSize", trainingBuffer.size());
        stats.put("auditLogSize", auditLog.size());
        stats.put("anomalyThreshold", anomalyThreshold);
        stats.put("trainingCycles", trainingCycle.get());
        return stats;
    }

    public void reportFalsePositive() {
        this.anomalyThreshold *= 1.05; // Slightly increase threshold
        logger.info("False positive reported. New threshold: " + this.anomalyThreshold);
    }

}
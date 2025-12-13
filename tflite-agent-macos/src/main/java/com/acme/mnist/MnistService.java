package com.acme.mnist;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MnistService {
    private static final Logger LOG = Logger.getLogger(MnistService.class);

    // Ensure TensorFlow library is loaded when this class is loaded, before any
    // TFRuntime code
    static {
        TensorFlowLibraryLoader.ensureLoaded();
    }

    private volatile TFRuntime runtime;
    private volatile String initializationError;
    private final Object initLock = new Object();

    private void ensureInitialized() {
        if (runtime == null && initializationError == null) {
            synchronized (initLock) {
                if (runtime == null && initializationError == null) {
                    // Default to frozen graph (more reliable with C API), fallback to SavedModel
                    String modelPath = System.getenv().getOrDefault("TENSORFLOW_MODEL_PATH", "mnist_frozen_graph.pb");
                    int threads = 2; // M-series chips handle 2-4 threads well for small models

                    LOG.infof("Loading TensorFlow model from %s", modelPath);
                    try {
                        this.runtime = new TFRuntime(modelPath, threads);
                        LOG.info("TensorFlow runtime initialized successfully");
                    } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
                        String errorMsg = String.format(
                                "Failed to load TensorFlow native library (libtensorflow.dylib). " +
                                        "The library must be installed and available in the system library path or java.library.path. "
                                        +
                                        "To fix this:\n" +
                                        "1. Install TensorFlow C library in Python venv\n"
                                        +
                                        "2. Or set java.library.path: -Djava.library.path=/path/to/lib\n" +
                                        "3. Or set DYLD_LIBRARY_PATH environment variable\n" +
                                        "Original error: %s: %s",
                                e.getClass().getSimpleName(),
                                e.getMessage());
                        LOG.error(errorMsg, e);
                        this.initializationError = errorMsg;
                    } catch (Exception e) {
                        String errorMsg = String.format(
                                "Failed to initialize TensorFlow runtime: %s. " +
                                        "Original error: %s: %s",
                                e.getMessage(),
                                e.getClass().getSimpleName(),
                                e.getMessage());
                        LOG.error(errorMsg, e);
                        this.initializationError = errorMsg;
                    }
                }
            }
        }
    }

    public Prediction predict(float[] pixels) {
        ensureInitialized();
        if (runtime == null) {
            throw new IllegalStateException(
                    "TensorFlow runtime not initialized. " +
                            (initializationError != null ? initializationError : "Unknown initialization error."));
        }
        long start = System.nanoTime();
        float[] logits = runtime.run(pixels);
        long duration = System.nanoTime() - start;

        int maxIndex = 0;
        float maxVal = logits[0];
        for (int i = 1; i < logits.length; i++) {
            if (logits[i] > maxVal) {
                maxVal = logits[i];
                maxIndex = i;
            }
        }
        return new Prediction(maxIndex, maxVal, duration / 1_000_000.0);
    }

    public record Prediction(int digit, float confidence, double inferenceMs) {
    }
}
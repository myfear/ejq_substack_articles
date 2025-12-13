package com.acme.mnist;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads the TensorFlow native library before any FFI classes are initialized.
 * This is necessary because the FFI bindings try to load the library during static initialization,
 * and we need to ensure the library path is available.
 */
public class TensorFlowLibraryLoader {
    
    // Python TensorFlow library path (check current directory, then parent directory)
    private static final String[] LIBRARY_PATHS = {
        "venv/lib/python3.9/site-packages/tensorflow",  // Python TensorFlow (in project directory)
    };
    
    private static final String LIBRARY_NAME = "tensorflow_cc.2";  // Python TensorFlow uses libtensorflow_cc.2.dylib
    
    private static volatile boolean loaded = false;
    private static String loadedFrom = null;
    
    static {
        loadLibrary();
    }
    
    private static void loadLibrary() {
        if (loaded) {
            return;
        }
        
        // Check environment variable first (highest priority)
        String envLibPath = System.getenv("TENSORFLOW_LIBRARY_PATH");
        if (envLibPath != null && !envLibPath.isEmpty()) {
            try {
                Path libPath = Paths.get(envLibPath);
                if (Files.exists(libPath)) {
                    loadLibraryFromPath(libPath);
                    return;
                }
            } catch (Exception e) {
                // Continue to other paths
            }
        }
        
        // Try each library path in order
        for (String libPathStr : LIBRARY_PATHS) {
            try {
                Path resolvedPath;
                
                // Resolve relative to project root (user.dir is typically the project root when running Maven/Quarkus)
                String projectRoot = System.getProperty("user.dir");
                
                if (libPathStr.startsWith("../")) {
                    // Path relative to parent directory
                    resolvedPath = Paths.get(projectRoot, libPathStr).normalize();
                    
                    // If that doesn't exist, try parent directory (in case we're in a subdirectory)
                    if (!Files.exists(resolvedPath)) {
                        resolvedPath = Paths.get(projectRoot, "..", libPathStr).normalize();
                    }
                } else {
                    // Path relative to current directory
                    resolvedPath = Paths.get(projectRoot, libPathStr).normalize();
                    
                    // If that doesn't exist, try as absolute path
                    if (!Files.exists(resolvedPath)) {
                        resolvedPath = Paths.get(libPathStr);
                    }
                }
                
                Path libFile = resolvedPath.resolve(System.mapLibraryName(LIBRARY_NAME));
                
                if (Files.exists(libFile)) {
                    loadLibraryFromPath(resolvedPath);
                    return;
                }
            } catch (Exception e) {
                // Continue to next path
                continue;
            }
        }
        
        // If we get here, none of the paths worked
        StringBuilder errorMsg = new StringBuilder("Failed to load TensorFlow library. Tried paths:\n");
        if (envLibPath != null) {
            errorMsg.append("  - ").append(envLibPath).append(" (from TENSORFLOW_LIBRARY_PATH)\n");
        }
        for (String path : LIBRARY_PATHS) {
            errorMsg.append("  - ").append(path).append("\n");
        }
        errorMsg.append("\nPlease ensure TensorFlow is installed:\n");
        errorMsg.append("  1. Set TENSORFLOW_LIBRARY_PATH environment variable, or\n");
        errorMsg.append("  2. Install in a venv at ./venv with: pip install tensorflow==2.13.0\n");
        
        throw new RuntimeException(errorMsg.toString());
    }
    
    private static void loadLibraryFromPath(Path libDir) {
        try {
            Path libFile = libDir.resolve(System.mapLibraryName(LIBRARY_NAME));
            
            if (!Files.exists(libFile)) {
                return;
            }
            
            String absolutePath = libFile.toAbsolutePath().toString();
            
            // Try to load the library, but handle the case where it's already loaded
            try {
                System.load(absolutePath);
            } catch (UnsatisfiedLinkError e) {
                // Check if the error is because the library is already loaded
                if (e.getMessage() != null && e.getMessage().contains("already loaded")) {
                    // Library is already loaded in another classloader (common in dev mode)
                    // This is fine - we can proceed
                    System.out.println("TensorFlow library already loaded (likely in another classloader): " + absolutePath);
                } else {
                    // Some other error - rethrow
                    throw e;
                }
            }
            
            // For Python TensorFlow, also load the framework library
            Path frameworkLib = libDir.resolve(System.mapLibraryName("tensorflow_framework.2"));
            if (Files.exists(frameworkLib)) {
                try {
                    System.load(frameworkLib.toAbsolutePath().toString());
                } catch (UnsatisfiedLinkError e) {
                    // Handle already loaded case for framework library too
                    if (e.getMessage() == null || !e.getMessage().contains("already loaded")) {
                        throw e;
                    }
                    // Already loaded is fine
                }
            }
            
            loaded = true;
            loadedFrom = absolutePath;
            System.out.println("TensorFlow library available from: " + absolutePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load library from " + libDir + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Ensures the library is loaded. Call this before using any TensorFlow FFI classes.
     */
    public static void ensureLoaded() {
        // Static block already loaded it, but this method ensures the class is initialized
        if (!loaded) {
            loadLibrary();
        }
    }
    
    /**
     * Returns the path from which the library was loaded, or null if not loaded.
     */
    public static String getLoadedFrom() {
        return loadedFrom;
    }
}


package com.acme.mnist;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_FLOAT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static tensorflow.ffi.c_api_h.TF_CloseSession;
import static tensorflow.ffi.c_api_h.TF_DeleteGraph;
import static tensorflow.ffi.c_api_h.TF_DeleteImportGraphDefOptions;
import static tensorflow.ffi.c_api_h.TF_DeleteSession;
import static tensorflow.ffi.c_api_h.TF_DeleteSessionOptions;
import static tensorflow.ffi.c_api_h.TF_DeleteStatus;
import static tensorflow.ffi.c_api_h.TF_DeleteTensor;
import static tensorflow.ffi.c_api_h.TF_GetCode;
import static tensorflow.ffi.c_api_h.TF_GraphImportGraphDef;
import static tensorflow.ffi.c_api_h.TF_GraphOperationByName;
import static tensorflow.ffi.c_api_h.TF_LoadSessionFromSavedModel;
import static tensorflow.ffi.c_api_h.TF_Message;
import static tensorflow.ffi.c_api_h.TF_NewGraph;
import static tensorflow.ffi.c_api_h.TF_NewImportGraphDefOptions;
import static tensorflow.ffi.c_api_h.TF_NewSession;
import static tensorflow.ffi.c_api_h.TF_NewSessionOptions;
import static tensorflow.ffi.c_api_h.TF_NewStatus;
import static tensorflow.ffi.c_api_h.TF_NewTensor;
import static tensorflow.ffi.c_api_h.TF_SessionRun;
import static tensorflow.ffi.c_api_h.TF_TensorByteSize;
import static tensorflow.ffi.c_api_h.TF_TensorData;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import tensorflow.ffi.TF_Buffer;

import jakarta.annotation.PreDestroy;
import tensorflow.ffi.TF_Output;

public class TFRuntime {
    private static final int TF_FLOAT = 1;
    private static final int TF_OK = 0;

    private final MemorySegment graph;
    private final MemorySegment session;
    private final MemorySegment sessionOptions;
    private final String inputOpName;
    private final String outputOpName;
    private final int inputOpIndex;
    private final int outputOpIndex;
    private final Arena modelArena;

    /**
     * Load a frozen graph model (.pb file).
     * Default operation names: inputs:0 and Identity:0
     */
    public TFRuntime(String modelPath, int numThreads) {
        this(modelPath, numThreads, "inputs:0", "Identity:0");
    }

    /**
     * Load a frozen graph model with custom operation names.
     */
    public TFRuntime(String modelPath, int numThreads, String inputOpName, String outputOpName) {
        // Parse operation names (format: "operation_name:index")
        String[] inputParts = inputOpName.split(":");
        String[] outputParts = outputOpName.split(":");
        this.inputOpName = inputParts[0];
        this.inputOpIndex = inputParts.length > 1 ? Integer.parseInt(inputParts[1]) : 0;
        this.outputOpName = outputParts[0];
        this.outputOpIndex = outputParts.length > 1 ? Integer.parseInt(outputParts[1]) : 0;

        var loaded = loadFrozenGraph(resolvePath(modelPath).toString());
        this.graph = loaded.graph();
        this.session = loaded.session();
        this.sessionOptions = loaded.sessionOptions();
        this.modelArena = loaded.arena();
    }

    private record ModelHandle(MemorySegment graph, MemorySegment session, MemorySegment sessionOptions, Arena arena) {
    }

    private Path resolvePath(String path) {
        Path modelPath = Paths.get(path);
        if (!modelPath.isAbsolute()) {
            modelPath = Paths.get(System.getProperty("user.dir")).resolve(path).normalize();
        }
        return modelPath;
    }
    
    private ModelHandle loadFrozenGraph(String path) {
        Arena arena = Arena.ofConfined();
        MemorySegment status = TF_NewStatus();
        try {
            byte[] graphBytes = Files.readAllBytes(Paths.get(path));
            
            MemorySegment sessionOptions = TF_NewSessionOptions();
            checkNotNull(sessionOptions, "Failed to create session options");
            
            MemorySegment graph = TF_NewGraph();
            checkNotNull(graph, "Failed to create graph");
            
            MemorySegment importOptions = TF_NewImportGraphDefOptions();
            checkNotNull(importOptions, "Failed to create import options");
            
            // Create TF_Buffer from graph bytes
            MemorySegment buffer = TF_Buffer.allocate(arena);
            MemorySegment data = arena.allocate(JAVA_BYTE, graphBytes.length);
            data.copyFrom(MemorySegment.ofArray(graphBytes));
            TF_Buffer.data(buffer, data);
            TF_Buffer.length(buffer, graphBytes.length);
            TF_Buffer.data_deallocator(buffer, NULL);
            
            // Import graph
            TF_GraphImportGraphDef(graph, buffer, importOptions, status);
            checkStatus(status, "Failed to import frozen graph from " + path);
            
            // Create session
            MemorySegment session = TF_NewSession(graph, sessionOptions, status);
            checkStatus(status, "Failed to create session");
            checkNotNull(session, "Failed to create session");
            
            TF_DeleteImportGraphDefOptions(importOptions);
            TF_DeleteStatus(status);
            
            return new ModelHandle(graph, session, sessionOptions, arena);
        } catch (Exception e) {
            cleanup(status, arena);
            throw new IllegalStateException("Failed to load frozen graph: " + e.getMessage(), e);
        }
    }
    
    private void checkNotNull(MemorySegment segment, String message) {
        if (segment.equals(NULL)) {
            throw new IllegalStateException(message);
        }
    }
    
    private void checkStatus(MemorySegment status, String message) {
        int code = TF_GetCode(status);
        if (code != TF_OK) {
            MemorySegment msgPtr = TF_Message(status);
            String msg = msgPtr.reinterpret(Long.MAX_VALUE).getString(0, StandardCharsets.UTF_8);
            throw new IllegalStateException(message + ": " + msg);
        }
    }
    
    private void cleanup(MemorySegment status, Arena arena) {
        if (status != null && !status.equals(NULL)) {
            TF_DeleteStatus(status);
        }
        if (arena != null) {
            arena.close();
        }
    }

    public float[] run(float[] input) {
        if (input.length != 28 * 28) {
            throw new IllegalArgumentException("Expected 784 input floats, got " + input.length);
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment status = TF_NewStatus();
            try {
                // Get operations
                MemorySegment inputOp = getOperation(arena, inputOpName);
                MemorySegment outputOp = getOperation(arena, outputOpName);

                // Create input tensor (Arena.ofAuto() for memory that outlives this scope)
                MemorySegment inputTensor = createInputTensor(arena, input);

                // Prepare I/O arrays
                MemorySegment inputs = createTFOutput(arena, inputOp, inputOpIndex);
                MemorySegment inputValues = arena.allocate(ADDRESS);
                inputValues.set(ADDRESS, 0, inputTensor);

                MemorySegment outputs = createTFOutput(arena, outputOp, outputOpIndex);
                MemorySegment outputValues = arena.allocate(ADDRESS);

                // Run inference
                TF_SessionRun(session, NULL, inputs, inputValues, 1, outputs, outputValues, 1, NULL, 0, NULL, status);
                checkStatus(status, "Session run failed");

                // Extract and read output
                MemorySegment outputTensor = outputValues.get(ADDRESS, 0);
                checkNotNull(outputTensor, "Output tensor is null");

                float[] result = readOutputTensor(outputTensor);
                
                // CRITICAL: Delete output tensor (TensorFlow expects caller to free it)
                // Do NOT delete input tensor (Arena.ofAuto() manages it)
                TF_DeleteTensor(outputTensor);

                return result;
            } finally {
                TF_DeleteStatus(status);
            }
        }
    }

    private MemorySegment getOperation(Arena arena, String opName) {
        byte[] nameBytes = (opName + "\0").getBytes(StandardCharsets.UTF_8);
        MemorySegment nameSeg = arena.allocate(JAVA_BYTE, nameBytes.length);
        nameSeg.copyFrom(MemorySegment.ofArray(nameBytes));
        MemorySegment op = TF_GraphOperationByName(graph, nameSeg);
        checkNotNull(op, "Operation '" + opName + "' not found in graph");
        return op;
    }

    private MemorySegment createInputTensor(Arena arena, float[] input) {
        // Shape: [1, 28, 28] for batch size 1
        MemorySegment dims = arena.allocate(JAVA_LONG.byteSize() * 3);
        dims.set(JAVA_LONG, 0, 1L);
        dims.set(JAVA_LONG, JAVA_LONG.byteSize(), 28L);
        dims.set(JAVA_LONG, JAVA_LONG.byteSize() * 2, 28L);

        // Use Arena.ofAuto() so memory outlives this method
        Arena tensorArena = Arena.ofAuto();
        long dataSize = (long) input.length * Float.BYTES;
        MemorySegment data = tensorArena.allocate(dataSize);
        data.copyFrom(MemorySegment.ofArray(input));

        MemorySegment tensor = TF_NewTensor(TF_FLOAT, dims, 3, data, dataSize, NULL, NULL);
        checkNotNull(tensor, "Failed to create input tensor");
        return tensor;
    }

    private MemorySegment createTFOutput(Arena arena, MemorySegment op, int index) {
        MemorySegment output = TF_Output.allocate(arena);
        TF_Output.oper(output, op);
        TF_Output.index(output, index);
        return output;
    }

    private float[] readOutputTensor(MemorySegment tensor) {
        long byteSize = TF_TensorByteSize(tensor);
        int size = (int) (byteSize / Float.BYTES);
        MemorySegment data = TF_TensorData(tensor);
        float[] result = new float[size];
        MemorySegment.copy(data, JAVA_FLOAT, 0, result, 0, size);
        return result;
    }

    @PreDestroy
    public void close() {
        // Close and delete session first
        if (session != null && !session.equals(NULL)) {
            MemorySegment status = TF_NewStatus();
            try {
                TF_CloseSession(session, status);
                TF_DeleteSession(session, status);
            } finally {
                TF_DeleteStatus(status);
            }
        }
        // Delete graph
        if (graph != null && !graph.equals(NULL)) {
            TF_DeleteGraph(graph);
        }
        // Delete session options last (after session is closed)
        if (sessionOptions != null && !sessionOptions.equals(NULL)) {
            TF_DeleteSessionOptions(sessionOptions);
        }
        // Close the arena last to free the strings
        if (modelArena != null) {
            modelArena.close();
        }
    }
}
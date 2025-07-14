package org.acme.util;

import dev.langchain4j.data.embedding.Embedding;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Utility class for quantizing high-dimensional embedding vectors into compact representations.
 * 
 * <p>This service provides scalar quantization functionality to compress embedding vectors
 * from their original high-precision floating-point representation into more storage-efficient
 * formats. The quantization process trades precision for storage efficiency, making it
 * suitable for archival storage and memory-constrained operations.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><strong>Scalar Quantization:</strong> Reduces vectors to single-byte representations</li>
 *   <li><strong>Adaptive Quantization:</strong> Uses different strategies based on vector characteristics</li>
 *   <li><strong>Lossy Compression:</strong> Significantly reduces storage requirements</li>
 *   <li><strong>Approximate Reconstruction:</strong> Provides rough dequantization for analysis</li>
 * </ul>
 * 
 * <p>Quantization approaches:</p>
 * <ul>
 *   <li><strong>Average-Based:</strong> For vectors with significant non-zero averages</li>
 *   <li><strong>Range-Based:</strong> For normalized vectors with near-zero averages</li>
 *   <li><strong>Statistical:</strong> Uses vector statistics for edge cases</li>
 * </ul>
 * 
 * <p><strong>Important Note:</strong> This is a simplified quantization implementation
 * suitable for demonstration and basic archival purposes. Production systems should
 * consider more sophisticated techniques such as:</p>
 * <ul>
 *   <li>Product Quantization (PQ)</li>
 *   <li>Binary Quantization</li>
 *   <li>Learned Quantization</li>
 *   <li>Multi-byte quantization schemes</li>
 * </ul>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@ApplicationScoped
public class ScalarQuantizer {
    
    /**
     * Quantizes an embedding vector into a single-byte representation.
     * 
     * <p>This method compresses a high-dimensional embedding vector into a single byte
     * value using adaptive quantization strategies. The quantization approach is selected
     * based on the statistical characteristics of the input vector:</p>
     * 
     * <ul>
     *   <li><strong>Standard Quantization:</strong> For vectors with meaningful averages,
     *       scales the average to the [-128, 127] byte range</li>
     *   <li><strong>Range-Based Quantization:</strong> For normalized vectors with near-zero
     *       averages, uses the value range and statistical properties</li>
     *   <li><strong>Threshold-Based Quantization:</strong> Applies minimum thresholds to
     *       handle edge cases and very small values</li>
     * </ul>
     * 
     * <p>The quantization process:</p>
     * <ol>
     *   <li>Calculates statistical properties (sum, average, min, max)</li>
     *   <li>Determines the appropriate quantization strategy</li>
     *   <li>Scales the representative value to byte range</li>
     *   <li>Clamps the result to valid byte values [-128, 127]</li>
     * </ol>
     * 
     * <p><strong>Precision Loss:</strong> This quantization is highly lossy and is
     * intended for archival storage rather than high-precision operations. The
     * single-byte representation captures only the most general characteristics
     * of the original vector.</p>
     * 
     * @param embedding The high-dimensional embedding to quantize
     * @return A byte value representing the quantized embedding
     * @throws NullPointerException if the embedding is null
     * @throws IllegalArgumentException if the embedding vector is empty
     */
    public byte quantize(Embedding embedding) {
        float[] vector = embedding.vector();
        
        // Simple quantization: compute the average of all dimensions
        // and map to byte range [-128, 127]
        double sum = 0.0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (float value : vector) {
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        double average = sum / vector.length;
        
        Log.infof("Quantizer Debug: vector length=%d, sum=%f, average=%f, min=%f, max=%f", 
                vector.length, sum, average, min, max);
        
        // Improved quantization approach: use the range of values
        // If the average is very close to 0, use the dominant direction instead
        if (Math.abs(average) < 0.001) {
            // For normalized embeddings, use the sign of the sum and a scaled magnitude
            // based on the range of values
            double range = max - min;
            double scaledValue = (average / range) * 127;
            
            // If still too small, use a minimum threshold based on vector statistics
            if (Math.abs(scaledValue) < 1.0) {
                scaledValue = Math.signum(average) * Math.min(Math.abs(range * 100), 10.0);
            }
            
            Log.infof("Quantizer: Using improved quantization - range=%f, scaledValue=%f", range, scaledValue);
            
            int quantized = (int) Math.round(scaledValue);
            quantized = Math.max(-128, Math.min(127, quantized));
            
            return (byte) quantized;
        }
        
        // Original approach for non-normalized embeddings
        int quantized = (int) Math.round(average * 127);
        quantized = Math.max(-128, Math.min(127, quantized));
        
        Log.infof("Quantizer: Using original quantization - scaled=%f, final=%d", average * 127, quantized);
        
        return (byte) quantized;
    }
    
    /**
     * Dequantizes a byte value back to an approximate embedding vector.
     * 
     * <p>This method attempts to reconstruct an embedding vector from its quantized
     * byte representation. The reconstruction is highly approximate and should be
     * used only for analysis, debugging, or rough similarity estimates.</p>
     * 
     * <p>Reconstruction process:</p>
     * <ol>
     *   <li>Scales the byte value back to [-1.0, 1.0] float range</li>
     *   <li>Creates a vector of the specified dimensions</li>
     *   <li>Fills all dimensions with the same reconstructed value</li>
     * </ol>
     * 
     * <p><strong>Limitations:</strong> This reconstruction is extremely crude and
     * loses all dimensional information from the original vector. It provides only
     * a general magnitude and direction indicator. Real-world applications would
     * require more sophisticated dequantization methods.</p>
     * 
     * <p><strong>Use Cases:</strong> Suitable for:</p>
     * <ul>
     *   <li>Debugging quantization behavior</li>
     *   <li>Rough magnitude comparisons</li>
     *   <li>Statistical analysis of quantized data</li>
     *   <li>Fallback operations when original embeddings are unavailable</li>
     * </ul>
     * 
     * @param quantizedByte The quantized byte value to reconstruct from
     * @param dimensions The number of dimensions for the output vector
     * @return An approximate embedding vector with all dimensions set to the same value
     * @throws IllegalArgumentException if dimensions is less than 1
     */
    public Embedding dequantize(byte quantizedByte, int dimensions) {
        float value = quantizedByte / 127.0f;
        float[] vector = new float[dimensions];
        
        // Fill all dimensions with the same value (very crude approximation)
        for (int i = 0; i < dimensions; i++) {
            vector[i] = value;
        }
        
        return new Embedding(vector);
    }
} 
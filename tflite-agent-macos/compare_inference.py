#!/usr/bin/env python3
"""
Unified inference comparison v2: Python TensorFlow vs Java REST API
Uses server-side benchmark endpoint for accurate Java measurements.
"""

import tensorflow as tf
import numpy as np
import time
import json
import requests
import statistics
import sys

# Configuration
API_URL = "http://localhost:8080/api/mnist/benchmark"
FROZEN_GRAPH_PATH = "mnist_frozen_graph.pb"
PAYLOAD_PATH = "payload.json"
NUM_ITERATIONS = 100
WARMUP_ITERATIONS = 10

def test_python_inference():
    """Test Python TensorFlow inference time."""
    print("\n" + "="*70)
    print("PYTHON TENSORFLOW INFERENCE TEST")
    print("="*70)
    
    # Load the frozen graph
    print("Loading frozen graph model...")
    with tf.io.gfile.GFile(FROZEN_GRAPH_PATH, 'rb') as f:
        graph_def = tf.compat.v1.GraphDef()
        graph_def.ParseFromString(f.read())
    
    # Load test sample
    with open(PAYLOAD_PATH, 'r') as f:
        payload = json.load(f)
    test_sample = np.array(payload['pixels'], dtype=np.float32).reshape(1, 28, 28)
    
    # Create session
    with tf.compat.v1.Session() as sess:
        tf.import_graph_def(graph_def, name='')
        input_tensor = sess.graph.get_tensor_by_name('inputs:0')
        output_tensor = sess.graph.get_tensor_by_name('Identity:0')
        
        # Warm-up
        print(f"Warming up ({WARMUP_ITERATIONS} iterations)...")
        for _ in range(WARMUP_ITERATIONS):
            _ = sess.run(output_tensor, feed_dict={input_tensor: test_sample})
        
        # Benchmark
        print(f"Benchmarking ({NUM_ITERATIONS} iterations)...")
        times = []
        for _ in range(NUM_ITERATIONS):
            start = time.perf_counter()
            result = sess.run(output_tensor, feed_dict={input_tensor: test_sample})
            elapsed = (time.perf_counter() - start) * 1000  # Convert to ms
            times.append(elapsed)
        
        # Get prediction for verification
        prediction = sess.run(output_tensor, feed_dict={input_tensor: test_sample})
        predicted_digit = np.argmax(prediction[0])
        confidence = np.max(prediction[0])
        
        return {
            'times': times,
            'digit': int(predicted_digit),
            'confidence': float(confidence),
            'average': statistics.mean(times),
            'median': statistics.median(times),
            'min': min(times),
            'max': max(times),
            'std_dev': statistics.stdev(times) if len(times) > 1 else 0,
            'p95': statistics.quantiles(times, n=20)[18],
            'p99': statistics.quantiles(times, n=100)[98],
            'throughput': 1000 / statistics.mean(times)
        }

def test_java_server_benchmark():
    """Test Java REST API using server-side benchmark endpoint."""
    print("\n" + "="*70)
    print("JAVA REST API BENCHMARK (Server-Side)")
    print("="*70)
    
    # Check if server is running
    try:
        response = requests.get("http://localhost:8080", timeout=2)
        print("✓ Server is running")
    except:
        print("✗ Server is not responding. Please start the application first.")
        return None
    
    # Load payload
    with open(PAYLOAD_PATH, 'r') as f:
        payload = json.load(f)
    
    # Call server-side benchmark endpoint
    print(f"Running server-side benchmark ({NUM_ITERATIONS} iterations, {WARMUP_ITERATIONS} warmup)...")
    
    try:
        response = requests.post(
            f"{API_URL}?iterations={NUM_ITERATIONS}&warmup={WARMUP_ITERATIONS}",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=30
        )
        
        if response.status_code == 200:
            result = response.json()
            print("✓ Benchmark completed")
            
            return {
                'times': None,  # Server doesn't return individual times
                'digit': result['predictedDigit'],
                'confidence': result['confidence'],
                'average': result['averageMs'],
                'median': result['medianMs'],
                'min': result['minMs'],
                'max': result['maxMs'],
                'std_dev': result['stdDevMs'],
                'p95': result['p95Ms'],
                'p99': result['p99Ms'],
                'throughput': result['throughputPerSec'],
                'iterations': result['iterations'],
                'warmup': result['warmupIterations']
            }
        else:
            print(f"✗ Benchmark failed with status {response.status_code}")
            print(f"Response: {response.text}")
            return None
    except Exception as e:
        print(f"✗ Benchmark failed: {e}")
        return None

def print_comparison(python_results, java_results):
    """Print side-by-side comparison of inference times."""
    print("\n" + "="*70)
    print("INFERENCE TIME COMPARISON (Pure Inference Only)")
    print("="*70)
    
    print(f"\n{'Metric':<20} {'Python TensorFlow':<25} {'Java (Server-Side)':<25}")
    print("-" * 70)
    
    # Average
    print(f"{'Average':<20} {python_results['average']:>10.3f} ms {'':<14} {java_results['average']:>10.3f} ms")
    
    # Median
    print(f"{'Median':<20} {python_results['median']:>10.3f} ms {'':<14} {java_results['median']:>10.3f} ms")
    
    # Min
    print(f"{'Min':<20} {python_results['min']:>10.3f} ms {'':<14} {java_results['min']:>10.3f} ms")
    
    # Max
    print(f"{'Max':<20} {python_results['max']:>10.3f} ms {'':<14} {java_results['max']:>10.3f} ms")
    
    # Std Dev
    print(f"{'Std Dev':<20} {python_results['std_dev']:>10.3f} ms {'':<14} {java_results['std_dev']:>10.3f} ms")
    
    # P95
    print(f"{'P95':<20} {python_results['p95']:>10.3f} ms {'':<14} {java_results['p95']:>10.3f} ms")
    
    # P99
    print(f"{'P99':<20} {python_results['p99']:>10.3f} ms {'':<14} {java_results['p99']:>10.3f} ms")
    
    print("\n" + "-" * 70)
    
    # Throughput
    print(f"{'Throughput':<20} {python_results['throughput']:>10.0f} pred/sec {'':<8} {java_results['throughput']:>10.0f} pred/sec")
    
    # Speedup
    speedup = java_results['average'] / python_results['average']
    if speedup > 1:
        print(f"\n{'Performance':<20} Python is {speedup:.2f}x FASTER than Java")
    else:
        print(f"\n{'Performance':<20} Java is {1/speedup:.2f}x FASTER than Python")
    
    # Absolute difference
    diff_ms = java_results['average'] - python_results['average']
    print(f"{'Difference':<20} {diff_ms:.3f} ms per inference")
    
    # Verify predictions match
    print("\n" + "="*70)
    print("PREDICTION VERIFICATION")
    print("="*70)
    print(f"{'Method':<20} {'Digit':<10} {'Confidence':<15} {'Match':<10}")
    print("-" * 70)
    print(f"{'Python':<20} {python_results['digit']:<10} {python_results['confidence']:<15.6f} {'✓':<10}")
    print(f"{'Java':<20} {java_results['digit']:<10} {java_results['confidence']:<15.6f} {'✓' if python_results['digit'] == java_results['digit'] else '✗':<10}")
    
    if python_results['digit'] == java_results['digit']:
        print("\n✅ Both methods predict the same digit!")
    else:
        print("\n⚠️  Warning: Predictions differ!")
    
    # Additional info
    print("\n" + "="*70)
    print("TEST CONFIGURATION")
    print("="*70)
    print(f"Iterations:      {NUM_ITERATIONS}")
    print(f"Warmup:          {WARMUP_ITERATIONS}")
    print(f"Java verified:   {java_results['iterations']} iterations, {java_results['warmup']} warmup")

def main():
    print("="*70)
    print("INFERENCE COMPARISON v2: Python vs Java (Server-Side Benchmark)")
    print("="*70)
    print(f"Iterations: {NUM_ITERATIONS}")
    print(f"Warmup: {WARMUP_ITERATIONS}")
    print("\nThis version uses server-side benchmarking for accurate Java measurements")
    print("without network overhead.")
    
    # Test Python
    python_results = test_python_inference()
    
    # Test Java (server-side)
    java_results = test_java_server_benchmark()
    
    if java_results is None:
        print("\n✗ Java benchmark failed. Exiting.")
        sys.exit(1)
    
    # Print comparison
    print_comparison(python_results, java_results)
    
    print("\n" + "="*70)
    print("COMPARISON COMPLETE")
    print("="*70)
    print("\n💡 Note: Both measurements are now pure inference time without")
    print("   network/HTTP overhead, providing an accurate comparison.")

if __name__ == "__main__":
    main()

# Made with Bob

#!/bin/bash

# Improved Quarkus Benchmark Script
# Usage: ./benchmark_improved.sh [jvm|native]

set -e

MODE=${1:-jvm}

# Enhanced configuration with endpoint-specific settings
LOAD_TEST_DURATION=60  # 3 minutes per test
HELLO_CONCURRENCY=50    # Concurrency for hello endpoint
COMPUTE_CONCURRENCY=20  # Lower concurrency for compute endpoint
IDLE_DURATION=30
WARMUP_DURATION=30      # Longer warmup for JIT

# System configuration
# SYSTEM_CPUS=${SYSTEM_CPUS:-$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo "12")}  # Auto-detect or default to 12
SYSTEM_CPUS=$(echo "4")

# Detect operating system
if [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
else
    OS="unknown"
fi

# Enhanced compute workload
COMPUTE_ITERATIONS=10000  # Heavy workload

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_debug() { echo -e "${BLUE}[DEBUG]${NC} $1"; }

# Function to wait for a process with timeout
wait_with_timeout() {
    local pid=$1
    local timeout=${2:-5}
    local count=0
    
    # Check if process exists
    if ! kill -0 $pid 2>/dev/null; then
        return 0
    fi
    
    # Wait for process to finish with timeout
    while [ $count -lt $timeout ]; do
        if ! kill -0 $pid 2>/dev/null; then
            return 0
        fi
        sleep 1
        count=$((count + 1))
    done
    
    # If we're here, the process didn't finish within timeout
    log_warn "Process $pid didn't finish within $timeout seconds, killing it"
    kill -9 $pid 2>/dev/null || true
    return 1
}

configure_mode() {
    if [ "$MODE" = "jvm" ]; then
        echo "╔════════════════════════════════════════════════════╗"
        echo "║        Improved Quarkus JVM Benchmark Suite        ║"
        echo "╚════════════════════════════════════════════════════╝"
        APP_NAME="JVM"
        APP_CMD="java -Xms512m -Xmx2g -XX:+UseParallelGC -XX:ActiveProcessorCount=$SYSTEM_CPUS -XX:+UseStringDeduplication -jar target/quarkus-app/quarkus-run.jar"
        
        if [ ! -f "target/quarkus-app/quarkus-run.jar" ]; then
            log_error "JVM JAR not found. Run: ./mvnw clean package"
            exit 1
        fi
        
    elif [ "$MODE" = "native" ]; then
        echo "╔════════════════════════════════════════════════════╗"
        echo "║      Improved Quarkus Native Benchmark Suite      ║"
        echo "╚════════════════════════════════════════════════════╝"
        APP_NAME="Native"
        
        NATIVE_RUNNER=$(find target -name "*-runner" -type f 2>/dev/null | head -n 1)
        if [ -z "$NATIVE_RUNNER" ]; then
            log_error "Native binary not found. Run: ./mvnw clean package -Pnative"
            exit 1
        fi
        
        # Set CPU affinity based on OS
        if [ "$OS" = "linux" ]; then
            APP_CMD="taskset -c 0-$(($SYSTEM_CPUS-1)) $NATIVE_RUNNER"
        elif [ "$OS" = "macos" ]; then
            # macOS: Use taskpolicy to control CPU usage
            # -c utility: Set QoS clamp to utility (lower priority than default)
            # Note: -B can only be used with -p (existing process), not for new processes
            APP_CMD="taskpolicy -c utility $NATIVE_RUNNER"
        else
            # Unknown OS, run without CPU affinity
            APP_CMD="$NATIVE_RUNNER"
        fi
        
    else
        echo "Usage: $0 [jvm|native]"
        exit 1
    fi
    
    LOG_FILE="${MODE}.log"
    IDLE_CSV="${MODE}_idle.csv"
    HELLO_LOAD_CSV="${MODE}_hello_load.csv"
    COMPUTE_LOAD_CSV="${MODE}_compute_load.csv"
    HELLO_RESULTS="${MODE}_hello_results.txt"
    COMPUTE_RESULTS="${MODE}_compute_results.txt"
    HELLO_SYSTEM_CSV="${MODE}_hello_system.csv"
    COMPUTE_SYSTEM_CSV="${MODE}_compute_system.csv"
}

# Enhanced compute endpoint with heavier workload, progress indicator, and timeout
test_compute_heavy() {
    local duration=$1
    local output=$2
    local concurrency=${3:-$COMPUTE_CONCURRENCY}
    
    log_info "Running heavy compute test (${COMPUTE_ITERATIONS} iterations) for ${duration}s with ${concurrency} concurrent connections..."
    
    # Start wrk in background
    wrk -t2 -c$concurrency -d${duration}s --latency "http://localhost:8080/api/compute?iterations=${COMPUTE_ITERATIONS}" > $output 2>&1 &
    local WRK_PID=$!
    
    # Show progress bar while test is running
    for i in $(seq 1 $duration); do
        if ! kill -0 $WRK_PID 2>/dev/null; then
            # wrk finished early
            printf "\r  Progress: [%-50s] %d%% (completed early)" "$(printf '#%.0s' $(seq 1 50))" 100
            break
        fi
        printf "\r  Progress: [%-50s] %d%%" "$(printf '#%.0s' $(seq 1 $((i * 50 / duration))))" $((i * 100 / duration))
        sleep 1
    done
    echo ""
    
    # Check if wrk is still running after the duration
    if kill -0 $WRK_PID 2>/dev/null; then
        log_warn "wrk is still running after ${duration}s. Terminating..."
        kill $WRK_PID 2>/dev/null
        sleep 1
        # Force kill if still running
        if kill -0 $WRK_PID 2>/dev/null; then
            kill -9 $WRK_PID 2>/dev/null
        fi
        echo "Benchmark terminated due to timeout" >> $output
        return 1
    fi
    
    # Wait for wrk to finish and capture exit code
    wait $WRK_PID
    local exit_code=$?
    
    # Validate exit code and output
    if [ $exit_code -ne 0 ]; then
        log_error "wrk failed with exit code $exit_code for compute test"
        return $exit_code
    fi
    
    # Validate that we got meaningful output
    if [ ! -s "$output" ] || ! grep -q "Requests/sec:" "$output"; then
        log_error "wrk produced no valid output for compute test"
        return 1
    fi
    
    return 0
}

# Enhanced hello endpoint test with progress indicator and timeout
test_hello() {
    local duration=$1
    local output=$2
    local concurrency=${3:-$HELLO_CONCURRENCY}
    
    log_info "Running hello endpoint test for ${duration}s with ${concurrency} concurrent connections..."
    
    # Start wrk in background
    wrk -t2 -c$concurrency -d${duration}s --latency http://localhost:8080/api/hello > $output 2>&1 &
    local WRK_PID=$!
    
    # Show progress bar while test is running
    for i in $(seq 1 $duration); do
        if ! kill -0 $WRK_PID 2>/dev/null; then
            # wrk finished early
            printf "\r  Progress: [%-50s] %d%% (completed early)" "$(printf '#%.0s' $(seq 1 50))" 100
            break
        fi
        printf "\r  Progress: [%-50s] %d%%" "$(printf '#%.0s' $(seq 1 $((i * 50 / duration))))" $((i * 100 / duration))
        sleep 1
    done
    echo ""
    
    # Check if wrk is still running after the duration
    if kill -0 $WRK_PID 2>/dev/null; then
        log_warn "wrk is still running after ${duration}s. Terminating..."
        kill $WRK_PID 2>/dev/null
        sleep 1
        # Force kill if still running
        if kill -0 $WRK_PID 2>/dev/null; then
            kill -9 $WRK_PID 2>/dev/null
        fi
        echo "Benchmark terminated due to timeout" >> $output
        return 1
    fi
    
    # Wait for wrk to finish and capture exit code
    wait $WRK_PID
    local exit_code=$?
    
    # Validate exit code and output
    if [ $exit_code -ne 0 ]; then
        log_error "wrk failed with exit code $exit_code for hello test"
        return $exit_code
    fi
    
    # Validate that we got meaningful output
    if [ ! -s "$output" ] || ! grep -q "Requests/sec:" "$output"; then
        log_error "wrk produced no valid output for hello test"
        return 1
    fi
    
    return 0
}

# System resource monitoring synchronized with test execution
monitor_system() {
    local output=$1
    local wrk_pid=$2
    
    log_info "Starting system monitoring..."
    echo "timestamp,cpu_percent,memory_percent,memory_pressure,load_avg" > $output
    
    # Monitor while wrk is running
    while kill -0 $wrk_pid 2>/dev/null; do
        # Get system-wide CPU usage using top
        CPU=$(top -l 1 -stats cpu | grep "CPU usage" | awk '{print $3}' | sed 's/%//' | head -1)
        # Fallback to ps if top fails
        if [ -z "$CPU" ] || [ "$CPU" = "" ]; then
            CPU=$(ps -A -o %cpu | awk '{s+=$1} END {print s}')
        fi
        
        # Get memory usage using vm_stat
        MEMORY=$(vm_stat | grep "Pages active" | awk '{print $3}' | sed 's/\.//')
        MEMORY=$(echo "scale=2; $MEMORY * 4096 / 1048576" | bc)
        
        # Get memory pressure if available
        MEMORY_PRESSURE=$(memory_pressure 2>/dev/null | grep "System-wide memory free percentage" | awk '{print $5}' | sed 's/%//' || echo "N/A")
        
        # Get load average
        LOAD=$(uptime | awk '{print $(NF-2)}' | sed 's/,//')
        
        # Write to output file
        echo "$(date +%s),$CPU,$MEMORY,$MEMORY_PRESSURE,$LOAD" >> $output
        sleep 1
    done
    
    log_info "System monitoring stopped (wrk finished)"
}

# Function to monitor a specific process synchronized with test execution
monitor_process() {
    local pid=$1
    local output=$2
    local wrk_pid=$3
    
    log_info "Starting process monitoring for PID ${pid}..."
    echo "timestamp,rss_mb,cpu_percent" > "$output"
    
    # Monitor while wrk is running and target process exists
    while kill -0 $wrk_pid 2>/dev/null && kill -0 $pid 2>/dev/null; do
        # Get memory usage directly
        local mem=$(ps -p $pid -o rss= 2>/dev/null | awk '{printf "%.2f", $1/1024}')
        local cpu=$(ps -p $pid -o pcpu= 2>/dev/null | awk '{printf "%.2f", $1}')
        
        # Append to CSV
        echo "$(date +%s),$mem,$cpu" >> "$output"
        sleep 1
    done
    
    # Check why monitoring stopped
    if ! kill -0 $wrk_pid 2>/dev/null; then
        log_info "Process monitoring stopped (wrk finished)"
    elif ! kill -0 $pid 2>/dev/null; then
        log_warn "Process $pid no longer exists, stopping monitoring"
    fi
}

# Run test with synchronized monitoring
run_test_with_monitoring() {
    local test_type=$1
    local duration=$2
    local output=$3
    local concurrency=$4
    local system_csv=$5
    local process_csv=$6
    local app_pid=$7
    
    log_info "Starting $test_type test with synchronized monitoring..."
    
    # Start the actual test with JSON output
    if [ "$test_type" = "hello" ]; then
        wrk -t2 -c$concurrency -d${duration}s -s wrk_json.lua http://localhost:8080/api/hello > $output 2>&1 &
    else
        wrk -t2 -c$concurrency -d${duration}s -s wrk_json.lua "http://localhost:8080/api/compute?iterations=${COMPUTE_ITERATIONS}" > $output 2>&1 &
    fi
    local WRK_PID=$!
    
    # Start monitoring synchronized with wrk
    log_info "  → Starting monitoring processes..."
    monitor_system $system_csv $WRK_PID &
    local SYSTEM_MONITOR_PID=$!
    
    monitor_process $app_pid $process_csv $WRK_PID &
    local PROCESS_MONITOR_PID=$!
    
    # Show progress bar while test is running
    for i in $(seq 1 $duration); do
        if ! kill -0 $WRK_PID 2>/dev/null; then
            # wrk finished early
            printf "\r  Progress: [%-50s] %d%% (completed early)" "$(printf '#%.0s' $(seq 1 50))" 100
            break
        fi
        printf "\r  Progress: [%-50s] %d%%" "$(printf '#%.0s' $(seq 1 $((i * 50 / duration))))" $((i * 100 / duration))
        sleep 1
    done
    echo ""
    
    # Check if wrk is still running after the duration
    if kill -0 $WRK_PID 2>/dev/null; then
        log_warn "wrk is still running after ${duration}s. Terminating..."
        kill $WRK_PID 2>/dev/null
        sleep 1
        # Force kill if still running
        if kill -0 $WRK_PID 2>/dev/null; then
            kill -9 $WRK_PID 2>/dev/null
        fi
        echo "Benchmark terminated due to timeout" >> $output
        return 1
    fi
    
    # Wait for wrk to finish and capture exit code
    wait $WRK_PID
    local exit_code=$?
    
    # Stop monitoring (it should stop automatically when wrk finishes)
    wait $SYSTEM_MONITOR_PID 2>/dev/null || true
    wait $PROCESS_MONITOR_PID 2>/dev/null || true
    
    # Validate exit code and output
    if [ $exit_code -ne 0 ]; then
        log_error "wrk failed with exit code $exit_code for $test_type test"
        return $exit_code
    fi
    
    # Validate that we got meaningful output (JSON or text format)
    if [ ! -s "$output" ] || (! grep -q "requests_per_second" "$output" && ! grep -q "Requests/sec:" "$output"); then
        log_error "wrk produced no valid output for $test_type test"
        return 1
    fi
    
    return 0
}

# This function is no longer needed as we handle stopping directly
# Keeping as a placeholder to avoid changing too much code
stop_monitoring() {
    # This function is now a no-op
    :
}

# Enhanced throughput calculation for wrk JSON output
calculate_throughput() {
    local result_file=$1
    local test_duration=$2
    
    # Try to parse JSON output first
    local rps=""
    if command -v jq >/dev/null 2>&1; then
        # Use jq to parse JSON
        rps=$(jq -r '.requests_per_second // empty' "$result_file" 2>/dev/null)
        if [ -n "$rps" ] && [ "$rps" != "null" ]; then
            printf "%.2f" "$rps"
            return 0
        fi
    fi
    
    # Fallback: try to extract JSON manually (basic parsing)
    rps=$(grep -o '"requests_per_second":[0-9.]*' "$result_file" 2>/dev/null | cut -d':' -f2)
    if [ -n "$rps" ]; then
        printf "%.2f" "$rps"
        return 0
    fi
    
    # Fallback: try to calculate from requests and duration
    local requests=$(grep -o '"requests":[0-9]*' "$result_file" 2>/dev/null | cut -d':' -f2)
    if [ -n "$requests" ] && [ "$requests" != "0" ]; then
        rps=$(echo "scale=2; $requests / $test_duration" | bc 2>/dev/null)
        if [ -n "$rps" ]; then
            printf "%.2f" "$rps"
            return 0
        fi
    fi
    
    # Final fallback: try old text format parsing
    rps=$(grep "Requests/sec:" $result_file 2>/dev/null | awk '{printf "%.2f", $2}')
    if [ -n "$rps" ]; then
        printf "%.2f" "$rps"
        return 0
    fi
    
    # If all else fails, return N/A
    echo "N/A"
}

# Cleanup function to ensure all processes are terminated
cleanup() {
    log_info "Cleaning up processes and temporary files..."
    
    # Kill application if it's still running
    if [ -n "$APP_PID" ] && kill -0 $APP_PID 2>/dev/null; then
        kill $APP_PID 2>/dev/null || true
        wait $APP_PID 2>/dev/null || true
    fi
    
    # Remove any temporary files
    rm -f /tmp/benchmark_*.sh 2>/dev/null || true
    rm -f /tmp/tmp.* 2>/dev/null || true
}

main() {
    # Set up trap to ensure cleanup on exit
    trap cleanup EXIT INT TERM
    
    configure_mode
    
    # Log system configuration
    log_info "System configuration: ${SYSTEM_CPUS} CPUs detected on ${OS}"
    if [ "$MODE" = "native" ]; then
        if [ "$OS" = "linux" ]; then
            log_info "CPU affinity: taskset -c 0-$(($SYSTEM_CPUS-1))"
        elif [ "$OS" = "macos" ]; then
            log_info "CPU affinity: taskpolicy -c utility"
        else
            log_warn "CPU affinity: None (unknown OS)"
        fi
    else
        log_info "CPU affinity: JVM -XX:ActiveProcessorCount=$SYSTEM_CPUS"
    fi
    
    # Clean previous results
    log_info "Cleaning previous results..."
    rm -f ${MODE}_*.csv ${MODE}_*.txt ${MODE}_*.log
    
    # Clear OS caches for consistent measurements
    log_info "Clearing OS caches for consistent measurements..."
    sync && sudo purge
    
    # Enhanced startup measurement - measuring true time to first request
    log_info "Starting application and measuring time to first request..."
    
    # Start application
    $APP_CMD > $LOG_FILE 2>&1 &
    APP_PID=$!
    echo "  → PID: $APP_PID"
    
    # Measure initial memory immediately after process start
    log_info "Measuring startup memory allocation..."
    local initial_mem=$(ps -p $APP_PID -o rss= 2>/dev/null | awk '{print $1/1024}')
    sleep 1  # Brief pause to let process initialize
    
    # Wait for basic HTTP server to be ready (with reasonable polling interval)
    log_info "Waiting for HTTP server to be ready..."
    local attempts=0
    local max_attempts=60  # 60 seconds max
    
    until curl -sf http://localhost:8080/api/hello > /dev/null 2>&1; do
        sleep 1  # Use 1 second intervals for more reliable timing
        attempts=$((attempts + 1))
        if [ $attempts -ge $max_attempts ]; then
            log_error "Application failed to start within 60 seconds"
            cat $LOG_FILE
            exit 1
        fi
    done
    
    log_info "HTTP server is ready, measuring first response time..."
    
    # Measure time to first request using curl with timing (more accurate than wall-clock deltas)
    local first_response_time=$(curl -w "%{time_total}" -sf http://localhost:8080/api/hello -o /dev/null 2>/dev/null)
    
    # Fallback to ab if curl timing fails or returns invalid value
    if [ -z "$first_response_time" ] || [ "$first_response_time" = "0.000" ]; then
        log_warn "curl timing failed, using ab for first response measurement..."
        local ab_output=$(ab -n1 -c1 -q http://localhost:8080/api/hello 2>&1)
        first_response_time=$(echo "$ab_output" | grep "Time taken for tests" | awk '{print $5}' || echo "0")
    fi
    
    # Validate the timing result
    if [ -z "$first_response_time" ] || [ "$first_response_time" = "0" ]; then
        log_error "Failed to measure first response time"
        exit 1
    fi
    
    # Extract JVM startup time from logs if available
    local jvm_startup_time=""
    if [ "$MODE" = "jvm" ] && grep -q "started in" $LOG_FILE; then
        jvm_startup_time=$(grep "started in" $LOG_FILE | sed 's/.*started in \([0-9.]*\)s.*/\1/')
        log_debug "JVM reported startup time: ${jvm_startup_time}s"
    fi
    
    # Use first response time as the primary startup metric
    STARTUP_TIME="$first_response_time"
    log_info "Time to first request: ${STARTUP_TIME}s"
    
    # Measure final memory after startup is complete
    local final_mem=$(ps -p $APP_PID -o rss= 2>/dev/null | awk '{print $1/1024}')
    local mem_diff=$(echo "$final_mem - $initial_mem" | bc 2>/dev/null || echo "0")
    log_debug "Memory allocated during startup: ${mem_diff} MB"
    
    # Analyze startup logs
    log_info "Analyzing startup logs..."
    if [ -f "$LOG_FILE" ]; then
        echo "  ✓ Quarkus framework initialized"
        if grep -q "started in" $LOG_FILE; then
            local app_reported_time=$(grep "started in" $LOG_FILE | sed 's/.*started in \([0-9.]*\)s.*/\1/')
            echo "  ✓ Application reported startup time: ${app_reported_time}s"
        fi
        if grep -q "Listening on" $LOG_FILE; then
            echo "  ✓ HTTP server started"
        fi
        if grep -q "Profile" $LOG_FILE; then
            local profile=$(grep "Profile" $LOG_FILE | tail -1 | sed 's/.*Profile \([^ ]*\) activated.*/\1/')
            echo "  ✓ Profile activated: $profile"
        fi
    fi
    
    # Verify application is responsive
    log_info "Verifying application health..."
    if ! curl -sf http://localhost:8080/api/hello > /dev/null; then
        log_error "Application health check failed"
        exit 1
    fi
    echo "  ✓ Application is healthy"
    
    # Idle memory measurement
    log_info "Measuring idle memory baseline (${IDLE_DURATION}s)..."
    
    # Create the CSV file with header
    echo "timestamp,rss_mb,cpu_percent" > "$IDLE_CSV"
    
    # Measure memory in a loop
    for i in $(seq 1 $IDLE_DURATION); do
        # Get memory usage directly
        local mem=$(ps -p $APP_PID -o rss= 2>/dev/null | awk '{printf "%.2f", $1/1024}')
        local cpu=$(ps -p $APP_PID -o pcpu= 2>/dev/null | awk '{printf "%.2f", $1}')
        
        # Append to CSV
        echo "$(date +%s),$mem,$cpu" >> "$IDLE_CSV"
        
        # Show progress
        printf "\r  Progress: [%-50s] %d%%" $(printf '#%.0s' $(seq 1 $((i * 50 / IDLE_DURATION)))) $((i * 100 / IDLE_DURATION))
        sleep 1
    done
    echo ""
    
    # Calculate average idle memory
    IDLE_MEM=$(awk -F',' 'NR>1 {sum+=$2; count++} END {if(count>0) printf "%.2f", sum/count; else print "0"}' $IDLE_CSV)
    log_info "Average idle memory: ${IDLE_MEM} MB"
    
    # Enhanced warmup phase
    log_info "Warming up application (${WARMUP_DURATION}s) with high concurrency..."
    wrk -t2 -c$HELLO_CONCURRENCY -d${WARMUP_DURATION}s --latency http://localhost:8080/api/hello > /dev/null 2>&1 &
    WARMUP_PID=$!
    
    for i in $(seq 1 $WARMUP_DURATION); do
        if ! kill -0 $WARMUP_PID 2>/dev/null; then
            wrk -t2 -c$HELLO_CONCURRENCY -d$((WARMUP_DURATION - i))s --latency http://localhost:8080/api/hello > /dev/null 2>&1 &
            WARMUP_PID=$!
        fi
        printf "\r  Progress: [%-50s] %d%%" $(printf '#%.0s' $(seq 1 $((i * 50 / WARMUP_DURATION)))) $((i * 100 / WARMUP_DURATION))
        sleep 1
    done
    echo ""
    
    wait $WARMUP_PID 2>/dev/null || true
    echo "  ✓ Warmup complete"
    
    # SEQUENTIAL TEST EXECUTION - Run hello endpoint test first
    log_info "Starting sequential load tests..."
    
    # Run hello endpoint test with synchronized monitoring
    log_info "  → Running hello endpoint test (${LOAD_TEST_DURATION}s)..."
    
    run_test_with_monitoring "hello" $LOAD_TEST_DURATION $HELLO_RESULTS $HELLO_CONCURRENCY $HELLO_SYSTEM_CSV $HELLO_LOAD_CSV $APP_PID
    HELLO_EXIT_CODE=$?
    if [ $HELLO_EXIT_CODE -ne 0 ]; then
        log_error "Hello endpoint test failed with exit code $HELLO_EXIT_CODE"
        log_error "Aborting benchmark due to test failure"
        exit 1
    fi
    log_info "  ✓ Hello endpoint test completed"
    
    # Calculate hello throughput
    HELLO_RPS=$(calculate_throughput $HELLO_RESULTS $LOAD_TEST_DURATION)
    if [ "$HELLO_RPS" = "N/A" ]; then
        log_error "Failed to calculate hello endpoint throughput"
        log_error "Aborting benchmark due to invalid metrics"
        exit 1
    fi
    
    # Give the system a moment to recover
    log_info "Giving system time to recover before next test..."
    for i in {5..1}; do
        printf "\r  Resuming in %d seconds..." $i
        sleep 1
    done
    echo ""
    
    # Run compute endpoint test with synchronized monitoring
    log_info "  → Running heavy compute test (${LOAD_TEST_DURATION}s)..."
    
    run_test_with_monitoring "compute" $LOAD_TEST_DURATION $COMPUTE_RESULTS $COMPUTE_CONCURRENCY $COMPUTE_SYSTEM_CSV $COMPUTE_LOAD_CSV $APP_PID
    COMPUTE_EXIT_CODE=$?
    if [ $COMPUTE_EXIT_CODE -ne 0 ]; then
        log_error "Compute endpoint test failed with exit code $COMPUTE_EXIT_CODE"
        log_error "Aborting benchmark due to test failure"
        exit 1
    fi
    log_info "  ✓ Compute endpoint test completed"
    
    # Calculate compute throughput
    COMPUTE_RPS=$(calculate_throughput $COMPUTE_RESULTS $LOAD_TEST_DURATION)
    if [ "$COMPUTE_RPS" = "N/A" ]; then
        log_error "Failed to calculate compute endpoint throughput"
        log_error "Aborting benchmark due to invalid metrics"
        exit 1
    fi
    
    log_info "All tests completed"
    
    # Calculate metrics
    log_info "Calculating metrics..."
    
    # Hello test metrics
    HELLO_PEAK_MEM=$(awk -F',' 'NR>1 {if($2>max) max=$2} END {if(max>0) printf "%.2f", max; else print "N/A"}' $HELLO_LOAD_CSV)
    HELLO_AVG_MEM=$(awk -F',' 'NR>1 {sum+=$2; count++} END {if(count>0) printf "%.2f", sum/count; else print "N/A"}' $HELLO_LOAD_CSV)
    HELLO_AVG_CPU_RAW=$(awk -F',' 'NR>1 {sum+=$3; count++} END {if(count>0) printf "%.2f", sum/count; else print "N/A"}' $HELLO_LOAD_CSV)
    HELLO_AVG_CPU=$(echo "scale=2; $HELLO_AVG_CPU_RAW / ($SYSTEM_CPUS * 100) * 100" | bc 2>/dev/null || echo "N/A")
    
    # Compute test metrics
    COMPUTE_PEAK_MEM=$(awk -F',' 'NR>1 {if($2>max) max=$2} END {if(max>0) printf "%.2f", max; else print "N/A"}' $COMPUTE_LOAD_CSV)
    COMPUTE_AVG_MEM=$(awk -F',' 'NR>1 {sum+=$2; count++} END {if(count>0) printf "%.2f", sum/count; else print "N/A"}' $COMPUTE_LOAD_CSV)
    COMPUTE_AVG_CPU_RAW=$(awk -F',' 'NR>1 {sum+=$3; count++} END {if(count>0) printf "%.2f", sum/count; else print "N/A"}' $COMPUTE_LOAD_CSV)
    COMPUTE_AVG_CPU=$(echo "scale=2; $COMPUTE_AVG_CPU_RAW / ($SYSTEM_CPUS * 100) * 100" | bc 2>/dev/null || echo "N/A")
    
    # Overall peak values
    PEAK_MEM=$(echo "$HELLO_PEAK_MEM $COMPUTE_PEAK_MEM" | awk '{if ($1 > $2) print $1; else print $2}')
    AVG_MEM=$(echo "scale=2; ($HELLO_AVG_MEM + $COMPUTE_AVG_MEM) / 2" | bc)
    AVG_CPU=$(echo "scale=2; ($HELLO_AVG_CPU_RAW + $COMPUTE_AVG_CPU_RAW) / 2 / ($SYSTEM_CPUS * 100) * 100" | bc 2>/dev/null || echo "N/A")
    
    # Display enhanced results
    echo ""
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║                Improved $APP_NAME Benchmark Results                ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Configuration:"
    echo "  System CPUs:                 ${SYSTEM_CPUS}"
    if [ "$MODE" = "native" ]; then
        if [ "$OS" = "linux" ]; then
            echo "  CPU affinity:                taskset -c 0-$(($SYSTEM_CPUS-1))"
        elif [ "$OS" = "macos" ]; then
            echo "  CPU affinity:                taskpolicy -c utility"
        else
            echo "  CPU affinity:                None (unknown OS)"
        fi
    else
        echo "  CPU affinity:                JVM -XX:ActiveProcessorCount=$SYSTEM_CPUS"
    fi
    echo "  Hello endpoint concurrency:  ${HELLO_CONCURRENCY}"
    echo "  Compute endpoint concurrency: ${COMPUTE_CONCURRENCY}"
    echo "  Test duration (per endpoint): ${LOAD_TEST_DURATION}s"
    echo "  Compute iterations:          ${COMPUTE_ITERATIONS}"
    echo ""
    echo "Startup Performance:"
    echo "  Time to first request:       ${STARTUP_TIME}s"
    if [ "$MODE" = "jvm" ] && [ -n "$jvm_startup_time" ]; then
        echo "  JVM reported time:           ${jvm_startup_time}s"
    fi
    echo "  Startup memory:              ${final_mem} MB"
    echo ""
    echo "Memory Usage:"
    echo "  Idle memory:                 ${IDLE_MEM} MB"
    echo "  Peak memory (hello):         ${HELLO_PEAK_MEM} MB"
    echo "  Peak memory (compute):       ${COMPUTE_PEAK_MEM} MB"
    echo "  Overall peak memory:         ${PEAK_MEM} MB"
    echo "  Avg memory (under load):     ${AVG_MEM} MB"
    echo ""
    echo "CPU Usage (% of box):"
    echo "  Average CPU (hello):         ${HELLO_AVG_CPU}%"
    echo "  Average CPU (compute):       ${COMPUTE_AVG_CPU}%"
    echo "  Overall average CPU:         ${AVG_CPU}%"
    echo ""
    echo "Throughput:"
    echo "  /hello endpoint:             ${HELLO_RPS} req/s"
    echo "  /compute endpoint:           ${COMPUTE_RPS} req/s"
    echo ""
    
    # Show test completion status
    echo "Test Completion Status:"
    if grep -q "requests_per_second\|Requests/sec:" $HELLO_RESULTS; then
        echo "  Hello endpoint test:         ✓ Completed successfully"
    elif grep -q "Connection reset\|Connection refused\|timeout" $HELLO_RESULTS; then
        local hello_requests=$(grep -o '"requests":[0-9]*' $HELLO_RESULTS | cut -d':' -f2 | head -1 || echo "0")
        if [ -z "$hello_requests" ]; then
            hello_requests=$(grep -o "[0-9]* requests" $HELLO_RESULTS | awk '{print $1}' | head -1 || echo "0")
        fi
        echo "  Hello endpoint test:         ⚠️ Terminated early (connection error after $hello_requests requests)"
    else
        echo "  Hello endpoint test:         ✗ Failed"
    fi
    
    if grep -q "requests_per_second\|Requests/sec:" $COMPUTE_RESULTS; then
        echo "  Compute endpoint test:       ✓ Completed successfully"
    elif grep -q "Connection reset\|Connection refused\|timeout" $COMPUTE_RESULTS; then
        local compute_requests=$(grep -o '"requests":[0-9]*' $COMPUTE_RESULTS | cut -d':' -f2 | head -1 || echo "0")
        if [ -z "$compute_requests" ]; then
            compute_requests=$(grep -o "[0-9]* requests" $COMPUTE_RESULTS | awk '{print $1}' | head -1 || echo "0")
        fi
        echo "  Compute endpoint test:       ⚠️ Terminated early (connection error after $compute_requests requests)"
    else
        echo "  Compute endpoint test:       ✗ Failed"
    fi
    echo ""
    
    echo "Files generated:"
    echo "  - ${MODE}_hello_*.csv (hello test monitoring data)"
    echo "  - ${MODE}_compute_*.csv (compute test monitoring data)"
    echo "  - ${MODE}_*.txt (ab results)"
    echo "  - ${MODE}_*_system.csv (system monitoring)"
    echo ""
}

# Run main function
main

# Made with Bob



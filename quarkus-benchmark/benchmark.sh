#!/bin/bash

# Improved Quarkus Benchmark Script
# Usage: ./benchmark_improved.sh [jvm|native]

set -e

MODE=${1:-jvm}

# Enhanced configuration with endpoint-specific settings
LOAD_TEST_DURATION=180  # 3 minutes per test
HELLO_CONCURRENCY=50    # Concurrency for hello endpoint
COMPUTE_CONCURRENCY=20  # Lower concurrency for compute endpoint
IDLE_DURATION=60
WARMUP_DURATION=60      # Longer warmup for JIT

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
        echo "║        Improved Quarkus JVM Benchmark Suite       ║"
        echo "╚════════════════════════════════════════════════════╝"
        APP_NAME="JVM"
        APP_CMD="java -Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -jar target/quarkus-app/quarkus-run.jar"
        
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
        APP_CMD="$NATIVE_RUNNER"
        
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
    
    # Start ApacheBench in background
    ab -t $duration -c $concurrency -n 999999999 -q "http://localhost:8080/api/compute?iterations=${COMPUTE_ITERATIONS}" > $output 2>&1 &
    local AB_PID=$!
    
    # Show progress bar while test is running
    for i in $(seq 1 $duration); do
        if ! kill -0 $AB_PID 2>/dev/null; then
            # ApacheBench finished early
            printf "\r  Progress: [%-50s] %d%% (completed early)" "$(printf '#%.0s' $(seq 1 50))" 100
            break
        fi
        printf "\r  Progress: [%-50s] %d%%" "$(printf '#%.0s' $(seq 1 $((i * 50 / duration))))" $((i * 100 / duration))
        sleep 1
    done
    echo ""
    
    # Check if ApacheBench is still running after the duration
    if kill -0 $AB_PID 2>/dev/null; then
        log_warn "ApacheBench is still running after ${duration}s. Terminating..."
        kill $AB_PID 2>/dev/null
        sleep 1
        # Force kill if still running
        if kill -0 $AB_PID 2>/dev/null; then
            kill -9 $AB_PID 2>/dev/null
        fi
        echo "Benchmark terminated due to timeout" >> $output
        return 1
    fi
    
    local exit_code=$?
    
    if [ $exit_code -ne 0 ]; then
        log_warn "ApacheBench exited with code $exit_code for compute test"
    fi
    
    return $exit_code
}

# Enhanced hello endpoint test with progress indicator and timeout
test_hello() {
    local duration=$1
    local output=$2
    local concurrency=${3:-$HELLO_CONCURRENCY}
    
    log_info "Running hello endpoint test for ${duration}s with ${concurrency} concurrent connections..."
    
    # Start ApacheBench in background
    ab -t $duration -c $concurrency -n 999999999 -q http://localhost:8080/api/hello > $output 2>&1 &
    local AB_PID=$!
    
    # Show progress bar while test is running
    for i in $(seq 1 $duration); do
        if ! kill -0 $AB_PID 2>/dev/null; then
            # ApacheBench finished early
            printf "\r  Progress: [%-50s] %d%% (completed early)" "$(printf '#%.0s' $(seq 1 50))" 100
            break
        fi
        printf "\r  Progress: [%-50s] %d%%" "$(printf '#%.0s' $(seq 1 $((i * 50 / duration))))" $((i * 100 / duration))
        sleep 1
    done
    echo ""
    
    # Check if ApacheBench is still running after the duration
    if kill -0 $AB_PID 2>/dev/null; then
        log_warn "ApacheBench is still running after ${duration}s. Terminating..."
        kill $AB_PID 2>/dev/null
        sleep 1
        # Force kill if still running
        if kill -0 $AB_PID 2>/dev/null; then
            kill -9 $AB_PID 2>/dev/null
        fi
        echo "Benchmark terminated due to timeout" >> $output
        return 1
    fi
    
    local exit_code=$?
    
    if [ $exit_code -ne 0 ]; then
        log_warn "ApacheBench exited with code $exit_code for hello test"
    fi
    
    return $exit_code
}

# Direct system resource monitoring without duration limit
monitor_system() {
    local output=$1
    local stop_file=$2
    
    log_info "Starting system monitoring..."
    echo "timestamp,cpu_percent,memory_percent,load_avg" > $output
    
    # Monitor until stop file exists
    while true; do
        # Check for stop file
        if [[ -f "$stop_file" ]]; then
            log_info "System monitoring detected stop file, exiting..."
            break
        fi
        
        # Get system-wide CPU usage
        CPU=$(ps -A -o %cpu | awk '{s+=$1} END {print s}')
        
        # Get memory usage
        MEMORY=$(vm_stat | grep "Pages active" | awk '{print $3}' | sed 's/\.//')
        MEMORY=$(echo "scale=2; $MEMORY * 4096 / 1048576" | bc)
        
        # Get load average
        LOAD=$(uptime | awk '{print $(NF-2)}' | sed 's/,//')
        
        # Write to output file
        echo "$(date +%s),$CPU,$MEMORY,$LOAD" >> $output
        sleep 1
    done
    
    log_info "System monitoring stopped"
}

# Function to monitor a specific process
monitor_process() {
    local pid=$1
    local output=$2
    local stop_file=$3
    
    log_info "Starting process monitoring for PID ${pid}..."
    echo "timestamp,rss_mb,cpu_percent" > "$output"
    
    # Monitor until stop file exists
    while true; do
        # Check for stop file
        if [[ -f "$stop_file" ]]; then
            log_info "Process monitoring detected stop file, exiting..."
            break
        fi
        
        # Check if process still exists
        if ! kill -0 $pid 2>/dev/null; then
            log_warn "Process $pid no longer exists, stopping monitoring"
            break
        fi
        
        # Get memory usage directly
        local mem=$(ps -p $pid -o rss= 2>/dev/null | awk '{printf "%.2f", $1/1024}')
        local cpu=$(ps -p $pid -o pcpu= 2>/dev/null | awk '{printf "%.2f", $1}')
        
        # Append to CSV
        echo "$(date +%s),$mem,$cpu" >> "$output"
        sleep 1
    done
    
    log_info "Process monitoring stopped"
}

# This function is no longer needed as we handle stopping directly
# Keeping as a placeholder to avoid changing too much code
stop_monitoring() {
    # This function is now a no-op
    :
}

# Enhanced throughput calculation
calculate_throughput() {
    local result_file=$1
    local test_duration=$2
    
    # Try standard ab output format first
    local rps=$(grep "Requests per second" $result_file 2>/dev/null | awk '{printf "%.2f", $4}')
    
    # If not found, try to calculate from completed requests
    if [ -z "$rps" ]; then
        local requests=$(grep "Total of.*requests completed" $result_file | awk '{print $3}' || echo "0")
        
        # Try to get time from ab output, fall back to test duration if not found
        local time=$(grep "Time taken for tests" $result_file | awk '{print $5}' || echo "$test_duration")
        
        if [ "$requests" != "0" ] && [ -n "$time" ] && [ "$time" != "0" ]; then
            rps=$(echo "scale=2; $requests / $time" | bc 2>/dev/null)
        fi
    fi
    
    # If still empty, check for connection reset and use partial data
    if [ -z "$rps" ] && grep -q "Connection reset by peer" $result_file; then
        local requests=$(grep "Total of.*requests completed" $result_file | awk '{print $3}' || echo "0")
        if [ "$requests" != "0" ]; then
            # Estimate time based on when connection was reset
            # This is approximate but better than nothing
            rps=$(echo "scale=2; $requests / $test_duration" | bc 2>/dev/null)
            log_warn "Connection reset detected. Throughput is an estimate based on $requests completed requests."
        fi
    fi
    
    echo "${rps:-N/A}"
}

# Cleanup function to ensure all processes are terminated
cleanup() {
    log_info "Cleaning up processes and temporary files..."
    
    # Stop system monitoring processes
    if [ -n "$HELLO_SYSTEM_MONITOR_STOP_FILE" ]; then
        touch "$HELLO_SYSTEM_MONITOR_STOP_FILE" 2>/dev/null || true
    fi
    
    if [ -n "$COMPUTE_SYSTEM_MONITOR_STOP_FILE" ]; then
        touch "$COMPUTE_SYSTEM_MONITOR_STOP_FILE" 2>/dev/null || true
    fi
    
    # Stop process monitoring
    if [ -n "$HELLO_PROCESS_MONITOR_STOP_FILE" ]; then
        touch "$HELLO_PROCESS_MONITOR_STOP_FILE" 2>/dev/null || true
    fi
    
    if [ -n "$COMPUTE_PROCESS_MONITOR_STOP_FILE" ]; then
        touch "$COMPUTE_PROCESS_MONITOR_STOP_FILE" 2>/dev/null || true
    fi
    
    # Wait for any monitoring processes to finish with timeout
    if [ -n "$HELLO_SYSTEM_MONITOR_PID" ]; then
        wait_with_timeout $HELLO_SYSTEM_MONITOR_PID 3
    fi
    
    if [ -n "$COMPUTE_SYSTEM_MONITOR_PID" ]; then
        wait_with_timeout $COMPUTE_SYSTEM_MONITOR_PID 3
    fi
    
    if [ -n "$HELLO_PROCESS_MONITOR_PID" ]; then
        wait_with_timeout $HELLO_PROCESS_MONITOR_PID 3
    fi
    
    if [ -n "$COMPUTE_PROCESS_MONITOR_PID" ]; then
        wait_with_timeout $COMPUTE_PROCESS_MONITOR_PID 3
    fi
    
    # Kill application if it's still running
    if [ -n "$APP_PID" ] && kill -0 $APP_PID 2>/dev/null; then
        kill $APP_PID 2>/dev/null || true
        wait $APP_PID 2>/dev/null || true
    fi
    
    # Remove any temporary files
    rm -f /tmp/benchmark_*.sh 2>/dev/null || true
    rm -f /tmp/tmp.* 2>/dev/null || true
    rm -f /tmp/stop_monitor_* 2>/dev/null || true
    rm -f /tmp/stop_process_monitor_* 2>/dev/null || true
}

main() {
    # Set up trap to ensure cleanup on exit
    trap cleanup EXIT INT TERM
    
    configure_mode
    
    # Clean previous results
    log_info "Cleaning previous results..."
    rm -f ${MODE}_*.csv ${MODE}_*.txt ${MODE}_*.log
    
    # Enhanced startup measurement
    log_info "Starting application and measuring detailed startup time..."
    
    # Measure process launch time
    local start_launch=$(date +%s.%N)
    $APP_CMD > $LOG_FILE 2>&1 &
    APP_PID=$!
    local end_launch=$(date +%s.%N)
    local launch_time=$(echo "$end_launch - $start_launch" | bc)
    echo "  → PID: $APP_PID"
    log_debug "Process launch time: ${launch_time}s"
    
    # Measure JVM class loading (JVM only)
    if [ "$MODE" = "jvm" ]; then
        log_info "Measuring JVM class loading time..."
        local attempts=0
        local max_attempts=10000  # 10 seconds max
        
        while [ $attempts -lt $max_attempts ]; do
            if grep -q "Quarkus" $LOG_FILE 2>/dev/null; then
                break
            fi
            sleep 0.001
            attempts=$((attempts + 1))
        done
        
        if [ $attempts -lt $max_attempts ]; then
            if grep -q "started in" $LOG_FILE; then
                local jvm_startup_time=$(grep "started in" $LOG_FILE | sed 's/.*started in \([0-9.]*\)s.*/\1/')
                log_debug "JVM reported startup time: ${jvm_startup_time}s"
            fi
        fi
    fi
    
    # Measure HTTP readiness
    log_info "Measuring HTTP readiness time..."
    local attempts=0
    local max_attempts=60000  # 60 seconds max
    local start_http=$(date +%s.%N)
    
    until curl -sf http://localhost:8080/api/hello > /dev/null 2>&1; do
        sleep 0.001
        attempts=$((attempts + 1))
        if [ $attempts -ge $max_attempts ]; then
            log_error "Application failed to start within 60 seconds"
            cat $LOG_FILE
            exit 1
        fi
    done
    
    local end_http=$(date +%s.%N)
    local http_time=$(echo "$end_http - $start_http" | bc)
    log_debug "HTTP readiness time: ${http_time}s"
    
    # Calculate total startup time
    STARTUP_TIME=$(echo "$launch_time + $http_time" | bc)
    log_info "Total startup time: ${STARTUP_TIME}s"
    
    # Measure startup memory allocation
    log_info "Measuring startup memory allocation..."
    local initial_mem=$(ps -p $APP_PID -o rss= 2>/dev/null | awk '{print $1/1024}')
    sleep 2  # Let memory stabilize
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
    ab -t $WARMUP_DURATION -c $HELLO_CONCURRENCY -q http://localhost:8080/api/hello > /dev/null 2>&1 &
    WARMUP_PID=$!
    
    for i in $(seq 1 $WARMUP_DURATION); do
        if ! kill -0 $WARMUP_PID 2>/dev/null; then
            ab -t $((WARMUP_DURATION - i)) -c $HELLO_CONCURRENCY -q http://localhost:8080/api/hello > /dev/null 2>&1 &
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
    
    # Run hello endpoint test
    log_info "  → Running hello endpoint test (${LOAD_TEST_DURATION}s)..."
    
    # Create stop files first
    HELLO_SYSTEM_MONITOR_STOP_FILE="/tmp/stop_monitor_$$_$RANDOM"
    HELLO_PROCESS_MONITOR_STOP_FILE="/tmp/stop_process_monitor_$$_$RANDOM"
    
    # Start monitoring processes
    log_info "  → Starting monitoring processes..."
    monitor_system $HELLO_SYSTEM_CSV $HELLO_SYSTEM_MONITOR_STOP_FILE &
    HELLO_SYSTEM_MONITOR_PID=$!
    
    monitor_process $APP_PID $HELLO_LOAD_CSV $HELLO_PROCESS_MONITOR_STOP_FILE &
    HELLO_PROCESS_MONITOR_PID=$!
    
    # Give monitoring a moment to initialize
    sleep 2
    
    # Run hello test
    test_hello $LOAD_TEST_DURATION $HELLO_RESULTS
    HELLO_EXIT_CODE=$?
    log_info "  ✓ Hello endpoint test completed"
    
    # Stop monitoring processes
    log_info "  → Stopping monitoring processes..."
    # Create stop files to signal monitoring to stop
    touch "$HELLO_SYSTEM_MONITOR_STOP_FILE"
    touch "$HELLO_PROCESS_MONITOR_STOP_FILE"
    
    # Wait for monitoring processes to finish with timeout
    log_info "  → Waiting for system monitoring to finish..."
    wait_with_timeout $HELLO_SYSTEM_MONITOR_PID 5
    
    log_info "  → Waiting for process monitoring to finish..."
    wait_with_timeout $HELLO_PROCESS_MONITOR_PID 5
    
    # Clean up stop files
    rm -f "$HELLO_SYSTEM_MONITOR_STOP_FILE" 2>/dev/null || true
    rm -f "$HELLO_PROCESS_MONITOR_STOP_FILE" 2>/dev/null || true
    
    # Calculate hello throughput
    HELLO_RPS=$(calculate_throughput $HELLO_RESULTS $LOAD_TEST_DURATION)
    
    # Give the system a moment to recover
    log_info "Giving system time to recover before next test..."
    for i in {5..1}; do
        printf "\r  Resuming in %d seconds..." $i
        sleep 1
    done
    echo ""
    
    # Run compute endpoint test
    log_info "  → Running heavy compute test (${LOAD_TEST_DURATION}s)..."
    
    # Create stop files first
    COMPUTE_SYSTEM_MONITOR_STOP_FILE="/tmp/stop_monitor_$$_$RANDOM"
    COMPUTE_PROCESS_MONITOR_STOP_FILE="/tmp/stop_process_monitor_$$_$RANDOM"
    
    # Start monitoring processes
    log_info "  → Starting monitoring processes..."
    monitor_system $COMPUTE_SYSTEM_CSV $COMPUTE_SYSTEM_MONITOR_STOP_FILE &
    COMPUTE_SYSTEM_MONITOR_PID=$!
    
    monitor_process $APP_PID $COMPUTE_LOAD_CSV $COMPUTE_PROCESS_MONITOR_STOP_FILE &
    COMPUTE_PROCESS_MONITOR_PID=$!
    
    # Give monitoring a moment to initialize
    sleep 2
    
    # Run compute test
    test_compute_heavy $LOAD_TEST_DURATION $COMPUTE_RESULTS
    COMPUTE_EXIT_CODE=$?
    log_info "  ✓ Compute endpoint test completed"
    
    # Stop monitoring processes
    log_info "  → Stopping monitoring processes..."
    # Create stop files to signal monitoring to stop
    touch "$COMPUTE_SYSTEM_MONITOR_STOP_FILE"
    touch "$COMPUTE_PROCESS_MONITOR_STOP_FILE"
    
    # Wait for monitoring processes to finish with timeout
    log_info "  → Waiting for system monitoring to finish..."
    wait_with_timeout $COMPUTE_SYSTEM_MONITOR_PID 5
    
    log_info "  → Waiting for process monitoring to finish..."
    wait_with_timeout $COMPUTE_PROCESS_MONITOR_PID 5
    
    # Clean up stop files
    rm -f "$COMPUTE_SYSTEM_MONITOR_STOP_FILE" 2>/dev/null || true
    rm -f "$COMPUTE_PROCESS_MONITOR_STOP_FILE" 2>/dev/null || true
    
    # Calculate compute throughput
    COMPUTE_RPS=$(calculate_throughput $COMPUTE_RESULTS $LOAD_TEST_DURATION)
    
    log_info "All tests completed"
    
    # Calculate metrics
    log_info "Calculating metrics..."
    
    # Hello test metrics
    HELLO_PEAK_MEM=$(awk -F',' 'NR>1 {if($2>max) max=$2} END {if(max>0) printf "%.2f", max; else print "N/A"}' $HELLO_LOAD_CSV)
    HELLO_AVG_MEM=$(awk -F',' 'NR>1 {sum+=$2; count++} END {if(count>0) printf "%.2f", sum/count; else print "N/A"}' $HELLO_LOAD_CSV)
    HELLO_AVG_CPU=$(awk -F',' 'NR>1 {sum+=$3; count++} END {if(count>0) printf "%.2f", sum/count; else print "N/A"}' $HELLO_LOAD_CSV)
    
    # Compute test metrics
    COMPUTE_PEAK_MEM=$(awk -F',' 'NR>1 {if($2>max) max=$2} END {if(max>0) printf "%.2f", max; else print "N/A"}' $COMPUTE_LOAD_CSV)
    COMPUTE_AVG_MEM=$(awk -F',' 'NR>1 {sum+=$2; count++} END {if(count>0) printf "%.2f", sum/count; else print "N/A"}' $COMPUTE_LOAD_CSV)
    COMPUTE_AVG_CPU=$(awk -F',' 'NR>1 {sum+=$3; count++} END {if(count>0) printf "%.2f", sum/count; else print "N/A"}' $COMPUTE_LOAD_CSV)
    
    # Overall peak values
    PEAK_MEM=$(echo "$HELLO_PEAK_MEM $COMPUTE_PEAK_MEM" | awk '{if ($1 > $2) print $1; else print $2}')
    AVG_MEM=$(echo "scale=2; ($HELLO_AVG_MEM + $COMPUTE_AVG_MEM) / 2" | bc)
    AVG_CPU=$(echo "scale=2; ($HELLO_AVG_CPU + $COMPUTE_AVG_CPU) / 2" | bc)
    
    # Display enhanced results
    echo ""
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║                Improved $APP_NAME Benchmark Results                ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Configuration:"
    echo "  Hello endpoint concurrency:  ${HELLO_CONCURRENCY}"
    echo "  Compute endpoint concurrency: ${COMPUTE_CONCURRENCY}"
    echo "  Test duration (per endpoint): ${LOAD_TEST_DURATION}s"
    echo "  Compute iterations:          ${COMPUTE_ITERATIONS}"
    echo ""
    echo "Startup Performance:"
    echo "  Process launch:              ${launch_time}s"
    echo "  HTTP readiness:              ${http_time}s"
    echo "  Total startup:               ${STARTUP_TIME}s"
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
    echo "CPU Usage:"
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
    if grep -q "Requests per second" $HELLO_RESULTS; then
        echo "  Hello endpoint test:         ✓ Completed successfully"
    elif grep -q "Connection reset by peer" $HELLO_RESULTS; then
        local hello_requests=$(grep "Total of.*requests completed" $HELLO_RESULTS | awk '{print $3}' || echo "0")
        echo "  Hello endpoint test:         ⚠️ Terminated early (connection reset after $hello_requests requests)"
    else
        echo "  Hello endpoint test:         ✗ Failed"
    fi
    
    if grep -q "Requests per second" $COMPUTE_RESULTS; then
        echo "  Compute endpoint test:       ✓ Completed successfully"
    elif grep -q "Connection reset by peer" $COMPUTE_RESULTS; then
        local compute_requests=$(grep "Total of.*requests completed" $COMPUTE_RESULTS | awk '{print $3}' || echo "0")
        echo "  Compute endpoint test:       ⚠️ Terminated early (connection reset after $compute_requests requests)"
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

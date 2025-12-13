#!/bin/bash
# Generate jextract bindings using Python TensorFlow libraries
# This ensures consistency between Python TensorFlow and Java FFI bindings

set -e

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if venv exists and activate it to get TensorFlow path
# Try current directory first, then parent directory
VENV_PATH=""
if [ -d "venv" ]; then
    VENV_PATH="venv"
elif [ -d "../venv" ]; then
    VENV_PATH="../venv"
else
    echo "Error: venv directory not found. Please create a virtual environment and install TensorFlow first."
    exit 1
fi

source "$VENV_PATH/bin/activate"

# Get TensorFlow installation directory
TF_DIR=$(python3 -c "import tensorflow as tf; import os; print(os.path.dirname(tf.__file__))" 2>/dev/null)

if [ -z "$TF_DIR" ]; then
    echo "Error: Could not find TensorFlow installation in venv"
    exit 1
fi

# Set paths
INCLUDE_PATH="$TF_DIR/include"
LIB_PATH="$TF_DIR"
OUTPUT_DIR="src/main/java"
PACKAGE="tensorflow.ffi"
C_API_HEADER="$INCLUDE_PATH/tensorflow/c/c_api.h"

# Verify header exists
if [ ! -f "$C_API_HEADER" ]; then
    echo "Error: C API header not found at $C_API_HEADER"
    exit 1
fi

# Verify library exists
if [ ! -f "$LIB_PATH/libtensorflow_cc.2.dylib" ]; then
    echo "Error: libtensorflow_cc.2.dylib not found at $LIB_PATH"
    exit 1
fi

echo "Using TensorFlow from: $TF_DIR"
echo "Include path: $INCLUDE_PATH"
echo "Library path: $LIB_PATH"
echo "Output directory: $OUTPUT_DIR"
echo "Package: $PACKAGE"
echo ""

# Set DYLD_LIBRARY_PATH to use Python TensorFlow libraries during jextract
export DYLD_LIBRARY_PATH="$LIB_PATH:$DYLD_LIBRARY_PATH"

# Remove Homebrew libtensorflow from path to avoid conflicts
export DYLD_LIBRARY_PATH=$(echo "$DYLD_LIBRARY_PATH" | tr ':' '\n' | grep -v "/opt/homebrew.*libtensorflow" | tr '\n' ':' | sed 's/:$//')

# Run jextract
echo "Running jextract..."
# Note: jextract uses -l for library name, and DYLD_LIBRARY_PATH for library path
# The library name should match what's in the venv (libtensorflow_cc.2.dylib)
jextract \
  --output "$OUTPUT_DIR" \
  -t "$PACKAGE" \
  -l tensorflow_cc.2 \
  -I "$INCLUDE_PATH" \
  "$C_API_HEADER"

echo ""
echo "Successfully generated jextract bindings!"
echo "Bindings are in: $OUTPUT_DIR/$PACKAGE"


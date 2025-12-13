import tensorflow as tf
import numpy as np
import os

# 1. Prepare the data
mnist = tf.keras.datasets.mnist
(x_train, y_train), (x_test, y_test) = mnist.load_data()
x_train, x_test = x_train / 255.0, x_test / 255.0

# 2. Define a simple model (Input: 28x28, Output: 10 classes)
# We name the input layer "input_1" to make it easy to find later
model = tf.keras.models.Sequential([
  tf.keras.layers.Flatten(input_shape=(28, 28), name='input_1'),
  tf.keras.layers.Dense(256, activation='relu'),  # Increased from 128
  tf.keras.layers.Dropout(0.3),  # Increased from 0.2
  tf.keras.layers.Dense(10, activation='softmax', name='output_1')
])

model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

# 3. Train with 5 epochs for better accuracy (was 1 epoch)
print("\nTraining model with 5 epochs...")
model.fit(x_train, y_train, epochs=5, validation_data=(x_test, y_test), verbose=1)

# Evaluate the model
test_loss, test_acc = model.evaluate(x_test, y_test, verbose=0)
print(f"\nTest accuracy: {test_acc:.4f}")

# Save a real test sample to payload.json for testing
import json
test_sample_idx = 0
test_sample = x_test[test_sample_idx].flatten().tolist()
test_label = int(y_test[test_sample_idx])
payload_path = os.path.join(os.getcwd(), "payload.json")
with open(payload_path, 'w') as f:
    json.dump({'pixels': test_sample}, f)
print(f"\nTest sample saved to {payload_path}")
print(f"Expected digit: {test_label}")
print("Use this payload to test the inference endpoint")

# 4. Save as "SavedModel" format with explicit signatures
save_path = os.path.join(os.getcwd(), "mnist_saved_model")

# Create a callable that wraps the model to ensure proper variable handling
@tf.function(input_signature=[tf.TensorSpec(shape=[None, 28, 28], dtype=tf.float32, name='inputs')])
def serving_fn(inputs):
    return {'output_0': model(inputs)}

# Save with the serving function to ensure variables are properly saved
tf.saved_model.save(
    model,
    save_path,
    signatures={'serving_default': serving_fn}
)

print(f"\nSUCCESS! Model saved to: {save_path}")
print("Use 'saved_model_cli' to verify the exact node names.")

# Also save as frozen graph for C API compatibility
# This is a workaround for TensorFlow C API variable loading issues
try:
    from tensorflow.python.framework import convert_to_constants
    
    # Get the concrete function
    concrete_func = serving_fn.get_concrete_function()
    
    # Convert variables to constants (freeze the graph)
    frozen_func = convert_to_constants.convert_variables_to_constants_v2(concrete_func)
    
    # Save frozen graph
    frozen_graph_path = os.path.join(os.getcwd(), "mnist_frozen_graph.pb")
    graph_def = frozen_func.graph.as_graph_def()
    
    # Write the frozen graph
    with tf.io.gfile.GFile(frozen_graph_path, 'wb') as f:
        f.write(graph_def.SerializeToString())
    
    print(f"Frozen graph saved to: {frozen_graph_path}")
    print("Note: You may need to use TF_GraphImportGraphDef instead of TF_LoadSessionFromSavedModel")
    print("      to load the frozen graph with the C API.")
except Exception as e:
    print(f"Warning: Could not create frozen graph: {e}")
    print("You may need to use SavedModel format, which has known C API compatibility issues.")
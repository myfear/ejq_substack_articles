import torch
from transformers import CLIPModel, CLIPProcessor
import onnx

# Download and load the model
model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32")
processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")
model.eval()

# Export the vision encoder (for image embeddings)
dummy_input = torch.randn(1, 3, 224, 224)

torch.onnx.export(
    model.vision_model,
    dummy_input,
    "clip_vision_fp32.onnx",
    input_names=['pixel_values'],
    output_names=['last_hidden_state', 'pooler_output'],
    dynamic_axes={
        'pixel_values': {0: 'batch_size'},
        'last_hidden_state': {0: 'batch_size'},
        'pooler_output': {0: 'batch_size'}
    },
    opset_version=14,
    do_constant_folding=True
)

print("✓ Vision model exported to clip_vision_fp32.onnx")

# Verify the model
onnx_model = onnx.load("clip_vision_fp32.onnx")
onnx.checker.check_model(onnx_model)
print("✓ Model verified successfully")
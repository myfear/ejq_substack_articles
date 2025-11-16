package com.example.embeddings;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OnnxClipEmbeddingModel implements EmbeddingModel {

    private final OrtEnvironment env;
    private final OrtSession session;
    private final ImagePreprocessor preprocess;

    public OnnxClipEmbeddingModel(
            @ConfigProperty(name = "clip.model.path") String modelPath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(modelPath, new OrtSession.SessionOptions());
        this.preprocess = new ImagePreprocessor(224, 224);
    }

    @Override
    public Response<Embedding> embed(TextSegment textSegment) {
        throw new UnsupportedOperationException("Use embed(byte[]) for images");
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        List<Embedding> embeddings = textSegments.stream()
                .map(segment -> embed(segment).content())
                .collect(Collectors.toList());
        return Response.from(embeddings);
    }

    public Response<Embedding> embed(byte[] imageBytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));

            float[] pixels = preprocess.process(img);

            long[] shape = { 1, 3, 224, 224 }; // NCHW
            OnnxTensor input = OnnxTensor.createTensor(env, FloatBuffer.wrap(pixels), shape);

            OrtSession.Result result = session.run(Map.of("pixel_values", input));

            Object outputValue = result.get(0).getValue();
            float[] vector;

            // Handle different output shapes
            if (outputValue instanceof float[][][]) {
                // 3D array: [batch, 1, features] or similar
                float[][][] output3d = (float[][][]) outputValue;
                vector = output3d[0][0];
            } else if (outputValue instanceof float[][]) {
                // 2D array: [batch, features]
                float[][] output2d = (float[][]) outputValue;
                vector = output2d[0];
            } else if (outputValue instanceof float[]) {
                // 1D array: [features]
                vector = (float[]) outputValue;
            } else {
                throw new RuntimeException("Unexpected output type: " + outputValue.getClass());
            }

            normalize(vector);

            return Response.from(Embedding.from(vector));

        } catch (Exception e) {
            throw new RuntimeException("Embedding failed", e);
        }
    }

    private void normalize(float[] v) {
        float sum = 0;
        for (float f : v)
            sum += f * f;
        float norm = (float) Math.sqrt(sum);
        for (int i = 0; i < v.length; i++)
            v[i] /= norm;
    }
}
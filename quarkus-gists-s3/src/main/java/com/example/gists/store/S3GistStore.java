package com.example.gists.store;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.gists.model.Gist;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@ApplicationScoped
@Startup
public class S3GistStore {

    @Inject
    ObjectMapper mapper;
    @Inject
    S3Client s3;

    @ConfigProperty(name = "quarkus.s3.devservices.buckets")
    String bucket;

    public void save(Gist gist) {
        try {
            if (!bucketExists(bucket)) {
                createBucket(bucket);
            }
            String key = "gists/" + gist.id + ".json";
            byte[] data = mapper.writeValueAsBytes(gist);
            s3.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/json")
                    .build(), RequestBody.fromBytes(data));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save gist", e);
        }
    }

    public Gist find(String id) {
        String key = "gists/" + id + ".json";
        try (var obj = s3.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build())) {
            return mapper.readValue(obj, Gist.class);
        } catch (NoSuchKeyException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read gist", e);
        }
    }

    public List<String> listIds() {
        var res = s3.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix("gists/")
                .build());
        return res.contents().stream()
                .map(S3Object::key)
                .map(k -> k.substring(k.lastIndexOf('/') + 1, k.indexOf(".json")))
                .toList();
    }

    private boolean bucketExists(String name) {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(name).build());
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    private void createBucket(String name) {
        s3.createBucket(CreateBucketRequest.builder().bucket(name).build());
    }
}
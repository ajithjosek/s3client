package com.leapfront;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.util.List;

public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private S3Client s3;

    public S3Service(String clientId, String clientSecret, String region) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("Client ID and Client Secret cannot be blank.");
        }
        Region awsRegion = Region.US_EAST_1; // Default region
        if (region != null && !region.trim().isEmpty()) {
            try {
                awsRegion = Region.of(region.trim());
            } catch (Exception e) {
                logger.warn("Invalid region specified: " + region + ", using default: us-east-1. Error: " + e.getMessage());
            }
        }
        this.s3 = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(clientId, clientSecret)))
                .forcePathStyle(true) // This can help with some endpoint issues
                .httpClientBuilder(ApacheHttpClient.builder())
                .build();
    }

    public S3Service(String clientId, String clientSecret) {
        this(clientId, clientSecret, null); // Use default region
    }

    public void createBucket(String bucketName) {
        try {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            logger.info("Bucket " + bucketName + " created.");
        } catch (S3Exception e) {
            logger.error(e.awsErrorDetails().errorMessage());
        }
    }

    public void listBuckets() {
        try {
            ListBucketsResponse listBucketsResponse = s3.listBuckets();
            List<Bucket> buckets = listBucketsResponse.buckets();
            logger.info("Buckets:");
            for (Bucket b : buckets) {
                logger.info("* " + b.name());
            }
        } catch (S3Exception e) {
            logger.error(e.awsErrorDetails().errorMessage());
        }
    }

    public List<String> listObjects(String bucketName, String prefix) {
        List<String> result = new java.util.ArrayList<>();
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().bucket(bucketName);
            if (prefix != null && !prefix.isBlank()) {
                requestBuilder = requestBuilder.prefix(prefix);
            }
            ListObjectsV2Request listObjectsV2Request = requestBuilder.build();

            ListObjectsV2Response listObjectsV2Response = s3.listObjectsV2(listObjectsV2Request);
            if (listObjectsV2Response.contents() != null) {
                for (S3Object s3Object : listObjectsV2Response.contents()) {
                    result.add(s3Object.key());
                }
            }
        } catch (S3Exception e) {
            logger.error("Error listing objects in bucket: " + e.awsErrorDetails().errorMessage());
            throw e; // Re-throw to let the UI handle the error appropriately
        } catch (Exception e) {
            logger.error("Unexpected error listing objects: " + e.getMessage());
            throw e;
        }
        return result;
    }

    public void putObject(String bucketName, String key, String content) {
        try {
            s3.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build(),
                RequestBody.fromString(content));
            logger.info("Object " + key + " uploaded to bucket " + bucketName + ".");
        } catch (S3Exception e) {
            logger.error("Error uploading object: " + e.awsErrorDetails().errorMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error uploading object: " + e.getMessage());
            throw e;
        }
    }

    public void putObject(String bucketName, String key, java.io.File file) {
        try {
            s3.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build(),
                RequestBody.fromFile(file));
            logger.info("Object " + key + " uploaded to bucket " + bucketName + ".");
        } catch (S3Exception e) {
            logger.error("Error uploading file: " + e.awsErrorDetails().errorMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error uploading file: " + e.getMessage());
            throw e;
        }
    }

    public String getObject(String bucketName, String key) {
        try (ResponseInputStream<GetObjectResponse> response = s3.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build())) {
            logger.info("Object " + key + " retrieved from bucket " + bucketName + ".");
            return new String(response.readAllBytes());
        } catch (S3Exception e) {
            logger.error("Error getting object: " + e.awsErrorDetails().errorMessage());
            throw e;
        } catch (IOException e) {
            logger.error("IO error getting object: " + e.getMessage());
            throw new RuntimeException("IO error while reading object: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error getting object: " + e.getMessage());
            throw e;
        }
    }

    public void deleteObject(String bucketName, String key) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            logger.info("Object " + key + " deleted from bucket " + bucketName + ".");
        } catch (S3Exception e) {
            logger.error("Error deleting object: " + e.awsErrorDetails().errorMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting object: " + e.getMessage());
            throw e;
        }
    }

    public void deleteBucket(String bucketName) {
        try {
            s3.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
            logger.info("Bucket " + bucketName + " deleted.");
        } catch (S3Exception e) {
            logger.error(e.awsErrorDetails().errorMessage());
        }
    }

    public void close() {
        s3.close();
    }
}
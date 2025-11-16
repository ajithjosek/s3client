package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockS3Client {

    private Map<String, Map<String, String>> buckets = new HashMap<>();

    public void putObject(String bucketName, String key, String content) {
        buckets.computeIfAbsent(bucketName, k -> new HashMap<>()).put(key, content);
    }

    public List<String> listObjects(String bucketName, String prefix) {
        List<String> result = new ArrayList<>();
        Map<String, String> objects = buckets.get(bucketName);
        if (objects != null) {
            for (String key : objects.keySet()) {
                if (key.startsWith(prefix)) {
                    result.add(key);
                }
            }
        }
        return result;
    }

    public String getObject(String bucketName, String key) {
        Map<String, String> objects = buckets.get(bucketName);
        if (objects != null) {
            return objects.get(key);
        }
        return null;
    }

    public void deleteObject(String bucketName, String key) {
        Map<String, String> objects = buckets.get(bucketName);
        if (objects != null) {
            objects.remove(key);
        }
    }
}
package com.example;

public class Profile {
    private String profileName;
    private String bucketName;
    private String clientId;
    private String clientSecret;
    private String region;

    public Profile(String profileName, String bucketName, String clientId, String clientSecret) {
        this.profileName = profileName;
        this.bucketName = bucketName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.region = "us-east-1"; // default region
    }

    public Profile(String profileName, String bucketName, String clientId, String clientSecret, String region) {
        this.profileName = profileName;
        this.bucketName = bucketName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.region = region != null ? region : "us-east-1";
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return profileName;
    }
}

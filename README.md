# S3 Client for Desktop Application

This project provides a GUI-based S3 client for desktop applications using the AWS SDK for Java. The application allows users to manage S3 buckets and objects through an intuitive interface with profile management capabilities.

## Features

- **Profile Management**: Create, save, and manage multiple AWS profiles with different credentials and regions
- **S3 Operations**: List, upload, download, and delete S3 objects
- **Multi-Region Support**: Specify the AWS region for each profile
- **GUI Interface**: User-friendly Swing-based interface for easy S3 management

## Prerequisites

1. **Java Development Kit (JDK) 11 or later**
2. **Maven 3.6.0 or later**
3. **AWS Account with appropriate S3 permissions**

## Setup

1. **Clone or navigate to the project directory:**
   ```bash
   cd s3client
   ```

2. **Build the project:**
   ```bash
   mvn clean package
   ```

## Running the Application

1. **Run the built JAR file:**
   ```bash
   java -jar target/s3client-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

## Using the Application

1. **Create a new profile**: Click the "New Profile" button and enter a profile name
2. **Enter credentials**: Fill in your AWS Access Key ID, Secret Access Key, and S3 bucket name
3. **Set the region**: Enter the AWS region where your S3 bucket is located (defaults to us-east-1)
4. **Save the profile**: Click "Save Profile" to store your configuration
5. **S3 Operations**:
   - Enter a path in the path field (or leave empty to list the root)
   - Click "List" to view objects in the bucket
   - Select an object and click "Get Object" to view its content
   - Click "Upload" to upload a file to the bucket
   - Select an object and click "Delete" to remove it

## Configuration

- AWS credentials and settings are stored in `profiles.json` in the project root
- Each profile contains:
  - Profile name
  - S3 bucket name
  - AWS access key ID
  - AWS secret access key
  - AWS region

## Troubleshooting

- Ensure your AWS credentials have appropriate S3 permissions
- Verify that the specified region matches your S3 bucket's region
- Check that the bucket name is correct and accessible

## Dependencies

- AWS SDK for Java v2.28.0
- SLF4J with Logback for logging
- Google Gson for JSON serialization
- Maven for build management
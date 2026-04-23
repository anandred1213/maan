# Image Storage in Docker - Implementation Guide

## Overview
This document outlines the steps to implement image storage for user profile images using MinIO (S3-compatible storage) in Docker containers.

## Architecture

```
User → Revision Service → MinIO Container → Docker Volume
                ↓
         Store/Retrieve Images
```

## Why MinIO?
- S3-compatible API (easy migration to AWS S3 later)
- Runs in Docker container
- Persistent storage with volumes
- Production-ready
- Easy to configure

## Implementation Steps

### Step 1: Update docker-compose.yml
Add MinIO service with persistent volume:
```yaml
services:
  minio:
    image: minio/minio
    container_name: minio
    ports:
      - "9000:9000"      # API port
      - "9001:9001"      # Console UI port
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin123
    command: server /data --console-address ":9001"
    volumes:
      - minio-data:/data
    networks:
      - microservices-network

volumes:
  minio-data:
    driver: local
```

### Step 2: Add MinIO Dependencies to revision/pom.xml
```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

### Step 3: Configure MinIO in application.properties
Add to revision service:
```properties
# MinIO Configuration
minio.url=http://minio:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin123
minio.bucket-name=user-images
minio.image-size-limit=5242880
```

### Step 4: Create MinIO Configuration Class
File: `revision/src/main/java/com/practise/revision/config/MinioConfig.java`
- Initialize MinIO client
- Create bucket if not exists
- Bean configuration

### Step 5: Update UserProfileImageService
File: `revision/src/main/java/com/practise/revision/service/serviceImpl/UserProfileImageService.java`
- Add upload method (MultipartFile → MinIO)
- Add download method (MinIO → byte[])
- Add delete method
- Handle file validation (size, type)

### Step 6: Create Image Controller
File: `revision/src/main/java/com/practise/revision/controller/ImageController.java`
Endpoints:
- POST /api/images/upload/{userId} - Upload image
- GET /api/images/{userId} - Get image
- DELETE /api/images/{userId} - Delete image

### Step 7: Update User Entity
Add field to store image reference:
```java
private String profileImageUrl;
```

### Step 8: Update Dockerfile (if needed)
Ensure proper file upload handling in container

### Step 9: Testing
- Start containers: `docker-compose up -d`
- Access MinIO Console: http://localhost:9001
- Test upload endpoint
- Verify image in MinIO bucket
- Test download endpoint

## File Structure After Implementation

```
revision/
├── src/main/java/com/practise/revision/
│   ├── config/
│   │   └── MinioConfig.java (NEW)
│   ├── controller/
│   │   └── ImageController.java (NEW)
│   ├── service/
│   │   └── ImageStorageService.java (NEW - Interface)
│   └── service/serviceImpl/
│       └── UserProfileImageService.java (UPDATED)
├── src/main/resources/
│   └── application.properties (UPDATED)
└── pom.xml (UPDATED)
```

## Security Considerations

1. **File Validation**
   - Check file type (only images: jpg, png, jpeg)
   - Limit file size (max 5MB)
   - Sanitize filename

2. **Access Control**
   - Only authenticated users can upload
   - Users can only access their own images
   - Use JWT token validation

3. **Production Changes**
   - Change MinIO credentials
   - Use environment variables
   - Enable HTTPS
   - Consider AWS S3 migration

## Alternative: Simple Volume Mount (Simpler but Less Scalable)

If you want simpler approach without MinIO:

```yaml
revision:
  volumes:
    - ./images:/app/images
```

Store images directly in filesystem, but:
- Not scalable across multiple containers
- No built-in replication
- Manual backup needed

## Recommended: Use MinIO for Production-Ready Solution

## Next Steps After Documentation Review
1. Review and approve approach
2. Implement code changes
3. Test locally
4. Update AWS documentation for S3 migration path

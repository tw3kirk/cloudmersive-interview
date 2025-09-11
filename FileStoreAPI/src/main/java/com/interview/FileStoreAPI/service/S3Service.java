package com.interview.FileStoreAPI.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class S3Service {
    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    public void uploadFile(byte[] file, String filename, String filepath) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filepath+filename)
                .build(),
            RequestBody.fromBytes(file));
    }

    public byte[] downloadFile(String fileName, String filePath) {
        ResponseBytes<GetObjectResponse> objectAsBytes =
                s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath+fileName)
                .build());
        return objectAsBytes.asByteArray();
    }

    public void deleteFile(String fileName, String filePath) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filePath+fileName)
                        .build());
    }
}

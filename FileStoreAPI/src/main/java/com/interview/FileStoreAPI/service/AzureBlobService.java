package com.interview.FileStoreAPI.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class AzureBlobService {
    @Autowired
    private BlobServiceClient blobServiceClient;

    @Value("${azure.blob-container.name}")
    private String blobContainerName;

    public void uploadFile(byte[] file, String filename, String filepath) throws RuntimeException {
        BlobContainerClient containerClient = blobServiceClient
                .getBlobContainerClient(blobContainerName);

        BlobClient blobClient = containerClient.getBlobClient(filepath + filename);
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(file)) {
            blobClient.upload(dataStream, file.length, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] downloadFile(String fileName, String filePath) {
        BlobContainerClient containerClient = blobServiceClient
                .getBlobContainerClient(blobContainerName);

        BlobClient blobClient = containerClient.getBlobClient(filePath + fileName);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.downloadStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }


    public void deleteFile(String fileName, String filePath) {
        BlobContainerClient containerClient = blobServiceClient
                .getBlobContainerClient(blobContainerName);

        BlobClient blobClient = containerClient.getBlobClient(filePath + fileName);
        blobClient.delete();
    }
}

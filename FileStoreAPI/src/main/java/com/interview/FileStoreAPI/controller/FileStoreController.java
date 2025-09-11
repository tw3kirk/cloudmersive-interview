package com.interview.FileStoreAPI.controller;

import com.interview.FileStoreAPI.service.AzureBlobService;
import com.interview.FileStoreAPI.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("api/files")
public class FileStoreController {
    @Autowired
    private S3Service s3Service;

    @Autowired
    private AzureBlobService azureBlobService;

    @PostMapping("/create-file")
    public ResponseEntity<String> uploadFile(
            @RequestParam("FileContents") String fileContents,
            @RequestParam("FileName") String fileName,
            @RequestParam("FilePath") String filePath,
            @RequestParam("ConnectionName") String connectionName)
                throws IOException {
        byte[] fileBytes = Base64.getDecoder().decode(fileContents);
        if (connectionName.equals("AWS_S3")) {
            s3Service.uploadFile(fileBytes, fileName, filePath);
            return ResponseEntity.ok("File uploaded to S3");
        } else if (connectionName.equals("AZURE_BLOB")) {
            azureBlobService.uploadFile(fileBytes, fileName, filePath);
            return ResponseEntity.ok("File uploaded to Azure Blob");
        } else {
            return ResponseEntity.badRequest().body("Missing Valid Connection Name");
        }
    }

    @GetMapping("/download-file")
    public ResponseEntity<?> downloadFile(
            @RequestParam("FileName") String fileName,
            @RequestParam("FilePath") String filePath,
            @RequestParam("ConnectionName") String connectionName) {
        if (connectionName.equals("AWS_S3")) {
            byte[] data = s3Service.downloadFile(fileName, filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(data);
        } else if (connectionName.equals("AZURE_BLOB")) {
            byte[] data = azureBlobService.downloadFile(fileName, filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(data);
        } else {
            return ResponseEntity.badRequest().body("Missing Valid Connection Name");
        }
    }

    @DeleteMapping("/delete-file")
    public ResponseEntity<String> deleteFile(
            @RequestParam("FileName") String fileName,
            @RequestParam("FilePath") String filePath,
            @RequestParam("ConnectionName") String connectionName) {
        if (connectionName.equals("AWS_S3")) {
            s3Service.deleteFile(fileName, filePath);
            return ResponseEntity.ok("File deleted successfully");
        } else if (connectionName.equals("AZURE_BLOB")) {
            azureBlobService.deleteFile(fileName, filePath);
            return ResponseEntity.ok("File deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Missing Valid Connection Name");
        }
    }
}

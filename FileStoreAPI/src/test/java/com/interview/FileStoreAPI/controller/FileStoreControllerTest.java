package com.interview.FileStoreAPI.controller;

import com.interview.FileStoreAPI.service.AzureBlobService;
import com.interview.FileStoreAPI.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileStoreController.class)
class FileStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private AzureBlobService azureBlobService;

    @Test
    void uploadFile_toS3_returnsOkAndInvokesService() throws Exception {
        byte[] payload = "Hello world".getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(post("/api/files/create-file")
                        .param("FileContents", "SGVsbG8gd29ybGQ=")
                        .param("FileName", "hello.txt")
                        .param("FilePath", "docs/")
                        .param("ConnectionName", "AWS_S3"))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded to S3"));

        verify(s3Service).uploadFile(payload, "hello.txt", "docs/");
        verifyNoMoreInteractions(s3Service, azureBlobService);
    }

    @Test
    void uploadFile_toAzure_returnsOkAndInvokesService() throws Exception {
        byte[] payload = "Hello world".getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(post("/api/files/create-file")
                        .param("FileContents", "SGVsbG8gd29ybGQ=")
                        .param("FileName", "hello.txt")
                        .param("FilePath", "docs/")
                        .param("ConnectionName", "AZURE_BLOB"))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded to Azure Blob"));

        verify(azureBlobService).uploadFile(payload, "hello.txt", "docs/");
        verifyNoMoreInteractions(s3Service, azureBlobService);
    }

    @Test
    void downloadFile_fromS3_returnsBytesAndDisposition() throws Exception {
        byte[] data = "s3-bytes".getBytes(StandardCharsets.UTF_8);
        when(s3Service.downloadFile("report.pdf", "docs/")).thenReturn(data);

        mockMvc.perform(get("/api/files/download-file")
                        .param("FileName", "report.pdf")
                        .param("FilePath", "docs/")
                        .param("ConnectionName", "AWS_S3"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\""))
                .andExpect(content().bytes(data));

        verify(s3Service).downloadFile("report.pdf", "docs/");
        verifyNoMoreInteractions(s3Service, azureBlobService);
    }

    @Test
    void downloadFile_fromAzure_returnsBytesAndDisposition() throws Exception {
        byte[] data = "azure-bytes".getBytes(StandardCharsets.UTF_8);
        when(azureBlobService.downloadFile("report.pdf", "docs/")).thenReturn(data);

        mockMvc.perform(get("/api/files/download-file")
                        .param("FileName", "report.pdf")
                        .param("FilePath", "docs/")
                        .param("ConnectionName", "AZURE_BLOB"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\""))
                .andExpect(content().bytes(data));

        verify(azureBlobService).downloadFile("report.pdf", "docs/");
        verifyNoMoreInteractions(s3Service, azureBlobService);
    }
}

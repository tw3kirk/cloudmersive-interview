package com.interview.FileStoreAPI.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureBlobServiceTest {

    @Mock
    private BlobServiceClient blobServiceClient;

    @Mock
    private BlobContainerClient blobContainerClient;

    @Mock
    private BlobClient blobClient;

    @InjectMocks
    private AzureBlobService azureBlobService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(azureBlobService, "blobContainerName", "my-container");
        when(blobServiceClient.getBlobContainerClient("my-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient("path/file.txt")).thenReturn(blobClient);
    }

    @Test
    void uploadFile_success_uploadsWithOverwriteTrue() {
        byte[] bytes = "content".getBytes(StandardCharsets.UTF_8);
        azureBlobService.uploadFile(bytes, "file.txt", "path/");
        verify(blobClient).upload(any(InputStream.class), eq((long) bytes.length), eq(true));
    }

    @Test
    void uploadFile_whenSdkThrows_wrapsInRuntimeException() {
        byte[] bytes = "boom".getBytes(StandardCharsets.UTF_8);
        doThrow(new RuntimeException("sdk error")).when(blobClient).upload(any(InputStream.class), eq(bytes.length), eq(true));
        assertThrows(RuntimeException.class, () -> azureBlobService.uploadFile(bytes, "file.txt", "path/"));
    }

    @Test
    void downloadFile_success_readsBytesViaDownloadStream() throws Exception {
        byte[] expected = "downloaded".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write(expected);
            return null;
        }).when(blobClient).downloadStream(any(ByteArrayOutputStream.class));

        byte[] result = azureBlobService.downloadFile("file.txt", "path/");
        assertArrayEquals(expected, result);
    }

    @Test
    void deleteFile_success_callsDeleteOnce() {
        azureBlobService.deleteFile("file.txt", "path/");
        verify(blobClient, times(1)).delete();
    }
}

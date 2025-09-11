package com.interview.FileStoreAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", "my-bucket");
    }

    @Test
    void uploadFile_success_putsObjectWithExpectedBucketAndKey() throws Exception {
        byte[] bytes = "content".getBytes(StandardCharsets.UTF_8);

        s3Service.uploadFile(bytes, "file.txt", "path/");

        ArgumentCaptor<PutObjectRequest> reqCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(reqCaptor.capture(), any(RequestBody.class));

        PutObjectRequest req = reqCaptor.getValue();
        assertEquals("my-bucket", req.bucket());
        assertEquals("path/file.txt", req.key());
    }

    @Test
    void uploadFile_whenClientThrows_exceptionPropagates() {
        byte[] bytes = "boom".getBytes(StandardCharsets.UTF_8);
        doThrow(new RuntimeException("s3 failure")).when(s3Client)
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertThrows(RuntimeException.class, () -> s3Service.uploadFile(bytes, "file.txt", "path/"));
    }

    @Test
    void downloadFile_success_returnsBytesAndUsesCorrectKey() {
        byte[] expected = "downloaded".getBytes(StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        ResponseBytes<GetObjectResponse> responseBytes = (ResponseBytes<GetObjectResponse>) mock(ResponseBytes.class);
        when(responseBytes.asByteArray()).thenReturn(expected);
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

        byte[] result = s3Service.downloadFile("file.txt", "path/");
        assertArrayEquals(expected, result);

        ArgumentCaptor<GetObjectRequest> reqCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObjectAsBytes(reqCaptor.capture());
        GetObjectRequest req = reqCaptor.getValue();
        assertEquals("my-bucket", req.bucket());
        assertEquals("path/file.txt", req.key());
    }

    @Test
    void deleteFile_success_callsDeleteWithExpectedBucketAndKey() {
        s3Service.deleteFile("file.txt", "path/");

        ArgumentCaptor<DeleteObjectRequest> reqCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client, times(1)).deleteObject(reqCaptor.capture());
        DeleteObjectRequest req = reqCaptor.getValue();
        assertEquals("my-bucket", req.bucket());
        assertEquals("path/file.txt", req.key());
    }
}

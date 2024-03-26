package ru.fcpsr.domainsport.dto;

import io.minio.ObjectWriteResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MinioResponse {
    private String originalFileName;
    private String uid;
    private String type;
    private float fileSize;
    private ObjectWriteResponse response;
}

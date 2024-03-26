package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public class MinioFile {
    @Id
    private long id;
    private long mid;

    private String uid;
    private String name;
    private String type;
    private String eTag;
    private String bucket;
    private String path;
    private String minioUrl;
    private float fileSize;

    public MinioFile(MinioFile minioFile){
        setId(minioFile.getId());
        setMid(minioFile.getMid());
        setUid(minioFile.getUid());
        setName(minioFile.getName());
        setType(minioFile.getType());
        setETag(minioFile.getETag());
        setBucket(minioFile.getBucket());
        setPath(minioFile.getPath());
        setMinioUrl(minioFile.getMinioUrl());
        setFileSize(minioFile.getFileSize());
    }
}

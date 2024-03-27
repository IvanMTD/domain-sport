package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.MinioResponse;
import ru.fcpsr.domainsport.models.MinioFile;
import ru.fcpsr.domainsport.repositories.MinioFileRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileService {
    private final MinioFileRepository fileRepository;

    public Mono<MinioFile> save(MinioResponse response, long mid){
        if(response.getResponse() != null) {
            MinioFile minioFile = new MinioFile();
            minioFile.setUid(response.getUid());
            minioFile.setName(response.getOriginalFileName());
            minioFile.setType(response.getType());
            minioFile.setETag(response.getResponse().etag());
            minioFile.setBucket(response.getResponse().bucket());
            minioFile.setPath(response.getResponse().object());
            minioFile.setMinioUrl(response.getResponse().region() != null ? response.getResponse().region() : "no url");
            minioFile.setFileSize(response.getFileSize());
            minioFile.setMid(mid);
            return fileRepository.save(minioFile);
        }else{
            return Mono.just(new MinioFile());
        }
    }

    public Mono<MinioFile> findById(int id){
        return fileRepository.findById(id);
    }

    public Flux<MinioFile> findAll(){
        return fileRepository.findAll();
    }

    public Mono<MinioFile> deleteById(int id){
        return fileRepository.findById(id).flatMap(minioFile -> fileRepository.deleteById(id).then(Mono.just(minioFile)));
    }

    public Mono<MinioFile> deletePostData(int dataId) {
        return deleteById(dataId);
    }
}

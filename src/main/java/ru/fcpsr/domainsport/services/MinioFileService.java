package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    // CREATE
    @CacheEvict(value = "files", allEntries = true)
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
    // READ-ALL
    @Cacheable("files")
    public Flux<MinioFile> findAll(){
        return fileRepository.findAll();
    }
    // READ-ONE
    @Cacheable("files")
    public Mono<MinioFile> findById(long id){
        return fileRepository.findById(id);
    }
    // UPDATE
    // DELETE
    @CacheEvict(value = "files", allEntries = true)
    public Mono<MinioFile> deleteById(long id){
        return fileRepository.findById(id)
                .flatMap(minioFile -> fileRepository.delete(minioFile).then(Mono.just(minioFile)));
    }
    // COUNT
}

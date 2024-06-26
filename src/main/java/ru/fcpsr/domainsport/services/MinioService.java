package ru.fcpsr.domainsport.services;

import io.minio.*;
import io.minio.errors.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.fcpsr.domainsport.dto.MinioResponse;
import ru.fcpsr.domainsport.models.MinioFile;
import ru.fcpsr.domainsport.utils.CustomFileUtil;
import ru.fcpsr.domainsport.utils.InputStreamCollector;
import ru.fcpsr.domainsport.utils.StringGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Slf4j
@Service
@PropertySource("classpath:application.properties")
public class MinioService {
    private final String bucket;
    private final MinioClient minioClient;
    private final String directory = "./img-decode/";

    @SneakyThrows
    public MinioService(MinioClient minioClient, @Value("${minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        if (!this.minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            this.minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    public Mono<MinioResponse> uploadStream(FilePart file) {
        return file.content()
                .subscribeOn(Schedulers.boundedElastic())
                .reduce(new InputStreamCollector(), (collector, dataBuffer) -> collector.collectInputStream(dataBuffer.asInputStream()))
                .map(inputStreamCollector -> {
                    long startMillis = System.currentTimeMillis();
                    String extension = CustomFileUtil.getExtension(file.filename()).orElse("not");
                    String randomWord = StringGenerator.getGeneratedString(20);
                    String uid = randomWord + "." + extension;
                    try {
                        String type = Objects.requireNonNull(file.headers().getContentType()).toString();
                        PutObjectArgs args = PutObjectArgs.builder()
                                .bucket(bucket)
                                .object("/" + type + "/" + uid)
                                .contentType(type)
                                .stream(inputStreamCollector.getStream(), inputStreamCollector.getStream().available(), -1)
                                .build();
                        ObjectWriteResponse response = minioClient.putObject(args);
                        inputStreamCollector.closeStream();
                        MinioResponse minioResponse = new MinioResponse();
                        minioResponse.setResponse(response);
                        minioResponse.setOriginalFileName(file.filename());
                        minioResponse.setUid(uid);
                        minioResponse.setType(type);
                        minioResponse.setFileSize(CustomFileUtil.getMegaBytes(inputStreamCollector.getStream().available()));
                        log.info("upload stream execution time {} ms", System.currentTimeMillis() - startMillis);
                        return minioResponse;
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                })
                .log();
    }

    public Mono<MinioResponse> uploadImage(FilePart image) {
        String tempName = StringGenerator.getGeneratedString(20);
        return image.transferTo(Path.of(directory + tempName))
                .then(Mono.just(new File(directory + tempName)))
                .flatMap(file -> {
                    if (file.canRead()) {
                        String extension = "webp";
                        String randomWord = StringGenerator.getGeneratedString(30);
                        String uid = randomWord + "." + extension;
                        log.info("try save " + uid);
                        try {
                            String temp = directory + tempName + "." + extension;
                            InputStream fileInputStream = new FileInputStream(file);
                            BufferedImage bufferedImage = ImageIO.read(fileInputStream);
                            log.info(bufferedImage.toString());
                            boolean completed = ImageIO.write(bufferedImage, "webp", new File(temp));
                            if (completed) {
                                log.info("try created webp image");
                                File webpImage = new File(temp);
                                InputStream webpImageInputStream = new FileInputStream(webpImage);
                                String type = "image/webp";
                                PutObjectArgs args = PutObjectArgs.builder()
                                        .bucket(bucket)
                                        .object("/" + type + "/" + uid)
                                        .contentType(type)
                                        .stream(webpImageInputStream, -1, 10485760)
                                        .build();
                                ObjectWriteResponse response = minioClient.putObject(args);
                                MinioResponse minioResponse = new MinioResponse();
                                minioResponse.setResponse(response);
                                minioResponse.setOriginalFileName(image.filename());
                                minioResponse.setUid(uid);
                                minioResponse.setType(type);
                                minioResponse.setFileSize(CustomFileUtil.getMegaBytes(fileInputStream.available()));
                                fileInputStream.close();
                                webpImageInputStream.close();
                                cleanup(tempName);
                                return Mono.just(minioResponse);
                            } else {
                                fileInputStream.close();
                                cleanup(tempName);
                                return uploadStream(image);
                            }
                        } catch (ServerException | InsufficientDataException | ErrorResponseException |
                                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
                                 XmlParserException | InternalException | IOException e) {
                            return Mono.error(new RuntimeException(e));
                        }
                    } else {
                        log.info("file in " + directory + "temp not exist");
                    }
                    return Mono.empty();
                });
    }

    public Mono<InputStreamResource> download(MinioFile fileInfo) {
        return Mono.fromCallable(() -> {
            InputStream response = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(fileInfo.getPath()).build());
            return new InputStreamResource(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @SneakyThrows
    public Mono<Void> delete(MinioFile fileInfo) {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(fileInfo.getBucket())
                        .object(fileInfo.getPath())
                        .build()
        );
        return Mono.empty();
    }

    @SneakyThrows
    private void cleanup(String tempName) {
        File f1 = new File(directory + tempName);
        File f2 = new File(directory + tempName + ".webp");
        if (f1.exists()) {
            Files.delete(f1.toPath());
        }
        if (f2.exists()) {
            Files.delete(f2.toPath());
        }
    }
}

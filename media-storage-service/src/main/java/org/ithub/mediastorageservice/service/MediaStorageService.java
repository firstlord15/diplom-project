package org.ithub.mediastorageservice.service;

import io.minio.*;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStorageService {
    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * Инициализация бакета при запуске
     */
    public void init() {
        try {
            // Проверка существования бакета
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            // Если бакета нет, создаем его
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Bucket {} created successfully", bucketName);
            } else {
                log.info("Bucket {} already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Could not initialize MinIO bucket", e);
        }
    }

    /**
     * Загрузка файла в MinIO
     */
    public String uploadFile(MultipartFile file) throws IOException {
        try {
            // Формируем уникальный ключ для хранения
            String filename = generateFileName(file.getOriginalFilename());

            // Загружаем файл в MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            return filename;
        } catch (Exception e) {
            log.error("Error uploading file to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Could not upload file to MinIO", e);
        }
    }

    /**
     * Получение файла из MinIO
     */
    public byte[] getFile(String filename) {
        try {
            // Получаем объект из MinIO
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());

            // Читаем содержимое в массив байтов
            return response.readAllBytes();
        } catch (Exception e) {
            log.error("Error getting file from MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Could not get file from MinIO", e);
        }
    }

    /**
     * Получение временной ссылки на файл
     */
    public String getPresignedUrl(String filename, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .method(Method.GET)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Could not generate presigned URL", e);
        }
    }

    /**
     * Удаление файла из MinIO
     */
    @Transactional
    public void deleteFile(String filename) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Could not delete file from MinIO", e);
        }
    }


    /**
     * Добавьте этот метод в MinioService
     */
    public void uploadBytes(byte[] data, String filename, String contentType) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(inputStream, data.length, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("Error uploading bytes to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Could not upload bytes to MinIO", e);
        }
    }

    /**
     * Генерация уникального имени файла с учетом текущей даты
     */
    private String generateFileName(String originalFilename) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String datePath = LocalDateTime.now().format(formatter);

        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Формат: 2023/04/15/uuid_filename.jpg
        return String.format("%s/%s_%s",
                datePath,
                UUID.randomUUID(),
                originalFilename);
    }
}

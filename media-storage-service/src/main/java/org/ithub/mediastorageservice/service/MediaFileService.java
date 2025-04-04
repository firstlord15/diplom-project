package org.ithub.mediastorageservice.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.mediastorageservice.dto.MediaFileDTO;
import org.ithub.mediastorageservice.enums.MediaStatus;
import org.ithub.mediastorageservice.enums.MediaType;
import org.ithub.mediastorageservice.model.MediaFile;
import org.ithub.mediastorageservice.repository.MediaFileRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.ithub.mediastorageservice.util.MediaFileMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileService {
    private final MediaFileRepository mediaFileRepository;
    private final MediaStorageService mediaStorageService;
    private final MediaTagService mediaTagService;
    private final MediaVariantService mediaVariantService;
    private final MediaAlbumService mediaAlbumService;
    private final MediaFileMapper fileMapper;

    /**
     * Сохранение медиа-файла
     */
    public MediaFile saveMediaFile(MediaFile mediaFile) {
        return mediaFileRepository.save(mediaFile);
    }

    /**
     * Получение медиа-файла
     */
    public MediaFile getMediaFile(Long id) {
        return mediaFileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MediaFile not found with id: " + id));
    }

    /**
     * Получение детальной информации о медиа-файле
     */
    public MediaFileDTO getMediaDetails(Long id) {
        return fileMapper.convertToDTO(getMediaFile(id));
    }

    /**
     * Создание медиа-файла
     */
    public MediaFile createMediaFile(MultipartFile file, String description, Set<String> tags) throws IOException {
        String contentType = file.getContentType();
        MediaType mediaType = determineMediaType(contentType);

        String storageKey = mediaStorageService.uploadFile(file);

        MediaFile mediaFile = new MediaFile();
        mediaFile.setOriginalFilename(file.getOriginalFilename());
        mediaFile.setSize(file.getSize());
        mediaFile.setStorageKey(storageKey);
        mediaFile.setMediaType(mediaType);
        mediaFile.setMimeType(contentType);
        mediaFile.setStatus(MediaStatus.PROCESSING);
        mediaFile.setUploadedBy("currentUser"); // тут потом надо настроить, чтобы получать данные из контекста безопасности

        // Для изображений извлекаем размеры
        if (mediaType == MediaType.IMAGE) {
            try (InputStream is = file.getInputStream()) {
                BufferedImage bufferedImage = ImageIO.read(is);
                if (bufferedImage != null) {
                    mediaFile.setWidth(bufferedImage.getWidth());
                    mediaFile.setHeight(bufferedImage.getHeight());
                }
            }
        }

        // Устанавливаем метаданные
        if (description != null && !description.isEmpty()) {
            mediaFile.setMetadata(description);
        }

        // Сохраняем файл в БД
        MediaFile savedMediaFile = mediaFileRepository.save(mediaFile);

        // Добавляем теги
        if (tags != null && !tags.isEmpty()) {
            mediaTagService.addTagsToFile(savedMediaFile, tags);
        }

        // Надо сделать метод асинхронным (запускаем создание вариантов)
        if (mediaType == MediaType.IMAGE) {
            mediaVariantService.processImageVariants(savedMediaFile);
        }

        return savedMediaFile;
    }

    /**
     * Удаление медиа-файла
     */
    @Transactional
    public void deleteMediaFile(Long id) {
        MediaFile mediaFile = getMediaFile(id);

        // Удаляем оригинальный файл из хранилища
        mediaStorageService.deleteFile(mediaFile.getStorageKey());

        // Удаляем все варианты
        mediaVariantService.deleteVariantsByMediaFileId(id);

        // Удаляем все теги
        mediaTagService.deleteAllTagsByMediaFileId(id);

        // Удаляем файл из всех альбомов (без удаления самих альбомов)
        mediaAlbumService.removeFileFromAllAlbums(mediaFile);

        // Удаляем запись из БД
        mediaFileRepository.delete(mediaFile);
    }

    /**
     * Получение содержимого файла
     */
    public Resource getMediaContent(Long mediaId) {
        MediaFile mediaFile = getMediaFile(mediaId);

        // Получаем файл из хранилища
        byte[] content = mediaStorageService.getFile(mediaFile.getStorageKey());

        // Создаем ресурс для возврата
        return new ByteArrayResource(content);
    }

    /**
     * Поиск медиа-файлов по тегам и типу
     */
    public Page<MediaFileDTO> findByTagsAndType(Set<String> tags, MediaType mediaType, Pageable pageable) {
        Page<MediaFile> mediaFiles;

        if ((tags == null || tags.isEmpty()) && mediaType == null) {
            mediaFiles = mediaFileRepository.findAll(pageable);
        } else if (tags == null || tags.isEmpty()) {
            mediaFiles = mediaFileRepository.findByMediaType(mediaType, pageable);
        } else if (mediaType == null) {
            mediaFiles = mediaFileRepository.findByTagsIn(tags, pageable);
        } else {
            mediaFiles = mediaFileRepository.findByTagsInAndMediaType(tags, mediaType, pageable);
        }

        return mediaFiles.map(fileMapper::convertToDTO);
    }

    /**
     * Поиск медиа-файлов по тегам
     */
    public Page<MediaFileDTO> findByTags(Set<String> tags, Pageable pageable) {
        Page<MediaFile> mediaFiles;

        if (tags == null || tags.isEmpty()) {
            mediaFiles = mediaFileRepository.findAll(pageable);
        } else {
            mediaFiles = mediaFileRepository.findByTagsIn(tags, pageable);
        }

        return mediaFiles.map(fileMapper::convertToDTO);
    }

    public MediaFileDTO convertToDTO(MediaFile mediaFile) {
        return fileMapper.convertToDTO(mediaFile);
    }

    /**
     * Определяем тип медиа-файла по MIME-типу
     */
    private MediaType determineMediaType(String contentType) {
        if (contentType == null) return MediaType.OTHER;

        if (contentType.startsWith("image/")) {
            return MediaType.IMAGE;
        } else if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        } else if (contentType.startsWith("audio/")) {
            return MediaType.AUDIO;
        } else if (contentType.startsWith("application/pdf")
                || contentType.startsWith("application/msword")
                || contentType.startsWith("application/vnd.openxmlformats-officedocument")
                || contentType.startsWith("text/")) {
            return MediaType.DOCUMENT;
        } else {
            return MediaType.OTHER;
        }
    }
}

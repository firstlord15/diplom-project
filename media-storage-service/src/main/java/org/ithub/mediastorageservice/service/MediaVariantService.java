package org.ithub.mediastorageservice.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.mediastorageservice.enums.MediaStatus;
import org.ithub.mediastorageservice.model.MediaFile;
import org.ithub.mediastorageservice.model.MediaVariant;
import org.ithub.mediastorageservice.repository.MediaFileRepository;
import org.ithub.mediastorageservice.repository.MediaVariantRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaVariantService {
    private final MediaVariantRepository mediaVariantRepository;
    private final MediaStorageService mediaStorageService;
    private final ImageProcessingService imageProcessingService;
    private final MediaFileRepository mediaFileRepository;


    /**
     * Обработка вариантов изображения
     */
    public void processImageVariants(MediaFile mediaFile) {
        try {
            // Получаем оригинальное изображение из хранилища
            byte[] imageData = mediaStorageService.getFile(mediaFile.getStorageKey());

            // Создаем миниатюру
            MediaVariant thumbnailVariant = imageProcessingService.createThumbnail(mediaFile, imageData, 150, 150);

            // Создаем вариант среднего размера
            MediaVariant mediumVariant = imageProcessingService.createResizedVariant(mediaFile, imageData, 600, 600, "medium");

            // Создаем оптимизированные варианты для социальных сетей
            MediaVariant instagramVariant = imageProcessingService.optimizeForSocialNetwork(mediaFile, imageData, "instagram");
            MediaVariant vkVariant = imageProcessingService.optimizeForSocialNetwork(mediaFile, imageData, "telegram");

            // Обновляем статус файла на READY
            mediaFile.setStatus(MediaStatus.READY);
            mediaFileRepository.save(mediaFile);

        } catch (Exception e) {
            log.error("Error processing image variants for mediaId {}: {}", mediaFile.getId(), e.getMessage(), e);
            mediaFile.setStatus(MediaStatus.ERROR);
            mediaFileRepository.save(mediaFile);
        }
    }

    /**
     * Получение вариантов по mediaFileId и имени variant
     */
    public MediaVariant getVariantByNameAndFileId(Long mediaFileId, String variantName) {
        return mediaVariantRepository.findByMediaFileIdAndVariantName(mediaFileId, variantName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Variant " + variantName + " not found for media with id: " + mediaFileId));
    }

    /**
     * Получение всех вариантов по mediaFileId
     */
    public List<MediaVariant> getAllVariantsByFileId(Long mediaFileId) {
        return mediaVariantRepository.findByMediaFileId(mediaFileId);
    }

    /**
     *  Получение содержимого варианта
     */
    public Resource getVariantContent(Long mediaId, String variantName) {
        MediaVariant variant = mediaVariantRepository.findByMediaFileIdAndVariantName(mediaId, variantName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Variant " + variantName + " not found for media with id: " + mediaId));

        // Получаем файл из хранилища
        byte[] content = mediaStorageService.getFile(variant.getStorageKey());

        // Создаем ресурс для возврата
        return new ByteArrayResource(content);
    }

    /**
     * Удаление всех вариантов файла
     */
    @Transactional
    public void deleteVariantsByMediaFileId(Long mediaFileId) {
        List<MediaVariant> variants = mediaVariantRepository.findByMediaFileId(mediaFileId);
        for (MediaVariant variant : variants) {
            mediaStorageService.deleteFile(variant.getStorageKey());
        }

        mediaVariantRepository.deleteByMediaFileId(mediaFileId);
    }
}

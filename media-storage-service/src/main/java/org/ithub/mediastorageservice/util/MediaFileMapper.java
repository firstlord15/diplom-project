package org.ithub.mediastorageservice.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.mediastorageservice.dto.MediaFileDTO;
import org.ithub.mediastorageservice.model.MediaFile;
import org.ithub.mediastorageservice.model.MediaVariant;
import org.ithub.mediastorageservice.service.MediaStorageService;
import org.ithub.mediastorageservice.service.MediaTagService;
import org.ithub.mediastorageservice.service.MediaVariantService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaFileMapper {
    private final MediaTagService mediaTagService;
    private final MediaVariantService mediaVariantService;
    private final MediaStorageService mediaStorageService;

    /**
     * Конвертация MediaFile в MediaFileDTO
     */
    public MediaFileDTO convertToDTO(MediaFile mediaFile) {
        // Получаем теги для файла
        Set<String> tags = mediaTagService.getTagsNameForFile(mediaFile.getId());

        // Создаем временный URL для оригинального файла
        String url = mediaStorageService.getPresignedUrl(mediaFile.getStorageKey(), 60);

        // Получаем все варианты файла с URL
        List<MediaVariant> variants = mediaVariantService.getAllVariantsByFileId(mediaFile.getId());
        Map<String, String> variantUrls = new HashMap<>();

        for (MediaVariant variant : variants) {
            String variantUrl = mediaStorageService.getPresignedUrl(variant.getStorageKey(), 60);
            variantUrls.put(variant.getVariantName(), variantUrl);
        }

        return MediaFileDTO.builder()
                .id(mediaFile.getId())
                .originalFilename(mediaFile.getOriginalFilename())
                .size(mediaFile.getSize())
                .mediaType(mediaFile.getMediaType().toString())
                .mimeType(mediaFile.getMimeType())
                .width(mediaFile.getWidth())
                .height(mediaFile.getHeight())
                .description(mediaFile.getMetadata())
                .createdAt(mediaFile.getCreatedAt())
                .updatedAt(mediaFile.getUpdatedAt())
                .uploadedBy(mediaFile.getUploadedBy())
                .tags(tags)
                .url(url)
                .variantUrls(variantUrls)
                .status(mediaFile.getStatus().toString())
                .build();
    }
}

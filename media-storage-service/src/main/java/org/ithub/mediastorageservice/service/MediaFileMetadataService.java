package org.ithub.mediastorageservice.service;

import lombok.RequiredArgsConstructor;
import org.ithub.mediastorageservice.dto.MediaFileDTO;
import org.ithub.mediastorageservice.dto.MediaMetadataDTO;
import org.ithub.mediastorageservice.model.MediaFile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaFileMetadataService {
    private final MediaFileService mediaFileService;
    private final MediaTagService mediaTagService;

    /**
     * Обновление метаданных медиа-файла
     */
    public MediaFileDTO updateMetadata(Long mediaId, MediaMetadataDTO metadata) {
        MediaFile mediaFile = mediaFileService.getMediaFile(mediaId);

        // Обновляем метаданные в соответствии с предоставленными данными
        if (metadata.getDescription() != null) {
            mediaFile.setMetadata(metadata.getDescription());
        }

        // Если есть новые теги, добавляем их
        mediaTagService.addTagsToFile(mediaFile, metadata.getTagsToAdd());

        // Если нужно удалить теги, удаляем их
        mediaTagService.removeTagsFromFile(mediaFile, metadata.getTagsToRemove());

        return mediaFileService.convertToDTO(mediaFileService.saveMediaFile(mediaFile));
    }
}

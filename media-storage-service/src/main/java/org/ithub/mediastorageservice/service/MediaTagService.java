package org.ithub.mediastorageservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.ithub.mediastorageservice.enums.MediaTagType;
import org.ithub.mediastorageservice.model.MediaFile;
import org.ithub.mediastorageservice.model.MediaTag;
import org.ithub.mediastorageservice.repository.MediaTagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaTagService {
    private final MediaTagRepository mediaTagRepository;

    public void addTagsToFile(MediaFile mediaFile, Set<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            List<MediaTag> existingTags  = mediaTagRepository.findByMediaFileId(mediaFile.getId());
            Set<String> existingTagNames  = existingTags.stream()
                    .map(MediaTag::getName)
                    .collect(Collectors.toSet());

            // Добавляем только новые теги
            for(String tag : tags) {
                if (!existingTagNames.contains(tag)) {
                    MediaTag mediaTag = new MediaTag();
                    mediaTag.setMediaFile(mediaFile);
                    mediaTag.setName(tag);
                    mediaTag.setType(MediaTagType.MANUAL);
                    mediaTagRepository.save(mediaTag);
                }
            }
        }
    }

    public void removeTagsFromFile(MediaFile mediaFile, Set<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            List<MediaTag> tagsToRemove = mediaTagRepository.findByMediaFileIdAndNameIn(
                    mediaFile.getId(), tags);
            mediaTagRepository.deleteAll(tagsToRemove);
        }
    }

    public List<MediaTag> getTagsForFile(Long mediaFileId) {
        return mediaTagRepository.findByMediaFileId(mediaFileId);
    }

    public Set<String> getTagsNameForFile(Long mediaFileId) {
        List<MediaTag> tags = getTagsForFile(mediaFileId);
        return tags.stream()
                .map(MediaTag::getName)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void deleteAllTagsByMediaFileId(Long mediaFileId) {
        mediaTagRepository.deleteByMediaFileId(mediaFileId);
    }
}

package org.ithub.postservice.client;

import lombok.extern.slf4j.Slf4j;
import org.ithub.postservice.dto.MediaFileDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MediaStorageClientFallback implements MediaStorageClient {

    @Override
    public MediaFileDto getMediaFileDetails(Long id) {
        log.error("Fallback: Media storage service is unavailable. Cannot get details for mediaId: {}", id);
        return MediaFileDto.builder()
                .id(id)
                .originalFilename("unavailable.file")
                .mediaType("unknown")
                .mimeType("application/octet-stream")
                .build();
    }

    @Override
    public Resource getMediaContent(Long id) {
        log.error("Fallback: Media storage service is unavailable. Cannot get content for mediaId: {}", id);
        return new ByteArrayResource(new byte[0]);
    }

    @Override
    public Resource getMediaVariant(Long id, String variantName) {
        log.error("Fallback: Media storage service is unavailable. Cannot get variant {} for mediaId: {}", variantName, id);
        return new ByteArrayResource(new byte[0]);
    }
}
package org.ithub.postservice.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.ithub.postservice.client.MediaStorageClient;
import org.ithub.postservice.dto.MediaFileDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

@Slf4j
public class MediaStorageClientFallback implements MediaStorageClient {
    private final Throwable cause;

    public MediaStorageClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public MediaFileDto getMediaFileDetails(Long id) {
        log.error("Fallback: Media storage service is unavailable. Cannot get details for mediaId: {}, cause: {}", id, cause.getMessage());
        return MediaFileDto.builder()
                .id(id)
                .originalFilename("unavailable.file")
                .mediaType("unknown")
                .mimeType("application/octet-stream")
                .build();
    }

    @Override
    public Resource getMediaContent(Long id) {
        log.error("Fallback: Media storage service is unavailable. Cannot get content for mediaId: {}, cause: {}", id, cause.getMessage());
        return new ByteArrayResource(new byte[0]);
    }

    @Override
    public Resource getMediaVariant(Long id, String variantName) {
        log.error("Fallback: Media storage service is unavailable. Cannot get variant {} for mediaId: {}, cause: {}", variantName, id, cause.getMessage());
        return new ByteArrayResource(new byte[0]);
    }
}
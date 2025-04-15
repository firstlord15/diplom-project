package org.ithub.postservice.client;

import org.ithub.postservice.client.fallback.MediaStorageClientFallback;
import org.ithub.postservice.dto.MediaFileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "media-storage-service", fallback = MediaStorageClientFallback.class)
public interface MediaStorageClient {
    @GetMapping("/media/files/{id}")
    MediaFileDto getMediaFileDetails(@PathVariable Long id);

    @GetMapping("/media/files/{id}/content")
    Resource getMediaContent(@PathVariable Long id);

    @GetMapping("/media/files/{id}/variants/{variantName}")
    Resource getMediaVariant(@PathVariable Long id, @PathVariable String variantName);
}

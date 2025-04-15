package org.ithub.postservice.client;

import org.ithub.postservice.client.fallback.SocialPublishClientFallbackFactory;
import org.ithub.postservice.dto.PublishTextRequest;
import org.ithub.postservice.enums.SocialPlatform;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

// Клиент для публикации
@FeignClient(name = "social-integration-service", url="http://localhost:8084", path = "/social/publish")
public interface SocialPublishClient {
    @PostMapping("/text")
    Map<String, Boolean> publishText(@RequestBody PublishTextRequest request);

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Boolean> publishMedia(@RequestPart("file") MultipartFile file,
                                      @RequestParam("userId") Long userId,
                                      @RequestParam("platform") SocialPlatform platform,
                                      @RequestParam(value = "caption", required = false) String caption);
}


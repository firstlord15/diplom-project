package org.ithub.postservice.client;

import org.ithub.postservice.client.fallback.SocialClientFallbackFactory;
import org.ithub.postservice.dto.PublishTextRequest;
import org.ithub.postservice.dto.SocialAccountDto;
import org.ithub.postservice.enums.SocialPlatform;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

// Клиент для публикации
@FeignClient(name = "social-integration-service", path = "/social", fallbackFactory = SocialClientFallbackFactory.class)
public interface SocialClient {

    @PostMapping("/publish/text")
    Map<String, Boolean> publishText(@RequestBody PublishTextRequest request);

    @PostMapping(value = "/publish/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Boolean> publishMedia(@RequestPart("file") MultipartFile file,
                                      @RequestParam("userId") Long userId,
                                      @RequestParam("platform") SocialPlatform platform,
                                      @RequestParam(value = "caption", required = false) String caption);

    @GetMapping("/active/{userId}")
    List<SocialAccountDto> getActiveAccounts(@PathVariable Long userId);

    @GetMapping("/platform/{userId}/{platform}")
    List<SocialAccountDto> getAccountsByPlatform(@PathVariable Long userId, @PathVariable String platform);
}


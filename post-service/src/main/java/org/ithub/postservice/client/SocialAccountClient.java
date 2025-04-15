package org.ithub.postservice.client;

import org.ithub.postservice.client.fallback.SocialPublishClientFallbackFactory;
import org.ithub.postservice.dto.SocialAccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "social-service-account", path = "/social", fallbackFactory = SocialPublishClientFallbackFactory.class)
public interface SocialAccountClient {
    @GetMapping("/active/{userId}")
    List<SocialAccountDto> getActiveAccounts(@PathVariable Long userId);

    @GetMapping("/platform/{userId}/{platform}")
    List<SocialAccountDto> getAccountsByPlatform(@PathVariable Long userId, @PathVariable String platform);
}
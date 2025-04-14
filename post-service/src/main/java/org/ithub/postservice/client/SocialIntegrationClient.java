package org.ithub.postservice.client;

import org.ithub.postservice.dto.SocialAccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("social-integration-service")
public interface SocialIntegrationClient {
    @GetMapping("/social/active/{userId}")
    List<SocialAccountDto> getActiveAccounts(@PathVariable Long userId);

    @GetMapping("/social/platform/{userId}/{platform}")
    List<SocialAccountDto> getAccountsByPlatform(@PathVariable Long userId, @PathVariable String platform);
}

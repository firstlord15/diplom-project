package org.ithub.postservice.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.ithub.postservice.client.SocialClient;
import org.ithub.postservice.dto.PublishTextRequest;
import org.ithub.postservice.dto.SocialAccountDto;
import org.ithub.postservice.enums.SocialPlatform;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class SocialClientFallback implements SocialClient {
    private final Throwable cause;

    public SocialClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public List<SocialAccountDto> getActiveAccounts(Long userId) {
        log.error("Fallback: Cannot get active accounts for user: {}, cause: {}", userId, cause.getMessage());
        return Collections.emptyList(); // Возвращаем пустой список
    }

    @Override
    public List<SocialAccountDto> getAccountsByPlatform(Long userId, String platform) {
        log.error("Fallback: Cannot get accounts by platform for user: {}, platform: {}, cause: {}",
                userId, platform, cause.getMessage());
        return Collections.emptyList();
    }

    @Override
    public Map<String, Boolean> publishText(PublishTextRequest request) {
        return Collections.singletonMap("success", false);
    }

    @Override
    public Map<String, Boolean> publishMedia(MultipartFile file, Long userId, SocialPlatform platform, String caption) {
        return Collections.singletonMap("success", false);
    }
}
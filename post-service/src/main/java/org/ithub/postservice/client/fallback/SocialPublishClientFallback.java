package org.ithub.postservice.client.fallback;

import org.ithub.postservice.client.SocialPublishClient;
import org.ithub.postservice.dto.PublishTextRequest;
import org.ithub.postservice.enums.SocialPlatform;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

public class SocialPublishClientFallback implements SocialPublishClient {
    private final Throwable cause;

    public SocialPublishClientFallback(Throwable cause) {
        this.cause = cause;
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
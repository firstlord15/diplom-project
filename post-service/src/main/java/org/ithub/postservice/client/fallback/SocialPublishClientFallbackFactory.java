package org.ithub.postservice.client.fallback;

import org.ithub.postservice.client.SocialPublishClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class SocialPublishClientFallbackFactory implements FallbackFactory<SocialPublishClient> {
    @Override
    public SocialPublishClient create(Throwable cause) {
        return new SocialPublishClientFallback(cause);
    }
}

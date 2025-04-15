package org.ithub.postservice.client.fallback;

import org.ithub.postservice.client.SocialClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class SocialClientFallbackFactory implements FallbackFactory<SocialClient> {
    @Override
    public SocialClient create(Throwable cause) {
        return new SocialClientFallback(cause);
    }
}

package org.ithub.socialintegrationservice.util.converter;

import org.ithub.socialintegrationservice.enums.SocialPlatform;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSocialPlatformConverter implements Converter<String, SocialPlatform>{
    @Override
    public SocialPlatform convert(String source) {
        return SocialPlatform.valueOf(source.toUpperCase());
    }
}

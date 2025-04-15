package org.ithub.socialintegrationservice.dto;

import lombok.Data;
import org.ithub.socialintegrationservice.enums.SocialPlatform;

@Data
public class PublishTextRequest {
    private Long userId;
    private SocialPlatform platform;
    private String text;
}

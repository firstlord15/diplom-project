package org.ithub.postservice.dto;

import lombok.Data;
import org.ithub.postservice.enums.SocialPlatform;

@Data
public class PublishTextRequest {
    private Long userId;
    private SocialPlatform platform;
    private String text;
}

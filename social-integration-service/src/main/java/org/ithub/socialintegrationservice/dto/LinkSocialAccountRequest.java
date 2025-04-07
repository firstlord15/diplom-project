package org.ithub.socialintegrationservice.dto;

import lombok.Data;
import org.ithub.socialintegrationservice.enums.SocialPlatform;

@Data
public class LinkSocialAccountRequest {
    private Long userId;
    private String externalId;
    private SocialPlatform platform;
    private String accessToken;
}


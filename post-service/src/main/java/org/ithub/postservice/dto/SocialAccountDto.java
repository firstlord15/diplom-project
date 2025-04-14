package org.ithub.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.postservice.enums.SocialPlatform;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccountDto {
    private Long id;
    private Long userId;
    private SocialPlatform platform;
    private String externalId;
    private String accessToken;
    private String additionalData;
    private LocalDateTime linkedAt;
    private boolean active;
}

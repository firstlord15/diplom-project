package org.ithub.socialintegrationservice.dto;

import lombok.Data;
import org.ithub.socialintegrationservice.enums.SocialPlatform;

@Data
public class PublishMediaRequest {
    private Long userId;
    private SocialPlatform platform;
    private String caption;
    // Файл будет передаваться через MultipartFile
}
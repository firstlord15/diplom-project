package org.ithub.postservice.dto;

import lombok.Data;
import org.ithub.postservice.enums.SocialPlatform;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SendMessageRequest {
    private MultipartFile file;
    private Long userId;
    private SocialPlatform platform;
    private String message;
}

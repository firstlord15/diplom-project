package org.ithub.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.postservice.enums.SocialPlatform;
import org.ithub.postservice.enums.TaskStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialPostTaskDto {
    private Long id;
    private Long postId;
    private SocialPlatform platform;
    private Long socialAccountId;
    private TaskStatus status;
    private String errorMessage;
    private String externalPostId;
    private String externalPostUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime executedAt;
}

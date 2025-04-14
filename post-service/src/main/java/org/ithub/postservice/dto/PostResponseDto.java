package org.ithub.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.postservice.enums.PostStatus;
import org.ithub.postservice.enums.PostType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private Long authorId;
    private String title;
    private String content;
    private PostType type;
    private PostStatus status;
    private Set<String> tags = new HashSet<>();
    private List<PostMediaDto> media = new ArrayList<>();
    private List<SocialPostTaskDto> socialTasks = new ArrayList<>();
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}

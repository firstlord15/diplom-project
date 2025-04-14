package org.ithub.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class PostRequestDto {
    private String title;
    private String content;
    private PostType type;
    private Set<String> tags = new HashSet<>();
    private List<Long> mediaIds = new ArrayList<>();
    private List<String> mediaCaptions = new ArrayList<>();
    private List<Long> socialAccountIds = new ArrayList<>();
    private LocalDateTime scheduledAt;
}

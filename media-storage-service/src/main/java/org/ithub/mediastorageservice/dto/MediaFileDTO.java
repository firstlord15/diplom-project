package org.ithub.mediastorageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileDTO {
    private Long id;
    private String originalFilename;
    private Long size;
    private String mediaType;
    private String mimeType;
    private Integer width;
    private Integer height;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String uploadedBy;
    private Set<String> tags;
    private String url;
    private Map<String, String> variantUrls;
    private String status;
}

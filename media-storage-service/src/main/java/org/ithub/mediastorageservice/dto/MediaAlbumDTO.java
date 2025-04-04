package org.ithub.mediastorageservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MediaAlbumDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
    private Boolean isPublic;
    private Integer fileCount;
    private List<MediaFileDTO> previewFiles; // Первые несколько файлов для превью
}
package org.ithub.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileDto {
    private Long id;
    private String originalFilename;
    private Long size;
    private String mediaType;
    private String mimeType;
    private Integer width;
    private Integer height;
    private String description;
    private Set<String> tags;
    private String url;
    private Map<String, String> variantUrls;
}

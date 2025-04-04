package org.ithub.mediastorageservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaVariantDTO {
    private Long id;
    private String variantName;
    private String url;
    private Integer width;
    private Integer height;
    private Long size;
}

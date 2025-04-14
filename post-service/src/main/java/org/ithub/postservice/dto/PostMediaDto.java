package org.ithub.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.postservice.enums.MediaType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMediaDto {
    private Long id;
    private MediaType type;
    private Long mediaId;
    private String caption;
    private MediaFileDto mediaDetails;
    private Integer sortOrder;
}
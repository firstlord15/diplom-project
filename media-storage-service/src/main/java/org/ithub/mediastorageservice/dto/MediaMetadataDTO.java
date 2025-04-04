package org.ithub.mediastorageservice.dto;

import lombok.Data;

import java.util.Set;

@Data
public class MediaMetadataDTO {
    private String description;
    private Set<String> tagsToAdd;
    private Set<String> tagsToRemove;
}

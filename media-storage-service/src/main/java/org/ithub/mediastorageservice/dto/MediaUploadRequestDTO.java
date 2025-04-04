package org.ithub.mediastorageservice.dto;

import lombok.Data;

import java.util.Set;

@Data
public class MediaUploadRequestDTO {
    private String description;
    private Set<String> tags;
    private String albumName; // Опционально: добавить файл сразу в альбом
}
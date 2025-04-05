package org.ithub.mediastorageservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.mediastorageservice.dto.MediaFileDTO;
import org.ithub.mediastorageservice.dto.MediaUploadRequestDTO;
import org.ithub.mediastorageservice.enums.MediaType;
import org.ithub.mediastorageservice.model.MediaFile;
import org.ithub.mediastorageservice.service.MediaFileService;
import org.ithub.mediastorageservice.service.MediaVariantService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/media/files")
public class MediaFileController {
    private final MediaFileService mediaFileService;
    private final MediaVariantService mediaVariantService;

    @GetMapping("/{id}")
    @Operation(summary = "Получение информации о медиа-файле по ID", description = "Возвращает детальную информацию о файле и его вариантах")
    public ResponseEntity<MediaFileDTO> getMediaFileById(@PathVariable Long id) {
        log.info("Retrieving media file details for id: {}", id);
        MediaFileDTO mediaFile = mediaFileService.getMediaDetails(id);
        log.debug("Found media file: {}", mediaFile.getOriginalFilename());
        return ResponseEntity.ok(mediaFile);
    }

    @GetMapping("/{id}/content")
    @Operation(summary = "Получение содержимого медиа-файла", description = "Возвращает бинарное содержимое файла")
    public ResponseEntity<Resource> getContentMediaFileById(@PathVariable Long id) {
        log.info("Retrieving media file content for id: {}", id);
        Resource content = mediaFileService.getMediaContent(id);
        return ResponseEntity.ok(content);
    }

    @GetMapping("/{id}/variants/{variantName}")
    @Operation(summary = "Получение варианта медиа-файла", description = "Возвращает содержимое указанного варианта файла (например, thumbnail)")
    public ResponseEntity<Resource> getVariantContentById(@PathVariable Long id, @PathVariable String variantName) {
        log.info("Retrieving variant {} for media file id: {}", variantName, id);
        Resource content = mediaVariantService.getVariantContent(id, variantName);
        return ResponseEntity.ok(content);
    }


    @PostMapping
    @Operation(summary = "Загрузка нового медиа-файла", description = "Загружает файл и связанные с ним метаданные")
    public ResponseEntity<MediaFileDTO> uploadMediaFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "metadata", required = false) MediaUploadRequestDTO requestDTO
    ) {
        log.info("Uploading new file: {}, size: {}", file.getOriginalFilename(), file.getSize());

        try {
            String description = requestDTO != null ? requestDTO.getDescription() : null;
            Set<String> tags = requestDTO != null ? requestDTO.getTags() : null;

            MediaFile mediaFile = mediaFileService.createMediaFile(file, description, tags);
            MediaFileDTO mediaFileDTO = mediaFileService.convertToDTO(mediaFile);

            log.info("File uploaded successfully. Media ID: {}", mediaFile.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(mediaFileDTO);
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", e);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление медиа-файла", description = "Полностью удаляет файл, его варианты и связанные данные")
    public ResponseEntity<Void> deleteMediaFileById(@PathVariable Long id) {
        log.info("Deleting media file with id: {}", id);
        mediaFileService.deleteMediaFile(id);
        log.info("Media file with id: {} successfully deleted", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Поиск медиа-файлов", description = "Поиск с фильтрацией по тегам, типу и пагинацией")
    public ResponseEntity<Page<MediaFileDTO>> searchFiles(
            @RequestParam(required = false) Set<String> tags,
            @RequestParam(required = false) String mediaType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        log.info("Searching media files with tags: {}, mediaType: {}, page: {}, size: {}",
                tags, mediaType, page, size);

        Sort sort = Sort.by(
                sortDirection.equalsIgnoreCase("asc") ?
                        Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MediaFileDTO> results;
        // Если указан mediaType, добавляем фильтрацию по типу
        if (mediaType != null && !mediaType.isEmpty()) {
            try {
                MediaType type = MediaType.valueOf(mediaType.toUpperCase());
                results = mediaFileService.findByTagsAndType(tags, type, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid media type: {}", mediaType);
                // Если указан неверный тип, игнорируем его
                results = mediaFileService.findByTags(tags, pageable);
            }
        } else {
            // Поиск только по тегам
            results = mediaFileService.findByTags(tags, pageable);
        }

        log.info("Found {} media files matching criteria", results.getTotalElements());
        return ResponseEntity.ok(results);
    }
}

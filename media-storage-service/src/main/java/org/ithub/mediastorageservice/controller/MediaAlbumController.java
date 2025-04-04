package org.ithub.mediastorageservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.mediastorageservice.dto.MediaAlbumDTO;
import org.ithub.mediastorageservice.dto.MediaFileDTO;
import org.ithub.mediastorageservice.service.MediaAlbumService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/media/albums")
public class MediaAlbumController {
    private final MediaAlbumService mediaAlbumService;

    @GetMapping("/{id}")
    @Operation(summary = "Получение альбома по id", description = "Возвращает альбом по id")
    public ResponseEntity<MediaAlbumDTO> getAlbumById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaAlbumService.convertToDTO(
                mediaAlbumService.getAlbum(id)
        ));
    }

    @GetMapping
    @Operation(summary = "Получение списка альбомов", description = "Возвращает список альбомов с пагинацией")
    public ResponseEntity<Page<MediaAlbumDTO>> getAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Boolean isPublic
    ) {
        log.info("Retrieving albums page: {}, size: {}, sortBy: {}, sortDirection: {}, isPublic: {}", page, size, sortBy, sortDirection, isPublic);
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MediaAlbumDTO> albums;

        if (isPublic != null) {
            albums = mediaAlbumService.findByIsPublic(isPublic, pageable);
        } else {
            albums = mediaAlbumService.findAll(pageable);
        }

        log.info("Found {} albums", albums.getTotalElements());
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/{id}/files")
    @Operation(summary = "Получение списка альбомов", description = "Возвращает список альбомов с пагинацией")
    public ResponseEntity<Page<MediaFileDTO>> getFilesFromAlbum(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("Retrieving files from album with id: {}, page: {}, size: {}", id, page, size);
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MediaFileDTO> files = mediaAlbumService.getFilesFromAlbum(id, pageable);

        log.info("Found {} files in album with id: {}", files.getTotalElements(), id);
        return ResponseEntity.ok(files);
    }
}

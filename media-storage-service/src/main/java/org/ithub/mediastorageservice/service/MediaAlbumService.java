package org.ithub.mediastorageservice.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.mediastorageservice.dto.MediaAlbumDTO;
import org.ithub.mediastorageservice.dto.MediaFileDTO;
import org.ithub.mediastorageservice.model.MediaAlbum;
import org.ithub.mediastorageservice.model.MediaFile;
import org.ithub.mediastorageservice.repository.MediaAlbumRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.ithub.mediastorageservice.util.MediaFileMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaAlbumService {
    private final MediaAlbumRepository mediaAlbumRepository;
    private final MediaFileMapper fileMapper;

    /**
     * Сохранение альбома
     */
    public MediaAlbum saveAlbum(MediaAlbum mediaAlbum) {
        return mediaAlbumRepository.save(mediaAlbum);
    }

    /**
    * Получение альбома
    */
    public MediaAlbum getAlbum(Long id) {
        return mediaAlbumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MediaAlbum not found with id: " + id));
    }

    /**
     * Получение всех альбомов с пагинацией
     */
    public Page<MediaAlbumDTO> findAll(Pageable pageable) {
        log.debug("Finding all albums with pageable: {}", pageable);
        Page<MediaAlbum> albums = mediaAlbumRepository.findAll(pageable);
        return albums.map(this::convertToDTO);
    }

    /**
     * Получение альбомов по признаку публичности с пагинацией
     */
    public Page<MediaAlbumDTO> findByIsPublic(Boolean isPublic, Pageable pageable) {
        log.debug("Finding albums with isPublic={} and pageable: {}", isPublic, pageable);
        Page<MediaAlbum> albums = mediaAlbumRepository.findByIsPublic(isPublic, pageable);
        return albums.map(this::convertToDTO);
    }

    /**
     * Получение файлов из альбома с пагинацией
     */
    public Page<MediaFileDTO> getFilesFromAlbum(Long albumId, Pageable pageable) {
        log.debug("Getting files from album with id: {} and pageable: {}", albumId, pageable);

        MediaAlbum album = getAlbum(albumId);

        // Преобразуем список файлов в Page
        List<MediaFile> mediaFiles = album.getMediaFiles();

        // Применяем пагинацию вручную
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), mediaFiles.size());

        if (start > mediaFiles.size()) {
            return Page.empty(pageable);
        }

        // Применяем сортировку
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            // Сортировка файлов по указанному полю (упрощенная реализация)
            mediaFiles.sort((a, b) -> {
                Sort.Order order = sort.iterator().next();
                int result = 0;

                // Сравниваем по указанному полю (здесь только для createdAt)
                if (order.getProperty().equals("createdAt")) {
                    result = a.getCreatedAt().compareTo(b.getCreatedAt());
                }

                // Инвертируем результат для DESC сортировки
                return order.isAscending() ? result : -result;
            });
        }

        // Берем нужный подсписок
        List<MediaFile> pageContent = mediaFiles.subList(start, end);

        // Конвертируем в DTO
        List<MediaFileDTO> contentDto = pageContent.stream()
                .map(fileMapper::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(contentDto, pageable, mediaFiles.size());
    }

    /**
     * Добавление файла в альбом
     */
    public void addFileToAlbum(Long albumId, MediaFile mediaFile) {
//        MediaFile mediaFile = mediaFileService.getMediaFile(mediaId);
        MediaAlbum mediaAlbum = getAlbum(albumId);

        // проверим нету ли mediaFile уже в альбоме
        if (mediaAlbum.getMediaFiles().contains(mediaFile)) {
            return; // Файл уже в альбоме
        }

        // Добавляем медаи файл в альбом
        mediaAlbum.getMediaFiles().add(mediaFile);

        // Сохраняем в бд
        mediaAlbumRepository.save(mediaAlbum);
    }

    /**
     * Удаление файла из альбома
     */
    public void removeFileFromAlbum(Long albumId, MediaFile mediaFile) {
//        MediaFile mediaFile = mediaFileService.getMediaFile(mediaId);
        MediaAlbum mediaAlbum = getAlbum(albumId);

        // Удаляем медаи файл из альбома
        mediaAlbum.getMediaFiles().remove(mediaFile);

        // Сохраняем в бд
        mediaAlbumRepository.save(mediaAlbum);
    }

    /**
     * Удаление файла из всех альбомов
     */
    @Transactional
    public void removeFileFromAllAlbums(MediaFile mediaFile) {
        List<MediaAlbum> albums = mediaAlbumRepository.findByMediaFilesContaining(mediaFile);
        for (MediaAlbum album : albums) {
            album.getMediaFiles().remove(mediaFile);
            mediaAlbumRepository.save(album);
        }
    }

    /**
     * Конвертация MediaAlbum в MediaAlbumDTO
     */
    public MediaAlbumDTO convertToDTO(MediaAlbum mediaAlbum) {
        // Получаем только первые несколько файлов для превью
        List<MediaFileDTO> previewFiles = mediaAlbum.getMediaFiles().stream()
                .limit(4)
                .map(fileMapper::convertToDTO)
                .toList();

        return MediaAlbumDTO.builder()
                .id(mediaAlbum.getId())
                .name(mediaAlbum.getName())
                .description(mediaAlbum.getDescription())
                .createdAt(mediaAlbum.getCreatedAt())
                .createdBy(mediaAlbum.getCreatedBy())
                .isPublic(mediaAlbum.getIsPublic())
                .previewFiles(previewFiles)
                .fileCount(mediaAlbum.getMediaFiles().size())
                .build();
    }
}

package org.ithub.mediastorageservice.repository;

import org.ithub.mediastorageservice.enums.MediaStatus;
import org.ithub.mediastorageservice.enums.MediaType;
import org.ithub.mediastorageservice.model.MediaFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    Page<MediaFile> findByMediaType(MediaType mediaType, Pageable pageable);
    List<MediaFile> findByStatus(MediaStatus status);
    Page<MediaFile> findByTagsIn(Set<String> tags, Pageable pageable);
    Page<MediaFile> findByTagsInAndMediaType(Set<String> tags, MediaType mediaType, Pageable pageable);
}

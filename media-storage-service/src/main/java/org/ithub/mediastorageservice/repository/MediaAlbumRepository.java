package org.ithub.mediastorageservice.repository;

import org.ithub.mediastorageservice.model.MediaAlbum;
import org.ithub.mediastorageservice.model.MediaFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaAlbumRepository extends JpaRepository<MediaAlbum, Long> {
    List<MediaAlbum> findByCreatedBy(String createdBy);
    List<MediaAlbum> findByIsPublicTrue();
    Optional<MediaAlbum> findByNameAndCreatedBy(String name, String createdBy);
    List<MediaAlbum> findByMediaFilesContaining(MediaFile mediaFile);
    Page<MediaAlbum> findByIsPublic(Boolean isPublic, Pageable pageable);
}

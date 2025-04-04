package org.ithub.mediastorageservice.repository;

import org.ithub.mediastorageservice.model.MediaVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaVariantRepository extends JpaRepository<MediaVariant, Long> {
    List<MediaVariant> findByMediaFileId(Long mediaFileId);
    Optional<MediaVariant> findByMediaFileIdAndVariantName(Long mediaFileId, String variantName);
    void deleteByMediaFileId(Long mediaFileId);
}
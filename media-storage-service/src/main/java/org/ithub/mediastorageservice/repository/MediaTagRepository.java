package org.ithub.mediastorageservice.repository;

import org.ithub.mediastorageservice.model.MediaTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MediaTagRepository extends JpaRepository<MediaTag, Long> {
    List<MediaTag> findByMediaFileId(Long mediaFileId);
    List<MediaTag> findByName(String name);
    void deleteByMediaFileId(Long mediaFileId);
    List<MediaTag> findByMediaFileIdAndNameIn(Long mediaFileId, Collection<String> names);
}

package org.ithub.postservice.repository;

import org.ithub.postservice.enums.PostStatus;
import org.ithub.postservice.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    long countByAuthorId(Long authorId);

    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    Page<Post> findByAuthorIdAndStatus(Long authorId, PostStatus status, Pageable pageable);

    Page<Post> findByTagsContaining(String tag, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = 'SCHEDULED' AND p.scheduledAt <= :now")
    List<Post> findScheduledPostsDueForPublication(LocalDateTime now);
}

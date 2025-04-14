package org.ithub.postservice.repository;

import org.ithub.postservice.enums.SocialPlatform;
import org.ithub.postservice.enums.TaskStatus;
import org.ithub.postservice.model.SocialPostTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialPostTaskRepository extends JpaRepository<SocialPostTask, Long> {
    List<SocialPostTask> findByPostId(Long postId);

    List<SocialPostTask> findByStatus(TaskStatus status);

    List<SocialPostTask> findByPostIdAndPlatform(Long postId, SocialPlatform platform);
}

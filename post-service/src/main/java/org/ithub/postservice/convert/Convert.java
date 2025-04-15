package org.ithub.postservice.convert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.postservice.client.MediaStorageClient;
import org.ithub.postservice.dto.MediaFileDto;
import org.ithub.postservice.dto.PostMediaDto;
import org.ithub.postservice.dto.PostResponseDto;
import org.ithub.postservice.dto.SocialPostTaskDto;
import org.ithub.postservice.model.Post;
import org.ithub.postservice.model.PostMedia;
import org.ithub.postservice.model.SocialPostTask;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Convert {
    private final MediaStorageClient mediaStorageClient;
    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    // Конвертация модели в DTO
    public PostResponseDto convertToResponseDto(Post post) {
        PostResponseDto dto = PostResponseDto.builder()
                .id(post.getId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .type(post.getType())
                .status(post.getStatus())
                .tags(post.getTags())
                .scheduledAt(post.getScheduledAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .publishedAt(post.getPublishedAt())
                .build();

        // Загружаем медиа
        List<PostMediaDto> mediaDtos = post.getMedia().stream()
                .map(this::convertToMediaDto)
                .collect(Collectors.toList());
        dto.setMedia(mediaDtos);

        // Загружаем задачи публикации
        List<SocialPostTaskDto> taskDtos = post.getSocialTasks().stream()
                .map(this::convertToTaskDto)
                .collect(Collectors.toList());
        dto.setSocialTasks(taskDtos);

        return dto;
    }

    public PostMediaDto convertToMediaDto(PostMedia media) {
        PostMediaDto dto = PostMediaDto.builder()
                .id(media.getId())
                .type(media.getType())
                .mediaId(media.getMediaId())
                .caption(media.getCaption())
                .sortOrder(media.getSortOrder())
                .build();

        // Загружаем детали медиафайла
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("mediaStorage");
        try {
            MediaFileDto mediaFile = circuitBreaker.run(
                    () -> mediaStorageClient.getMediaFileDetails(media.getMediaId()),
                    throwable -> {
                        log.error("Error fetching media details for ID {}: {}", media.getMediaId(), throwable.getMessage());
                        return MediaFileDto.builder()
                                .id(media.getMediaId())
                                .originalFilename("unavailable.file")
                                .mediaType("unknown")
                                .mimeType("application/octet-stream")
                                .build();
                    }
            );
            dto.setMediaDetails(mediaFile);
        } catch (Exception e) {
            log.error("Circuit breaker executed fallback for media ID {}: {}", media.getMediaId(), e.getMessage());
        }

        return dto;
    }

    public SocialPostTaskDto convertToTaskDto(SocialPostTask task) {
        return SocialPostTaskDto.builder()
                .id(task.getId())
                .postId(task.getPost().getId())
                .platform(task.getPlatform())
                .socialAccountId(task.getSocialAccountId())
                .status(task.getStatus())
                .errorMessage(task.getErrorMessage())
                .externalPostId(task.getExternalPostId())
                .externalPostUrl(task.getExternalPostUrl())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .executedAt(task.getExecutedAt())
                .build();
    }
}

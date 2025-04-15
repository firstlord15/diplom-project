package org.ithub.postservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.postservice.client.MediaStorageClient;
import org.ithub.postservice.dto.*;
import org.ithub.postservice.enums.PostStatus;
import org.ithub.postservice.enums.PostType;
import org.ithub.postservice.enums.TaskStatus;
import org.ithub.postservice.model.Post;
import org.ithub.postservice.model.PostMedia;
import org.ithub.postservice.model.SocialPostTask;
import org.ithub.postservice.repository.PostMediaRepository;
import org.ithub.postservice.repository.PostRepository;
import org.ithub.postservice.repository.SocialPostTaskRepository;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MediaStorageClient mediaStorageClient;
    private final PostMediaRepository postMediaRepository;
    private final SocialPostTaskRepository socialPostTaskRepository;
    private final SocialPostPublisher socialPostPublisher;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Transactional
    public PostResponseDto createPost(Long userId, PostRequestDto postRequestDto) {
        log.info("Creating post for user {} with title: {}", userId, postRequestDto.getTitle());
        PostStatus status = postRequestDto.getScheduledAt() != null ? PostStatus.SCHEDULED : PostStatus.DRAFT;

        Post post = Post.builder()
                .authorId(userId)
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .type(determinePostType(postRequestDto))
                .socialTasks(new ArrayList<>())
                .status(status)
                .tags(postRequestDto.getTags())
                .scheduledAt(postRequestDto.getScheduledAt())
                .build();

        Post savedPost = postRepository.save(post);

        // Добавляем медиа
        if (postRequestDto.getMediaIds() != null && !postRequestDto.getMediaIds().isEmpty()){
            List<PostMedia> mediaList = createPostMediaList(savedPost, postRequestDto);
            savedPost.setMedia(mediaList);
        }

        // Публикуем сразу, если не запланировано
        if (savedPost.getStatus() != PostStatus.SCHEDULED && !savedPost.getSocialTasks().isEmpty() ) {
            socialPostPublisher.publishPost(savedPost.getId());
            savedPost.setStatus(PostStatus.PUBLISHED);
            savedPost.setPublishedAt(LocalDateTime.now());
            savedPost = postRepository.save(savedPost);
        }

        return convertToResponseDto(savedPost);
    }

    private List<PostMedia> createPostMediaList(Post post, PostRequestDto dto) {
        List<PostMedia> mediaList = new ArrayList<>();

        for (int i = 0; i < dto.getMediaIds().size(); i++) {
            Long mediaId = dto.getMediaIds().get(i);
            String caption = (i < dto.getMediaCaptions().size()) ? dto.getMediaCaptions().get(i) : null;

            // Получаем информацию о медиафайле, чтобы определить его тип
            try {
                MediaFileDto mediaFile = mediaStorageClient.getMediaFileDetails(mediaId);

                PostMedia media = PostMedia.builder()
                        .post(post)
                        .mediaId(mediaId)
                        .caption(caption)
                        .sortOrder(i)
                        .type(determineMediaType(mediaFile.getMediaType()))
                        .build();

                mediaList.add(postMediaRepository.save(media));
            } catch (Exception e) {
                log.error("Error fetching media details for ID {}: {}", mediaId, e.getMessage());
                // Можно добавить fallback логику или просто пропустить
            }
        }

        return mediaList;
    }

    private List<SocialPostTask> createSocialTasks(Post post, List<Long> socialAccountIds) {
        List<SocialPostTask> tasks = new ArrayList<>();
        for (Long accountId : socialAccountIds) {
            SocialPostTask task = SocialPostTask.builder()
                    .post(post)
                    .socialAccountId(accountId)
                    .status(TaskStatus.PENDING)
                    .build();

            tasks.add(socialPostTaskRepository.save(task));
        }
        return tasks;
    }

    public PostResponseDto getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return convertToResponseDto(post);
    }

    public Page<PostResponseDto> getPostsByUser(Long userId, PostStatus status, Pageable pageable) {
        Page<Post> postsPage;
        if (status != null) {
            postsPage = postRepository.findByAuthorIdAndStatus(userId, status, pageable);
        } else {
            postsPage = postRepository.findByAuthorId(userId, pageable);
        }

        List<PostResponseDto> responseDtos = postsPage.getContent().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(responseDtos, pageable, postsPage.getTotalElements());
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto postRequestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Обновляем только если пост в статусе DRAFT
        if (post.getStatus() != PostStatus.DRAFT) {
            throw new IllegalStateException("Cannot update post that is not in DRAFT status");
        }

        post.setTitle(postRequestDto.getTitle());
        post.setContent(postRequestDto.getContent());
        post.setType(determinePostType(postRequestDto));
        post.setTags(postRequestDto.getTags());
        post.setScheduledAt(postRequestDto.getScheduledAt());

        if (postRequestDto.getScheduledAt() != null) {
            post.setStatus(PostStatus.SCHEDULED);
        }

        // Обновление медиа требует отдельной логики, которую можно добавить при необходимости

        Post savedPost = postRepository.save(post);
        return convertToResponseDto(savedPost);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Удаляем только если пост в статусе DRAFT или FAILED
        if (post.getStatus() != PostStatus.DRAFT && post.getStatus() != PostStatus.FAILED) {
            throw new IllegalStateException("Cannot delete post that is not in DRAFT or FAILED status");
        }

        postRepository.delete(post);
    }

    @Transactional
    public PostResponseDto publishPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (post.getStatus() != PostStatus.DRAFT) {
            throw new IllegalStateException("Cannot publish post that is not in DRAFT status");
        }

        // Проверяем, есть ли задачи для публикации
        if (post.getSocialTasks().isEmpty()) {
            throw new IllegalStateException("No social accounts selected for publication");
        }

        boolean published = socialPostPublisher.publishPost(postId);

        if (published) {
            post.setStatus(PostStatus.PUBLISHED);
            post.setPublishedAt(LocalDateTime.now());
            Post savedPost = postRepository.save(post);
            return convertToResponseDto(savedPost);
        } else {
            throw new RuntimeException("Failed to publish post");
        }
    }

    // Метод для планировщика задач, который будет публиковать запланированные посты
    @Transactional
    public void publishScheduledPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> scheduledPosts = postRepository.findScheduledPostsDueForPublication(now);

        for (Post post : scheduledPosts) {
            try {
                boolean published = socialPostPublisher.publishPost(post.getId());

                if (published) {
                    post.setStatus(PostStatus.PUBLISHED);
                    post.setPublishedAt(LocalDateTime.now());
                } else {
                    post.setStatus(PostStatus.FAILED);
                }

                postRepository.save(post);
            } catch (Exception e) {
                log.error("Error publishing scheduled post {}: {}", post.getId(), e.getMessage());
                post.setStatus(PostStatus.FAILED);
                postRepository.save(post);
            }
        }
    }

    private PostType determinePostType(PostRequestDto dto) {
        if (dto.getMediaIds() == null || dto.getMediaIds().isEmpty()) {
            return PostType.TEXT;
        }
        // Здесь можно добавить более сложную логику определения типа поста
        return PostType.MIXED;
    }

    private org.ithub.postservice.enums.MediaType determineMediaType(String mediaType) {
        try {
            return org.ithub.postservice.enums.MediaType.valueOf(mediaType);
        } catch (IllegalArgumentException e) {
            return org.ithub.postservice.enums.MediaType.IMAGE; // Возвращаем значение по умолчанию
        }
    }

    // Конвертация модели в DTO
    private PostResponseDto convertToResponseDto(Post post) {
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

    private PostMediaDto convertToMediaDto(PostMedia media) {
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

    private SocialPostTaskDto convertToTaskDto(SocialPostTask task) {
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

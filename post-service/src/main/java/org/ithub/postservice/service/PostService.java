package org.ithub.postservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.postservice.client.MediaStorageClient;
import org.ithub.postservice.client.SocialClient;
import org.ithub.postservice.convert.Convert;
import org.ithub.postservice.dto.MediaFileDto;
import org.ithub.postservice.dto.PostRequestDto;
import org.ithub.postservice.dto.PostResponseDto;
import org.ithub.postservice.dto.SocialAccountDto;
import org.ithub.postservice.enums.PostStatus;
import org.ithub.postservice.enums.PostType;
import org.ithub.postservice.enums.TaskStatus;
import org.ithub.postservice.model.Post;
import org.ithub.postservice.model.PostMedia;
import org.ithub.postservice.model.SocialPostTask;
import org.ithub.postservice.repository.PostMediaRepository;
import org.ithub.postservice.repository.PostRepository;
import org.ithub.postservice.repository.SocialPostTaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final Convert convert;
    private final PostRepository postRepository;
    private final MediaStorageClient mediaStorageClient;
    private final PostMediaRepository postMediaRepository;
    private final SocialPostTaskRepository socialPostTaskRepository;
    private final SocialPostPublisher socialPostPublisher;
    private final SocialClient socialClient;

    @Transactional
    public PostResponseDto createPost(Long userId, PostRequestDto postRequestDto) {
        log.info("Creating post for user {} with title: {}", userId, postRequestDto.getTitle());
        PostStatus status = postRequestDto.getScheduledAt() != null ? PostStatus.SCHEDULED : PostStatus.DRAFT;

        Post post = Post.builder()
                .authorId(userId)
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .type(determinePostType(postRequestDto))
                .status(status)
                .tags(postRequestDto.getTags())
                .scheduledAt(postRequestDto.getScheduledAt())
                .media(new ArrayList<>())       // Инициализируем пустыми списками
                .socialTasks(new ArrayList<>()) // Явно указываем пустые списки
                .build();

        Post savedPost = postRepository.save(post);

        // Добавляем медиа
        if (postRequestDto.getMediaIds() != null && !postRequestDto.getMediaIds().isEmpty()){
            List<PostMedia> mediaList = createPostMediaList(savedPost, postRequestDto);
            savedPost.setMedia(mediaList);
        }

        // Добавляем задачи для социальных сетей
        if (postRequestDto.getSocialAccountIds() != null && !postRequestDto.getSocialAccountIds().isEmpty()) {
            List<SocialPostTask> socialTasks = createSocialTasks(savedPost, postRequestDto.getSocialAccountIds());
            savedPost.setSocialTasks(socialTasks);
            savedPost = postRepository.save(savedPost);
        }

        // Публикуем сразу, если не запланировано
//        if (savedPost.getStatus() != PostStatus.SCHEDULED && !savedPost.getSocialTasks().isEmpty() ) {
//            socialPostPublisher.publishPost(savedPost.getId());
//            savedPost = postRepository.findById(savedPost.getId()).orElseThrow();
//            savedPost.setStatus(PostStatus.PUBLISHED);
//            savedPost.setPublishedAt(LocalDateTime.now());
//            savedPost = postRepository.save(savedPost);
//        }

        return convert.convertToResponseDto(savedPost);
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
            try {
                // Получаем информацию о социальном аккаунте, чтобы узнать платформу
                List<SocialAccountDto> accounts = socialClient.getActiveAccounts(post.getAuthorId());

                Optional<SocialAccountDto> accountOpt = accounts.stream()
                        .filter(acc -> acc.getId().equals(accountId))
                        .findFirst();

                if (accountOpt.isEmpty()) {
                    log.warn("Social account with ID {} not found for user {}", accountId, post.getAuthorId());
                    continue;
                }

                SocialAccountDto account = accountOpt.get();
                SocialPostTask task = SocialPostTask.builder()
                        .post(post)
                        .socialAccountId(accountId)
                        .platform(account.getPlatform()) // Устанавливаем платформу из аккаунта
                        .status(TaskStatus.PENDING)
                        .build();

                tasks.add(socialPostTaskRepository.save(task));
            } catch (Exception e) {
                log.error("Error creating social task for account ID {}: {}", accountId, e.getMessage());
                // Можно рассмотреть вариант, создавать задачу даже при ошибке получения платформы
            }
        }
        return tasks;
    }

    public PostResponseDto getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return convert.convertToResponseDto(post);
    }

    public Page<PostResponseDto> getPostsByUser(Long userId, PostStatus status, Pageable pageable) {
        Page<Post> postsPage;
        if (status != null) {
            postsPage = postRepository.findByAuthorIdAndStatus(userId, status, pageable);
        } else {
            postsPage = postRepository.findByAuthorId(userId, pageable);
        }

        List<PostResponseDto> responseDtos = postsPage.getContent().stream()
                .map(convert::convertToResponseDto)
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
        return convert.convertToResponseDto(savedPost);
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

    // Метод для планировщика задач, который будет публиковать запланированные посты
    @Transactional
    public void publishScheduledPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> scheduledPosts = postRepository.findScheduledPostsDueForPublication(now);

        for (Post post : scheduledPosts) {
            try {
                PostResponseDto responseDto = socialPostPublisher.publishPost(post.getId());

                if (responseDto.getStatus() == PostStatus.PUBLISHED) {
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
}

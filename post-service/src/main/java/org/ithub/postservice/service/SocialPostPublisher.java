package org.ithub.postservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.ithub.postservice.client.MediaStorageClient;
import org.ithub.postservice.client.SocialClient;
import org.ithub.postservice.convert.Convert;
import org.ithub.postservice.dto.MediaFileDto;
import org.ithub.postservice.dto.PostResponseDto;
import org.ithub.postservice.dto.PublishTextRequest;
import org.ithub.postservice.dto.SocialAccountDto;
import org.ithub.postservice.enums.PostStatus;
import org.ithub.postservice.enums.TaskStatus;
import org.ithub.postservice.model.Post;
import org.ithub.postservice.model.PostMedia;
import org.ithub.postservice.model.SocialPostTask;
import org.ithub.postservice.repository.PostRepository;
import org.ithub.postservice.repository.SocialPostTaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialPostPublisher {
    private final Convert convert;
    private final SocialPostTaskRepository socialPostTaskRepository;
    private final SocialClient socialClient;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MediaStorageClient mediaStorageClient;
    private final PostRepository postRepository;

    @Value("${integration.social.service.url}")
    private String socialServiceUrl;

    @Transactional
    public PostResponseDto publishPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        boolean allSuccess = true;

        for (SocialPostTask task : post.getSocialTasks()) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                continue; // Пропускаем уже выполненные задачи
            }

            task.setStatus(TaskStatus.PROCESSING);
            socialPostTaskRepository.save(task);

            try {
                // Получаем данные социального аккаунта
                List<SocialAccountDto> accounts = socialClient.getAccountsByPlatform(
                        post.getAuthorId(), task.getPlatform().toString()
                );
                log.info("Получаем активные аккаунты: {}", accounts);

                if (accounts.isEmpty()) {
                    throw new RuntimeException("No active social account found for platform: " + task.getPlatform());
                }

                // Находим конкретный аккаунт
                Optional<SocialAccountDto> accountOpt = accounts.stream()
                        .filter(acc -> acc.getId().equals(task.getSocialAccountId()))
                        .findFirst();

                if (accountOpt.isEmpty()) {
                    throw new RuntimeException("Social account not found with id: " + task.getSocialAccountId());
                }

                SocialAccountDto account = accountOpt.get();

                // Публикуем пост в социальную сеть
                boolean success = publishToSocialPlatform(post, task, account);
                if (success) {
                    task.setStatus(TaskStatus.COMPLETED);
                    task.setExecutedAt(LocalDateTime.now());
                } else {
                    task.setStatus(TaskStatus.FAILED);
                    task.setErrorMessage("Unknown error during publication");
                    allSuccess = false;
                }
            } catch (Exception e) {
                log.error("Error publishing to {}: {}", task.getPlatform(), e.getMessage());
                task.setStatus(TaskStatus.FAILED);
                task.setErrorMessage(e.getMessage());
                allSuccess = false;
            }

            socialPostTaskRepository.save(task);
        }
        if (allSuccess) {
            if (post.getStatus() != PostStatus.PUBLISHED) {
                post.setStatus(PostStatus.PUBLISHED);
            }
            post.setPublishedAt(LocalDateTime.now());
        } else {
            post.setStatus(PostStatus.FAILED);
        }

        Post savedPost = postRepository.save(post);
        return convert.convertToResponseDto(savedPost);
    }

    private boolean publishToSocialPlatform(Post post, SocialPostTask task, SocialAccountDto account) {
        return switch (task.getPlatform()) {
            case TELEGRAM -> publishToTelegram(post, task, account);
            case INSTAGRAM -> publishToInstagram(post, task, account);
            default -> throw new UnsupportedOperationException("Platform not supported: " + task.getPlatform());
        };
    }

    private boolean publishToTelegram(Post post, SocialPostTask task, SocialAccountDto account) {
        log.info("Начинаем публикацию в Telegram для поста с ID {}", post.getId());
        log.info("Данные аккаунта: {}", account);

        try {
            if (post.getMedia().isEmpty()) {
                // Текстовый пост - используем social-integration-service
                PublishTextRequest request = new PublishTextRequest();
                request.setUserId(post.getAuthorId());
                request.setPlatform(task.getPlatform());
                request.setText(post.getContent());

                // Вызов social-integration-service через REST
                Map<String, Boolean> response = socialClient.publishText(request);

                boolean success = !response.isEmpty() && Boolean.TRUE.equals(response.get("success"));
                if (success) {
                    // Добавляем дополнительную информацию о публикации
                    task.setExternalPostId("telegram_" + System.currentTimeMillis());
                }

                log.info("Результат публикации в Telegram: {}", success);
                return success;
            } else {
                // Пост с медиа
                PostMedia firstMedia = post.getMedia().get(0);
                MediaFileDto mediaFile = mediaStorageClient.getMediaFileDetails(firstMedia.getMediaId());
                Resource mediaContent = mediaStorageClient.getMediaContent(firstMedia.getMediaId());

                // Конвертируем Resource в byte[]
                byte[] fileContent;
                try (InputStream is = mediaContent.getInputStream()) {
                    fileContent = IOUtils.toByteArray(is);
                }

                // Создаем MultiValueMap для multipart запроса
                MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();

                // Добавляем файл
                ByteArrayResource fileResource = new ByteArrayResource(fileContent) {
                    @Override
                    @NonNull
                    public String getFilename() {
                        return mediaFile.getOriginalFilename() != null ? mediaFile.getOriginalFilename() : "attachment";
                    }
                };
                bodyMap.add("file", fileResource);

                // Добавляем остальные параметры
                bodyMap.add("userId", post.getAuthorId().toString());
                bodyMap.add("platform", task.getPlatform().toString());

                // Добавляем подпись, если есть
                String caption = post.getContent();
                if (caption != null && !caption.isEmpty()) {
                    if (caption.length() > 1024) {
                        caption = caption.substring(0, 1021) + "...";
                    }
                    bodyMap.add("caption", caption);
                }

                // Настраиваем заголовки
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                // Создаем запрос
                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

                // Отправляем запрос в social-integration-service
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        socialServiceUrl + "/social/publish/media",
                        requestEntity,
                        Map.class
                );

                response.getBody();
                boolean success = Boolean.TRUE.equals(response.getBody().get("success"));
                if (success) {
                    task.setExternalPostId("telegram_media_" + System.currentTimeMillis());
                    if (response.getBody().containsKey("url")) {
                        task.setExternalPostUrl((String) response.getBody().get("url"));
                    }
                }

                log.info("Результат публикации в Telegram: {}", success);
                return success;
            }
        } catch (Exception e) {
            log.error("Error publishing to Telegram: {}", e.getMessage(), e);
            task.setErrorMessage("Error publishing to Telegram: " + e.getMessage());
            return false;
        }
    }


    private boolean publishToInstagram(Post post, SocialPostTask task, SocialAccountDto account) {
        // Реализация публикации в Instagram
        return true; // Заглушка
    }
}

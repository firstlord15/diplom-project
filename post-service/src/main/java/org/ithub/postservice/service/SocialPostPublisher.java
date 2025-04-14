package org.ithub.postservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.postservice.client.MediaStorageClient;
import org.ithub.postservice.client.SocialIntegrationClient;
import org.ithub.postservice.dto.MediaFileDto;
import org.ithub.postservice.dto.SocialAccountDto;
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
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialPostPublisher {
    private final SocialPostTaskRepository socialPostTaskRepository;
    private final SocialIntegrationClient socialIntegrationClient;
    private final MediaStorageClient mediaStorageClient;
    private final PostRepository postRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot-token}")
    private String botToken;

    private String telegramApiUrl(String method) {
        return "https://api.telegram.org/bot" + botToken + "/" + method;
    }

    @Transactional
    public boolean publishPost(Long postId) {
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
                List<SocialAccountDto> accounts = socialIntegrationClient.getAccountsByPlatform(
                        post.getAuthorId(), task.getPlatform().toString());

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

        return allSuccess;
    }

    private boolean sendTelegramTextMessage(String chatId, String text, SocialPostTask task) {
        String url = telegramApiUrl("sendMessage");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", chatId);
        params.add("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // Здесь можно распарсить ответ, чтобы получить message_id для externalPostId
                task.setExternalPostId("telegram_" + System.currentTimeMillis());
                return true;
            } else {
                task.setErrorMessage("Telegram API error: " + response.getBody());
                return false;
            }
        } catch (Exception e) {
            task.setErrorMessage("Error sending message to Telegram: " + e.getMessage());
            return false;
        }
    }

    private boolean sendTelegramMedia(String chatId, Resource media, String caption,
                                      SocialPostTask task, String method, String fileFieldName) {
        int messageId = sendFile(chatId, media, caption, method, fileFieldName);

        if (messageId > 0) { // -1 или 0 означают ошибку
            // Обновляем информацию в задаче
            task.setExternalPostId(String.valueOf(messageId));

            // Создаем URL для публичных каналов/групп
            if (chatId.startsWith("-100")) { // Публичный канал/группа
                String publicChatId = chatId.substring(4); // Убираем "-100" префикс
                task.setExternalPostUrl("https://t.me/c/" + publicChatId + "/" + messageId);
            }
            return true;
        } else {
            // Устанавливаем сообщение об ошибке
            task.setErrorMessage("Failed to send " + fileFieldName + " to Telegram");
            return false;
        }
    }

    // Универсальный метод отправки файла
    private int sendFile(String chatId, Resource file, String caption, String method, String fileFieldName) {
        if (file == null) {
            log.error("File resource is null");
            return -1;
        }

        try {
            // Читаем содержимое ресурса в байтовый массив
            byte[] fileBytes = StreamUtils.copyToByteArray(file.getInputStream());

            // Создаем ресурс из байтового массива
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                @NonNull
                public String getFilename() {
                    return file.getFilename().isEmpty() ? file.getFilename() : "file";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("chat_id", chatId);
            if (caption != null && !caption.isEmpty()) body.add("caption", caption);
            body.add(fileFieldName, fileResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    telegramApiUrl(method), requestEntity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Successfully sent file to Telegram chat {}", chatId);
                return parseMessageIdFromResponse(response.getBody());
            } else {
                log.error("Failed to send file to Telegram. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
                return -1;
            }
        } catch (Exception e) {
            log.error("Failed to send file via Telegram", e);
            return -1;
        }
    }

    private int parseMessageIdFromResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            log.error("Empty JSON response from Telegram");
            return 0;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);

            if (rootNode.has("ok") && rootNode.get("ok").asBoolean() &&
                    rootNode.has("result") && rootNode.get("result").has("message_id")) {
                return rootNode.get("result").get("message_id").asInt();
            } else {
                log.warn("Message ID not found in Telegram response: {}", jsonResponse);
                return 0;
            }
        } catch (Exception e) {
            log.error("Error parsing Telegram response: {}", e.getMessage());
            return 0;
        }
    }

    private boolean publishToInstagram(Post post, SocialPostTask task, SocialAccountDto account) {
        // Реализация публикации в Instagram
        return true; // Заглушка
    }

    private boolean publishToSocialPlatform(Post post, SocialPostTask task, SocialAccountDto account) {
        return switch (task.getPlatform()) {
            case TELEGRAM -> publishToTelegram(post, task, account);
            case INSTAGRAM -> publishToInstagram(post, task, account);
            default -> throw new UnsupportedOperationException("Platform not supported: " + task.getPlatform());
        };
    }

    private boolean publishToTelegram(Post post, SocialPostTask task, SocialAccountDto account) {
        try {
            String chatId = account.getExternalId();

            if (post.getMedia().isEmpty()) {
                // Текстовый пост
                return sendTelegramTextMessage(chatId, post.getContent(), task);
            } else {
                // Пост с медиа
                PostMedia firstMedia = post.getMedia().get(0);
                MediaFileDto mediaFile = mediaStorageClient.getMediaFileDetails(firstMedia.getMediaId());
                Resource mediaContent = mediaStorageClient.getMediaContent(firstMedia.getMediaId());

                // Формируем текст
                String caption = post.getContent();
                if (caption.length() > 1024) {
                    caption = caption.substring(0, 1021) + "...";
                }

                // Определяем тип файла и отправляем соответствующим методом
                String mimeType = mediaFile.getMimeType();

                if (mimeType.startsWith("image/")) {
                    return sendFile(chatId, mediaContent, caption, "sendPhoto", "photo") > -1;
                } else if (mimeType.startsWith("video/")) {
                    return sendFile(chatId, mediaContent, caption, "sendVideo", "video") > -1;
                } else {
                    return sendFile(chatId, mediaContent, caption, "sendDocument", "document") > -1;
                }
            }
        } catch (Exception e) {
            log.error("Error publishing to Telegram: {}", e.getMessage());
            task.setErrorMessage("Error publishing to Telegram: " + e.getMessage());
            return false;
        }
    }
}

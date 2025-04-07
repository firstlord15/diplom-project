package org.ithub.socialintegrationservice;

import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramSender {
    @Value("${telegram.bot-token}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();

    private String telegramApiUrl(String method) {
        return "https://api.telegram.org/bot" + botToken + "/" + method;
    }

    // Отправка простого текстового сообщения
    public void sendMessage(String chatId, String message) {
        String url = telegramApiUrl("sendMessage");
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId);
        body.add("text", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, entity, String.class);
    }

    // Отправка документа из MultipartFile
    public void sendDocument(String chatId, MultipartFile file, String caption) {
        sendFile(chatId, file, caption, "sendDocument", "document");
    }

    // Отправка фотографии из MultipartFile
    public void sendPhoto(String chatId, MultipartFile file, String caption) {
        sendFile(chatId, file, caption, "sendPhoto", "photo");
    }

    // Универсальный метод отправки файла
    private void sendFile(String chatId, MultipartFile file, String caption, String method, String fileFieldName) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        try {
            // Создаем ресурс напрямую из MultipartFile
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                @NonNull
                public String   getFilename() {
                    return file.getOriginalFilename(); // Сохраняем оригинальное имя файла
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("chat_id", chatId);
            if (caption != null) body.add("caption", caption);
            body.add(fileFieldName, fileResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(telegramApiUrl(method), requestEntity, String.class);

        } catch (IOException e) {
            log.error("Failed to send file via Telegram", e);
            throw new RuntimeException("Failed to send file via Telegram", e);
        }
    }

//    // Отправка Markdown-сообщения
//    public void sendMarkdownMessage(String chatId, String markdownText) {
//        String url = telegramApiUrl("sendMessage");
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("chat_id", chatId);
//        body.add("text", markdownText);
//        body.add("parse_mode", "Markdown");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
//        restTemplate.postForEntity(url, entity, String.class);
//    }
//
//    // Отправка HTML-сообщения
//    public void sendHtmlMessage(String chatId, String htmlText) {
//        String url = telegramApiUrl("sendMessage");
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("chat_id", chatId);
//        body.add("text", htmlText);
//        body.add("parse_mode", "HTML");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
//        restTemplate.postForEntity(url, entity, String.class);
//    }
}

package org.ithub.socialintegrationservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Component
public class TelegramClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${telegram.bot.token}")
    private String botToken;

    private String getApiUrl(String method) {
        return "https://api.telegram.org/bot" + botToken + "/" + method;
    }

    public boolean sendTextMessage(String chatId, String text) {
        String url = getApiUrl("sendMessage");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", chatId);
        params.add("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Sent text message to {}, success: {}", chatId, success);
            return success;
        } catch (Exception e) {
            log.error("Error sending message to Telegram: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean sendPhoto(String chatId, Resource photo, String caption) {
        return sendFile(chatId, photo, caption, "sendPhoto", "photo");
    }

    public boolean sendDocument(String chatId, Resource document, String caption) {
        return sendFile(chatId, document, caption, "sendDocument", "document");
    }

    private boolean sendFile(String chatId, Resource file, String caption, String method, String fileFieldName) {
        if (file == null) {
            log.error("File resource is null");
            return false;
        }

        try {
            // Читаем содержимое ресурса в байтовый массив
            byte[] fileBytes = StreamUtils.copyToByteArray(file.getInputStream());

            // Создаем ресурс из байтового массива
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return file.getFilename() != null ? file.getFilename() : "file";
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
                    getApiUrl(method), requestEntity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent file to Telegram chat {}", chatId);
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                return rootNode.has("ok") && rootNode.get("ok").asBoolean();
            } else {
                log.error("Failed to send file to Telegram. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (IOException e) {
            log.error("Failed to send file via Telegram", e);
            return false;
        }
    }
}

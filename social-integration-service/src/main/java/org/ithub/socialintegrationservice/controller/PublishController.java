package org.ithub.socialintegrationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.socialintegrationservice.dto.PublishTextRequest;
import org.ithub.socialintegrationservice.enums.SocialPlatform;
import org.ithub.socialintegrationservice.service.TelegramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/social/publish")
@RequiredArgsConstructor
public class PublishController {
    private final TelegramService telegramService;

    @PostMapping("/text")
    public ResponseEntity<Map<String, Boolean>> publishText(@RequestBody PublishTextRequest request) {
        log.info("Publishing text for user {} to platform {}", request.getUserId(), request.getPlatform());

        boolean result = false;
        if (request.getPlatform() == SocialPlatform.TELEGRAM) {
            result = telegramService.sendMessage(request.getUserId(), request.getText());
        } else {
            throw new UnsupportedOperationException("Platform not supported: " + request.getPlatform());
        }

        return ResponseEntity.ok(Map.of("success", result));
    }

    @PostMapping("/media")
    public ResponseEntity<Map<String, Boolean>> publishMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("platform") SocialPlatform platform,
            @RequestParam(value = "caption", required = false) String caption) {

        log.info("Publishing media for user {} to platform {}", userId, platform);

        boolean result = false;
        if (platform == SocialPlatform.TELEGRAM) {
            String contentType = file.getContentType();
            if (contentType.startsWith("image/")) {
                result = telegramService.sendPhoto(userId, file.getResource(), caption);
            } else {
                result = telegramService.sendDocument(userId, file.getResource(), caption);
            }
        } else {
            throw new UnsupportedOperationException("Platform not supported: " + platform);
        }

        return ResponseEntity.ok(Map.of("success", result));
    }
}

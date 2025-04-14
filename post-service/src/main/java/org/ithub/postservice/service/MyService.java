package org.ithub.postservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyService {
//    public void sendMessage(SendMessageRequest request) {
//        SocialAccount account = getAccount(request.getUserId(), request.getPlatform());
//
//        switch (request.getPlatform()) {
//            case TELEGRAM -> {
//                try {
//                    telegramSender.sendMessage(account.getExternalId(), request.getMessage());
//                } catch (Exception e) {
//                    throw new RuntimeException("Failed to send Telegram message: " + e.getMessage(), e);
//                }
//            }
//            case INSTAGRAM -> throw new UnsupportedOperationException("Instagram not yet supported");
//            default -> throw new IllegalArgumentException("Unknown platform: " + request.getPlatform());
//        }
//    }
//
//    public void sendMessage(SendMessageRequest request, MultipartFile file) {
//        SocialAccount account = getAccount(request.getUserId(), request.getPlatform());
//
//        switch (request.getPlatform()) {
//            case TELEGRAM -> {
//                try {
//                    String contentType = file.getContentType();
//                    if (contentType.startsWith("image/")) {
//                        telegramSender.sendPhoto(account.getExternalId(), file, request.getMessage());
//                    } else {
//                        telegramSender.sendDocument(account.getExternalId(), file, request.getMessage());
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException("Failed to send file to Telegram: " + e.getMessage(), e);
//                }
//            }
//            case INSTAGRAM -> throw new UnsupportedOperationException("Instagram not yet supported");
//            default -> throw new IllegalArgumentException("Unknown platform: " + request.getPlatform());
//        }
//    }
}

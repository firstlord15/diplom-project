package org.ithub.socialintegrationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.socialintegrationservice.TelegramSender;
import org.ithub.socialintegrationservice.dto.LinkSocialAccountRequest;
import org.ithub.socialintegrationservice.dto.SendMessageRequest;
import org.ithub.socialintegrationservice.enums.SocialPlatform;
import org.ithub.socialintegrationservice.model.SocialAccount;
import org.ithub.socialintegrationservice.repository.SocialAccountRepository;
import org.ithub.socialintegrationservice.util.converter.StringToSocialPlatformConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAccountService {
    private final SocialAccountRepository repository;
    private final TelegramSender telegramSender;
    private final StringToSocialPlatformConverter converter;

    public SocialAccount linkAccount(LinkSocialAccountRequest request) {
        SocialAccount socialAccount = getAccount(request.getUserId(), request.getPlatform());

        if (socialAccount != null) {
            throw new RuntimeException("Account is already linked to this platform.");
        }

        log.info("Linked account {}, for user with id {}",
                request.getExternalId(), request.getUserId());

        SocialAccount account = SocialAccount.builder()
                .userId(request.getUserId())
                .externalId(request.getExternalId())
                .accessToken(request.getAccessToken())
                .platform(request.getPlatform())
                .linkedAt(LocalDateTime.now())
                .build();

        return repository.save(account);
    }

    public SocialAccount getAccount(Long userId, SocialPlatform platform) {
        log.info("Get account with user's id {} at {}", userId, platform);
        return repository.findByUserIdAndPlatform(userId, platform)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public SocialAccount getAccount(Long userId, String platform) {
        log.info("Get account with user's id {} at {}", userId, platform);
        return repository.findByUserIdAndPlatform(userId, converter.convert(platform))
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public void deleteAccount(Long userId, String platform) {
        repository.delete(getAccount(userId, platform));
    }

    public void sendMessage(SendMessageRequest request) {
        SocialAccount account = getAccount(request.getUserId(), request.getPlatform());

        switch (request.getPlatform()) {
            case TELEGRAM -> {
                try {
                    telegramSender.sendMessage(account.getExternalId(), request.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to send Telegram message: " + e.getMessage(), e);
                }
            }
            case INSTAGRAM -> throw new UnsupportedOperationException("Instagram not yet supported");
            default -> throw new IllegalArgumentException("Unknown platform: " + request.getPlatform());
        }
    }

    public void sendMessage(SendMessageRequest request, MultipartFile file) {
        SocialAccount account = getAccount(request.getUserId(), request.getPlatform());

        switch (request.getPlatform()) {
            case TELEGRAM -> {
                try {
                    String contentType = file.getContentType();
                    if (contentType.startsWith("image/")) {
                        telegramSender.sendPhoto(account.getExternalId(), file, request.getMessage());
                    } else {
                        telegramSender.sendDocument(account.getExternalId(), file, request.getMessage());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to send file to Telegram: " + e.getMessage(), e);
                }
            }
            case INSTAGRAM -> throw new UnsupportedOperationException("Instagram not yet supported");
            default -> throw new IllegalArgumentException("Unknown platform: " + request.getPlatform());
        }
    }

}

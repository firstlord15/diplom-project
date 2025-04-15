package org.ithub.socialintegrationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.socialintegrationservice.client.TelegramClient;
import org.ithub.socialintegrationservice.enums.SocialPlatform;
import org.ithub.socialintegrationservice.model.SocialAccount;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {
    private final TelegramClient telegramClient;
    private final SocialAccountService accountService;

    public boolean sendMessage(Long userId, String text) {
        SocialAccount account = accountService.getAccount(userId, SocialPlatform.TELEGRAM);
        if (account == null) {
            log.error("No Telegram account found for user {}", userId);
            return false;
        }

        return telegramClient.sendTextMessage(account.getExternalId(), text);
    }

    public boolean sendPhoto(Long userId, Resource photo, String caption) {
        SocialAccount account = accountService.getAccount(userId, SocialPlatform.TELEGRAM);
        if (account == null) {
            log.error("No Telegram account found for user {}", userId);
            return false;
        }

        return telegramClient.sendPhoto(account.getExternalId(), photo, caption);
    }

    public boolean sendDocument(Long userId, Resource document, String caption) {
        SocialAccount account = accountService.getAccount(userId, SocialPlatform.TELEGRAM);
        if (account == null) {
            log.error("No Telegram account found for user {}", userId);
            return false;
        }

        return telegramClient.sendDocument(account.getExternalId(), document, caption);
    }
}

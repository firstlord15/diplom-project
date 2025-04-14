package org.ithub.socialintegrationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ithub.socialintegrationservice.dto.LinkSocialAccountRequest;
import org.ithub.socialintegrationservice.enums.SocialPlatform;
import org.ithub.socialintegrationservice.model.SocialAccount;
import org.ithub.socialintegrationservice.repository.SocialAccountRepository;
import org.ithub.socialintegrationservice.util.converter.StringToSocialPlatformConverter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAccountService {
    private final SocialAccountRepository repository;
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

    public List<SocialAccount> getActiveAccountsForUser(Long userId) {
        return repository.findByUserIdAndActiveTrue(userId);
    }

    public List<SocialAccount> getAccountsByPlatform(Long userId, SocialPlatform platform) {
        return repository.findByUserIdAndPlatformAndActiveTrue(userId, platform);
    }

    public List<SocialAccount> getAccountsByPlatform(Long userId, String platformStr) {
        SocialPlatform platform = SocialPlatform.valueOf(platformStr.toUpperCase());
        return getAccountsByPlatform(userId, platform);
    }

    public SocialAccount refreshToken(Long accountId) {
        SocialAccount account = repository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Аккаунт не найден"));

        // Реализовать логику обновления токена, специфичную для платформы
        // Это будет вызывать соответствующие OAuth эндпоинты

        return repository.save(account);
    }
}

package org.ithub.socialintegrationservice.service;

import jakarta.persistence.EntityNotFoundException;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAccountService {
    private final SocialAccountRepository repository;
    private final StringToSocialPlatformConverter converter;

    public SocialAccount linkAccount(LinkSocialAccountRequest request) {
        SocialAccount account = new SocialAccount();
        try {
            account = getAccount(request.getUserId(), request.getPlatform());
        } catch (Exception e) {
            log.info("Linked account {}, for user with id {}",
                    request.getExternalId(), request.getUserId());

            account = SocialAccount.builder()
                    .userId(request.getUserId())
                    .externalId(request.getExternalId())
                    .accessToken(request.getAccessToken())
                    .platform(request.getPlatform())
                    .linkedAt(LocalDateTime.now())
                    .active(request.isActive())
                    .build();
        }

        return repository.save(account);
    }

    public SocialAccount toggleAccount(Long userId, String platform) {
        log.info("Updating 'active status' from account for user {} with platform {}", userId, platform);

        // Получаем существующий аккаунт и проверяем, что он существует
        SocialAccount existingAccount = getAccount(userId, platform);
        if (existingAccount == null) {
            throw new EntityNotFoundException("Account not found for user " + userId + " with platform " + platform);
        }

        log.info("Toggling active status for user {} with platform {} (current status: {})", userId, platform, existingAccount.isActive());

        existingAccount.setActive(!existingAccount.isActive());

        SocialAccount updatedAccount = repository.save(existingAccount);
        log.info("Account {} updated successfully: active status changed to {}", updatedAccount.getId(), updatedAccount.isActive());
        return updatedAccount;
    }

    public SocialAccount getAccount(Long userId, SocialPlatform platform) {
        log.info("Get account with user's id {} at {}", userId, platform);
        return repository.findByUserIdAndPlatform(userId, platform)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public SocialAccount getAccount(Long userId, String platform) {
        log.info("Getting account for user {} with platform {}", userId, platform);

        // Попытка найти аккаунт
        Optional<SocialAccount> accountOpt = repository.findByUserIdAndPlatform(
                userId, SocialPlatform.valueOf(platform.toUpperCase())
        );

        // Если аккаунт не найден, возвращаем null или пустой объект вместо исключения
        if (accountOpt.isEmpty()) {
            log.info("No account found for user {} with platform {}", userId, platform);
            return null; // Или новый пустой аккаунт
        }

        return accountOpt.get();
    }

    public SocialAccount updateAccount(Long userId, String platform, SocialAccount newAccount) {
        log.info("Updating account for user {} with platform {}", userId, platform);

        // Получаем существующий аккаунт и проверяем, что он существует
        SocialAccount existingAccount = getAccount(userId, platform);
        if (existingAccount == null) {
            throw new EntityNotFoundException("Account not found for user " + userId + " with platform " + platform);
        }

        // Сохраняем ID и userId существующего аккаунта
        Long accountId = existingAccount.getId();
        Long accountUserId = existingAccount.getUserId();

        // Обновляем только те поля, которые можно менять
        existingAccount.setActive(newAccount.isActive());
        existingAccount.setExternalId(newAccount.getExternalId());
        existingAccount.setAccessToken(newAccount.getAccessToken());
        existingAccount.setRefreshToken(newAccount.getRefreshToken());
        existingAccount.setTokenExpiresAt(newAccount.getTokenExpiresAt());
        existingAccount.setPlatformSettings(newAccount.getPlatformSettings());

        // Проверяем, что ID и userId не изменились
        existingAccount.setId(accountId);
        existingAccount.setUserId(accountUserId);

        // Сохраняем и возвращаем обновленный аккаунт
        SocialAccount updatedAccount = repository.save(existingAccount);
        log.info("Account updated successfully: {}", updatedAccount.getId());

        return updatedAccount;
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

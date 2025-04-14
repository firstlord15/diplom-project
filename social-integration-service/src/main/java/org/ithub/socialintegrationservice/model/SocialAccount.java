package org.ithub.socialintegrationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.socialintegrationservice.enums.SocialPlatform;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private SocialPlatform platform;

    // Для Telegram это chatId, для других платформ - соответствующие идентификаторы
    private String externalId;

    // Для OAuth-платформ (Instagram, VK и т.д.)
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;

    // Дополнительные настройки для платформы в формате JSON
    @Column(columnDefinition = "TEXT")
    private String platformSettings;

    private LocalDateTime linkedAt;
    private boolean active = true;
}

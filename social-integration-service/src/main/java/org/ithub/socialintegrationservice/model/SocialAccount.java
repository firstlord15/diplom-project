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
    private SocialPlatform platform; // TELEGRAM, INSTAGRAM

    private String externalId;  // chatId или userId в Instagram
    private String accessToken; // если требуется

    private LocalDateTime linkedAt;
}

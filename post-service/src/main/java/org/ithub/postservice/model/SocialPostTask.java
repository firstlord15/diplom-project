package org.ithub.postservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.ithub.postservice.enums.SocialPlatform;
import org.ithub.postservice.enums.TaskStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "posts")
@AllArgsConstructor
@NoArgsConstructor
public class SocialPostTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    private SocialPlatform platform;

    private Long socialAccountId;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String externalPostId; // ID поста в социальной сети

    private String externalPostUrl; // URL поста в социальной сети

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime executedAt;
}

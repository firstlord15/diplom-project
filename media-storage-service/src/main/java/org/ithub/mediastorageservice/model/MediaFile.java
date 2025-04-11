package org.ithub.mediastorageservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.mediastorageservice.enums.MediaStatus;
import org.ithub.mediastorageservice.enums.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "media_files", indexes = {
        @Index(name = "idx_media_status", columnList = "status"),
        @Index(name = "idx_media_type", columnList = "mediaType"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false, unique = true)
    private String storageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status = MediaStatus.UPLOADING;

    private String uploadedBy;

    // Для изображений и видео
    private Integer width;
    private Integer height;

    // Метаданные
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "media_tags", joinColumns = @JoinColumn(name = "media_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    // Ссылки на варианты (разные размеры)
    @OneToMany(mappedBy = "mediaFile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaVariant> variants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

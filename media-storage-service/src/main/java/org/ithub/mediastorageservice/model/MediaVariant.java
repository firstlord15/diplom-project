package org.ithub.mediastorageservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_variants")
@Data
@NoArgsConstructor
public class MediaVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private MediaFile mediaFile;

    @Column(nullable = false)
    private String variantName;

    @Column(nullable = false, unique = true)
    private String storageKey;

    private Integer width;
    private Integer height;
    private Long size;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

package org.ithub.mediastorageservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.mediastorageservice.enums.MediaTagType;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_tags", indexes = {
        @Index(name = "idx_tag_name", columnList = "name")
})
@Data
@NoArgsConstructor
public class MediaTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private MediaFile mediaFile;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaTagType type;

    private LocalDateTime createdAt = LocalDateTime.now();
    private String createdBy;
}
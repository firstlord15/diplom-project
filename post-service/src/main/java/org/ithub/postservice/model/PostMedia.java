package org.ithub.postservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ithub.postservice.enums.MediaType;

@Data
@Entity
@Builder
@Table(name = "post_media")
@AllArgsConstructor
@NoArgsConstructor
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    private MediaType type;

    @Column(nullable = false)
    private Long mediaId; // ID из media-storage-service

    private String caption;

    private Integer sortOrder;
}

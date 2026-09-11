package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entity representing an official artisan accreditation, diploma, or qualification card.
 * Captures official credentials issued by institutions like the Chambre d'Artisanat et des Métiers (CAM)
 * or recognized vocational training guilds across Algeria.
 */
@Entity
@Table(
    name = "artisan_certifications",
    indexes = {
        @Index(name = "idx_cert_artisan", columnList = "artisan_id, deleted_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtisanCertification extends BaseEntity {

    /**
     * The artisan to whom this certification or qualification card was issued.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false)
    private Artisan artisan;

    /**
     * Title of the credential (e.g., Carte d'Artisan Professionnel, Diplôme de Maître Artisan).
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Issuing institution or authority (e.g., CAM Tizi Ouzou, Ministère du Tourisme et de l'Artisanat).
     */
    @Column(nullable = false, length = 255)
    private String issuer;

    /**
     * Date of official issuance.
     */
    @Column(name = "issued_at")
    private LocalDate issuedAt;

    /**
     * Date when this certification expires, if applicable.
     */
    @Column(name = "expires_at")
    private LocalDate expiresAt;

    /**
     * Stored document file access URI (PDF scan or verification image).
     */
    @Column(name = "document_url", length = 500)
    private String documentUrl;

    /**
     * Whether this credential has been reviewed and verified by platform administrators.
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;
}

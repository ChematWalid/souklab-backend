package com.project.souklab.dto.artisan;

import com.project.souklab.model.ArtisanCertification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Representation of an artisan's professional credential or accreditation card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationResponseDTO {

    private String id;
    private String title;
    private String issuer;
    private LocalDate issuedAt;
    private LocalDate expiresAt;
    private boolean isVerified;
    private String documentUrl;

    /**
     * Maps an {@link ArtisanCertification} entity into a response DTO.
     *
     * @param cert the certification entity
     * @return response DTO, or null if cert is null
     */
    public static CertificationResponseDTO from(ArtisanCertification cert) {
        if (cert == null) {
            return null;
        }
        return CertificationResponseDTO.builder()
                .id(cert.getId())
                .title(cert.getTitle())
                .issuer(cert.getIssuer())
                .issuedAt(cert.getIssuedAt())
                .expiresAt(cert.getExpiresAt())
                .isVerified(cert.isVerified())
                .documentUrl(cert.getDocumentUrl())
                .build();
    }
}

package com.project.souklab.dto.artisan;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Multipart metadata used to update an artisan certification. */
@Data
public class CertificationUpdateDTO {
    private String title;
    private String issuer;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issuedAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiresAt;
}

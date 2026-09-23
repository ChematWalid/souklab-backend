package com.project.souklab.dto.catalog.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin request DTO for creating or updating an Epoque catalog entry.
 * Extends the Technique shape with {@code periodEra} (optional historical date range).
 */
@Data
@NoArgsConstructor
public class EpoqueRequest {

    /** Display name of the epoch. Required. VARCHAR(100). */
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    /** URL-friendly slug. Optional — auto-generated from name when omitted. VARCHAR(120). */
    @Size(max = 120, message = "slug must not exceed 120 characters")
    private String slug;

    /** Historical date-range label (e.g., "XVIe – XIXe siècle"). Optional. VARCHAR(100). */
    @Size(max = 100, message = "periodEra must not exceed 100 characters")
    private String periodEra;

    /** Historical context and artistic description. Optional. TEXT. */
    private String description;

    /**
     * Explicit display ordering weight. Optional — assigned max+1 when omitted.
     * Null signals "use default" so the service can distinguish "not sent" from 0.
     */
    private Integer displayOrder;

    /** Whether this epoque appears in public catalog filters. Defaults to {@code true}. */
    private Boolean isActive;
}

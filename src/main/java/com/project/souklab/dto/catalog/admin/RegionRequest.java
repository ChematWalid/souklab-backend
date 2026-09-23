package com.project.souklab.dto.catalog.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin request DTO for creating or updating a Region entry.
 *
 * <p>Additional region-specific fields:
 * <ul>
 *   <li>{@code parentId} — optional FK to an existing active region; missing parent → 404.</li>
 *   <li>{@code code} — optional Wilaya code (e.g., "16" for Alger). VARCHAR(10).</li>
 *   <li>Circular-reference guard: if setting {@code parentId} would make this region an ancestor
 *       of itself, the service rejects the request with 422.</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class RegionRequest {

    /** Official name of the region. Required. VARCHAR(100). */
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    /** URL-friendly slug. Optional — auto-generated from name when omitted. VARCHAR(120). */
    @Size(max = 120, message = "slug must not exceed 120 characters")
    private String slug;

    /**
     * Parent region id (Wilaya for a Commune, null for a top-level Wilaya).
     * The referenced region must exist and be active, otherwise the request is rejected with 404.
     */
    private String parentId;

    /** Official Wilaya administrative code (e.g., "15", "47"). Optional. VARCHAR(10). */
    @Size(max = 10, message = "code must not exceed 10 characters")
    private String code;

    /**
     * Explicit display ordering weight. Optional — assigned max+1 when omitted.
     * Null signals "use default" so the service can distinguish "not sent" from 0.
     */
    private Integer displayOrder;

    /** Whether this region is active for artisan assignment and catalog discovery. Defaults to {@code true}. */
    private Boolean isActive;
}

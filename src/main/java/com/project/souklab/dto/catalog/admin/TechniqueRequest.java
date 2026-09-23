package com.project.souklab.dto.catalog.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin request DTO for creating or updating a Technique entry.
 *
 * <ul>
 *   <li>{@code slug} is optional: if omitted the service auto-generates it from {@code name}.</li>
 *   <li>Uniqueness violation on {@code slug} returns 409, not 422.</li>
 *   <li>{@code displayOrder} is optional: if omitted, max+1 is used.</li>
 *   <li>{@code isActive} defaults to {@code true} when not supplied.</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class TechniqueRequest {

    /** Display name of the technique. Required. VARCHAR(100). */
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    /** URL-friendly slug. Optional — auto-generated from name when omitted. VARCHAR(120). */
    @Size(max = 120, message = "slug must not exceed 120 characters")
    private String slug;

    /** Human-readable description. Optional. TEXT. */
    private String description;

    /**
     * Explicit display ordering weight. Optional — assigned max+1 when omitted.
     * Null signals "use default" so the service can distinguish "not sent" from 0.
     */
    private Integer displayOrder;

    /** Whether this technique appears in public catalog filters. Defaults to {@code true}. */
    private Boolean isActive;
}

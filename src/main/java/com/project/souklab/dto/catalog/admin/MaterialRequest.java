package com.project.souklab.dto.catalog.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin request DTO for creating or updating a Material entry.
 */
@Data
@NoArgsConstructor
public class MaterialRequest {

    /** Name of the raw crafting material. Required. VARCHAR(100). */
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    /** URL-friendly slug. Optional — auto-generated from name when omitted. VARCHAR(120). */
    @Size(max = 120, message = "slug must not exceed 120 characters")
    private String slug;

    /** Physical properties and artisanal usage description. Optional. TEXT. */
    private String description;

    /** Parent material family ID. Required on create; optional on patch. */
    private String familyId;

    /**
     * Explicit display ordering weight within parent family. Optional — assigned max+1 when omitted.
     */
    private Integer displayOrder;

    /** Whether this material is active. Defaults to {@code true}. */
    @JsonProperty("isActive")
    private Boolean isActive;

    public void setStatus(Boolean status) {
        if (this.isActive == null) {
            this.isActive = status;
        }
    }

    public void setActive(Boolean active) {
        if (this.isActive == null) {
            this.isActive = active;
        }
    }
}

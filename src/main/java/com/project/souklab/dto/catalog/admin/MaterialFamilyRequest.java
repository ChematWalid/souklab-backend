package com.project.souklab.dto.catalog.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin request DTO for creating or updating a MaterialFamily catalog entry.
 */
@Data
@NoArgsConstructor
public class MaterialFamilyRequest {

    /** Name of the raw material family. Required. VARCHAR(100). */
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    /** URL-friendly slug. Optional — auto-generated from name when omitted. VARCHAR(120). */
    @Size(max = 120, message = "slug must not exceed 120 characters")
    private String slug;

    /** Technical and contextual description of this family of substances. Optional. TEXT. */
    private String description;

    /**
     * Explicit display ordering weight. Optional — assigned max+1 when omitted.
     */
    private Integer displayOrder;

    /** Whether this material family is active. Defaults to {@code true}. */
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

package com.project.souklab.dto.catalog.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin request DTO for creating or updating a JobCategory catalog entry.
 */
@Data
@NoArgsConstructor
public class JobCategoryRequest {

    /** Display name of the craft category. Required. VARCHAR(100). */
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    /** URL-friendly slug. Optional — auto-generated from name when omitted. VARCHAR(120). */
    @Size(max = 120, message = "slug must not exceed 120 characters")
    private String slug;

    /** Comprehensive description of the craftsmanship domain. Optional. TEXT. */
    private String description;

    /** Asset URL or icon identifier representing this craft category. Optional. VARCHAR(500). */
    @Size(max = 500, message = "iconUrl must not exceed 500 characters")
    private String iconUrl;

    /**
     * Explicit display ordering weight. Optional — assigned max+1 when omitted.
     */
    private Integer displayOrder;

    /** Whether this craft category is active. Defaults to {@code true}. */
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

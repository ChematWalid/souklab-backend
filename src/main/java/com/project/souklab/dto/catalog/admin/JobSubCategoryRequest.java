package com.project.souklab.dto.catalog.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin request DTO for creating or updating a JobSubCategory catalog entry.
 */
@Data
@NoArgsConstructor
public class JobSubCategoryRequest {

    /** Specific trade or craft subcategory name. Required. VARCHAR(100). */
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    /** URL-friendly slug. Optional — auto-generated from name when omitted. VARCHAR(120). */
    @Size(max = 120, message = "slug must not exceed 120 characters")
    private String slug;

    /** Detailed overview of the trade and cultural heritage background. Optional. TEXT. */
    private String description;

    /** Parent category ID. Required on create; optional on patch. */
    private String categoryId;

    /**
     * Explicit display ordering weight within parent category. Optional — assigned max+1 when omitted.
     */
    private Integer displayOrder;

    /** Whether this trade subcategory is active. Defaults to {@code true}. */
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

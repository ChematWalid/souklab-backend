package com.project.souklab.dto.review;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Decimal rating and comment submitted for an attended formation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanReviewRequestDTO {
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("5.00")
    @Digits(integer = 1, fraction = 2)
    private BigDecimal rating;

    @NotBlank
    @Size(max = 5000)
    private String comment;
}

package com.project.souklab.dto.formation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request payload for updating an existing formation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationUpdateDTO {

    /**
     * Title or headline of the masterclass.
     */
    @NotBlank(message = "Formation title is required.")
    @Size(max = 255, message = "Formation title must not exceed 255 characters.")
    private String title;

    /**
     * Detailed narrative, objectives, and curriculum description.
     */
    @NotBlank(message = "Formation description is required.")
    private String description;

    /**
     * Physical venue or workshop address if conducted in person.
     */
    private String location;

    /**
     * Flag indicating whether the masterclass is conducted remotely online.
     */
    private Boolean isOnline;

    /**
     * Helper accessor returning primitive boolean defaulting to false when null.
     *
     * @return true if online masterclass, false otherwise
     */
    public boolean isOnline() {
        return Boolean.TRUE.equals(this.isOnline);
    }

    /**
     * Scheduled start timestamp for the masterclass.
     */
    @Future(message = "Scheduled date and time must be in the future.")
    private LocalDateTime scheduledAt;

    /**
     * Total planned duration of the training session in hours.
     */
    @Min(value = 1, message = "Duration must be at least 1 hour.")
    private int durationHours;

    /**
     * Maximum capacity of enrolled participant seats.
     */
    @Min(value = 1, message = "Max participants must be at least 1.")
    private int maxParticipants;

    /**
     * Admission fee per enrolled participant in major currency units.
     */
    @Min(value = 0, message = "Price cannot be negative.")
    private int price;

    /**
     * ISO standard currency code (default: DZD).
     */
    @Builder.Default
    private String currency = "DZD";
}

package com.project.souklab.dto.formation;
import com.project.souklab.model.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
public record AttendanceRequest(@NotNull EnrollmentStatus status) {}

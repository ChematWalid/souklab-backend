package com.project.souklab.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FormationTest {

    @Test
    void countsOnlyNonDeletedConfirmedEnrollments() {
        Formation formation = new Formation();
        assertThat(formation.getActiveEnrollmentsCount()).isZero();
        formation.setEnrollments(null);
        assertThat(formation.getActiveEnrollmentsCount()).isZero();

        FormationEnrollment confirmed = mock(FormationEnrollment.class);
        FormationEnrollment deleted = mock(FormationEnrollment.class);
        FormationEnrollment pending = mock(FormationEnrollment.class);
        org.mockito.Mockito.when(confirmed.getStatus()).thenReturn(EnrollmentStatus.CONFIRMED);
        org.mockito.Mockito.when(confirmed.getDeletedAt()).thenReturn(null);
        org.mockito.Mockito.when(deleted.getStatus()).thenReturn(EnrollmentStatus.CONFIRMED);
        org.mockito.Mockito.when(deleted.getDeletedAt()).thenReturn(java.time.LocalDateTime.now());
        org.mockito.Mockito.when(pending.getStatus()).thenReturn(EnrollmentStatus.CANCELLED);
        org.mockito.Mockito.when(pending.getDeletedAt()).thenReturn(null);
        formation.setEnrollments(java.util.List.of(confirmed, deleted, pending));

        assertThat(formation.getActiveEnrollmentsCount()).isEqualTo(1);
    }

    @Test
    void associatesAndDissociatesFilesEnrollmentsAndReviews() {
        Formation formation = new Formation();
        FormationFile file = mock(FormationFile.class);
        FormationEnrollment enrollment = mock(FormationEnrollment.class);
        FormationReview review = mock(FormationReview.class);

        formation.addFile(file);
        formation.addEnrollment(enrollment);
        formation.addReview(review);
        assertThat(formation.getFiles()).containsExactly(file);
        assertThat(formation.getEnrollments()).containsExactly(enrollment);
        assertThat(formation.getReviews()).containsExactly(review);
        verify(file).setFormation(formation);
        verify(enrollment).setFormation(formation);
        verify(review).setFormation(formation);

        formation.removeFile(file);
        assertThat(formation.getFiles()).isEmpty();
        verify(file).setFormation(null);
    }
}

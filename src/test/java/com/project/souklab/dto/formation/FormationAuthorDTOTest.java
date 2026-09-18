package com.project.souklab.dto.formation;

import com.project.souklab.model.Artisan;
import com.project.souklab.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormationAuthorDTOTest {
    @Test
    void mapsNamesWithFallbacksAndNullAuthor() {
        Artisan named = Artisan.builder().id("a1")
                .user(User.builder().email("fallback@example.com").firstName("Ada").lastName("Lovelace").build())
                .city("Algiers").isTeacher(true).build();
        Artisan fallback = Artisan.builder().id("a2")
                .user(User.builder().email("fallback@example.com").build()).build();
        Artisan withoutUser = Artisan.builder().id("a3").build();

        assertThat(FormationAuthorDTO.from(named).getName()).isEqualTo("Ada Lovelace");
        assertThat(FormationAuthorDTO.from(fallback).getName()).isEqualTo("fallback@example.com");
        assertThat(FormationAuthorDTO.from(withoutUser).getName()).isNull();
        assertThat(FormationAuthorDTO.from(null)).isNull();
    }
}

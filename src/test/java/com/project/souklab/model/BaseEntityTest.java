package com.project.souklab.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {
    @Test
    void ensureIdCreatesIdOnlyWhenMissingOrBlank() {
        User user = new User();
        user.ensureId();
        assertThat(user.getId()).isNotBlank();
        user.setId("existing");
        user.ensureId();
        assertThat(user.getId()).isEqualTo("existing");
        user.setId(" ");
        user.ensureId();
        assertThat(user.getId()).isNotBlank().isNotEqualTo(" ");
    }
}

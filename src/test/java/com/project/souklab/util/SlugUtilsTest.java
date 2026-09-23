package com.project.souklab.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SlugUtilsTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "'Filigrane en argent', 'filigrane-en-argent'",
            "'Filigrane d''argent', 'filigrane-d-argent'",
            "'Émaillage traditionnel', 'emaillage-traditionnel'",
            "'Période Zianide', 'periode-zianide'",
            "'Époque Ottomane', 'epoque-ottomane'",
            "'Tizi Ouzou', 'tizi-ouzou'",
            "'Ghardaïa', 'ghardaia'",
            "'Alger-Centre', 'alger-centre'",
            "'   Multiple   Spaces   ', 'multiple-spaces'",
            "'Special !@#$ Characters %^&*', 'special-characters'",
            "'---Leading and Trailing---', 'leading-and-trailing'",
            "'Gros œuvre', 'gros-oeuvre'",
            "'Trompe-l''œil', 'trompe-l-oeil'"
    })
    @DisplayName("toSlug should normalize accents, lowercase, and hyphenate properly")
    void toSlug_shouldNormalizeProperly(String input, String expected) {
        assertThat(SlugUtils.toSlug(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("toSlug should return empty string for null or blank input")
    void toSlug_whenNullOrBlank_shouldReturnEmpty() {
        assertThat(SlugUtils.toSlug(null)).isEmpty();
        assertThat(SlugUtils.toSlug("")).isEmpty();
        assertThat(SlugUtils.toSlug("   ")).isEmpty();
    }
}

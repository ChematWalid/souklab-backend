package com.project.souklab.dto.directory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests verifying {@link DirectorySearchFilterDTO} validation,
 * keyword sanitization, taxonomy filter detection, and pagination bounds.
 */
class DirectorySearchFilterDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("hasKeyword: should correctly evaluate null, whitespace, and non-empty keywords")
    void hasKeyword_evaluation() {
        DirectorySearchFilterDTO nullKeyword = DirectorySearchFilterDTO.builder().keyword(null).build();
        assertThat(nullKeyword.hasKeyword()).isFalse();

        DirectorySearchFilterDTO blankKeyword = DirectorySearchFilterDTO.builder().keyword("    ").build();
        assertThat(blankKeyword.hasKeyword()).isFalse();

        DirectorySearchFilterDTO populatedKeyword = DirectorySearchFilterDTO.builder().keyword("poterie").build();
        assertThat(populatedKeyword.hasKeyword()).isTrue();
    }

    @Test
    @DisplayName("getCleanKeyword: should return trimmed string or empty string for null")
    void getCleanKeyword_sanitization() {
        DirectorySearchFilterDTO nullDto = DirectorySearchFilterDTO.builder().keyword(null).build();
        assertThat(nullDto.getCleanKeyword()).isEmpty();

        DirectorySearchFilterDTO trimmedDto = DirectorySearchFilterDTO.builder().keyword("  argile rouge  ").build();
        assertThat(trimmedDto.getCleanKeyword()).isEqualTo("argile rouge");
    }

    @Test
    @DisplayName("hasTaxonomyFilters: should detect any active geographic or craft criteria")
    void hasTaxonomyFilters_detection() {
        DirectorySearchFilterDTO defaultDto = DirectorySearchFilterDTO.builder().build();
        assertThat(defaultDto.hasTaxonomyFilters()).isFalse();

        DirectorySearchFilterDTO regionDto = DirectorySearchFilterDTO.builder().regionSlug("tizi-ouzou").build();
        assertThat(regionDto.hasTaxonomyFilters()).isTrue();

        DirectorySearchFilterDTO wilayaDto = DirectorySearchFilterDTO.builder().wilayaCode("15").build();
        assertThat(wilayaDto.hasTaxonomyFilters()).isTrue();

        DirectorySearchFilterDTO categoryDto = DirectorySearchFilterDTO.builder().categorySlug("art-du-feu").build();
        assertThat(categoryDto.hasTaxonomyFilters()).isTrue();

        DirectorySearchFilterDTO subCategoryDto = DirectorySearchFilterDTO.builder().subCategorySlug("poterie-kabylie").build();
        assertThat(subCategoryDto.hasTaxonomyFilters()).isTrue();

        DirectorySearchFilterDTO materialsDto = DirectorySearchFilterDTO.builder().materials(List.of("argile")).build();
        assertThat(materialsDto.hasTaxonomyFilters()).isTrue();

        DirectorySearchFilterDTO techniquesDto = DirectorySearchFilterDTO.builder().techniques(List.of("filigrane")).build();
        assertThat(techniquesDto.hasTaxonomyFilters()).isTrue();

        DirectorySearchFilterDTO epoquesDto = DirectorySearchFilterDTO.builder().epoques(List.of("zianide")).build();
        assertThat(epoquesDto.hasTaxonomyFilters()).isTrue();
    }

    @Test
    @DisplayName("resolvePage: should default null page to zero")
    void resolvePage_handling() {
        DirectorySearchFilterDTO nullPage = DirectorySearchFilterDTO.builder().page(null).build();
        assertThat(nullPage.resolvePage()).isZero();

        DirectorySearchFilterDTO explicitPage = DirectorySearchFilterDTO.builder().page(3).build();
        assertThat(explicitPage.resolvePage()).isEqualTo(3);
        assertThat(nullPage.resolvePage(7)).isEqualTo(7);
        assertThat(explicitPage.resolvePage(7)).isEqualTo(3);
    }

    @Test
    @DisplayName("resolveSize: should enforce boundary clamping between 1 and 100")
    void resolveSize_boundaryClamping() {
        DirectorySearchFilterDTO nullSize = DirectorySearchFilterDTO.builder().size(null).build();
        assertThat(nullSize.resolveSize()).isEqualTo(20);

        DirectorySearchFilterDTO underflowSize = DirectorySearchFilterDTO.builder().size(0).build();
        assertThat(underflowSize.resolveSize()).isEqualTo(1);

        DirectorySearchFilterDTO negativeSize = DirectorySearchFilterDTO.builder().size(-10).build();
        assertThat(negativeSize.resolveSize()).isEqualTo(1);

        DirectorySearchFilterDTO validSize = DirectorySearchFilterDTO.builder().size(50).build();
        assertThat(validSize.resolveSize()).isEqualTo(50);

        DirectorySearchFilterDTO overflowSize = DirectorySearchFilterDTO.builder().size(500).build();
        assertThat(overflowSize.resolveSize()).isEqualTo(100);
        assertThat(nullSize.resolveSize(9, 2, 50)).isEqualTo(9);
        assertThat(underflowSize.resolveSize(9, 2, 50)).isEqualTo(2);
        assertThat(overflowSize.resolveSize(9, 2, 50)).isEqualTo(50);
    }

    @Test
    @DisplayName("resolveSortBy: should default null sortBy to RELEVANCE")
    void resolveSortBy_handling() {
        DirectorySearchFilterDTO nullSort = DirectorySearchFilterDTO.builder().sortBy(null).build();
        assertThat(nullSort.resolveSortBy()).isEqualTo(DirectorySortOrder.Relevance.DEFAULT);

        DirectorySearchFilterDTO explicitSort = DirectorySearchFilterDTO.builder().sortBy(DirectorySortOrder.Rating.DESC).build();
        assertThat(explicitSort.resolveSortBy()).isEqualTo(DirectorySortOrder.Rating.DESC);
    }

    @Test
    void taxonomyFiltersHandleNullCollectionsAndBlankValues() {
        DirectorySearchFilterDTO dto = DirectorySearchFilterDTO.builder()
                .regionSlug(" ").wilayaCode(null).categorySlug(" ").subCategorySlug(null)
                .materials(null).techniques(null).epoques(null).build();
        assertThat(dto.hasTaxonomyFilters()).isFalse();
        dto.setQ(" query ");
        assertThat(dto.getQ()).isEqualTo(" query ");
        assertThat(dto.hasKeyword()).isTrue();
    }

    @Test
    @DisplayName("validation: valid filter should pass bean validation with zero violations")
    void validation_withValidParameters_shouldPass() {
        DirectorySearchFilterDTO dto = DirectorySearchFilterDTO.builder()
                .keyword("céramique kabyle")
                .regionSlug("tizi-ouzou")
                .wilayaCode("15")
                .categorySlug("art-du-feu")
                .subCategorySlug("poterie")
                .minRating(4.5)
                .verifiedOnly(true)
                .premiumOnly(false)
                .teacherOnly(true)
                .sortBy(DirectorySortOrder.Views.DESC)
                .page(0)
                .size(25)
                .build();

        Set<ConstraintViolation<DirectorySearchFilterDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("validation: invalid ranges and excessive lengths should produce violations")
    void validation_withInvalidParameters_shouldViolateConstraints() {
        String excessiveKeyword = "a".repeat(121);
        DirectorySearchFilterDTO invalidDto = DirectorySearchFilterDTO.builder()
                .keyword(excessiveKeyword)
                .minRating(5.5)
                .page(-1)
                .size(150)
                .build();

        Set<ConstraintViolation<DirectorySearchFilterDTO>> violations = validator.validate(invalidDto);
        assertThat(violations).hasSize(4);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .containsExactlyInAnyOrder("keyword", "minRating", "page", "size");
    }
}

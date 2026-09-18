package com.project.souklab.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArtisanTest {

    @Test
    void resolvesAndAssignsRegionAndSubcategoryIdentifiers() {
        Artisan artisan = new Artisan();

        assertThat(artisan.getRegionId()).isNull();
        assertThat(artisan.getSubCategoryId()).isNull();

        artisan.setRegionId("region-1");
        artisan.setSubCategoryId("subcategory-1");
        assertThat(artisan.getRegionId()).isEqualTo("region-1");
        assertThat(artisan.getSubCategoryId()).isEqualTo("subcategory-1");

        artisan.setRegionId(null);
        artisan.setSubCategoryId(null);
        assertThat(artisan.getRegionId()).isNull();
        assertThat(artisan.getSubCategoryId()).isNull();
    }

    @Test
    void builderIdentifierHelpersSupportAssignmentAndClearing() {
        Artisan assigned = Artisan.builder()
                .regionId("region-2")
                .subCategoryId("subcategory-2")
                .build();
        assertThat(assigned.getRegionId()).isEqualTo("region-2");
        assertThat(assigned.getSubCategoryId()).isEqualTo("subcategory-2");

        Artisan cleared = Artisan.builder().regionId(null).subCategoryId(null).build();
        assertThat(cleared.getRegionId()).isNull();
        assertThat(cleared.getSubCategoryId()).isNull();
    }
}

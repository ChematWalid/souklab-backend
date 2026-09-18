package com.project.souklab.dto.catalog;

import com.project.souklab.model.Material;
import com.project.souklab.model.MaterialFamily;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialSummaryDTOTest {
    @Test
    void mapsMaterialWithAndWithoutFamily() {
        MaterialFamily family = MaterialFamily.builder().name("Clay").build();
        Material material = Material.builder().name("Red clay").slug("red-clay").family(family).build();
        Material withoutFamily = Material.builder().name("Wire").slug("wire").build();

        MaterialSummaryDTO result = MaterialSummaryDTO.from(material);

        assertThat(result.getName()).isEqualTo("Red clay");
        assertThat(result.getFamilyName()).isEqualTo("Clay");
        assertThat(MaterialSummaryDTO.from(withoutFamily).getFamilyName()).isNull();
        assertThat(MaterialSummaryDTO.from(null)).isNull();
    }
}

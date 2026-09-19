package com.project.souklab.dto.profile;

import com.project.souklab.model.ClientType;
import com.project.souklab.dto.common.PatchField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserPatchDTOTest {

    @Test
    void defaultsToAnEmptyPatchAndResolvesRegionAliases() {
        UserPatchDTO patch = new UserPatchDTO();

        assertThat(patch.isEmpty()).isTrue();
        assertThat(patch.resolveRegionField().isDefined()).isFalse();

        patch.setRegion(PatchField.of("region-1"));
        assertThat(patch.resolveRegionField().getValue()).isEqualTo("region-1");

        patch.setRegionId(PatchField.of("region-2"));
        assertThat(patch.resolveRegionField().getValue()).isEqualTo("region-2");

        patch.setRegionId(null);
        assertThat(patch.resolveRegionField().getValue()).isEqualTo("region-1");
        patch.setRegion(null);
        assertThat(patch.resolveRegionField().isDefined()).isFalse();
    }

    @Test
    void treatsEveryDefinedFieldAndNullFieldAsARealPatch() {
        assertThat(patchWithBio(PatchField.of("bio")).isEmpty()).isFalse();
        assertThat(patchWithCity(PatchField.of("city")).isEmpty()).isFalse();
        assertThat(patchWithAddress(PatchField.of("address")).isEmpty()).isFalse();
        assertThat(patchWithWebsite(PatchField.of("https://example.test")).isEmpty()).isFalse();
        assertThat(patchWithRegionId(PatchField.of("region")).isEmpty()).isFalse();
        assertThat(patchWithRegion(PatchField.of("legacy-region")).isEmpty()).isFalse();
        assertThat(patchWithSubCategory(PatchField.of("subcategory")).isEmpty()).isFalse();
        assertThat(patchWithMaterials(PatchField.of(List.of("material"))).isEmpty()).isFalse();
        assertThat(patchWithTechniques(PatchField.of(List.of("technique"))).isEmpty()).isFalse();
        assertThat(patchWithEpoques(PatchField.of(List.of("epoque"))).isEmpty()).isFalse();
        assertThat(patchWithCompany(PatchField.of("company")).isEmpty()).isFalse();
        assertThat(patchWithClientType(PatchField.of(ClientType.INDIVIDUAL)).isEmpty()).isFalse();
        assertThat(patchWithBio(null).isEmpty()).isTrue();
        assertThat(patchWithBio(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithCity(null).isEmpty()).isTrue();
        assertThat(patchWithCity(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithAddress(null).isEmpty()).isTrue();
        assertThat(patchWithAddress(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithWebsite(null).isEmpty()).isTrue();
        assertThat(patchWithWebsite(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithRegionId(null).isEmpty()).isTrue();
        assertThat(patchWithRegionId(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithRegion(null).isEmpty()).isTrue();
        assertThat(patchWithRegion(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithSubCategory(null).isEmpty()).isTrue();
        assertThat(patchWithSubCategory(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithMaterials(null).isEmpty()).isTrue();
        assertThat(patchWithMaterials(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithTechniques(null).isEmpty()).isTrue();
        assertThat(patchWithTechniques(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithEpoques(null).isEmpty()).isTrue();
        assertThat(patchWithEpoques(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithCompany(null).isEmpty()).isTrue();
        assertThat(patchWithCompany(PatchField.of(null)).isEmpty()).isFalse();
        assertThat(patchWithClientType(null).isEmpty()).isTrue();
        assertThat(patchWithClientType(PatchField.of(null)).isEmpty()).isFalse();
    }

    private UserPatchDTO patchWithBio(PatchField<String> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setBio(value); return patch;
    }
    private UserPatchDTO patchWithCity(PatchField<String> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setCity(value); return patch;
    }
    private UserPatchDTO patchWithAddress(PatchField<String> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setAddress(value); return patch;
    }
    private UserPatchDTO patchWithWebsite(PatchField<String> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setWebsite(value); return patch;
    }
    private UserPatchDTO patchWithRegionId(PatchField<String> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setRegionId(value); return patch;
    }
    private UserPatchDTO patchWithRegion(PatchField<String> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setRegion(value); return patch;
    }
    private UserPatchDTO patchWithSubCategory(PatchField<String> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setSubCategoryId(value); return patch;
    }
    private UserPatchDTO patchWithMaterials(PatchField<List<String>> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setMaterialIds(value); return patch;
    }
    private UserPatchDTO patchWithTechniques(PatchField<List<String>> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setTechniqueIds(value); return patch;
    }
    private UserPatchDTO patchWithEpoques(PatchField<List<String>> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setEpoqueIds(value); return patch;
    }
    private UserPatchDTO patchWithCompany(PatchField<String> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setCompanyName(value); return patch;
    }
    private UserPatchDTO patchWithClientType(PatchField<ClientType> value) {
        UserPatchDTO patch = new UserPatchDTO(); patch.setClientType(value); return patch;
    }
}

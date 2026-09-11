package com.project.souklab.service.catalog;

import com.project.souklab.dao.EpoqueRepository;
import com.project.souklab.dao.JobCategoryRepository;
import com.project.souklab.dao.JobSubCategoryRepository;
import com.project.souklab.dao.MaterialFamilyRepository;
import com.project.souklab.dao.MaterialRepository;
import com.project.souklab.dao.RegionRepository;
import com.project.souklab.dao.TechniqueRepository;
import com.project.souklab.dto.catalog.EpoqueDTO;
import com.project.souklab.dto.catalog.JobCategoryDTO;
import com.project.souklab.dto.catalog.MaterialFamilyDTO;
import com.project.souklab.dto.catalog.RegionDTO;
import com.project.souklab.dto.catalog.TechniqueDTO;
import com.project.souklab.model.Epoque;
import com.project.souklab.model.JobCategory;
import com.project.souklab.model.JobSubCategory;
import com.project.souklab.model.Material;
import com.project.souklab.model.MaterialFamily;
import com.project.souklab.model.Region;
import com.project.souklab.model.Technique;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CatalogService}.
 * Verifies repository delegation, hierarchical parent-child assembly,
 * and entity-to-DTO conversion across all reference taxonomy domains.
 */
@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private JobCategoryRepository jobCategoryRepository;

    @Mock
    private JobSubCategoryRepository jobSubCategoryRepository;

    @Mock
    private MaterialFamilyRepository materialFamilyRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private EpoqueRepository epoqueRepository;

    @Mock
    private TechniqueRepository techniqueRepository;

    @InjectMocks
    private CatalogService catalogService;

    /**
     * Verifies that getAllRegions populates Wilayas with their corresponding child Communes.
     */
    @Test
    @DisplayName("getAllRegions: retrieves top-level Wilayas and populates child Communes in hierarchical DTOs")
    void getAllRegions_shouldAssembleHierarchicalWilayaAndCommunes() {
        Region wilaya = Region.builder()
            .name("Tizi Ouzou")
            .slug("tizi-ouzou")
            .code("15")
            .displayOrder(15)
            .isActive(true)
            .build();
        wilaya.setId("reg-15");

        Region commune = Region.builder()
            .name("Beni Yenni")
            .slug("beni-yenni")
            .displayOrder(1)
            .isActive(true)
            .parent(wilaya)
            .build();
        commune.setId("com-1");

        when(regionRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc())
            .thenReturn(List.of(wilaya));
        when(regionRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc("reg-15"))
            .thenReturn(List.of(commune));

        List<RegionDTO> result = catalogService.getAllRegions();

        assertThat(result).hasSize(1);
        RegionDTO wilayaDTO = result.get(0);
        assertThat(wilayaDTO.getId()).isEqualTo("reg-15");
        assertThat(wilayaDTO.getName()).isEqualTo("Tizi Ouzou");
        assertThat(wilayaDTO.getSlug()).isEqualTo("tizi-ouzou");
        assertThat(wilayaDTO.getCode()).isEqualTo("15");
        assertThat(wilayaDTO.getDisplayOrder()).isEqualTo(15);
        assertThat(wilayaDTO.getChildren()).hasSize(1);

        RegionDTO communeDTO = wilayaDTO.getChildren().get(0);
        assertThat(communeDTO.getId()).isEqualTo("com-1");
        assertThat(communeDTO.getName()).isEqualTo("Beni Yenni");
        assertThat(communeDTO.getSlug()).isEqualTo("beni-yenni");

        verify(regionRepository).findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        verify(regionRepository).findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc("reg-15");
    }

    /**
     * Verifies that getAllRegions returns an empty list when no top-level regions are present.
     */
    @Test
    @DisplayName("getAllRegions: returns empty list when repository returns empty")
    void getAllRegions_whenEmpty_shouldReturnEmptyList() {
        when(regionRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc())
            .thenReturn(Collections.emptyList());

        List<RegionDTO> result = catalogService.getAllRegions();

        assertThat(result).isEmpty();
        verify(regionRepository).findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Verifies that getAllCategories assembles parent categories with child subcategories.
     */
    @Test
    @DisplayName("getAllCategories: retrieves active categories and populates nested subcategories")
    void getAllCategories_shouldAssembleCategoryAndSubCategories() {
        JobCategory category = JobCategory.builder()
            .name("Métiers du Bois")
            .slug("metiers-du-bois")
            .description("Artisanat du bois")
            .iconUrl("wood.png")
            .displayOrder(1)
            .isActive(true)
            .build();
        category.setId("cat-1");

        JobSubCategory subCategory = JobSubCategory.builder()
            .name("Ébénisterie Traditionnelle")
            .slug("ebenisterie-traditionnelle")
            .description("Menuiserie fine")
            .displayOrder(1)
            .isActive(true)
            .category(category)
            .build();
        subCategory.setId("sub-1");

        when(jobCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc())
            .thenReturn(List.of(category));
        when(jobSubCategoryRepository.findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc("cat-1"))
            .thenReturn(List.of(subCategory));

        List<JobCategoryDTO> result = catalogService.getAllCategories();

        assertThat(result).hasSize(1);
        JobCategoryDTO catDTO = result.get(0);
        assertThat(catDTO.getId()).isEqualTo("cat-1");
        assertThat(catDTO.getName()).isEqualTo("Métiers du Bois");
        assertThat(catDTO.getSlug()).isEqualTo("metiers-du-bois");
        assertThat(catDTO.getDescription()).isEqualTo("Artisanat du bois");
        assertThat(catDTO.getIconUrl()).isEqualTo("wood.png");
        assertThat(catDTO.getSubCategories()).hasSize(1);

        assertThat(catDTO.getSubCategories().get(0).getId()).isEqualTo("sub-1");
        assertThat(catDTO.getSubCategories().get(0).getSlug()).isEqualTo("ebenisterie-traditionnelle");
        assertThat(catDTO.getSubCategories().get(0).getCategoryId()).isEqualTo("cat-1");

        verify(jobCategoryRepository).findByIsActiveTrueOrderByDisplayOrderAsc();
        verify(jobSubCategoryRepository).findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc("cat-1");
    }

    /**
     * Verifies that getAllMaterials assembles material families with child materials.
     */
    @Test
    @DisplayName("getAllMaterials: retrieves material families and populates nested raw materials")
    void getAllMaterials_shouldAssembleFamilyAndMaterials() {
        MaterialFamily family = MaterialFamily.builder()
            .name("Terres & Argiles")
            .slug("terres-et-argiles")
            .description("Argiles naturelles")
            .displayOrder(1)
            .isActive(true)
            .build();
        family.setId("fam-1");

        Material material = Material.builder()
            .name("Argile Rouge de Kabylie")
            .slug("argile-rouge-kabylie")
            .description("Argile locale")
            .displayOrder(1)
            .isActive(true)
            .family(family)
            .build();
        material.setId("mat-1");

        when(materialFamilyRepository.findByIsActiveTrueOrderByDisplayOrderAsc())
            .thenReturn(List.of(family));
        when(materialRepository.findByFamilyIdAndIsActiveTrueOrderByDisplayOrderAsc("fam-1"))
            .thenReturn(List.of(material));

        List<MaterialFamilyDTO> result = catalogService.getAllMaterials();

        assertThat(result).hasSize(1);
        MaterialFamilyDTO familyDTO = result.get(0);
        assertThat(familyDTO.getId()).isEqualTo("fam-1");
        assertThat(familyDTO.getName()).isEqualTo("Terres & Argiles");
        assertThat(familyDTO.getSlug()).isEqualTo("terres-et-argiles");
        assertThat(familyDTO.getMaterials()).hasSize(1);

        assertThat(familyDTO.getMaterials().get(0).getId()).isEqualTo("mat-1");
        assertThat(familyDTO.getMaterials().get(0).getSlug()).isEqualTo("argile-rouge-kabylie");
        assertThat(familyDTO.getMaterials().get(0).getFamilyId()).isEqualTo("fam-1");

        verify(materialFamilyRepository).findByIsActiveTrueOrderByDisplayOrderAsc();
        verify(materialRepository).findByFamilyIdAndIsActiveTrueOrderByDisplayOrderAsc("fam-1");
    }

    /**
     * Verifies that getAllEpoques correctly transforms entities into EpoqueDTO instances.
     */
    @Test
    @DisplayName("getAllEpoques: retrieves and maps historical eras")
    void getAllEpoques_shouldMapEpoqueDTOs() {
        Epoque epoque = Epoque.builder()
            .name("Période Numide")
            .slug("periode-numide")
            .periodEra("IIIe siècle av. J.-C.")
            .description("Royaume numide antique")
            .displayOrder(1)
            .isActive(true)
            .build();
        epoque.setId("ep-1");

        when(epoqueRepository.findByIsActiveTrueOrderByDisplayOrderAsc())
            .thenReturn(List.of(epoque));

        List<EpoqueDTO> result = catalogService.getAllEpoques();

        assertThat(result).hasSize(1);
        EpoqueDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo("ep-1");
        assertThat(dto.getName()).isEqualTo("Période Numide");
        assertThat(dto.getSlug()).isEqualTo("periode-numide");
        assertThat(dto.getPeriodEra()).isEqualTo("IIIe siècle av. J.-C.");
        assertThat(dto.getDescription()).isEqualTo("Royaume numide antique");
        assertThat(dto.getDisplayOrder()).isEqualTo(1);

        verify(epoqueRepository).findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Verifies that getAllTechniques correctly transforms entities into TechniqueDTO instances.
     */
    @Test
    @DisplayName("getAllTechniques: retrieves and maps craftsmanship techniques")
    void getAllTechniques_shouldMapTechniqueDTOs() {
        Technique technique = Technique.builder()
            .name("Filigrane en argent")
            .slug("filigrane-argent")
            .description("Artisanat traditionnel du bijou")
            .displayOrder(1)
            .isActive(true)
            .build();
        technique.setId("tech-1");

        when(techniqueRepository.findByIsActiveTrueOrderByDisplayOrderAsc())
            .thenReturn(List.of(technique));

        List<TechniqueDTO> result = catalogService.getAllTechniques();

        assertThat(result).hasSize(1);
        TechniqueDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo("tech-1");
        assertThat(dto.getName()).isEqualTo("Filigrane en argent");
        assertThat(dto.getSlug()).isEqualTo("filigrane-argent");
        assertThat(dto.getDescription()).isEqualTo("Artisanat traditionnel du bijou");
        assertThat(dto.getDisplayOrder()).isEqualTo(1);

        verify(techniqueRepository).findByIsActiveTrueOrderByDisplayOrderAsc();
    }
}

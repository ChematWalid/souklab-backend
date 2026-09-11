package com.project.souklab.controller.catalog;

import com.project.souklab.controller.support.ControllerSliceTest;
import com.project.souklab.dto.catalog.EpoqueDTO;
import com.project.souklab.dto.catalog.JobCategoryDTO;
import com.project.souklab.dto.catalog.JobSubCategoryDTO;
import com.project.souklab.dto.catalog.MaterialDTO;
import com.project.souklab.dto.catalog.MaterialFamilyDTO;
import com.project.souklab.dto.catalog.RegionDTO;
import com.project.souklab.dto.catalog.TechniqueDTO;
import com.project.souklab.service.catalog.CatalogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice test verifying public catalog endpoints under /api/v1/catalog.
 * Validates HTTP 200 responses, public accessibility without credentials,
 * ApiResponse envelopes, and empty collection handling.
 */
@ControllerSliceTest(controllers = CatalogController.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogService catalogService;

    /**
     * Verifies GET /api/v1/catalog/regions returns hierarchical Wilayas and child Communes with 200 OK.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/regions: returns 200 OK with hierarchical regions payload without authentication")
    void getRegions_shouldReturn200OkWithHierarchicalData() throws Exception {
        RegionDTO commune = RegionDTO.builder()
            .id("commune-1")
            .name("Beni Yenni")
            .slug("beni-yenni")
            .code(null)
            .displayOrder(1)
            .children(Collections.emptyList())
            .build();

        RegionDTO wilaya = RegionDTO.builder()
            .id("wilaya-15")
            .name("Tizi Ouzou")
            .slug("tizi-ouzou")
            .code("15")
            .displayOrder(15)
            .children(List.of(commune))
            .build();

        when(catalogService.getAllRegions()).thenReturn(List.of(wilaya));

        mockMvc.perform(get("/api/v1/catalog/regions")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].id").value("wilaya-15"))
            .andExpect(jsonPath("$.data[0].name").value("Tizi Ouzou"))
            .andExpect(jsonPath("$.data[0].slug").value("tizi-ouzou"))
            .andExpect(jsonPath("$.data[0].code").value("15"))
            .andExpect(jsonPath("$.data[0].children[0].id").value("commune-1"))
            .andExpect(jsonPath("$.data[0].children[0].name").value("Beni Yenni"))
            .andExpect(jsonPath("$.data[0].children[0].slug").value("beni-yenni"));

        verify(catalogService).getAllRegions();
    }

    /**
     * Verifies GET /api/v1/catalog/regions returns 200 OK with empty array when no regions exist.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/regions: returns 200 OK with empty array when no regions exist")
    void getRegions_whenEmpty_shouldReturn200OkWithEmptyList() throws Exception {
        when(catalogService.getAllRegions()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/catalog/regions")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());

        verify(catalogService).getAllRegions();
    }

    /**
     * Verifies GET /api/v1/catalog/categories returns 200 OK with categories and nested subcategories.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/categories: returns 200 OK with categories and subcategories")
    void getCategories_shouldReturn200OkWithSubCategories() throws Exception {
        JobSubCategoryDTO subCat = JobSubCategoryDTO.builder()
            .id("sub-1")
            .name("Ébénisterie")
            .slug("ebenisterie")
            .description("Travail fin du bois")
            .displayOrder(1)
            .categoryId("cat-1")
            .build();

        JobCategoryDTO category = JobCategoryDTO.builder()
            .id("cat-1")
            .name("Métiers du Bois")
            .slug("metiers-du-bois")
            .description("Artisanat du bois")
            .iconUrl("icon.png")
            .displayOrder(1)
            .subCategories(List.of(subCat))
            .build();

        when(catalogService.getAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/v1/catalog/categories")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].id").value("cat-1"))
            .andExpect(jsonPath("$.data[0].slug").value("metiers-du-bois"))
            .andExpect(jsonPath("$.data[0].subCategories[0].id").value("sub-1"))
            .andExpect(jsonPath("$.data[0].subCategories[0].slug").value("ebenisterie"))
            .andExpect(jsonPath("$.data[0].subCategories[0].categoryId").value("cat-1"));

        verify(catalogService).getAllCategories();
    }

    /**
     * Verifies GET /api/v1/catalog/categories returns 200 OK with empty array when no categories exist.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/categories: returns 200 OK with empty array when empty")
    void getCategories_whenEmpty_shouldReturn200OkWithEmptyList() throws Exception {
        when(catalogService.getAllCategories()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/catalog/categories")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());

        verify(catalogService).getAllCategories();
    }

    /**
     * Verifies GET /api/v1/catalog/materials returns 200 OK with material families and nested materials.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/materials: returns 200 OK with material families and materials")
    void getMaterials_shouldReturn200OkWithMaterials() throws Exception {
        MaterialDTO mat = MaterialDTO.builder()
            .id("mat-1")
            .name("Argile Rouge")
            .slug("argile-rouge")
            .description("Terre naturelle")
            .displayOrder(1)
            .familyId("fam-1")
            .build();

        MaterialFamilyDTO family = MaterialFamilyDTO.builder()
            .id("fam-1")
            .name("Terres & Argiles")
            .slug("terres-et-argiles")
            .description("Matières premières minérales")
            .displayOrder(1)
            .materials(List.of(mat))
            .build();

        when(catalogService.getAllMaterials()).thenReturn(List.of(family));

        mockMvc.perform(get("/api/v1/catalog/materials")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].id").value("fam-1"))
            .andExpect(jsonPath("$.data[0].slug").value("terres-et-argiles"))
            .andExpect(jsonPath("$.data[0].materials[0].id").value("mat-1"))
            .andExpect(jsonPath("$.data[0].materials[0].slug").value("argile-rouge"))
            .andExpect(jsonPath("$.data[0].materials[0].familyId").value("fam-1"));

        verify(catalogService).getAllMaterials();
    }

    /**
     * Verifies GET /api/v1/catalog/materials returns 200 OK with empty array when empty.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/materials: returns 200 OK with empty array when empty")
    void getMaterials_whenEmpty_shouldReturn200OkWithEmptyList() throws Exception {
        when(catalogService.getAllMaterials()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/catalog/materials")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());

        verify(catalogService).getAllMaterials();
    }

    /**
     * Verifies GET /api/v1/catalog/epoques returns 200 OK with historical epochs list.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/epoques: returns 200 OK with epochs payload")
    void getEpoques_shouldReturn200OkWithEpochs() throws Exception {
        EpoqueDTO epoque = EpoqueDTO.builder()
            .id("epoque-1")
            .name("Période Numide")
            .slug("periode-numide")
            .periodEra("IIIe siècle av. J.-C.")
            .description("Époque royale numide")
            .displayOrder(1)
            .build();

        when(catalogService.getAllEpoques()).thenReturn(List.of(epoque));

        mockMvc.perform(get("/api/v1/catalog/epoques")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].id").value("epoque-1"))
            .andExpect(jsonPath("$.data[0].name").value("Période Numide"))
            .andExpect(jsonPath("$.data[0].slug").value("periode-numide"))
            .andExpect(jsonPath("$.data[0].periodEra").value("IIIe siècle av. J.-C."));

        verify(catalogService).getAllEpoques();
    }

    /**
     * Verifies GET /api/v1/catalog/epoques returns 200 OK with empty array when empty.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/epoques: returns 200 OK with empty array when empty")
    void getEpoques_whenEmpty_shouldReturn200OkWithEmptyList() throws Exception {
        when(catalogService.getAllEpoques()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/catalog/epoques")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());

        verify(catalogService).getAllEpoques();
    }

    /**
     * Verifies GET /api/v1/catalog/techniques returns 200 OK with craftsmanship techniques list.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/techniques: returns 200 OK with techniques payload")
    void getTechniques_shouldReturn200OkWithTechniques() throws Exception {
        TechniqueDTO technique = TechniqueDTO.builder()
            .id("tech-1")
            .name("Filigrane en argent")
            .slug("filigrane-en-argent")
            .description("Tressage de fils d'argent")
            .displayOrder(1)
            .build();

        when(catalogService.getAllTechniques()).thenReturn(List.of(technique));

        mockMvc.perform(get("/api/v1/catalog/techniques")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].id").value("tech-1"))
            .andExpect(jsonPath("$.data[0].name").value("Filigrane en argent"))
            .andExpect(jsonPath("$.data[0].slug").value("filigrane-en-argent"));

        verify(catalogService).getAllTechniques();
    }

    /**
     * Verifies GET /api/v1/catalog/techniques returns 200 OK with empty array when empty.
     */
    @Test
    @DisplayName("GET /api/v1/catalog/techniques: returns 200 OK with empty array when empty")
    void getTechniques_whenEmpty_shouldReturn200OkWithEmptyList() throws Exception {
        when(catalogService.getAllTechniques()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/catalog/techniques")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());

        verify(catalogService).getAllTechniques();
    }
}

package com.project.souklab.controller.catalog;

import com.project.souklab.dto.catalog.EpoqueDTO;
import com.project.souklab.dto.catalog.JobCategoryDTO;
import com.project.souklab.dto.catalog.MaterialFamilyDTO;
import com.project.souklab.dto.catalog.RegionDTO;
import com.project.souklab.dto.catalog.TechniqueDTO;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.service.catalog.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public REST controller exposing reference catalog taxonomy endpoints.
 * Provides public read access to Algerian Wilayas and Communes, Craft Categories and Subcategories,
 * Material Families and Materials, Historical Epochs, and Craftsmanship Techniques.
 */
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    /**
     * Retrieves the complete hierarchical list of Algerian Wilayas with their nested child Communes.
     *
     * @return 200 OK with a list of RegionDTO
     */
    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<RegionDTO>>> getRegions() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAllRegions()));
    }

    /**
     * Retrieves the two-tier craftsmanship category taxonomy including nested subcategories.
     *
     * @return 200 OK with a list of JobCategoryDTO
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<JobCategoryDTO>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAllCategories()));
    }

    /**
     * Retrieves the raw crafting materials taxonomy grouped by material family.
     *
     * @return 200 OK with a list of MaterialFamilyDTO
     */
    @GetMapping("/materials")
    public ResponseEntity<ApiResponse<List<MaterialFamilyDTO>>> getMaterials() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAllMaterials()));
    }

    /**
     * Retrieves all traditional and historical Algerian epochs and cultural periods.
     *
     * @return 200 OK with a list of EpoqueDTO
     */
    @GetMapping("/epoques")
    public ResponseEntity<ApiResponse<List<EpoqueDTO>>> getEpoques() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAllEpoques()));
    }

    /**
     * Retrieves all traditional craftsmanship methods and artisanal techniques.
     *
     * @return 200 OK with a list of TechniqueDTO
     */
    @GetMapping("/techniques")
    public ResponseEntity<ApiResponse<List<TechniqueDTO>>> getTechniques() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAllTechniques()));
    }
}

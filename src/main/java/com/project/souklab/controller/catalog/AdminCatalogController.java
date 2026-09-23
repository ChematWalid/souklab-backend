package com.project.souklab.controller.catalog;

import com.project.souklab.dto.catalog.EpoqueDTO;
import com.project.souklab.dto.catalog.JobCategoryDTO;
import com.project.souklab.dto.catalog.JobSubCategoryDTO;
import com.project.souklab.dto.catalog.MaterialDTO;
import com.project.souklab.dto.catalog.MaterialFamilyDTO;
import com.project.souklab.dto.catalog.RegionDTO;
import com.project.souklab.dto.catalog.TechniqueDTO;
import com.project.souklab.dto.catalog.admin.EpoqueRequest;
import com.project.souklab.dto.catalog.admin.JobCategoryRequest;
import com.project.souklab.dto.catalog.admin.JobSubCategoryRequest;
import com.project.souklab.dto.catalog.admin.MaterialFamilyRequest;
import com.project.souklab.dto.catalog.admin.MaterialRequest;
import com.project.souklab.dto.catalog.admin.RegionRequest;
import com.project.souklab.dto.catalog.admin.TechniqueRequest;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.service.catalog.AdminCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST controller for write operations on the three static catalog taxonomies:
 * Technique, Epoque, and Region.
 *
 * <p>All endpoints require the {@code permission:admin:catalog} capability.
 * Authorization is enforced at the class level via Spring Method Security,
 * matching the {@code @accessControl} predicate pattern used by all other admin controllers.
 *
 * <p>Response conventions:
 * <ul>
 *   <li>POST  → 201 Created  with the created resource in {@code data}</li>
 *   <li>PUT   → 200 OK       with the updated resource in {@code data}</li>
 *   <li>PATCH → 200 OK       with the updated resource in {@code data} (partial update)</li>
 *   <li>DELETE → 200 OK      with {@code data: null}</li>
 * </ul>
 *
 * <p>PATCH shares the same request DTO as PUT. Null fields in the body are treated as
 * "leave unchanged" by the service layer (merge-patch semantics).
 */
@RestController
@RequestMapping("/api/v1/admin/catalog")
@RequiredArgsConstructor
@PreAuthorize("@accessControl.canManageCatalog(authentication)")
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    // ─── Technique ────────────────────────────────────────────────────────────

    /**
     * Creates a new craft technique catalog entry.
     *
     * @param request validated technique create request
     * @return 201 Created with the persisted technique as {@link TechniqueDTO}
     */
    @PostMapping("/techniques")
    public ResponseEntity<ApiResponse<TechniqueDTO>> createTechnique(
            @Valid @RequestBody TechniqueRequest request) {
        TechniqueDTO created = adminCatalogService.createTechnique(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Technique created successfully"));
    }

    /**
     * Fully replaces an existing craft technique (PUT — all fields replaced).
     *
     * @param id      technique id
     * @param request validated technique update request
     * @return 200 OK with the updated {@link TechniqueDTO}
     */
    @PutMapping("/techniques/{id}")
    public ResponseEntity<ApiResponse<TechniqueDTO>> updateTechnique(
            @PathVariable String id,
            @Valid @RequestBody TechniqueRequest request) {
        TechniqueDTO updated = adminCatalogService.updateTechnique(id, request, false);
        return ResponseEntity.ok(ApiResponse.success(updated, "Technique updated successfully"));
    }

    /**
     * Partially updates a craft technique (PATCH — null fields are preserved).
     * Typical uses: toggle {@code isActive}, reorder {@code displayOrder}.
     *
     * @param id      technique id
     * @param request partial technique patch request
     * @return 200 OK with the updated {@link TechniqueDTO}
     */
    @PatchMapping("/techniques/{id}")
    public ResponseEntity<ApiResponse<TechniqueDTO>> patchTechnique(
            @PathVariable String id,
            @RequestBody TechniqueRequest request) {
        TechniqueDTO updated = adminCatalogService.updateTechnique(id, request, true);
        return ResponseEntity.ok(ApiResponse.success(updated, "Technique updated successfully"));
    }

    /**
     * Deletes a craft technique if no artisans reference it.
     * Returns 409 Conflict if artisan references exist — deactivate via PATCH instead.
     *
     * @param id technique id
     * @return 200 OK with {@code data: null}
     */
    @DeleteMapping("/techniques/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTechnique(@PathVariable String id) {
        adminCatalogService.deleteTechnique(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Technique deleted successfully"));
    }

    // ─── Epoque ───────────────────────────────────────────────────────────────

    /**
     * Creates a new historical epoch catalog entry.
     *
     * @param request validated epoque create request
     * @return 201 Created with the persisted epoque as {@link EpoqueDTO}
     */
    @PostMapping("/epoques")
    public ResponseEntity<ApiResponse<EpoqueDTO>> createEpoque(
            @Valid @RequestBody EpoqueRequest request) {
        EpoqueDTO created = adminCatalogService.createEpoque(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Epoque created successfully"));
    }

    /**
     * Fully replaces an existing historical epoch (PUT — all fields replaced).
     *
     * @param id      epoque id
     * @param request validated epoque update request
     * @return 200 OK with the updated {@link EpoqueDTO}
     */
    @PutMapping("/epoques/{id}")
    public ResponseEntity<ApiResponse<EpoqueDTO>> updateEpoque(
            @PathVariable String id,
            @Valid @RequestBody EpoqueRequest request) {
        EpoqueDTO updated = adminCatalogService.updateEpoque(id, request, false);
        return ResponseEntity.ok(ApiResponse.success(updated, "Epoque updated successfully"));
    }

    /**
     * Partially updates a historical epoch (PATCH — null fields are preserved).
     *
     * @param id      epoque id
     * @param request partial epoque patch request
     * @return 200 OK with the updated {@link EpoqueDTO}
     */
    @PatchMapping("/epoques/{id}")
    public ResponseEntity<ApiResponse<EpoqueDTO>> patchEpoque(
            @PathVariable String id,
            @RequestBody EpoqueRequest request) {
        EpoqueDTO updated = adminCatalogService.updateEpoque(id, request, true);
        return ResponseEntity.ok(ApiResponse.success(updated, "Epoque updated successfully"));
    }

    /**
     * Deletes a historical epoch if no artisans reference it.
     * Returns 409 Conflict if artisan references exist — deactivate via PATCH instead.
     *
     * @param id epoque id
     * @return 200 OK with {@code data: null}
     */
    @DeleteMapping("/epoques/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEpoque(@PathVariable String id) {
        adminCatalogService.deleteEpoque(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Epoque deleted successfully"));
    }

    // ─── Region ───────────────────────────────────────────────────────────────

    /**
     * Creates a new administrative region (Wilaya or Commune).
     * Optional {@code parentId} links the new region to a parent Wilaya.
     *
     * @param request validated region create request
     * @return 201 Created with the persisted region as {@link RegionDTO}
     */
    @PostMapping("/regions")
    public ResponseEntity<ApiResponse<RegionDTO>> createRegion(
            @Valid @RequestBody RegionRequest request) {
        RegionDTO created = adminCatalogService.createRegion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Region created successfully"));
    }

    /**
     * Fully replaces an existing region entry (PUT — all fields replaced).
     *
     * @param id      region id
     * @param request validated region update request
     * @return 200 OK with the updated {@link RegionDTO}
     */
    @PutMapping("/regions/{id}")
    public ResponseEntity<ApiResponse<RegionDTO>> updateRegion(
            @PathVariable String id,
            @Valid @RequestBody RegionRequest request) {
        RegionDTO updated = adminCatalogService.updateRegion(id, request, false);
        return ResponseEntity.ok(ApiResponse.success(updated, "Region updated successfully"));
    }

    /**
     * Partially updates a region (PATCH — null fields are preserved).
     * Typical uses: toggle {@code isActive}, change {@code displayOrder}.
     *
     * @param id      region id
     * @param request partial region patch request
     * @return 200 OK with the updated {@link RegionDTO}
     */
    @PatchMapping("/regions/{id}")
    public ResponseEntity<ApiResponse<RegionDTO>> patchRegion(
            @PathVariable String id,
            @RequestBody RegionRequest request) {
        RegionDTO updated = adminCatalogService.updateRegion(id, request, true);
        return ResponseEntity.ok(ApiResponse.success(updated, "Region updated successfully"));
    }

    /**
     * Deletes a region if it has no child regions.
     * Returns 409 Conflict if child regions exist — delete or reassign them first,
     * or deactivate the region via PATCH.
     *
     * @param id region id
     * @return 200 OK with {@code data: null}
     */
    @DeleteMapping("/regions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRegion(@PathVariable String id) {
        adminCatalogService.deleteRegion(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Region deleted successfully"));
    }

    // ─── Category ─────────────────────────────────────────────────────────────

    /**
     * Creates a new craft category catalog entry.
     *
     * @param request validated category create request
     * @return 201 Created with the persisted {@link JobCategoryDTO}
     */
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<JobCategoryDTO>> createCategory(
            @Valid @RequestBody JobCategoryRequest request) {
        JobCategoryDTO created = adminCatalogService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Category created successfully"));
    }

    /**
     * Fully replaces an existing craft category (PUT).
     *
     * @param id      category id
     * @param request validated category update request
     * @return 200 OK with the updated {@link JobCategoryDTO}
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<JobCategoryDTO>> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody JobCategoryRequest request) {
        JobCategoryDTO updated = adminCatalogService.updateCategory(id, request, false);
        return ResponseEntity.ok(ApiResponse.success(updated, "Category updated successfully"));
    }

    /**
     * Partially updates a craft category or its status (PATCH).
     *
     * @param id      category id
     * @param request partial category patch request
     * @return 200 OK with the updated {@link JobCategoryDTO}
     */
    @PatchMapping({"/categories/{id}", "/categories/{id}/status"})
    public ResponseEntity<ApiResponse<JobCategoryDTO>> patchCategory(
            @PathVariable String id,
            @RequestBody JobCategoryRequest request) {
        JobCategoryDTO updated = adminCatalogService.updateCategory(id, request, true);
        return ResponseEntity.ok(ApiResponse.success(updated, "Category updated successfully"));
    }

    /**
     * Deletes a craft category if it has no child subcategories.
     *
     * @param id category id
     * @return 200 OK with {@code data: null}
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
        adminCatalogService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }

    // ─── SubCategory ──────────────────────────────────────────────────────────

    /**
     * Creates a new craft trade subcategory.
     *
     * @param request validated subcategory create request
     * @return 201 Created with the persisted {@link JobSubCategoryDTO}
     */
    @PostMapping("/subcategories")
    public ResponseEntity<ApiResponse<JobSubCategoryDTO>> createSubCategory(
            @Valid @RequestBody JobSubCategoryRequest request) {
        JobSubCategoryDTO created = adminCatalogService.createSubCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Subcategory created successfully"));
    }

    /**
     * Fully replaces an existing craft subcategory (PUT).
     *
     * @param id      subcategory id
     * @param request validated subcategory update request
     * @return 200 OK with the updated {@link JobSubCategoryDTO}
     */
    @PutMapping("/subcategories/{id}")
    public ResponseEntity<ApiResponse<JobSubCategoryDTO>> updateSubCategory(
            @PathVariable String id,
            @Valid @RequestBody JobSubCategoryRequest request) {
        JobSubCategoryDTO updated = adminCatalogService.updateSubCategory(id, request, false);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subcategory updated successfully"));
    }

    /**
     * Partially updates a craft subcategory or its status (PATCH).
     *
     * @param id      subcategory id
     * @param request partial subcategory patch request
     * @return 200 OK with the updated {@link JobSubCategoryDTO}
     */
    @PatchMapping({"/subcategories/{id}", "/subcategories/{id}/status"})
    public ResponseEntity<ApiResponse<JobSubCategoryDTO>> patchSubCategory(
            @PathVariable String id,
            @RequestBody JobSubCategoryRequest request) {
        JobSubCategoryDTO updated = adminCatalogService.updateSubCategory(id, request, true);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subcategory updated successfully"));
    }

    /**
     * Deletes a craft subcategory if no artisans reference it.
     *
     * @param id subcategory id
     * @return 200 OK with {@code data: null}
     */
    @DeleteMapping("/subcategories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubCategory(@PathVariable String id) {
        adminCatalogService.deleteSubCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Subcategory deleted successfully"));
    }

    // ─── MaterialFamily ───────────────────────────────────────────────────────

    /**
     * Creates a new material family catalog entry.
     *
     * @param request validated material family create request
     * @return 201 Created with the persisted {@link MaterialFamilyDTO}
     */
    @PostMapping("/material-families")
    public ResponseEntity<ApiResponse<MaterialFamilyDTO>> createMaterialFamily(
            @Valid @RequestBody MaterialFamilyRequest request) {
        MaterialFamilyDTO created = adminCatalogService.createMaterialFamily(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Material family created successfully"));
    }

    /**
     * Fully replaces an existing material family (PUT).
     *
     * @param id      family id
     * @param request validated update request
     * @return 200 OK with the updated {@link MaterialFamilyDTO}
     */
    @PutMapping("/material-families/{id}")
    public ResponseEntity<ApiResponse<MaterialFamilyDTO>> updateMaterialFamily(
            @PathVariable String id,
            @Valid @RequestBody MaterialFamilyRequest request) {
        MaterialFamilyDTO updated = adminCatalogService.updateMaterialFamily(id, request, false);
        return ResponseEntity.ok(ApiResponse.success(updated, "Material family updated successfully"));
    }

    /**
     * Partially updates a material family or its status (PATCH).
     *
     * @param id      family id
     * @param request partial update request
     * @return 200 OK with the updated {@link MaterialFamilyDTO}
     */
    @PatchMapping({"/material-families/{id}", "/material-families/{id}/status"})
    public ResponseEntity<ApiResponse<MaterialFamilyDTO>> patchMaterialFamily(
            @PathVariable String id,
            @RequestBody MaterialFamilyRequest request) {
        MaterialFamilyDTO updated = adminCatalogService.updateMaterialFamily(id, request, true);
        return ResponseEntity.ok(ApiResponse.success(updated, "Material family updated successfully"));
    }

    /**
     * Deletes a material family if it has no child materials.
     *
     * @param id family id
     * @return 200 OK with {@code data: null}
     */
    @DeleteMapping("/material-families/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMaterialFamily(@PathVariable String id) {
        adminCatalogService.deleteMaterialFamily(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Material family deleted successfully"));
    }

    // ─── Material ─────────────────────────────────────────────────────────────

    /**
     * Creates a new crafting material catalog entry.
     *
     * @param request validated material create request
     * @return 201 Created with the persisted {@link MaterialDTO}
     */
    @PostMapping("/materials")
    public ResponseEntity<ApiResponse<MaterialDTO>> createMaterial(
            @Valid @RequestBody MaterialRequest request) {
        MaterialDTO created = adminCatalogService.createMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Material created successfully"));
    }

    /**
     * Fully replaces an existing crafting material (PUT).
     *
     * @param id      material id
     * @param request validated update request
     * @return 200 OK with the updated {@link MaterialDTO}
     */
    @PutMapping("/materials/{id}")
    public ResponseEntity<ApiResponse<MaterialDTO>> updateMaterial(
            @PathVariable String id,
            @Valid @RequestBody MaterialRequest request) {
        MaterialDTO updated = adminCatalogService.updateMaterial(id, request, false);
        return ResponseEntity.ok(ApiResponse.success(updated, "Material updated successfully"));
    }

    /**
     * Partially updates a crafting material or its status (PATCH).
     *
     * @param id      material id
     * @param request partial update request
     * @return 200 OK with the updated {@link MaterialDTO}
     */
    @PatchMapping({"/materials/{id}", "/materials/{id}/status"})
    public ResponseEntity<ApiResponse<MaterialDTO>> patchMaterial(
            @PathVariable String id,
            @RequestBody MaterialRequest request) {
        MaterialDTO updated = adminCatalogService.updateMaterial(id, request, true);
        return ResponseEntity.ok(ApiResponse.success(updated, "Material updated successfully"));
    }

    /**
     * Deletes a crafting material if no artisans reference it.
     *
     * @param id material id
     * @return 200 OK with {@code data: null}
     */
    @DeleteMapping("/materials/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(@PathVariable String id) {
        adminCatalogService.deleteMaterial(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Material deleted successfully"));
    }
}

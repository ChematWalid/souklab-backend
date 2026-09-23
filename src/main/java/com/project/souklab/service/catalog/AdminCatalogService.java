package com.project.souklab.service.catalog;

import com.project.souklab.config.CacheConfig;
import com.project.souklab.dao.EpoqueRepository;
import com.project.souklab.dao.JobCategoryRepository;
import com.project.souklab.dao.JobSubCategoryRepository;
import com.project.souklab.dao.MaterialFamilyRepository;
import com.project.souklab.dao.MaterialRepository;
import com.project.souklab.dao.RegionRepository;
import com.project.souklab.dao.TechniqueRepository;
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
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.exception.UnprocessableEntityException;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.Epoque;
import com.project.souklab.model.JobCategory;
import com.project.souklab.model.JobSubCategory;
import com.project.souklab.model.Material;
import com.project.souklab.model.MaterialFamily;
import com.project.souklab.model.Region;
import com.project.souklab.model.Technique;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Admin service for write operations on catalog taxonomies:
 * {@link Technique}, {@link Epoque}, {@link Region},
 * {@link JobCategory}, {@link JobSubCategory}, {@link MaterialFamily}, and {@link Material}.
 *
 * <h3>Delete semantics (Option A — hard-delete with 409 guard)</h3>
 * Hard-delete is only permitted when no artisan references the entity
 * or child entities point to it. If references exist, the service throws
 * {@link ConflictException} (409) and suggests deactivating the entry via {@code isActive = false} instead.
 *
 * <h3>Slug auto-generation and validation</h3>
 * If {@code slug} is not supplied in the request, {@link SlugUtils#toSlug(String)} derives it
 * from {@code name}. Explicit slugs are validated for format (422) and uniqueness (409).
 *
 * <h3>Cache eviction</h3>
 * Every write evicts the corresponding Caffeine cache entry so the next public
 * {@code GET /api/v1/catalog/*} reflects the change immediately.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCatalogService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final TechniqueRepository techniqueRepository;
    private final EpoqueRepository epoqueRepository;
    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobSubCategoryRepository jobSubCategoryRepository;
    private final MaterialFamilyRepository materialFamilyRepository;
    private final MaterialRepository materialRepository;
    private final AuditLogService auditLogService;

    // ─── Technique ────────────────────────────────────────────────────────────

    /**
     * Creates a new {@link Technique} catalog entry.
     *
     * @param request validated admin create request
     * @return populated response DTO of the persisted entity
     * @throws ConflictException if the resolved slug is already taken
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_TECHNIQUES, allEntries = true)
    public TechniqueDTO createTechnique(TechniqueRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (techniqueRepository.existsBySlug(slug)) {
            throw new ConflictException("Technique with slug '" + slug + "' already exists");
        }

        Technique technique = new Technique();
        applyTechniqueFields(technique, request, slug);
        Technique saved = techniqueRepository.save(technique);

        auditLogService.logAction(AuditLogAction.Catalog.TECHNIQUE_CREATED,
                "Created technique id=" + saved.getId() + " name=" + saved.getName() + " slug=" + saved.getSlug());
        log.info("catalog.technique.created id={} slug={}", saved.getId(), saved.getSlug());
        return TechniqueDTO.from(saved);
    }

    /**
     * Updates an existing {@link Technique} entry (full replacement — PUT semantics).
     * PATCH callers supply the same DTO; null fields are treated as "leave unchanged".
     *
     * @param id      technique id
     * @param request admin update request (may have null non-required fields for PATCH)
     * @param isPatch when {@code true}, null fields in request are skipped
     * @return updated response DTO
     * @throws ResourceNotFoundException if the technique does not exist
     * @throws ConflictException         if the new slug conflicts with another technique
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_TECHNIQUES, allEntries = true)
    public TechniqueDTO updateTechnique(String id, TechniqueRequest request, boolean isPatch) {
        Technique technique = techniqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technique not found with id: '" + id + "'"));

        String slug;
        if (isPatch && request.getSlug() == null && request.getName() == null) {
            slug = technique.getSlug();
        } else {
            String rawSlug = request.getSlug();
            String sourceName = (request.getName() != null) ? request.getName() : technique.getName();
            slug = resolveSlug(rawSlug, sourceName);
            if (techniqueRepository.existsBySlugAndIdNot(slug, id)) {
                throw new ConflictException("Technique with slug '" + slug + "' already exists");
            }
        }

        String previous = "name=" + technique.getName() + " slug=" + technique.getSlug();
        applyTechniqueFields(technique, request, slug, isPatch);

        auditLogService.logAction(AuditLogAction.Catalog.TECHNIQUE_UPDATED,
                "Updated technique id=" + id + " previous=[" + previous + "] new=[name=" + technique.getName() + " slug=" + technique.getSlug() + "]");
        log.info("catalog.technique.updated id={}", id);
        return TechniqueDTO.from(technique);
    }

    /**
     * Hard-deletes a {@link Technique} if no artisans reference it.
     *
     * @param id technique id
     * @throws ResourceNotFoundException if the technique does not exist
     * @throws ConflictException         if artisans reference this technique (suggest deactivation)
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_TECHNIQUES, allEntries = true)
    public void deleteTechnique(String id) {
        Technique technique = techniqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technique not found with id: '" + id + "'"));

        int refs = techniqueRepository.countArtisanReferences(id);
        if (refs > 0) {
            throw new ConflictException(
                    "Cannot delete technique '" + technique.getName() + "' — " + refs +
                    " artisan(s) reference it. Set isActive=false to deactivate it instead.");
        }

        techniqueRepository.delete(technique);
        auditLogService.logAction(AuditLogAction.Catalog.TECHNIQUE_DELETED,
                "Deleted technique id=" + id + " name=" + technique.getName());
        log.info("catalog.technique.deleted id={}", id);
    }

    // ─── Epoque ───────────────────────────────────────────────────────────────

    /**
     * Creates a new {@link Epoque} catalog entry.
     *
     * @param request validated admin create request
     * @return populated response DTO of the persisted entity
     * @throws ConflictException if the resolved slug is already taken
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_EPOQUES, allEntries = true)
    public EpoqueDTO createEpoque(EpoqueRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (epoqueRepository.existsBySlug(slug)) {
            throw new ConflictException("Epoque with slug '" + slug + "' already exists");
        }

        Epoque epoque = new Epoque();
        applyEpoqueFields(epoque, request, slug);
        Epoque saved = epoqueRepository.save(epoque);

        auditLogService.logAction(AuditLogAction.Catalog.EPOQUE_CREATED,
                "Created epoque id=" + saved.getId() + " name=" + saved.getName() + " slug=" + saved.getSlug());
        log.info("catalog.epoque.created id={} slug={}", saved.getId(), saved.getSlug());
        return EpoqueDTO.from(saved);
    }

    /**
     * Updates an existing {@link Epoque} entry.
     *
     * @param id      epoque id
     * @param request admin update request
     * @param isPatch when {@code true}, null fields are skipped
     * @return updated response DTO
     * @throws ResourceNotFoundException if the epoque does not exist
     * @throws ConflictException         if the new slug conflicts with another epoque
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_EPOQUES, allEntries = true)
    public EpoqueDTO updateEpoque(String id, EpoqueRequest request, boolean isPatch) {
        Epoque epoque = epoqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Epoque not found with id: '" + id + "'"));

        String slug;
        if (isPatch && request.getSlug() == null && request.getName() == null) {
            slug = epoque.getSlug();
        } else {
            String rawSlug = request.getSlug();
            String sourceName = (request.getName() != null) ? request.getName() : epoque.getName();
            slug = resolveSlug(rawSlug, sourceName);
            if (epoqueRepository.existsBySlugAndIdNot(slug, id)) {
                throw new ConflictException("Epoque with slug '" + slug + "' already exists");
            }
        }

        String previous = "name=" + epoque.getName() + " slug=" + epoque.getSlug();
        applyEpoqueFields(epoque, request, slug, isPatch);

        auditLogService.logAction(AuditLogAction.Catalog.EPOQUE_UPDATED,
                "Updated epoque id=" + id + " previous=[" + previous + "] new=[name=" + epoque.getName() + " slug=" + epoque.getSlug() + "]");
        log.info("catalog.epoque.updated id={}", id);
        return EpoqueDTO.from(epoque);
    }

    /**
     * Hard-deletes an {@link Epoque} if no artisans reference it.
     *
     * @param id epoque id
     * @throws ResourceNotFoundException if the epoque does not exist
     * @throws ConflictException         if artisans reference this epoque
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_EPOQUES, allEntries = true)
    public void deleteEpoque(String id) {
        Epoque epoque = epoqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Epoque not found with id: '" + id + "'"));

        int refs = epoqueRepository.countArtisanReferences(id);
        if (refs > 0) {
            throw new ConflictException(
                    "Cannot delete epoque '" + epoque.getName() + "' — " + refs +
                    " artisan(s) reference it. Set isActive=false to deactivate it instead.");
        }

        epoqueRepository.delete(epoque);
        auditLogService.logAction(AuditLogAction.Catalog.EPOQUE_DELETED,
                "Deleted epoque id=" + id + " name=" + epoque.getName());
        log.info("catalog.epoque.deleted id={}", id);
    }

    // ─── Region ───────────────────────────────────────────────────────────────

    /**
     * Creates a new {@link Region} catalog entry.
     *
     * <p>Validates that the {@code parentId}, if provided, references an existing active region
     * and that no circular hierarchy would result (422).
     *
     * @param request validated admin create request
     * @return populated response DTO of the persisted entity
     * @throws ResourceNotFoundException if {@code parentId} does not reference an existing region
     * @throws UnprocessableEntityException if setting the parent would create a circular reference
     * @throws ConflictException         if the resolved slug is already taken
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_REGIONS, allEntries = true)
    public RegionDTO createRegion(RegionRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (regionRepository.existsBySlug(slug)) {
            throw new ConflictException("Region with slug '" + slug + "' already exists");
        }

        Region parent = resolveAndValidateParent(null, request.getParentId());

        Region region = new Region();
        applyRegionFields(region, request, slug, parent);
        Region saved = regionRepository.save(region);

        auditLogService.logAction(AuditLogAction.Catalog.REGION_CREATED,
                "Created region id=" + saved.getId() + " name=" + saved.getName() + " slug=" + saved.getSlug()
                + (parent != null ? " parentId=" + parent.getId() : ""));
        log.info("catalog.region.created id={} slug={}", saved.getId(), saved.getSlug());
        return RegionDTO.from(saved);
    }

    /**
     * Updates an existing {@link Region} entry.
     *
     * @param id      region id
     * @param request admin update request
     * @param isPatch when {@code true}, null fields are skipped
     * @return updated response DTO
     * @throws ResourceNotFoundException if the region or the referenced parent does not exist
     * @throws UnprocessableEntityException if setting the parent would create a circular reference
     * @throws ConflictException         if the new slug conflicts with another region
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_REGIONS, allEntries = true)
    public RegionDTO updateRegion(String id, RegionRequest request, boolean isPatch) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: '" + id + "'"));

        String slug;
        if (isPatch && request.getSlug() == null && request.getName() == null) {
            slug = region.getSlug();
        } else {
            String rawSlug = request.getSlug();
            String sourceName = (request.getName() != null) ? request.getName() : region.getName();
            slug = resolveSlug(rawSlug, sourceName);
            if (regionRepository.existsBySlugAndIdNot(slug, id)) {
                throw new ConflictException("Region with slug '" + slug + "' already exists");
            }
        }

        // Resolve parent only when provided (or when it's a PUT clearing the parent)
        Region parent = region.getParent(); // keep existing by default for PATCH
        if (!isPatch || request.getParentId() != null) {
            parent = resolveAndValidateParent(id, request.getParentId());
        }

        String previous = "name=" + region.getName() + " slug=" + region.getSlug();
        applyRegionFields(region, request, slug, parent, isPatch);

        auditLogService.logAction(AuditLogAction.Catalog.REGION_UPDATED,
                "Updated region id=" + id + " previous=[" + previous + "] new=[name=" + region.getName() + " slug=" + region.getSlug() + "]");
        log.info("catalog.region.updated id={}", id);
        return RegionDTO.from(region);
    }

    /**
     * Hard-deletes a {@link Region} if it has no child regions and no artisan references.
     *
     * @param id region id
     * @throws ResourceNotFoundException if the region does not exist
     * @throws ConflictException         if child regions or artisan references exist
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_REGIONS, allEntries = true)
    public void deleteRegion(String id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: '" + id + "'"));

        if (regionRepository.existsByParentId(id)) {
            throw new ConflictException(
                    "Cannot delete region '" + region.getName() + "' — it has child regions. " +
                    "Delete or reassign the children first, or set isActive=false to deactivate.");
        }

        regionRepository.delete(region);
        auditLogService.logAction(AuditLogAction.Catalog.REGION_DELETED,
                "Deleted region id=" + id + " name=" + region.getName());
        log.info("catalog.region.deleted id={}", id);
    }

    // ─── JobCategory ──────────────────────────────────────────────────────────

    /**
     * Creates a new craft category catalog entry.
     *
     * @param request validated category create request
     * @return populated {@link JobCategoryDTO}
     * @throws ConflictException if the resolved slug is already taken
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public JobCategoryDTO createCategory(JobCategoryRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (jobCategoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Category with slug '" + slug + "' already exists");
        }

        JobCategory category = new JobCategory();
        applyCategoryFields(category, request, slug);
        JobCategory saved = jobCategoryRepository.save(category);

        auditLogService.logAction(AuditLogAction.Catalog.CATEGORY_CREATED,
                "Created category id=" + saved.getId() + " name=" + saved.getName() + " slug=" + saved.getSlug());
        log.info("catalog.category.created id={} slug={}", saved.getId(), saved.getSlug());
        return JobCategoryDTO.from(saved, List.of());
    }

    /**
     * Updates an existing craft category (full or partial update).
     *
     * @param id      category id
     * @param request category update request
     * @param isPatch when true, null fields are skipped
     * @return updated {@link JobCategoryDTO}
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public JobCategoryDTO updateCategory(String id, JobCategoryRequest request, boolean isPatch) {
        JobCategory category = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: '" + id + "'"));

        String slug;
        if (isPatch && request.getSlug() == null && request.getName() == null) {
            slug = category.getSlug();
        } else {
            String rawSlug = request.getSlug();
            String sourceName = (request.getName() != null) ? request.getName() : category.getName();
            slug = resolveSlug(rawSlug, sourceName);
            if (jobCategoryRepository.existsBySlugAndIdNot(slug, id)) {
                throw new ConflictException("Category with slug '" + slug + "' already exists");
            }
        }

        String previous = "name=" + category.getName() + " slug=" + category.getSlug();
        applyCategoryFields(category, request, slug, isPatch);

        auditLogService.logAction(AuditLogAction.Catalog.CATEGORY_UPDATED,
                "Updated category id=" + id + " previous=[" + previous + "] new=[name=" + category.getName() + " slug=" + category.getSlug() + "]");
        log.info("catalog.category.updated id={}", id);

        List<JobSubCategoryDTO> subs = jobSubCategoryRepository
                .findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(id)
                .stream()
                .map(JobSubCategoryDTO::from)
                .toList();
        return JobCategoryDTO.from(category, subs);
    }

    /**
     * Deletes a craft category if it has no child subcategories.
     *
     * @param id category id
     * @throws ConflictException if child subcategories exist
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public void deleteCategory(String id) {
        JobCategory category = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: '" + id + "'"));

        if (jobSubCategoryRepository.existsByCategoryId(id)) {
            throw new ConflictException(
                    "Cannot delete category with active subcategories; delete or reassign them first");
        }

        jobCategoryRepository.delete(category);
        auditLogService.logAction(AuditLogAction.Catalog.CATEGORY_DELETED,
                "Deleted category id=" + id + " name=" + category.getName());
        log.info("catalog.category.deleted id={}", id);
    }

    // ─── JobSubCategory ───────────────────────────────────────────────────────

    /**
     * Creates a new specialized craft trade subcategory.
     *
     * @param request validated subcategory create request
     * @return populated {@link JobSubCategoryDTO}
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public JobSubCategoryDTO createSubCategory(JobSubCategoryRequest request) {
        if (request.getCategoryId() == null || request.getCategoryId().isBlank()) {
            throw new BadRequestException("categoryId is required");
        }
        JobCategory parent = jobCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: '" + request.getCategoryId() + "'"));

        String slug = resolveSlug(request.getSlug(), request.getName());
        if (jobSubCategoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Subcategory with slug '" + slug + "' already exists");
        }

        JobSubCategory subCategory = new JobSubCategory();
        applySubCategoryFields(subCategory, request, slug, parent);
        JobSubCategory saved = jobSubCategoryRepository.save(subCategory);

        auditLogService.logAction(AuditLogAction.Catalog.SUBCATEGORY_CREATED,
                "Created subcategory id=" + saved.getId() + " name=" + saved.getName() + " slug=" + saved.getSlug());
        log.info("catalog.subcategory.created id={} slug={}", saved.getId(), saved.getSlug());
        return JobSubCategoryDTO.from(saved);
    }

    /**
     * Updates an existing craft subcategory.
     *
     * @param id      subcategory id
     * @param request subcategory update request
     * @param isPatch when true, null fields are skipped
     * @return updated {@link JobSubCategoryDTO}
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public JobSubCategoryDTO updateSubCategory(String id, JobSubCategoryRequest request, boolean isPatch) {
        JobSubCategory subCategory = jobSubCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found with id: '" + id + "'"));

        JobCategory parent = subCategory.getCategory();
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            parent = jobCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: '" + request.getCategoryId() + "'"));
        } else if (!isPatch && request.getCategoryId() != null && request.getCategoryId().isBlank()) {
            throw new BadRequestException("categoryId cannot be blank");
        }

        String slug;
        if (isPatch && request.getSlug() == null && request.getName() == null) {
            slug = subCategory.getSlug();
        } else {
            String rawSlug = request.getSlug();
            String sourceName = (request.getName() != null) ? request.getName() : subCategory.getName();
            slug = resolveSlug(rawSlug, sourceName);
            if (jobSubCategoryRepository.existsBySlugAndIdNot(slug, id)) {
                throw new ConflictException("Subcategory with slug '" + slug + "' already exists");
            }
        }

        String previous = "name=" + subCategory.getName() + " slug=" + subCategory.getSlug();
        applySubCategoryFields(subCategory, request, slug, parent, isPatch);

        auditLogService.logAction(AuditLogAction.Catalog.SUBCATEGORY_UPDATED,
                "Updated subcategory id=" + id + " previous=[" + previous + "] new=[name=" + subCategory.getName() + " slug=" + subCategory.getSlug() + "]");
        log.info("catalog.subcategory.updated id={}", id);
        return JobSubCategoryDTO.from(subCategory);
    }

    /**
     * Deletes a craft subcategory if no artisans reference it.
     *
     * @param id subcategory id
     * @throws ConflictException if artisan references exist
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public void deleteSubCategory(String id) {
        JobSubCategory subCategory = jobSubCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found with id: '" + id + "'"));

        int refs = jobSubCategoryRepository.countArtisanReferences(id);
        if (refs > 0) {
            throw new ConflictException(
                    "Cannot delete subcategory '" + subCategory.getName() + "' — " + refs +
                    " artisan(s) reference it. Set isActive=false to deactivate it instead.");
        }

        jobSubCategoryRepository.delete(subCategory);
        auditLogService.logAction(AuditLogAction.Catalog.SUBCATEGORY_DELETED,
                "Deleted subcategory id=" + id + " name=" + subCategory.getName());
        log.info("catalog.subcategory.deleted id={}", id);
    }

    // ─── MaterialFamily ───────────────────────────────────────────────────────

    /**
     * Creates a new material family catalog entry.
     *
     * @param request validated material family create request
     * @return populated {@link MaterialFamilyDTO}
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_MATERIALS, allEntries = true)
    public MaterialFamilyDTO createMaterialFamily(MaterialFamilyRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        if (materialFamilyRepository.existsBySlug(slug)) {
            throw new ConflictException("Material family with slug '" + slug + "' already exists");
        }

        MaterialFamily family = new MaterialFamily();
        applyMaterialFamilyFields(family, request, slug);
        MaterialFamily saved = materialFamilyRepository.save(family);

        auditLogService.logAction(AuditLogAction.Catalog.MATERIAL_FAMILY_CREATED,
                "Created material family id=" + saved.getId() + " name=" + saved.getName() + " slug=" + saved.getSlug());
        log.info("catalog.material-family.created id={} slug={}", saved.getId(), saved.getSlug());
        return MaterialFamilyDTO.from(saved, List.of());
    }

    /**
     * Updates an existing material family.
     *
     * @param id      family id
     * @param request update request
     * @param isPatch when true, null fields are skipped
     * @return updated {@link MaterialFamilyDTO}
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_MATERIALS, allEntries = true)
    public MaterialFamilyDTO updateMaterialFamily(String id, MaterialFamilyRequest request, boolean isPatch) {
        MaterialFamily family = materialFamilyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material family not found with id: '" + id + "'"));

        String slug;
        if (isPatch && request.getSlug() == null && request.getName() == null) {
            slug = family.getSlug();
        } else {
            String rawSlug = request.getSlug();
            String sourceName = (request.getName() != null) ? request.getName() : family.getName();
            slug = resolveSlug(rawSlug, sourceName);
            if (materialFamilyRepository.existsBySlugAndIdNot(slug, id)) {
                throw new ConflictException("Material family with slug '" + slug + "' already exists");
            }
        }

        String previous = "name=" + family.getName() + " slug=" + family.getSlug();
        applyMaterialFamilyFields(family, request, slug, isPatch);

        auditLogService.logAction(AuditLogAction.Catalog.MATERIAL_FAMILY_UPDATED,
                "Updated material family id=" + id + " previous=[" + previous + "] new=[name=" + family.getName() + " slug=" + family.getSlug() + "]");
        log.info("catalog.material-family.updated id={}", id);

        List<MaterialDTO> mats = materialRepository
                .findByFamilyIdAndIsActiveTrueOrderByDisplayOrderAsc(id)
                .stream()
                .map(MaterialDTO::from)
                .toList();
        return MaterialFamilyDTO.from(family, mats);
    }

    /**
     * Deletes a material family if it has no child materials.
     *
     * @param id family id
     * @throws ConflictException if child materials exist
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_MATERIALS, allEntries = true)
    public void deleteMaterialFamily(String id) {
        MaterialFamily family = materialFamilyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material family not found with id: '" + id + "'"));

        if (materialRepository.existsByFamilyId(id)) {
            throw new ConflictException(
                    "Cannot delete material family with active materials; delete or reassign them first");
        }

        materialFamilyRepository.delete(family);
        auditLogService.logAction(AuditLogAction.Catalog.MATERIAL_FAMILY_DELETED,
                "Deleted material family id=" + id + " name=" + family.getName());
        log.info("catalog.material-family.deleted id={}", id);
    }

    // ─── Material ─────────────────────────────────────────────────────────────

    /**
     * Creates a new crafting material catalog entry.
     *
     * @param request validated material create request
     * @return populated {@link MaterialDTO}
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_MATERIALS, allEntries = true)
    public MaterialDTO createMaterial(MaterialRequest request) {
        if (request.getFamilyId() == null || request.getFamilyId().isBlank()) {
            throw new BadRequestException("familyId is required");
        }
        MaterialFamily family = materialFamilyRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new ResourceNotFoundException("Material family not found with id: '" + request.getFamilyId() + "'"));

        String slug = resolveSlug(request.getSlug(), request.getName());
        if (materialRepository.existsBySlug(slug)) {
            throw new ConflictException("Material with slug '" + slug + "' already exists");
        }

        Material material = new Material();
        applyMaterialFields(material, request, slug, family);
        Material saved = materialRepository.save(material);

        auditLogService.logAction(AuditLogAction.Catalog.MATERIAL_CREATED,
                "Created material id=" + saved.getId() + " name=" + saved.getName() + " slug=" + saved.getSlug());
        log.info("catalog.material.created id={} slug={}", saved.getId(), saved.getSlug());
        return MaterialDTO.from(saved);
    }

    /**
     * Updates an existing crafting material.
     *
     * @param id      material id
     * @param request material update request
     * @param isPatch when true, null fields are skipped
     * @return updated {@link MaterialDTO}
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_MATERIALS, allEntries = true)
    public MaterialDTO updateMaterial(String id, MaterialRequest request, boolean isPatch) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: '" + id + "'"));

        MaterialFamily family = material.getFamily();
        if (request.getFamilyId() != null && !request.getFamilyId().isBlank()) {
            family = materialFamilyRepository.findById(request.getFamilyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material family not found with id: '" + request.getFamilyId() + "'"));
        } else if (!isPatch && request.getFamilyId() != null && request.getFamilyId().isBlank()) {
            throw new BadRequestException("familyId cannot be blank");
        }

        String slug;
        if (isPatch && request.getSlug() == null && request.getName() == null) {
            slug = material.getSlug();
        } else {
            String rawSlug = request.getSlug();
            String sourceName = (request.getName() != null) ? request.getName() : material.getName();
            slug = resolveSlug(rawSlug, sourceName);
            if (materialRepository.existsBySlugAndIdNot(slug, id)) {
                throw new ConflictException("Material with slug '" + slug + "' already exists");
            }
        }

        String previous = "name=" + material.getName() + " slug=" + material.getSlug();
        applyMaterialFields(material, request, slug, family, isPatch);

        auditLogService.logAction(AuditLogAction.Catalog.MATERIAL_UPDATED,
                "Updated material id=" + id + " previous=[" + previous + "] new=[name=" + material.getName() + " slug=" + material.getSlug() + "]");
        log.info("catalog.material.updated id={}", id);
        return MaterialDTO.from(material);
    }

    /**
     * Hard-deletes a material if no artisans reference it.
     *
     * @param id material id
     * @throws ConflictException if artisan references exist
     */
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_MATERIALS, allEntries = true)
    public void deleteMaterial(String id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: '" + id + "'"));

        int refs = materialRepository.countArtisanReferences(id);
        if (refs > 0) {
            throw new ConflictException(
                    "Cannot delete material '" + material.getName() + "' — " + refs +
                    " artisan(s) reference it. Set isActive=false to deactivate it instead.");
        }

        materialRepository.delete(material);
        auditLogService.logAction(AuditLogAction.Catalog.MATERIAL_DELETED,
                "Deleted material id=" + id + " name=" + material.getName());
        log.info("catalog.material.deleted id={}", id);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Returns the provided slug if non-blank (validated for valid slug format),
     * otherwise auto-generates one from {@code name}.
     */
    private String resolveSlug(String explicitSlug, String name) {
        if (explicitSlug != null && !explicitSlug.isBlank()) {
            String trimmed = explicitSlug.trim();
            if (!SLUG_PATTERN.matcher(trimmed).matches()) {
                throw new UnprocessableEntityException(
                        "Invalid slug format: '" + trimmed + "'. Slugs must contain only lowercase alphanumeric characters and single hyphens without leading or trailing hyphens.");
            }
            return trimmed;
        }
        return SlugUtils.toSlug(name);
    }

    private void applyTechniqueFields(Technique t, TechniqueRequest req, String slug) {
        applyTechniqueFields(t, req, slug, false);
    }

    private void applyTechniqueFields(Technique t, TechniqueRequest req, String slug, boolean isPatch) {
        if (!isPatch || req.getName() != null) {
            t.setName(req.getName().trim());
        }
        t.setSlug(slug);
        if (!isPatch || req.getDescription() != null) {
            t.setDescription(req.getDescription());
        }
        if (req.getDisplayOrder() != null) {
            t.setDisplayOrder(req.getDisplayOrder());
        } else if (!isPatch || t.getDisplayOrder() == 0) {
            t.setDisplayOrder(techniqueRepository.findMaxDisplayOrder() + 1);
        }
        t.setActive(req.getIsActive() != null ? req.getIsActive() : (!isPatch || t.isActive()));
    }

    private void applyEpoqueFields(Epoque e, EpoqueRequest req, String slug) {
        applyEpoqueFields(e, req, slug, false);
    }

    private void applyEpoqueFields(Epoque e, EpoqueRequest req, String slug, boolean isPatch) {
        if (!isPatch || req.getName() != null) {
            e.setName(req.getName().trim());
        }
        e.setSlug(slug);
        if (!isPatch || req.getPeriodEra() != null) {
            e.setPeriodEra(req.getPeriodEra());
        }
        if (!isPatch || req.getDescription() != null) {
            e.setDescription(req.getDescription());
        }
        if (req.getDisplayOrder() != null) {
            e.setDisplayOrder(req.getDisplayOrder());
        } else if (!isPatch || e.getDisplayOrder() == 0) {
            e.setDisplayOrder(epoqueRepository.findMaxDisplayOrder() + 1);
        }
        e.setActive(req.getIsActive() != null ? req.getIsActive() : (!isPatch || e.isActive()));
    }

    private void applyRegionFields(Region r, RegionRequest req, String slug, Region parent) {
        applyRegionFields(r, req, slug, parent, false);
    }

    private void applyRegionFields(Region r, RegionRequest req, String slug, Region parent, boolean isPatch) {
        if (!isPatch || req.getName() != null) {
            r.setName(req.getName().trim());
        }
        r.setSlug(slug);
        r.setParent(parent);
        if (!isPatch || req.getCode() != null) {
            r.setCode(req.getCode());
        }
        if (req.getDisplayOrder() != null) {
            r.setDisplayOrder(req.getDisplayOrder());
        } else if (!isPatch || r.getDisplayOrder() == 0) {
            r.setDisplayOrder(regionRepository.findMaxDisplayOrder() + 1);
        }
        r.setActive(req.getIsActive() != null ? req.getIsActive() : (!isPatch || r.isActive()));
    }

    /**
     * Resolves the parent {@link Region} for a region write, performing:
     * <ol>
     *   <li>If {@code parentId} is null → returns null (top-level region).</li>
     *   <li>Loads the parent — 404 if not found.</li>
     *   <li>Circular-reference check: walks the ancestor chain of {@code parentId};
     *       if {@code regionId} (the region being written) appears as an ancestor → 422.</li>
     * </ol>
     *
     * @param regionId the id of the region being created/updated (null for create)
     * @param parentId the requested parent id (may be null)
     * @return the resolved parent Region entity, or null
     */
    private Region resolveAndValidateParent(String regionId, String parentId) {
        if (parentId == null) {
            return null;
        }
        Region parent = regionRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent region not found with id: '" + parentId + "'"));

        // Circular reference guard: would regionId appear as an ancestor of parentId?
        if (regionId != null) {
            List<String> ancestorIds = regionRepository.findAncestorIds(parentId);
            if (ancestorIds.contains(regionId) || regionId.equals(parentId)) {
                throw new UnprocessableEntityException(
                        "Cannot set parent: region '" + regionId + "' is already an ancestor of '" + parentId + "'. This would create a circular hierarchy.");
            }
        }
        return parent;
    }

    private void applyCategoryFields(JobCategory c, JobCategoryRequest req, String slug) {
        applyCategoryFields(c, req, slug, false);
    }

    private void applyCategoryFields(JobCategory c, JobCategoryRequest req, String slug, boolean isPatch) {
        if (!isPatch || req.getName() != null) {
            c.setName(req.getName().trim());
        }
        c.setSlug(slug);
        if (!isPatch || req.getDescription() != null) {
            c.setDescription(req.getDescription());
        }
        if (!isPatch || req.getIconUrl() != null) {
            c.setIconUrl(req.getIconUrl());
        }
        if (req.getDisplayOrder() != null) {
            c.setDisplayOrder(req.getDisplayOrder());
        } else if (!isPatch || c.getDisplayOrder() == 0) {
            c.setDisplayOrder(jobCategoryRepository.findMaxDisplayOrder() + 1);
        }
        c.setActive(req.getIsActive() != null ? req.getIsActive() : (!isPatch || c.isActive()));
    }

    private void applySubCategoryFields(JobSubCategory s, JobSubCategoryRequest req, String slug, JobCategory category) {
        applySubCategoryFields(s, req, slug, category, false);
    }

    private void applySubCategoryFields(JobSubCategory s, JobSubCategoryRequest req, String slug, JobCategory category, boolean isPatch) {
        if (!isPatch || req.getName() != null) {
            s.setName(req.getName().trim());
        }
        s.setSlug(slug);
        s.setCategory(category);
        if (!isPatch || req.getDescription() != null) {
            s.setDescription(req.getDescription());
        }
        if (req.getDisplayOrder() != null) {
            s.setDisplayOrder(req.getDisplayOrder());
        } else if (!isPatch || s.getDisplayOrder() == 0) {
            s.setDisplayOrder(jobSubCategoryRepository.findMaxDisplayOrderByCategoryId(category.getId()) + 1);
        }
        s.setActive(req.getIsActive() != null ? req.getIsActive() : (!isPatch || s.isActive()));
    }

    private void applyMaterialFamilyFields(MaterialFamily f, MaterialFamilyRequest req, String slug) {
        applyMaterialFamilyFields(f, req, slug, false);
    }

    private void applyMaterialFamilyFields(MaterialFamily f, MaterialFamilyRequest req, String slug, boolean isPatch) {
        if (!isPatch || req.getName() != null) {
            f.setName(req.getName().trim());
        }
        f.setSlug(slug);
        if (!isPatch || req.getDescription() != null) {
            f.setDescription(req.getDescription());
        }
        if (req.getDisplayOrder() != null) {
            f.setDisplayOrder(req.getDisplayOrder());
        } else if (!isPatch || f.getDisplayOrder() == 0) {
            f.setDisplayOrder(materialFamilyRepository.findMaxDisplayOrder() + 1);
        }
        f.setActive(req.getIsActive() != null ? req.getIsActive() : (!isPatch || f.isActive()));
    }

    private void applyMaterialFields(Material m, MaterialRequest req, String slug, MaterialFamily family) {
        applyMaterialFields(m, req, slug, family, false);
    }

    private void applyMaterialFields(Material m, MaterialRequest req, String slug, MaterialFamily family, boolean isPatch) {
        if (!isPatch || req.getName() != null) {
            m.setName(req.getName().trim());
        }
        m.setSlug(slug);
        m.setFamily(family);
        if (!isPatch || req.getDescription() != null) {
            m.setDescription(req.getDescription());
        }
        if (req.getDisplayOrder() != null) {
            m.setDisplayOrder(req.getDisplayOrder());
        } else if (!isPatch || m.getDisplayOrder() == 0) {
            m.setDisplayOrder(materialRepository.findMaxDisplayOrderByFamilyId(family.getId()) + 1);
        }
        m.setActive(req.getIsActive() != null ? req.getIsActive() : (!isPatch || m.isActive()));
    }
}

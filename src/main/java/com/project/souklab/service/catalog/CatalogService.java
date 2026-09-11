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
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service orchestrating public catalog reference data retrieval.
 * Assembles hierarchical representations for Algerian Wilayas/Communes, Craft Categories/Subcategories,
 * and Material Families/Materials, protected with Caffeine in-memory caching.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobSubCategoryRepository jobSubCategoryRepository;
    private final MaterialFamilyRepository materialFamilyRepository;
    private final MaterialRepository materialRepository;
    private final EpoqueRepository epoqueRepository;
    private final TechniqueRepository techniqueRepository;

    /**
     * Retrieves the complete hierarchical list of active Algerian administrative regions.
     * Top-level Wilayas are populated with their active child Communes ordered by display priority.
     *
     * @return List of hierarchical RegionDTO instances
     */
    @Cacheable(CacheConfig.CACHE_REGIONS)
    public List<RegionDTO> getAllRegions() {
        return regionRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc()
            .stream()
            .map(wilaya -> {
                List<RegionDTO> childCommunes = regionRepository
                    .findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(wilaya.getId())
                    .stream()
                    .map(RegionDTO::from)
                    .toList();
                return RegionDTO.from(wilaya, childCommunes);
            })
            .toList();
    }

    /**
     * Retrieves the two-tier craftsmanship category taxonomy.
     * Top-level categories are populated with their active specialized subcategories.
     *
     * @return List of hierarchical JobCategoryDTO instances
     */
    @Cacheable(CacheConfig.CACHE_CATEGORIES)
    public List<JobCategoryDTO> getAllCategories() {
        return jobCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
            .stream()
            .map(category -> {
                List<JobSubCategoryDTO> subCategories = jobSubCategoryRepository
                    .findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(category.getId())
                    .stream()
                    .map(JobSubCategoryDTO::from)
                    .toList();
                return JobCategoryDTO.from(category, subCategories);
            })
            .toList();
    }

    /**
     * Retrieves the raw materials taxonomy.
     * Material families are populated with their constituent authentic crafting materials.
     *
     * @return List of hierarchical MaterialFamilyDTO instances
     */
    @Cacheable(CacheConfig.CACHE_MATERIALS)
    public List<MaterialFamilyDTO> getAllMaterials() {
        return materialFamilyRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
            .stream()
            .map(family -> {
                List<MaterialDTO> materials = materialRepository
                    .findByFamilyIdAndIsActiveTrueOrderByDisplayOrderAsc(family.getId())
                    .stream()
                    .map(MaterialDTO::from)
                    .toList();
                return MaterialFamilyDTO.from(family, materials);
            })
            .toList();
    }

    /**
     * Retrieves all active historical epochs and cultural eras ordered chronologically.
     *
     * @return List of EpoqueDTO instances
     */
    @Cacheable(CacheConfig.CACHE_EPOQUES)
    public List<EpoqueDTO> getAllEpoques() {
        return epoqueRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
            .stream()
            .map(EpoqueDTO::from)
            .toList();
    }

    /**
     * Retrieves all active traditional craftsmanship techniques ordered by display weight.
     *
     * @return List of TechniqueDTO instances
     */
    @Cacheable(CacheConfig.CACHE_TECHNIQUES)
    public List<TechniqueDTO> getAllTechniques() {
        return techniqueRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
            .stream()
            .map(TechniqueDTO::from)
            .toList();
    }
}

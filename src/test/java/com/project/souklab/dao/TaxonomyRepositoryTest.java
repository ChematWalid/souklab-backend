package com.project.souklab.dao;


import com.project.souklab.model.Epoque;
import com.project.souklab.model.JobCategory;
import com.project.souklab.model.JobSubCategory;
import com.project.souklab.model.Material;
import com.project.souklab.model.MaterialFamily;
import com.project.souklab.model.Region;
import com.project.souklab.model.Technique;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository slice test verifying query derivation, hierarchical parent-child queries,
 * unique slug constraints, and display order sorting across the 7 taxonomy repositories.
 */
@DataJpaTest
@TestPropertySource(locations = TestJpaProperties.H2_PROPERTIES, properties = {
    TestJpaProperties.DISABLE_HIBERNATE_SEARCH,
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TaxonomyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private JobCategoryRepository jobCategoryRepository;

    @Autowired
    private JobSubCategoryRepository jobSubCategoryRepository;

    @Autowired
    private MaterialFamilyRepository materialFamilyRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private EpoqueRepository epoqueRepository;

    @Autowired
    private TechniqueRepository techniqueRepository;

    /**
     * Verifies hierarchical navigation on RegionRepository:
     * fetching top-level Wilayas, fetching child Communes for a Wilaya,
     * sorting by displayOrder, filtering by active status, and slug/code lookups.
     */
    @Test
    @DisplayName("RegionRepository: hierarchical navigation, displayOrder sorting, and slug/code lookups")
    void testRegionHierarchyAndLookups() {
        Region wilayaTizi = Region.builder()
            .name("Tizi Ouzou")
            .slug("tizi-ouzou")
            .code("15")
            .displayOrder(15)
            .isActive(true)
            .build();
        entityManager.persist(wilayaTizi);

        Region wilayaAlger = Region.builder()
            .name("Alger")
            .slug("alger")
            .code("16")
            .displayOrder(16)
            .isActive(true)
            .build();
        entityManager.persist(wilayaAlger);

        Region wilayaInactive = Region.builder()
            .name("Ancienne Wilaya")
            .slug("ancienne-wilaya")
            .code("99")
            .displayOrder(1)
            .isActive(false)
            .build();
        entityManager.persist(wilayaInactive);

        Region communeAzazga = Region.builder()
            .name("Azazga")
            .slug("azazga")
            .displayOrder(1)
            .isActive(true)
            .parent(wilayaTizi)
            .build();
        entityManager.persist(communeAzazga);

        Region communeBeniYenni = Region.builder()
            .name("Beni Yenni")
            .slug("beni-yenni")
            .displayOrder(2)
            .isActive(true)
            .parent(wilayaTizi)
            .build();
        entityManager.persist(communeBeniYenni);

        Region communeInactive = Region.builder()
            .name("Commune Desactivee")
            .slug("commune-desactivee")
            .displayOrder(0)
            .isActive(false)
            .parent(wilayaTizi)
            .build();
        entityManager.persist(communeInactive);

        entityManager.flush();
        entityManager.clear();

        List<Region> topLevelWilayas = regionRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        assertThat(topLevelWilayas).hasSize(2);
        assertThat(topLevelWilayas.get(0).getSlug()).isEqualTo("tizi-ouzou");
        assertThat(topLevelWilayas.get(1).getSlug()).isEqualTo("alger");

        List<Region> tiziCommunes = regionRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(wilayaTizi.getId());
        assertThat(tiziCommunes).hasSize(2);
        assertThat(tiziCommunes.get(0).getSlug()).isEqualTo("azazga");
        assertThat(tiziCommunes.get(1).getSlug()).isEqualTo("beni-yenni");

        Optional<Region> foundBySlug = regionRepository.findBySlug("beni-yenni");
        assertThat(foundBySlug).isPresent();
        assertThat(foundBySlug.get().getName()).isEqualTo("Beni Yenni");

        Optional<Region> foundByCode = regionRepository.findByCode("15");
        assertThat(foundByCode).isPresent();
        assertThat(foundByCode.get().getName()).isEqualTo("Tizi Ouzou");

        assertThat(regionRepository.existsBySlug("tizi-ouzou")).isTrue();
        assertThat(regionRepository.existsBySlug("unknown-region")).isFalse();
    }

    /**
     * Verifies JobCategory and JobSubCategory parent-child queries:
     * active filtering, displayOrder ordering, categoryId query, and categorySlug query.
     */
    @Test
    @DisplayName("JobCategory & JobSubCategory: parent-child traversal, slug lookup, and displayOrder sorting")
    void testJobCategoryAndSubCategoryHierarchy() {
        JobCategory catTerre = JobCategory.builder()
            .name("Métiers de la Terre & Céramique")
            .slug("ceramique-et-poterie")
            .displayOrder(1)
            .isActive(true)
            .build();
        entityManager.persist(catTerre);

        JobCategory catBois = JobCategory.builder()
            .name("Métiers du Bois")
            .slug("metiers-du-bois")
            .displayOrder(2)
            .isActive(true)
            .build();
        entityManager.persist(catBois);

        JobCategory catInactive = JobCategory.builder()
            .name("Métier Inactif")
            .slug("metier-inactif")
            .displayOrder(0)
            .isActive(false)
            .build();
        entityManager.persist(catInactive);

        JobSubCategory subSculpture = JobSubCategory.builder()
            .name("Sculpture sur bois")
            .slug("sculpture-sur-bois")
            .displayOrder(1)
            .isActive(true)
            .category(catBois)
            .build();
        entityManager.persist(subSculpture);

        JobSubCategory subEbenisterie = JobSubCategory.builder()
            .name("Ébénisterie Traditionnelle")
            .slug("ebenisterie-traditionnelle")
            .displayOrder(2)
            .isActive(true)
            .category(catBois)
            .build();
        entityManager.persist(subEbenisterie);

        JobSubCategory subInactive = JobSubCategory.builder()
            .name("Sous-catégorie inactive")
            .slug("sous-cat-inactive")
            .displayOrder(0)
            .isActive(false)
            .category(catBois)
            .build();
        entityManager.persist(subInactive);

        entityManager.flush();
        entityManager.clear();

        List<JobCategory> activeCategories = jobCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        assertThat(activeCategories).hasSize(2);
        assertThat(activeCategories.get(0).getSlug()).isEqualTo("ceramique-et-poterie");
        assertThat(activeCategories.get(1).getSlug()).isEqualTo("metiers-du-bois");

        List<JobSubCategory> subCatsById = jobSubCategoryRepository.findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(catBois.getId());
        assertThat(subCatsById).hasSize(2);
        assertThat(subCatsById.get(0).getSlug()).isEqualTo("sculpture-sur-bois");
        assertThat(subCatsById.get(1).getSlug()).isEqualTo("ebenisterie-traditionnelle");

        List<JobSubCategory> subCatsBySlug = jobSubCategoryRepository.findByCategorySlugAndIsActiveTrueOrderByDisplayOrderAsc("metiers-du-bois");
        assertThat(subCatsBySlug).hasSize(2);
        assertThat(subCatsBySlug.get(0).getSlug()).isEqualTo("sculpture-sur-bois");

        assertThat(jobCategoryRepository.existsBySlug("metiers-du-bois")).isTrue();
        assertThat(jobCategoryRepository.existsBySlug("unknown-cat")).isFalse();

        assertThat(jobSubCategoryRepository.existsBySlug("ebenisterie-traditionnelle")).isTrue();
        assertThat(jobSubCategoryRepository.existsBySlug("unknown-subcat")).isFalse();
    }

    /**
     * Verifies MaterialFamily and Material queries:
     * family association, filtering by familyId and familySlug, and ordering by displayOrder.
     */
    @Test
    @DisplayName("MaterialFamily & Material: family lookup, familySlug navigation, and displayOrder sorting")
    void testMaterialFamilyAndMaterialLookups() {
        MaterialFamily familyTerres = MaterialFamily.builder()
            .name("Terres & Argiles")
            .slug("terres-et-argiles")
            .displayOrder(1)
            .isActive(true)
            .build();
        entityManager.persist(familyTerres);

        Material matKaolin = Material.builder()
            .name("Kaolin")
            .slug("kaolin")
            .displayOrder(1)
            .isActive(true)
            .family(familyTerres)
            .build();
        entityManager.persist(matKaolin);

        Material matArgile = Material.builder()
            .name("Argile Rouge de Kabylie")
            .slug("argile-rouge-kabylie")
            .displayOrder(2)
            .isActive(true)
            .family(familyTerres)
            .build();
        entityManager.persist(matArgile);

        Material matInactive = Material.builder()
            .name("Argile Inactive")
            .slug("argile-inactive")
            .displayOrder(0)
            .isActive(false)
            .family(familyTerres)
            .build();
        entityManager.persist(matInactive);

        entityManager.flush();
        entityManager.clear();

        List<MaterialFamily> families = materialFamilyRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        assertThat(families).hasSize(1);
        assertThat(families.get(0).getSlug()).isEqualTo("terres-et-argiles");

        List<Material> materialsById = materialRepository.findByFamilyIdAndIsActiveTrueOrderByDisplayOrderAsc(familyTerres.getId());
        assertThat(materialsById).hasSize(2);
        assertThat(materialsById.get(0).getSlug()).isEqualTo("kaolin");
        assertThat(materialsById.get(1).getSlug()).isEqualTo("argile-rouge-kabylie");

        List<Material> materialsBySlug = materialRepository.findByFamilySlugAndIsActiveTrueOrderByDisplayOrderAsc("terres-et-argiles");
        assertThat(materialsBySlug).hasSize(2);
        assertThat(materialsBySlug.get(0).getSlug()).isEqualTo("kaolin");

        assertThat(materialRepository.existsBySlug("kaolin")).isTrue();
        assertThat(materialRepository.existsBySlug("non-existent-material")).isFalse();
    }

    /**
     * Verifies Epoque and Technique repositories:
     * active filtering, display order sorting, and slug lookups.
     */
    @Test
    @DisplayName("Epoque & Technique: active filtering, chronological ordering, and slug resolution")
    void testEpoqueAndTechniqueLookups() {
        Epoque epoqueNumide = Epoque.builder()
            .name("Période Numide")
            .slug("periode-numide")
            .periodEra("IIIe siècle av. J.-C.")
            .displayOrder(1)
            .isActive(true)
            .build();
        entityManager.persist(epoqueNumide);

        Epoque epoqueOttomane = Epoque.builder()
            .name("Époque Ottomane")
            .slug("epoque-ottomane")
            .periodEra("XVIe - XIXe siècle")
            .displayOrder(2)
            .isActive(true)
            .build();
        entityManager.persist(epoqueOttomane);

        Technique techCiselure = Technique.builder()
            .name("Ciselure au marteau")
            .slug("ciselure-marteau")
            .displayOrder(1)
            .isActive(true)
            .build();
        entityManager.persist(techCiselure);

        Technique techFiligrane = Technique.builder()
            .name("Filigrane en argent")
            .slug("filigrane-argent")
            .displayOrder(2)
            .isActive(true)
            .build();
        entityManager.persist(techFiligrane);

        entityManager.flush();
        entityManager.clear();

        List<Epoque> activeEpoques = epoqueRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        assertThat(activeEpoques).hasSize(2);
        assertThat(activeEpoques.get(0).getSlug()).isEqualTo("periode-numide");
        assertThat(activeEpoques.get(1).getSlug()).isEqualTo("epoque-ottomane");

        List<Technique> activeTechniques = techniqueRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        assertThat(activeTechniques).hasSize(2);
        assertThat(activeTechniques.get(0).getSlug()).isEqualTo("ciselure-marteau");
        assertThat(activeTechniques.get(1).getSlug()).isEqualTo("filigrane-argent");

        assertThat(epoqueRepository.existsBySlug("periode-numide")).isTrue();
        assertThat(techniqueRepository.existsBySlug("filigrane-argent")).isTrue();
    }

    /**
     * Verifies unique slug database constraint enforcement on taxonomy entities.
     * Persisting two entities with the same slug must trigger a constraint violation on flush.
     */
    @Test
    @DisplayName("Unique slug constraint: throws exception when attempting to persist duplicate slug")
    void testUniqueSlugConstraintEnforcement() {
        Region region1 = Region.builder()
            .name("Oran")
            .slug("oran")
            .code("31")
            .displayOrder(31)
            .isActive(true)
            .build();
        entityManager.persist(region1);
        entityManager.flush();

        Region duplicateSlugRegion = Region.builder()
            .name("Oran El Bahia")
            .slug("oran")
            .code("31")
            .displayOrder(32)
            .isActive(true)
            .build();
        entityManager.persist(duplicateSlugRegion);

        assertThatThrownBy(() -> entityManager.flush())
            .isInstanceOf(Exception.class);
    }
}

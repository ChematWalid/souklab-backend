package com.project.souklab.config;

import com.project.souklab.dao.EpoqueRepository;
import com.project.souklab.dao.JobCategoryRepository;
import com.project.souklab.dao.JobSubCategoryRepository;
import com.project.souklab.dao.MaterialFamilyRepository;
import com.project.souklab.dao.MaterialRepository;
import com.project.souklab.dao.RegionRepository;
import com.project.souklab.dao.TechniqueRepository;
import com.project.souklab.model.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying catalog reference data seeding, geographical tree integrity, and idempotency.
 */
@SpringBootTest
class DataSeederIntegrationTest {

    @Autowired
    private DataSeeder dataSeeder;

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

    @Test
    @DisplayName("DataSeeder: seeds multi-tier regions, categories, materials, epoques, and techniques with idempotency")
    void verifyDataSeederExecutionAndIdempotency() {
        Optional<Region> dzOpt = regionRepository.findByCode("DZ");
        assertThat(dzOpt).isPresent();
        Region dz = dzOpt.get();
        assertThat(dz.getName()).isEqualTo("Algérie");
        assertThat(dz.getSlug()).isEqualTo("algerie");
        assertThat(dz.getParent()).isNull();

        Optional<Region> frOpt = regionRepository.findByCode("FR");
        assertThat(frOpt).isPresent();
        Region fr = frOpt.get();
        assertThat(fr.getName()).isEqualTo("France");
        assertThat(fr.getSlug()).isEqualTo("france");
        assertThat(fr.getParent()).isNull();

        List<Region> algerianWilayas = regionRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(dz.getId());
        assertThat(algerianWilayas).hasSize(69);

        List<Region> allDzChildren = regionRepository.findAll().stream()
            .filter(r -> r.getParent() != null && "DZ".equals(r.getParent().getCode()))
            .toList();
        assertThat(allDzChildren).hasSize(69);

        Optional<Region> tiziOuzou = regionRepository.findByCode("15");
        assertThat(tiziOuzou).isPresent();
        List<Region> tiziChildren = regionRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(tiziOuzou.get().getId());
        assertThat(tiziChildren).extracting(Region::getName).contains("Beni Yenni", "Azazga");

        Optional<Region> alger = regionRepository.findByCode("16");
        assertThat(alger).isPresent();
        List<Region> algerChildren = regionRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(alger.get().getId());
        assertThat(algerChildren).extracting(Region::getName).contains("Casbah");

        Optional<Region> ghardaia = regionRepository.findByCode("47");
        assertThat(ghardaia).isPresent();
        List<Region> ghardaiaChildren = regionRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(ghardaia.get().getId());
        assertThat(ghardaiaChildren).extracting(Region::getName).contains("Beni Isguen");

        Optional<Region> idf = regionRepository.findByCode("IDF");
        assertThat(idf).isPresent();
        List<Region> idfChildren = regionRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(idf.get().getId());
        assertThat(idfChildren).extracting(Region::getName).contains("Paris");

        Optional<Region> ara = regionRepository.findByCode("ARA");
        assertThat(ara).isPresent();
        List<Region> araChildren = regionRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(ara.get().getId());
        assertThat(araChildren).extracting(Region::getName).contains("Lyon");

        Optional<Region> paca = regionRepository.findByCode("PACA");
        assertThat(paca).isPresent();
        List<Region> pacaChildren = regionRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(paca.get().getId());
        assertThat(pacaChildren).extracting(Region::getName).contains("Marseille");

        assertThat(jobCategoryRepository.count()).isEqualTo(8);
        assertThat(jobSubCategoryRepository.count()).isEqualTo(37);
        assertThat(materialFamilyRepository.count()).isEqualTo(6);
        assertThat(materialRepository.count()).isEqualTo(25);
        assertThat(epoqueRepository.count()).isEqualTo(6);
        assertThat(techniqueRepository.count()).isEqualTo(7);

        assertThat(jobCategoryRepository.findBySlug("gros-oeuvre-structure")).isPresent();
        assertThat(jobCategoryRepository.findBySlug("electricite-energie")).isPresent();
        assertThat(jobCategoryRepository.findBySlug("plomberie-systemes-techniques")).isPresent();
        assertThat(jobCategoryRepository.findBySlug("metal-serrurerie")).isPresent();
        assertThat(jobCategoryRepository.findBySlug("metiers-du-patrimoine")).isPresent();

        assertThat(materialFamilyRepository.findBySlug("materiaux-naturels-traditionnels")).isPresent();
        assertThat(materialFamilyRepository.findBySlug("materiaux-de-maconnerie")).isPresent();
        assertThat(materialFamilyRepository.findBySlug("materiaux-de-toiture")).isPresent();
        assertThat(materialFamilyRepository.findBySlug("metal-structure")).isPresent();
        assertThat(materialFamilyRepository.findBySlug("isolation-techniques-modernes")).isPresent();
        assertThat(materialFamilyRepository.findBySlug("revetements-finitions")).isPresent();

        long regionCountBefore = regionRepository.count();
        long catCountBefore = jobCategoryRepository.count();
        long subCatCountBefore = jobSubCategoryRepository.count();
        long matFamCountBefore = materialFamilyRepository.count();
        long matCountBefore = materialRepository.count();
        long epoqueCountBefore = epoqueRepository.count();
        long techCountBefore = techniqueRepository.count();

        dataSeeder.run();

        assertThat(regionRepository.count()).isEqualTo(regionCountBefore);
        assertThat(jobCategoryRepository.count()).isEqualTo(catCountBefore);
        assertThat(jobSubCategoryRepository.count()).isEqualTo(subCatCountBefore);
        assertThat(materialFamilyRepository.count()).isEqualTo(matFamCountBefore);
        assertThat(materialRepository.count()).isEqualTo(matCountBefore);
        assertThat(epoqueRepository.count()).isEqualTo(epoqueCountBefore);
        assertThat(techniqueRepository.count()).isEqualTo(techCountBefore);
    }
}

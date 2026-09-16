package com.project.souklab.config;

import com.project.souklab.dao.EpoqueRepository;
import com.project.souklab.dao.JobCategoryRepository;
import com.project.souklab.dao.JobSubCategoryRepository;
import com.project.souklab.dao.MaterialFamilyRepository;
import com.project.souklab.dao.MaterialRepository;
import com.project.souklab.dao.RegionRepository;
import com.project.souklab.dao.AuthorizationPermissionRepository;
import com.project.souklab.dao.TechniqueRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Epoque;
import com.project.souklab.model.JobCategory;
import com.project.souklab.model.JobSubCategory;
import com.project.souklab.model.Material;
import com.project.souklab.model.MaterialFamily;
import com.project.souklab.model.Region;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.Technique;
import com.project.souklab.model.User;
import com.project.souklab.security.Permission;
import com.project.souklab.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reference data seeder initializing permissions, admin user, geographic hierarchy,
 * craft categories, material families, historical epochs, and craftsmanship techniques.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AuthorizationPermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final EmailUtil emailUtil;
    private final Clock clock;
    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobSubCategoryRepository jobSubCategoryRepository;
    private final MaterialFamilyRepository materialFamilyRepository;
    private final MaterialRepository materialRepository;
    private final EpoqueRepository epoqueRepository;
    private final TechniqueRepository techniqueRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedPermissions();
        seedAdminUser();
        seedRegions();
        seedJobCategories();
        seedMaterials();
        seedEpoques();
        seedTechniques();
    }

    private void seedPermissions() {
        for (Permission permission : Permission.values()) {
            if (permissionRepository.findByPermissionKeyAndEnabledTrue(permission.authority()).isEmpty()) {
                AuthorizationPermission definition = new AuthorizationPermission();
                definition.setPermissionKey(permission.authority());
                definition.setDescription(permission.name());
                definition.setEnabled(true);
                permissionRepository.save(definition);
            }
        }
    }

    private void seedAdminUser() {
        String defaultEmail = appProperties.getAdmin().getDefaultEmail();
        if (defaultEmail == null || defaultEmail.isBlank()) {
            throw new IllegalStateException("APP_ADMIN_DEFAULT_EMAIL must be configured in the environment");
        }
        String normalizedEmail = defaultEmail.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        Set<AuthorizationPermission> adminPermissions = new HashSet<>(permissionRepository.findAll());

        String defaultPassword = appProperties.getAdmin().getDefaultPassword();
        if (defaultPassword == null || defaultPassword.isBlank()) {
            throw new IllegalStateException("APP_ADMIN_DEFAULT_PASSWORD must be configured in the environment");
        }

        User admin = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(defaultPassword))
                .firstName("System")
                .lastName("Administrator")
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now(clock))
                .permissions(adminPermissions)
                .build();

        userRepository.save(admin);

        try {
            emailUtil.sendAdminWelcomeEmail(normalizedEmail, defaultPassword);
        } catch (Exception e) {
            log.error("Failed to send first-boot administrator welcome email to {}", normalizedEmail, e);
        }
    }

    private void seedRegions() {
        if (regionRepository.count() > 0) {
            return;
        }

        Region algerie = Region.builder()
            .name("Algérie")
            .slug("algerie")
            .code("DZ")
            .displayOrder(1)
            .isActive(true)
            .parent(null)
            .build();
        algerie = regionRepository.save(algerie);

        Region france = Region.builder()
            .name("France")
            .slug("france")
            .code("FR")
            .displayOrder(2)
            .isActive(true)
            .parent(null)
            .build();
        france = regionRepository.save(france);

        String[][] wilayasData = {
            {"01", "Adrar", "adrar"},
            {"02", "Chlef", "chlef"},
            {"03", "Laghouat", "laghouat"},
            {"04", "Oum El Bouaghi", "oum-el-bouaghi"},
            {"05", "Batna", "batna"},
            {"06", "Béjaïa", "bejaia"},
            {"07", "Biskra", "biskra"},
            {"08", "Béchar", "bechar"},
            {"09", "Blida", "blida"},
            {"10", "Bouira", "bouira"},
            {"11", "Tamanrasset", "tamanrasset"},
            {"12", "Tébessa", "tebessa"},
            {"13", "Tlemcen", "tlemcen"},
            {"14", "Tiaret", "tiaret"},
            {"15", "Tizi Ouzou", "tizi-ouzou"},
            {"16", "Alger", "alger"},
            {"17", "Djelfa", "djelfa"},
            {"18", "Jijel", "jijel"},
            {"19", "Sétif", "setif"},
            {"20", "Saïda", "saida"},
            {"21", "Skikda", "skikda"},
            {"22", "Sidi Bel Abbès", "sidi-bel-abbes"},
            {"23", "Annaba", "annaba"},
            {"24", "Guelma", "guelma"},
            {"25", "Constantine", "constantine"},
            {"26", "Médéa", "medea"},
            {"27", "Mostaganem", "mostaganem"},
            {"28", "M'Sila", "msila"},
            {"29", "Mascara", "mascara"},
            {"30", "Ouargla", "ouargla"},
            {"31", "Oran", "oran"},
            {"32", "El Bayadh", "el-bayadh"},
            {"33", "Illizi", "illizi"},
            {"34", "Bordj Bou Arreridj", "bordj-bou-arreridj"},
            {"35", "Boumerdès", "boumerdes"},
            {"36", "El Tarf", "el-tarf"},
            {"37", "Tindouf", "tindouf"},
            {"38", "Tissemsilt", "tissemsilt"},
            {"39", "El Oued", "el-oued"},
            {"40", "Khenchela", "khenchela"},
            {"41", "Souk Ahras", "souk-ahras"},
            {"42", "Tipaza", "tipaza"},
            {"43", "Mila", "mila"},
            {"44", "Aïn Defla", "ain-defla"},
            {"45", "Naâma", "naama"},
            {"46", "Aïn Témouchent", "ain-temouchent"},
            {"47", "Ghardaïa", "ghardaia"},
            {"48", "Relizane", "relizane"},
            {"49", "Timimoun", "timimoun"},
            {"50", "Bordj Badji Mokhtar", "bordj-badji-mokhtar"},
            {"51", "Ouled Djellal", "ouled-djellal"},
            {"52", "Béni Abbès", "beni-abbes"},
            {"53", "In Salah", "in-salah"},
            {"54", "In Guezzam", "in-guezzam"},
            {"55", "Touggourt", "touggourt"},
            {"56", "Djanet", "djanet"},
            {"57", "El M'Ghair", "el-mghair"},
            {"58", "El Meniaa", "el-meniaa"},
            {"59", "Aflou", "aflou"},
            {"60", "Barika", "barika"},
            {"61", "El Kantara", "el-kantara"},
            {"62", "Bir el-Ater", "bir-el-ater"},
            {"63", "El Aricha", "el-aricha"},
            {"64", "Ksar Chellala", "ksar-chellala"},
            {"65", "Aïn Oussera", "ain-oussera"},
            {"66", "Messaad", "messaad"},
            {"67", "Ksar El Boukhari", "ksar-el-boukhari"},
            {"68", "Bou Saâda", "bou-saada"},
            {"69", "El Abiodh Sidi Cheikh", "el-abiodh-sidi-cheikh"}
        };

        Map<String, Region> wilayaMap = new HashMap<>();
        for (int i = 0; i < wilayasData.length; i++) {
            String code = wilayasData[i][0];
            String name = wilayasData[i][1];
            String slug = wilayasData[i][2];
            int displayOrder = i + 1;
            Region wilaya = Region.builder()
                .name(name)
                .slug(slug)
                .code(code)
                .displayOrder(displayOrder)
                .isActive(true)
                .parent(algerie)
                .build();
            wilaya = regionRepository.save(wilaya);
            wilayaMap.put(code, wilaya);
        }

        Region idf = Region.builder()
            .name("Île-de-France")
            .slug("ile-de-france")
            .code("IDF")
            .displayOrder(1)
            .isActive(true)
            .parent(france)
            .build();
        idf = regionRepository.save(idf);

        Region ara = Region.builder()
            .name("Auvergne-Rhône-Alpes")
            .slug("auvergne-rhone-alpes")
            .code("ARA")
            .displayOrder(2)
            .isActive(true)
            .parent(france)
            .build();
        ara = regionRepository.save(ara);

        Region paca = Region.builder()
            .name("Provence-Alpes-Côte d'Azur")
            .slug("provence-alpes-cote-d-azur")
            .code("PACA")
            .displayOrder(3)
            .isActive(true)
            .parent(france)
            .build();
        paca = regionRepository.save(paca);

        Region tiziOuzou = wilayaMap.get("15");
        Region alger = wilayaMap.get("16");
        Region ghardaia = wilayaMap.get("47");

        List<Region> communes = List.of(
            Region.builder()
                .name("Beni Yenni")
                .slug("beni-yenni")
                .code(null)
                .displayOrder(1)
                .isActive(true)
                .parent(tiziOuzou)
                .build(),
            Region.builder()
                .name("Azazga")
                .slug("azazga")
                .code(null)
                .displayOrder(2)
                .isActive(true)
                .parent(tiziOuzou)
                .build(),
            Region.builder()
                .name("Casbah")
                .slug("casbah")
                .code(null)
                .displayOrder(1)
                .isActive(true)
                .parent(alger)
                .build(),
            Region.builder()
                .name("Beni Isguen")
                .slug("beni-isguen")
                .code(null)
                .displayOrder(1)
                .isActive(true)
                .parent(ghardaia)
                .build(),
            Region.builder()
                .name("Paris")
                .slug("paris")
                .code(null)
                .displayOrder(1)
                .isActive(true)
                .parent(idf)
                .build(),
            Region.builder()
                .name("Lyon")
                .slug("lyon")
                .code(null)
                .displayOrder(1)
                .isActive(true)
                .parent(ara)
                .build(),
            Region.builder()
                .name("Marseille")
                .slug("marseille")
                .code(null)
                .displayOrder(1)
                .isActive(true)
                .parent(paca)
                .build()
        );
        regionRepository.saveAll(communes);
    }

    private void seedJobCategories() {
        if (jobCategoryRepository.count() > 0) {
            return;
        }

        saveCategoryWithSubs(
            "Métiers de la Terre & Céramique",
            "metiers-de-la-terre-ceramique",
            "Façonnage traditionnel de l'argile, poterie ancestrale et céramique d'art.",
            "category-ceramique.png",
            1,
            List.of(
                new SubCategorySeed("Poterie de Kabylie", "poterie-de-kabylie", "Poterie féminine modelée à la main et ornée de motifs berbères.", 1),
                new SubCategorySeed("Céramique Émaillée", "ceramique-emaillee", "Faïence architecturale, zellige et plats vernissés d'inspiration mauresque.", 2),
                new SubCategorySeed("Poterie Rurale Saharienne", "poterie-rurale-saharienne", "Ustensiles et poteries utilitaires cuites au feu ouvert du Sahara.", 3)
            )
        );

        saveCategoryWithSubs(
            "Métiers du Métal & Bijouterie",
            "metiers-du-metal-bijouterie",
            "Orfèvrerie traditionnelle, dinanderie d'art et travail des métaux précieux.",
            "category-bijouterie.png",
            2,
            List.of(
                new SubCategorySeed("Bijoux Kabyles en Argent", "bijoux-kabyles-en-argent", "Bijoux traditionnels en argent émaillé et rehaussés de corail méditerranéen.", 1),
                new SubCategorySeed("Dinanderie de Constantine", "dinanderie-de-constantine", "Plateaux, aiguières et récipients en cuivre martelé et ciselé.", 2),
                new SubCategorySeed("Bijouterie Traditionnelle Chaouie", "bijouterie-traditionnelle-chaouie", "Parures et fibules en argent massif de l'Aurès.", 3),
                new SubCategorySeed("Ferronnerie d'Art Mauresque", "ferronnerie-art-mauresque", "Grilles ouvragées, lanternes et ferronnerie d'inspiration hispano-mauresque.", 4)
            )
        );

        saveCategoryWithSubs(
            "Tapis & Tissage",
            "tapis-tissage",
            "Tissage de tapis traditionnels, tissage de laine et confection de textiles rituels.",
            "category-tapis.png",
            3,
            List.of(
                new SubCategorySeed("Tapis du M'Zab", "tapis-du-mzab", "Tapis mozabites en pure laine vierge aux motifs symboliques ibadites.", 1),
                new SubCategorySeed("Tapis de Babar & Nememcha", "tapis-de-babar-nememcha", "Tapis traditionnels aux motifs géométriques des hauts plateaux de l'Est.", 2),
                new SubCategorySeed("Tissage de Bernous & Gandoura", "tissage-bernous-gandoura", "Tissage de laine fine et poil de dromadaire pour habits d'honneur.", 3),
                new SubCategorySeed("Hanbel & Zarbiya Djebel Amour", "hanbel-zarbiya-djebel-amour", "Tissages ras et tapis de haute laine pastorale.", 4)
            )
        );

        saveCategoryWithSubs(
            "Cuir & Maroquinerie",
            "cuir-maroquinerie",
            "Travail traditionnel du cuir naturel, tannerie artisanale et sellerie d'art.",
            "category-cuir.png",
            4,
            List.of(
                new SubCategorySeed("Maroquinerie Traditionnelle", "maroquinerie-traditionnelle", "Sacs, sacoches et porte-documents en cuir repoussé.", 1),
                new SubCategorySeed("Sellerie d'Apparat & Harnachement", "sellerie-apparat-harnachement", "Selles brodées de fil d'or pour fantasias et apparat équestre.", 2),
                new SubCategorySeed("Babouches & Chaussures Traditionnelles", "babouches-chaussures-traditionnelles", "Babouches citadines et sahariennes brodées.", 3)
            )
        );

        saveCategoryWithSubs(
            "Métiers du Bois",
            "metiers-du-bois",
            "Menuiserie d'art, sculpture sur bois précieux et ébénisterie traditionnelle.",
            "category-bois.png",
            5,
            List.of(
                new SubCategorySeed("Ébénisterie Traditionnelle", "ebenisterie-traditionnelle", "Fabrication de mobilier d'art, coffres sculptés et boiseries précieuses.", 1),
                new SubCategorySeed("Sculpture sur Bois Mauresque", "sculpture-sur-bois-mauresque", "Arabesques et bas-reliefs sculptés sur cèdre et noyer.", 2),
                new SubCategorySeed("Tournage sur Bois & Moucharabieh", "tournage-sur-bois-moucharabieh", "Claustras en bois ajouré, quenouilles et objets tournés.", 3)
            )
        );

        saveCategoryWithSubs(
            "Costumes & Broderie Traditionnelle",
            "costumes-broderie-traditionnelle",
            "Haute couture patrimoniale, costumes régionaux et broderies ancestrales.",
            "category-costumes.png",
            6,
            List.of(
                new SubCategorySeed("Karakou & Broderie Algéroise", "karakou-broderie-algeroise", "Vestes de velours brodées au fil d'or et d'argent selon la technique du majboud.", 1),
                new SubCategorySeed("Gandoura Constantinoise", "gandoura-constantinoise", "Robes traditionnelles en velours lourd brodées à la fetla de Constantine.", 2),
                new SubCategorySeed("Blousa Oranaise", "blousa-oranaise", "Robe citadine oranaise parée de dentelles et de perles fines.", 3),
                new SubCategorySeed("Robe Kabyle Traditionnelle", "robe-kabyle-traditionnelle", "Robes d'apparat tissées et rehaussées de rubans zigzags multicolores.", 4)
            )
        );

        saveCategoryWithSubs(
            "Vannerie & Sparterie",
            "vannerie-sparterie",
            "Tressage de fibres végétales locales, sparterie d'alfa et vannerie de palme.",
            "category-vannerie.png",
            7,
            List.of(
                new SubCategorySeed("Vannerie de Palme Saharienne", "vannerie-de-palme-saharienne", "Paniers, chapeaux et nattes tressés en feuilles de palmier doum.", 1),
                new SubCategorySeed("Vannerie d'Alfa & Jonc", "vannerie-alfa-jonc", "Objets utilitaires et décoratifs en alfa et jonc des zones steppiques.", 2)
            )
        );
    }

    private record SubCategorySeed(String name, String slug, String description, int displayOrder) {}

    private void saveCategoryWithSubs(String name, String slug, String description, String iconUrl, int displayOrder, List<SubCategorySeed> subs) {
        JobCategory category = JobCategory.builder()
            .name(name)
            .slug(slug)
            .description(description)
            .iconUrl(iconUrl)
            .displayOrder(displayOrder)
            .isActive(true)
            .build();
        category = jobCategoryRepository.save(category);

        for (SubCategorySeed sub : subs) {
            JobSubCategory subCategory = JobSubCategory.builder()
                .name(sub.name())
                .slug(sub.slug())
                .description(sub.description())
                .displayOrder(sub.displayOrder())
                .isActive(true)
                .category(category)
                .build();
            jobSubCategoryRepository.save(subCategory);
        }
    }

    private record MaterialSeed(String name, String slug, String description, int displayOrder) {}

    private void seedMaterials() {
        if (materialFamilyRepository.count() > 0) {
            return;
        }

        saveMaterialFamilyWithMaterials(
            "Terres & Argiles",
            "terres-et-argiles",
            "Argiles pures, grès et terres réfractaires de terroirs artisanaux.",
            1,
            List.of(
                new MaterialSeed("Argile Rouge de Kabylie", "argile-rouge-de-kabylie", "Terre argileuse ferrugineuse riche, traditionnellement extraite des collines de Grande Kabylie.", 1),
                new MaterialSeed("Argile Blanche & Grès", "argile-blanche-et-gres", "Kaolin et grès fin pour céramiques et faïences émaillées.", 2)
            )
        );

        saveMaterialFamilyWithMaterials(
            "Métaux & Alliages",
            "metaux-et-alliages",
            "Métaux précieux, cuivre et alliages nobles façonnés par les maîtres artisans.",
            2,
            List.of(
                new MaterialSeed("Argent Massif 925", "argent-massif-925", "Argent au titre 925/1000 pour bijouterie et filigrane.", 1),
                new MaterialSeed("Cuivre Rouge & Jaune", "cuivre-rouge-et-jaune", "Cuivre martelé et laiton pour dinanderie et objets d'art.", 2),
                new MaterialSeed("Bronze & Laiton", "bronze-et-laiton", "Alliages résistants pour fonte d'art et éléments décoratifs.", 3)
            )
        );

        saveMaterialFamilyWithMaterials(
            "Bois & Essences Naturelles",
            "bois-et-essences-naturelles",
            "Essences arboricoles méditerranéennes et bois nobles des forêts d'Algérie.",
            3,
            List.of(
                new MaterialSeed("Bois de Cèdre de l'Atlas", "bois-de-cedre-de-l-atlas", "Bois noble odorant et imputrescible récolté dans les massifs de l'Atlas.", 1),
                new MaterialSeed("Bois de Noyer & Genévrier", "bois-de-noyer-et-genevrier", "Bois denses et nervurés pour sculpture fine et tabletterie.", 2),
                new MaterialSeed("Bois d'Olivier", "bois-d-olivier", "Bois dur aux veinures contrastées façonné pour ustensiles et pièces de collection.", 3)
            )
        );

        saveMaterialFamilyWithMaterials(
            "Cuirs & Peaux",
            "cuirs-et-peaux",
            "Peaux naturelles préparées selon des techniques de tannage traditionnel.",
            4,
            List.of(
                new MaterialSeed("Cuir Tanné Végétal", "cuir-tanne-vegetal", "Cuir pleine fleur tanné aux écorces végétales sans produits chimiques.", 1),
                new MaterialSeed("Peau de Chèvre & Mouton", "peau-de-chevre-et-mouton", "Peaux souples apprêtées pour reliure, babouches et maroquinerie fine.", 2),
                new MaterialSeed("Cuir de Chameau", "cuir-de-chameau", "Cuir saharien robuste et grainé pour bagagerie et selles de méharée.", 3)
            )
        );

        saveMaterialFamilyWithMaterials(
            "Fibres Végétales & Textiles",
            "fibres-vegetales-et-textiles",
            "Fibres naturelles textiles et graminées issues des écosystèmes algériens.",
            5,
            List.of(
                new MaterialSeed("Laine Chaouie", "laine-chaouie", "Laine dense de mouton des hauts plateaux de l'Aurès pour tissages rustiques.", 1),
                new MaterialSeed("Soie Naturelle & Fil d'Or", "soie-naturelle-et-fil-d-or", "Fils nobles pour broderies d'apparat au majboud et à la fetla.", 2),
                new MaterialSeed("Fibre d'Alfa & Palme Doum", "fibre-d-alfa-et-palme-doum", "Fibres résistantes récoltées dans les steppes et palmeraies.", 3)
            )
        );

        saveMaterialFamilyWithMaterials(
            "Pierres & Matières Organiques",
            "pierres-et-matieres-organiques",
            "Gemmes méditerranéennes, coraux et matières précieuses organiques.",
            6,
            List.of(
                new MaterialSeed("Corail Rouge d'El Kala", "corail-rouge-d-el-kala", "Corallium rubrum pêché durablement sur le littoral d'El Kala, emblème du bijou traditionnel.", 1),
                new MaterialSeed("Nacre & Émail Naturel", "nacre-et-email-naturel", "Incrustations nacrées et émaux cuits aux oxydes minéraux.", 2)
            )
        );
    }

    private void saveMaterialFamilyWithMaterials(String name, String slug, String description, int displayOrder, List<MaterialSeed> materials) {
        MaterialFamily family = MaterialFamily.builder()
            .name(name)
            .slug(slug)
            .description(description)
            .displayOrder(displayOrder)
            .isActive(true)
            .build();
        family = materialFamilyRepository.save(family);

        for (MaterialSeed mat : materials) {
            Material material = Material.builder()
                .name(mat.name())
                .slug(mat.slug())
                .description(mat.description())
                .displayOrder(mat.displayOrder())
                .isActive(true)
                .family(family)
                .build();
            materialRepository.save(material);
        }
    }

    private void seedEpoques() {
        if (epoqueRepository.count() > 0) {
            return;
        }

        List<Epoque> epoques = List.of(
            Epoque.builder()
                .name("Période Numide")
                .slug("periode-numide")
                .periodEra("IIIe siècle av. J.-C. - Ier siècle ap. J.-C.")
                .description("Royaume des rois numides (Massinissa, Syphax, Juba), marqué par une orfèvrerie et une poterie aux formes architecturales souveraines.")
                .displayOrder(1)
                .isActive(true)
                .build(),
            Epoque.builder()
                .name("Période Rustumide & Médiévale")
                .slug("periode-rustumide-medievale")
                .periodEra("VIIIe - XIVe siècle")
                .description("Âge d'or des cités sahariennes et médiévales (Tahert, Sedrata, M'Zab) avec un essor remarquable du tissage géométrique et de l'architecture en terre.")
                .displayOrder(2)
                .isActive(true)
                .build(),
            Epoque.builder()
                .name("Période Zianide")
                .slug("periode-zianide")
                .periodEra("XIIIe - XVIe siècle")
                .description("Dynastie de Tlemcen caractérisée par le raffinement hispano-mauresque, le zellige ornemental, la dinanderie fine et le travail du bois sculpté.")
                .displayOrder(3)
                .isActive(true)
                .build(),
            Epoque.builder()
                .name("Époque Ottomane")
                .slug("epoque-ottomane")
                .periodEra("XVIe - XIXe siècle")
                .description("Régence d'Alger marquée par le costume d'apparat, les velours brodés au fil d'or (karakou), la dinanderie d'art et la céramique citadine.")
                .displayOrder(4)
                .isActive(true)
                .build(),
            Epoque.builder()
                .name("Période Traditionnelle Pré-moderne")
                .slug("periode-traditionnelle-pre-moderne")
                .periodEra("XIXe - milieu XXe siècle")
                .description("Préservation rurale des savoir-faire ancestraux au cœur des villages de montagne, des oasis et des campements nomades.")
                .displayOrder(5)
                .isActive(true)
                .build(),
            Epoque.builder()
                .name("Renaissance Artisanale Contemporaine")
                .slug("renaissance-artisanale-contemporaine")
                .periodEra("Époque contemporaine")
                .description("Courant moderne réinterprétant les motifs patrimoniaux algériens dans des créations design et écoresponsables.")
                .displayOrder(6)
                .isActive(true)
                .build()
        );
        epoqueRepository.saveAll(epoques);
    }

    private void seedTechniques() {
        if (techniqueRepository.count() > 0) {
            return;
        }

        List<Technique> techniques = List.of(
            Technique.builder()
                .name("Filigrane d'argent")
                .slug("filigrane-d-argent")
                .description("Assemblage méticuleux de fils d'argent torsadés et soudés formant des arabesques aériennes et des bijoux d'orfèvrerie.")
                .displayOrder(1)
                .isActive(true)
                .build(),
            Technique.builder()
                .name("Ciselure au repoussé")
                .slug("ciselure-au-repousse")
                .description("Travail du cuivre et de l'argent par martelage et frappe sur envers et endroit pour créer des motifs en relief saisissants.")
                .displayOrder(2)
                .isActive(true)
                .build(),
            Technique.builder()
                .name("Émaillage cloisonné")
                .slug("emaillage-cloisonne")
                .description("Incrustation de poudres d'émaux minéraux vitrifiés au four dans de fines loges métalliques d'argent.")
                .displayOrder(3)
                .isActive(true)
                .build(),
            Technique.builder()
                .name("Tissage de haute lisse")
                .slug("tissage-de-haute-lisse")
                .description("Nouage et tissage manuel sur métier vertical traditionnel pour la confection de tapis d'apparat en pure laine.")
                .displayOrder(4)
                .isActive(true)
                .build(),
            Technique.builder()
                .name("Broderie au Majboud & Fetla")
                .slug("broderie-au-majboud-fetla")
                .description("Broderie précieuse au fil d'or et d'argent couché sur velours de soie pour costumes de fête et harnachements.")
                .displayOrder(5)
                .isActive(true)
                .build(),
            Technique.builder()
                .name("Tournage sur bois")
                .slug("tournage-sur-bois")
                .description("Façonnage mécanique et manuel du bois en rotation pour concevoir des pièces rondes, balustres et claustras moucharabieh.")
                .displayOrder(6)
                .isActive(true)
                .build(),
            Technique.builder()
                .name("Tannage végétal")
                .slug("tannage-vegetal")
                .description("Procédé écologique de traitement des peaux brutes au moyen d'écorces et de tanins végétaux préservant la souplesse du cuir.")
                .displayOrder(7)
                .isActive(true)
                .build()
        );
        techniqueRepository.saveAll(techniques);
    }
}

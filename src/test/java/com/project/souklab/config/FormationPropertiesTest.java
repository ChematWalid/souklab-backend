package com.project.souklab.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.unit.DataSize;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Isolated unit tests verifying AppProperties.FormationConfig property binding
 * and environment variable resolution.
 */
class FormationPropertiesTest {

    /**
     * Verifies that policy values are supplied by configuration binding rather than Java fallbacks.
     */
    @Test
    @DisplayName("FormationConfig does not provide policy defaults upon instantiation")
    void policyValuesAreNotHardcodedUponInstantiation() {
        AppProperties.FormationConfig config = new AppProperties.FormationConfig();

        assertThat(config.getThumbnail()).isNotNull();
        assertThat(config.getThumbnail().getMaxFileSize()).isNull();
        assertThat(config.getThumbnail().getAllowedMimeTypes()).isNull();

        assertThat(config.getFile()).isNotNull();
        assertThat(config.getFile().getMaxCount()).isZero();
        assertThat(config.getFile().getMaxFileSize()).isNull();
        assertThat(config.getFile().getAllowedMimeTypes()).isNull();

        assertThat(config.getCancellation()).isNotNull();
        assertThat(config.getCancellation().getDeadlineHours()).isZero();

        assertThat(config.getPagination()).isNotNull();
        assertThat(config.getPagination().getDefaultPageSize()).isZero();

        assertThat(config.getDefaultCurrency()).isNull();
    }

    /**
     * Verifies that AppProperties exposes nested formation and storage configuration
     * without supplying policy defaults in Java.
     */
    @Test
    @DisplayName("AppProperties root exposes FormationConfig and Storage without Java defaults")
    void appPropertiesRootExposesFormationDefaults() {
        AppProperties appProperties = new AppProperties();

        assertThat(appProperties.getStorage()).isNotNull();
        assertThat(appProperties.getStorage().getFileServingPrefix()).isNull();

        assertThat(appProperties.getFormation()).isNotNull();
        assertThat(appProperties.getFormation().getThumbnail().getMaxFileSize()).isNull();
        assertThat(appProperties.getFormation().getThumbnail().getAllowedMimeTypes()).isNull();
        assertThat(appProperties.getFormation().getFile().getMaxCount()).isZero();
        assertThat(appProperties.getFormation().getFile().getMaxFileSize()).isNull();
        assertThat(appProperties.getFormation().getFile().getAllowedMimeTypes()).isNull();
        assertThat(appProperties.getFormation().getCancellation().getDeadlineHours()).isZero();
        assertThat(appProperties.getFormation().getPagination().getDefaultPageSize()).isZero();
        assertThat(appProperties.getFormation().getDefaultCurrency()).isNull();
    }

    /**
     * Verifies that properties can be bound directly under prefix app.formation.
     */
    @Test
    @DisplayName("FormationConfig correctly binds custom configuration values from property sources")
    void bindsCustomConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test-formation", Map.of(
                "app.formation.thumbnail.max-file-size", "15MB",
                "app.formation.thumbnail.allowed-mime-types", "image/png,image/webp",
                "app.formation.file.max-count", "20",
                "app.formation.file.max-file-size", "50MB",
                "app.formation.file.allowed-mime-types", "application/pdf",
                "app.formation.cancellation.deadline-hours", "48",
                "app.formation.pagination.default-page-size", "25",
                "app.formation.default-currency", "EUR"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AppProperties.FormationConfig config = binder.bind("app.formation", AppProperties.FormationConfig.class).get();

        assertThat(config.getThumbnail().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(15));
        assertThat(config.getThumbnail().getAllowedMimeTypes()).containsExactly("image/png", "image/webp");
        assertThat(config.getFile().getMaxCount()).isEqualTo(20);
        assertThat(config.getFile().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(50));
        assertThat(config.getFile().getAllowedMimeTypes()).containsExactly("application/pdf");
        assertThat(config.getCancellation().getDeadlineHours()).isEqualTo(48);
        assertThat(config.getPagination().getDefaultPageSize()).isEqualTo(25);
        assertThat(config.getDefaultCurrency()).isEqualTo("EUR");
    }

    /**
     * Verifies that root AppProperties binds full nested app properties tree including storage and formation.
     */
    @Test
    @DisplayName("AppProperties root correctly binds nested storage and formation tree from property sources")
    void bindsRootAppPropertiesFormationTree() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test-app-formation", Map.of(
                "app.storage.file-serving-prefix", "/custom/files/",
                "app.formation.thumbnail.max-file-size", "8MB",
                "app.formation.thumbnail.allowed-mime-types", "image/jpeg",
                "app.formation.file.max-count", "5",
                "app.formation.file.max-file-size", "12MB",
                "app.formation.file.allowed-mime-types", "application/pdf,image/png",
                "app.formation.cancellation.deadline-hours", "12",
                "app.formation.pagination.default-page-size", "15",
                "app.formation.default-currency", "USD"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AppProperties appProperties = binder.bind("app", AppProperties.class).get();

        assertThat(appProperties.getStorage()).isNotNull();
        assertThat(appProperties.getStorage().getFileServingPrefix()).isEqualTo("/custom/files/");

        assertThat(appProperties.getFormation()).isNotNull();
        assertThat(appProperties.getFormation().getThumbnail().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(8));
        assertThat(appProperties.getFormation().getThumbnail().getAllowedMimeTypes()).containsExactly("image/jpeg");
        assertThat(appProperties.getFormation().getFile().getMaxCount()).isEqualTo(5);
        assertThat(appProperties.getFormation().getFile().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(12));
        assertThat(appProperties.getFormation().getFile().getAllowedMimeTypes()).containsExactly("application/pdf", "image/png");
        assertThat(appProperties.getFormation().getCancellation().getDeadlineHours()).isEqualTo(12);
        assertThat(appProperties.getFormation().getPagination().getDefaultPageSize()).isEqualTo(15);
        assertThat(appProperties.getFormation().getDefaultCurrency()).isEqualTo("USD");
    }
}

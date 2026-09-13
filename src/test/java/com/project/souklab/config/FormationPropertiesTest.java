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
 * Isolated unit tests verifying AppProperties.FormationConfig property binding,
 * default values, and environment variable resolution.
 */
class FormationPropertiesTest {

    /**
     * Verifies that newly instantiated FormationConfig contains the specified default values.
     */
    @Test
    @DisplayName("FormationConfig has expected defaults upon instantiation")
    void defaultValuesArePresentUponInstantiation() {
        AppProperties.FormationConfig config = new AppProperties.FormationConfig();

        assertThat(config.getThumbnail()).isNotNull();
        assertThat(config.getThumbnail().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(10));

        assertThat(config.getFile()).isNotNull();
        assertThat(config.getFile().getMaxCount()).isEqualTo(10);
        assertThat(config.getFile().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(25));

        assertThat(config.getCancellation()).isNotNull();
        assertThat(config.getCancellation().getDeadlineHours()).isEqualTo(24);
    }

    /**
     * Verifies that AppProperties exposes FormationConfig defaults at the root level.
     */
    @Test
    @DisplayName("AppProperties root exposes FormationConfig with expected defaults")
    void appPropertiesRootExposesFormationDefaults() {
        AppProperties appProperties = new AppProperties();

        assertThat(appProperties.getFormation()).isNotNull();
        assertThat(appProperties.getFormation().getThumbnail().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(10));
        assertThat(appProperties.getFormation().getFile().getMaxCount()).isEqualTo(10);
        assertThat(appProperties.getFormation().getFile().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(25));
        assertThat(appProperties.getFormation().getCancellation().getDeadlineHours()).isEqualTo(24);
    }

    /**
     * Verifies that properties can be bound directly under prefix app.formation.
     */
    @Test
    @DisplayName("FormationConfig correctly binds custom configuration values from property sources")
    void bindsCustomConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test-formation", Map.of(
                "app.formation.thumbnail.max-file-size", "15MB",
                "app.formation.file.max-count", "20",
                "app.formation.file.max-file-size", "50MB",
                "app.formation.cancellation.deadline-hours", "48"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AppProperties.FormationConfig config = binder.bind("app.formation", AppProperties.FormationConfig.class).get();

        assertThat(config.getThumbnail().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(15));
        assertThat(config.getFile().getMaxCount()).isEqualTo(20);
        assertThat(config.getFile().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(50));
        assertThat(config.getCancellation().getDeadlineHours()).isEqualTo(48);
    }

    /**
     * Verifies that root AppProperties binds full nested app.formation properties tree.
     */
    @Test
    @DisplayName("AppProperties root correctly binds nested formation tree from property sources")
    void bindsRootAppPropertiesFormationTree() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test-app-formation", Map.of(
                "app.formation.thumbnail.max-file-size", "8MB",
                "app.formation.file.max-count", "5",
                "app.formation.file.max-file-size", "12MB",
                "app.formation.cancellation.deadline-hours", "12"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AppProperties appProperties = binder.bind("app", AppProperties.class).get();

        assertThat(appProperties.getFormation()).isNotNull();
        assertThat(appProperties.getFormation().getThumbnail().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(8));
        assertThat(appProperties.getFormation().getFile().getMaxCount()).isEqualTo(5);
        assertThat(appProperties.getFormation().getFile().getMaxFileSize()).isEqualTo(DataSize.ofMegabytes(12));
        assertThat(appProperties.getFormation().getCancellation().getDeadlineHours()).isEqualTo(12);
    }
}

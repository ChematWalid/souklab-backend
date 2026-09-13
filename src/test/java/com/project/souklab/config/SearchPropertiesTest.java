package com.project.souklab.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Isolated unit tests for Search configuration properties binding and verification.
 * Validates that AppProperties.Search contains no hardcoded Java defaults and that
 * configuration strictly flows from environment variables and application.properties.
 */
class SearchPropertiesTest {

    /**
     * Verifies that a newly instantiated AppProperties.Search object contains no hardcoded Java defaults.
     */
    @Test
    @DisplayName("AppProperties.Search uninitialized instance has no hardcoded Java defaults")
    void uninitializedInstanceHasNoJavaDefaults() {
        AppProperties.Search search = new AppProperties.Search();

        assertThat(search.isEnabled()).isFalse();
        assertThat(search.getUris()).isNull();
        assertThat(search.getUsername()).isNull();
        assertThat(search.getPassword()).isNull();
        assertThat(search.getConnectionTimeout()).isZero();
        assertThat(search.getReadTimeout()).isZero();
        assertThat(search.getIndexPrefix()).isNull();
        assertThat(search.getSchemaManagement()).isNull();
        assertThat(search.isSyncOnStartup()).isFalse();
    }

    /**
     * Verifies that AppProperties root exposes an uninitialized Search child instance with no hardcoded Java defaults.
     */
    @Test
    @DisplayName("AppProperties root instance provides Search configuration without Java defaults")
    void appPropertiesRootProvidesSearchInstanceWithoutJavaDefaults() {
        AppProperties appProperties = new AppProperties();

        assertThat(appProperties.getSearch()).isNotNull();
        assertThat(appProperties.getSearch().isEnabled()).isFalse();
        assertThat(appProperties.getSearch().getUris()).isNull();
        assertThat(appProperties.getSearch().getUsername()).isNull();
        assertThat(appProperties.getSearch().getPassword()).isNull();
        assertThat(appProperties.getSearch().getConnectionTimeout()).isZero();
        assertThat(appProperties.getSearch().getReadTimeout()).isZero();
        assertThat(appProperties.getSearch().getIndexPrefix()).isNull();
        assertThat(appProperties.getSearch().getSchemaManagement()).isNull();
        assertThat(appProperties.getSearch().isSyncOnStartup()).isFalse();
    }

    /**
     * Verifies that AppProperties.Search successfully binds default configuration values from environment/properties.
     */
    @Test
    @DisplayName("AppProperties.Search binds default configuration values from properties")
    void bindsDefaultConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test-env", Map.of(
                "app.search.enabled", "true",
                "app.search.uris", "http://localhost:9200",
                "app.search.connection-timeout", "5000",
                "app.search.read-timeout", "30000",
                "app.search.index-prefix", "souklab_",
                "app.search.schema-management", "create-or-update",
                "app.search.sync-on-startup", "true"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AppProperties.Search search = binder.bind("app.search", AppProperties.Search.class).get();

        assertThat(search.isEnabled()).isTrue();
        assertThat(search.getUris()).isEqualTo("http://localhost:9200");
        assertThat(search.getUsername()).isNull();
        assertThat(search.getPassword()).isNull();
        assertThat(search.getConnectionTimeout()).isEqualTo(5000);
        assertThat(search.getReadTimeout()).isEqualTo(30000);
        assertThat(search.getIndexPrefix()).isEqualTo("souklab_");
        assertThat(search.getSchemaManagement()).isEqualTo("create-or-update");
        assertThat(search.isSyncOnStartup()).isTrue();
    }

    /**
     * Verifies that AppProperties.Search successfully binds all 9 properties when bound directly.
     */
    @Test
    @DisplayName("AppProperties.Search binds all 9 properties from environment source directly")
    void bindsDirectSearchProperties() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test-env", Map.of(
                "app.search.enabled", "false",
                "app.search.uris", "http://elasticsearch.cluster.internal:9200",
                "app.search.username", "elastic_user",
                "app.search.password", "secret_pass_123",
                "app.search.connection-timeout", "8000",
                "app.search.read-timeout", "45000",
                "app.search.index-prefix", "souklab_test_",
                "app.search.schema-management", "validate",
                "app.search.sync-on-startup", "false"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AppProperties.Search search = binder.bind("app.search", AppProperties.Search.class).get();

        assertThat(search.isEnabled()).isFalse();
        assertThat(search.getUris()).isEqualTo("http://elasticsearch.cluster.internal:9200");
        assertThat(search.getUsername()).isEqualTo("elastic_user");
        assertThat(search.getPassword()).isEqualTo("secret_pass_123");
        assertThat(search.getConnectionTimeout()).isEqualTo(8000);
        assertThat(search.getReadTimeout()).isEqualTo(45000);
        assertThat(search.getIndexPrefix()).isEqualTo("souklab_test_");
        assertThat(search.getSchemaManagement()).isEqualTo("validate");
        assertThat(search.isSyncOnStartup()).isFalse();
    }

    /**
     * Verifies that the nested AppProperties tree correctly maps app.search.* to appProperties.getSearch().
     */
    @Test
    @DisplayName("AppProperties binds nested search configuration tree correctly")
    void bindsAppPropertiesSearchTree() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test-env", Map.of(
                "app.search.enabled", "true",
                "app.search.uris", "http://elasticsearch.internal:9200",
                "app.search.username", "admin",
                "app.search.password", "strongPassword",
                "app.search.connection-timeout", "6000",
                "app.search.read-timeout", "25000",
                "app.search.index-prefix", "custom_prefix_",
                "app.search.schema-management", "drop-and-create",
                "app.search.sync-on-startup", "true"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AppProperties appProperties = binder.bind("app", AppProperties.class).get();

        assertThat(appProperties.getSearch()).isNotNull();
        assertThat(appProperties.getSearch().isEnabled()).isTrue();
        assertThat(appProperties.getSearch().getUris()).isEqualTo("http://elasticsearch.internal:9200");
        assertThat(appProperties.getSearch().getUsername()).isEqualTo("admin");
        assertThat(appProperties.getSearch().getPassword()).isEqualTo("strongPassword");
        assertThat(appProperties.getSearch().getConnectionTimeout()).isEqualTo(6000);
        assertThat(appProperties.getSearch().getReadTimeout()).isEqualTo(25000);
        assertThat(appProperties.getSearch().getIndexPrefix()).isEqualTo("custom_prefix_");
        assertThat(appProperties.getSearch().getSchemaManagement()).isEqualTo("drop-and-create");
        assertThat(appProperties.getSearch().isSyncOnStartup()).isTrue();
    }
}

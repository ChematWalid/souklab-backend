package com.project.souklab.dao;

public final class TestJpaProperties {
    public static final String H2_PROPERTIES = "classpath:test-h2.properties";
    public static final String DISABLE_HIBERNATE_SEARCH =
            "spring.jpa.properties.hibernate.search.enabled=false";

    private TestJpaProperties() {
    }
}

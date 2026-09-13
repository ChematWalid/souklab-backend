package com.project.souklab.config.search;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;
import org.springframework.stereotype.Component;

/**
 * Custom analysis configurer for Hibernate Search with Elasticsearch backend.
 * Provides custom token filters, full-text analyzers, and keyword normalizers
 * tailored for Algerian artisanal heritage terminology, French diacritics, and accent-insensitive matching.
 */
@Component("customElasticsearchAnalysisConfigurer")
public class CustomElasticsearchAnalysisConfigurer implements ElasticsearchAnalysisConfigurer {

    /**
     * General full-text analyzer with lowercase and ASCII folding for craft descriptions and biographies.
     */
    public static final String ANALYZER_TEXT = "artisanal_text";

    /**
     * Name and location analyzer with lowercase and ASCII folding for names, cities, and addresses.
     */
    public static final String ANALYZER_NAME = "artisanal_name";

    /**
     * Edge n-gram analyzer for live search autocomplete and prefix matching.
     */
    public static final String ANALYZER_AUTOCOMPLETE = "artisanal_autocomplete";

    /**
     * Query analyzer paired with {@link #ANALYZER_AUTOCOMPLETE} to evaluate user prefix inputs without re-splitting.
     */
    public static final String ANALYZER_AUTOCOMPLETE_QUERY = "artisanal_autocomplete_query";

    /**
     * Normalizer for exact keyword filtering on URL slugs and administrative codes with lowercase and accent folding.
     */
    public static final String NORMALIZER_KEYWORD = "artisanal_normalizer";

    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext context) {
        context.tokenFilter("artisanal_edge_ngram")
                .type("edge_ngram")
                .param("min_gram", 2)
                .param("max_gram", 15);

        context.analyzer(ANALYZER_TEXT).custom()
                .tokenizer("standard")
                .tokenFilters("lowercase", "asciifolding");

        context.analyzer(ANALYZER_NAME).custom()
                .tokenizer("standard")
                .tokenFilters("lowercase", "asciifolding");

        context.analyzer(ANALYZER_AUTOCOMPLETE).custom()
                .tokenizer("standard")
                .tokenFilters("lowercase", "asciifolding", "artisanal_edge_ngram");

        context.analyzer(ANALYZER_AUTOCOMPLETE_QUERY).custom()
                .tokenizer("standard")
                .tokenFilters("lowercase", "asciifolding");

        context.normalizer(NORMALIZER_KEYWORD).custom()
                .tokenFilters("lowercase", "asciifolding");
    }
}

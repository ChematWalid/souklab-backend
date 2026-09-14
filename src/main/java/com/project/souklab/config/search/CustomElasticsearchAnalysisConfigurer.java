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
    public static final String ANALYZER_NAME = "artisanal_name";
    public static final String ANALYZER_AUTOCOMPLETE = "artisanal_autocomplete";
    public static final String ANALYZER_AUTOCOMPLETE_QUERY = "artisanal_autocomplete_query";
    public static final String NORMALIZER_KEYWORD = "artisanal_normalizer";
    public static final String FILTER_EDGE_NGRAM = "artisanal_edge_ngram";

    private static final String TOKENIZER_STANDARD = "standard";
    private static final String FILTER_LOWERCASE = "lowercase";
    private static final String FILTER_ASCIIFOLDING = "asciifolding";
    private static final String TYPE_EDGE_NGRAM = "edge_ngram";
    private static final String PARAM_MIN_GRAM = "min_gram";
    private static final String PARAM_MAX_GRAM = "max_gram";
    private static final int MIN_GRAM_SIZE = 2;
    private static final int MAX_GRAM_SIZE = 15;

    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext context) {
        context.tokenFilter(FILTER_EDGE_NGRAM)
                .type(TYPE_EDGE_NGRAM)
                .param(PARAM_MIN_GRAM, MIN_GRAM_SIZE)
                .param(PARAM_MAX_GRAM, MAX_GRAM_SIZE);

        context.analyzer(ANALYZER_TEXT).custom()
                .tokenizer(TOKENIZER_STANDARD)
                .tokenFilters(FILTER_LOWERCASE, FILTER_ASCIIFOLDING);

        context.analyzer(ANALYZER_NAME).custom()
                .tokenizer(TOKENIZER_STANDARD)
                .tokenFilters(FILTER_LOWERCASE, FILTER_ASCIIFOLDING);

        context.analyzer(ANALYZER_AUTOCOMPLETE).custom()
                .tokenizer(TOKENIZER_STANDARD)
                .tokenFilters(FILTER_LOWERCASE, FILTER_ASCIIFOLDING, FILTER_EDGE_NGRAM);

        context.analyzer(ANALYZER_AUTOCOMPLETE_QUERY).custom()
                .tokenizer(TOKENIZER_STANDARD)
                .tokenFilters(FILTER_LOWERCASE, FILTER_ASCIIFOLDING);

        context.normalizer(NORMALIZER_KEYWORD).custom()
                .tokenFilters(FILTER_LOWERCASE, FILTER_ASCIIFOLDING);
    }
}

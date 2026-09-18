# Search Configuration Package (`com.project.souklab.config.search`)

Configuration and startup lifecycle orchestration for Hibernate Search 8.2.2.Final backed by an Elasticsearch 8.x search cluster.

---

## Architectural Principles

- **Custom Linguistic Analysis**: Registers specialized Elasticsearch token filters, edge n-gram filters, normalizers, and analyzers (`artisanal_text`, `artisanal_name`, `artisanal_autocomplete`, `artisanal_normalizer`) tailored to Algerian cultural terminology, French diacritics, and accent-insensitive matching.
- **Asynchronous Startup Mass Indexing**: Initiates asynchronous population of the Elasticsearch search index via Hibernate Search `MassIndexer` upon application startup, controlled by `app.search.sync-on-startup`.
- **Fault-Tolerant Error Boundaries**: Mass indexing failures log non-blocking warnings to ensure local test suites and offline environments do not crash the application context when Elasticsearch is unavailable.

---

## Classes Reference

| Class | Type | Responsibility |
| :--- | :---: | :--- |
| [`CustomElasticsearchAnalysisConfigurer`](CustomElasticsearchAnalysisConfigurer.java) | `ElasticsearchAnalysisConfigurer` | Configures custom Elasticsearch tokenizers, edge n-gram filters, and analyzers for artisan indexing. |
| [`IndexLifecycleRunner`](IndexLifecycleRunner.java) | `ApplicationReadyEvent` listener | Triggers asynchronous Hibernate Search `MassIndexer` execution during application startup when enabled. |
| [`SearchIndexingService`](SearchIndexingService.java) | Transactional service | Provides the transactional EntityManager boundary required to initialize the Hibernate Search mass indexer. |

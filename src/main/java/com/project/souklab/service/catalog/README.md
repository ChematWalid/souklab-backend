# Catalog Service Package (`com.project.souklab.service.catalog`)

Service layer orchestrating the retrieval and hierarchical assembly of reference taxonomy data for the Algerian crafts ecosystem.

---

## Architectural Principles

- **Read-Only Transactionality**: Annotated with `@Transactional(readOnly = true)` to optimize Hibernate dirty-checking and JDBC connection usage.
- **In-Memory Cache Integration**: Methods are decorated with Spring `@Cacheable` using Caffeine in-memory cache names (`regions`, `categories`, `materials`, `epoques`, `techniques`) defined in `CacheConfig`.
- **Hierarchical Nesting**: Assembles two-tier tree models (Wilayas -> Communes, Categories -> Subcategories, Material Families -> Materials) with display ordering applied.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`CatalogService`](CatalogService.java) | Assembles and caches hierarchical representations for Wilayas/Communes, Categories/Subcategories, Material Families/Materials, Historical Epochs, and Craft Techniques. |

# Catalog Service Package (`com.project.souklab.service.catalog`)

Service layer orchestrating the retrieval and hierarchical assembly of reference taxonomy data for the Algerian crafts ecosystem.

---

## Architectural Principles

- **Read-Only Transactionality**: Read methods are annotated with `@Transactional(readOnly = true)` to optimize Hibernate dirty-checking and JDBC connection usage.
- **Write Transactionality & Mutability**: Mutation methods declare `@Transactional` to enforce atomic database updates, automatic slugification (`SlugUtils.toSlug(name)`), and audit log emission (`AuditLogAction.Catalog`).
- **In-Memory Cache Integration**: Read methods use Spring `@Cacheable` Caffeine caches (`regions`, `categories`, `materials`, `epoques`, `techniques`). Mutation methods declare `@CacheEvict(allEntries = true)` on corresponding caches for real-time cache consistency.
- **Integrity Enforcement**: Two-tier parent-child foreign key delete restrictions, unique slug validations, and circular reference detection in regional hierarchy via recursive CTE.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`CatalogService`](CatalogService.java) | Assembles and caches hierarchical representations for Wilayas/Communes, Categories/Subcategories, Material Families/Materials, Historical Epochs, and Craft Techniques. |
| [`AdminCatalogService`](AdminCatalogService.java) | Manages administrative CRUD mutations across all reference taxonomies with cache eviction, slug generation, circular check, parent deletion guards, and audit trail logging. |

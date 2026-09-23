# Admin Catalog DTO Package (`com.project.souklab.dto.catalog.admin`)

Request Data Transfer Objects representing inbound payloads for administrative taxonomy management operations.

---

## Classes Reference

| Class | Inbound / Outbound | Description |
| :--- | :---: | :--- |
| [`TechniqueRequest`](TechniqueRequest.java) | Inbound | Request payload for creating and updating craftsmanship techniques. Validates required name, optional slug, description, display order, and active state. |
| [`EpoqueRequest`](EpoqueRequest.java) | Inbound | Request payload for creating and updating historical eras. Validates name, optional period/era, start/end years, and active state. |
| [`RegionRequest`](RegionRequest.java) | Inbound | Request payload for creating and updating Wilayas and Communes. Validates code, name, optional parent ID, and active state. |
| [`JobCategoryRequest`](JobCategoryRequest.java) | Inbound | Request payload for creating and updating job categories. Validates name, optional slug, description, and display order. |
| [`JobSubCategoryRequest`](JobSubCategoryRequest.java) | Inbound | Request payload for creating and updating job subcategories. Validates name, parent category ID, description, and display order. |
| [`MaterialFamilyRequest`](MaterialFamilyRequest.java) | Inbound | Request payload for creating and updating raw material families. Validates name, description, and display order. |
| [`MaterialRequest`](MaterialRequest.java) | Inbound | Request payload for creating and updating raw crafting materials. Validates name, parent family ID, description, and display order. |

# Catalog DTO Package (`com.project.souklab.dto.catalog`)

Data Transfer Objects representing reference craft taxonomy schemas, administrative geography, and historical period categorizations.

---

## Classes Reference

| DTO Class | Direction | Description |
| :--- | :---: | :--- |
| [`RegionDTO`](RegionDTO.java) | Outbound | Hierarchical representation of an Algerian Wilaya containing its child Communes. |
| [`RegionSummaryDTO`](RegionSummaryDTO.java) | Outbound | Flat, lightweight summary representation of an administrative Wilaya or Commune. |
| [`JobCategoryDTO`](JobCategoryDTO.java) | Outbound | Craftsmanship category representation containing specialized subcategories. |
| [`JobSubCategoryDTO`](JobSubCategoryDTO.java) | Outbound | Detailed specialized craft subcategory representation with parent category ID. |
| [`JobSubCategorySummaryDTO`](JobSubCategorySummaryDTO.java) | Outbound | Compact summary representation of a specialized craft subcategory. |
| [`MaterialFamilyDTO`](MaterialFamilyDTO.java) | Outbound | Material family grouping authentic crafting materials. |
| [`MaterialDTO`](MaterialDTO.java) | Outbound | Detailed crafting material representation with parent family association. |
| [`MaterialSummaryDTO`](MaterialSummaryDTO.java) | Outbound | Compact summary representation of a crafting material. |
| [`EpoqueDTO`](EpoqueDTO.java) | Outbound | Traditional and historical Algerian cultural epoch representation. |
| [`EpoqueSummaryDTO`](EpoqueSummaryDTO.java) | Outbound | Compact summary representation of a historical cultural era. |
| [`TechniqueDTO`](TechniqueDTO.java) | Outbound | Craftsmanship method and artisanal technique representation. |
| [`TechniqueSummaryDTO`](TechniqueSummaryDTO.java) | Outbound | Compact summary representation of an artisanal technique. |
| [`TechniqueRequest`](admin/TechniqueRequest.java) | Inbound | Request payload for creating and updating craftsmanship techniques. |
| [`EpoqueRequest`](admin/EpoqueRequest.java) | Inbound | Request payload for creating and updating historical epochs. |
| [`RegionRequest`](admin/RegionRequest.java) | Inbound | Request payload for creating and updating Wilayas and Communes. |
| [`JobCategoryRequest`](admin/JobCategoryRequest.java) | Inbound | Request payload for creating and updating craft job categories. |
| [`JobSubCategoryRequest`](admin/JobSubCategoryRequest.java) | Inbound | Request payload for creating and updating craft job subcategories. |
| [`MaterialFamilyRequest`](admin/MaterialFamilyRequest.java) | Inbound | Request payload for creating and updating raw material families. |
| [`MaterialRequest`](admin/MaterialRequest.java) | Inbound | Request payload for creating and updating raw crafting materials. |

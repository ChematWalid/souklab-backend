# Directory DTO Package (`com.project.souklab.dto.directory`)

Data Transfer Objects and enumerations for public artisan directory queries, multi-facet filtering, and card presentations.

---

## Classes Reference

| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`ArtisanDirectoryCardDTO`](ArtisanDirectoryCardDTO.java) | Outbound DTO | Public search result card representing an artisan with bio snippet, rating, craft taxonomies, and location. |
| [`DirectorySearchFilterDTO`](DirectorySearchFilterDTO.java) | Inbound DTO | Validated search criteria encapsulation including keyword, Wilaya, category, materials, epoques, and sort options. |
| [`DirectorySortOrder`](DirectorySortOrder.java) | Enum | Supported search ranking orders (`RELEVANCE`, `RATING_DESC`, `REVIEWS_DESC`, `VIEWS_DESC`, `NAME_ASC`). |

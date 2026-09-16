# Catalog Controller Package (`com.project.souklab.controller.catalog`)

Public REST controller exposing the Algerian artisanal craftsmanship reference taxonomy. Provides read access to administrative geography, craft categories, raw materials, historical epochs, and traditional techniques.

---

## Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/catalog/regions` | Public | Retrieves hierarchical list of Algerian Wilayas with nested child Communes. |
| `GET` | `/api/v1/catalog/categories` | Public | Retrieves two-tier craftsmanship categories with nested specialized subcategories. |
| `GET` | `/api/v1/catalog/materials` | Public | Retrieves raw crafting materials taxonomy grouped by material family. |
| `GET` | `/api/v1/catalog/epoques` | Public | Retrieves traditional and historical Algerian cultural epochs ordered chronologically. |
| `GET` | `/api/v1/catalog/techniques` | Public | Retrieves traditional craftsmanship methods and artisanal techniques. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`CatalogController`](CatalogController.java) | REST controller handling `/api/v1/catalog/**` requests and delegating to `CatalogService`. |

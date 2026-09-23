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
| `POST/PUT/PATCH/DELETE` | `/api/v1/admin/catalog/techniques[/{id}]` | `Admin.CATALOG` | Administrative CRUD management for craftsmanship techniques. |
| `POST/PUT/PATCH/DELETE` | `/api/v1/admin/catalog/epoques[/{id}]` | `Admin.CATALOG` | Administrative CRUD management for historical epochs. |
| `POST/PUT/PATCH/DELETE` | `/api/v1/admin/catalog/regions[/{id}]` | `Admin.CATALOG` | Administrative CRUD management for Wilayas and Communes (cycle protected). |
| `POST/PUT/PATCH/DELETE` | `/api/v1/admin/catalog/categories[/{id}]` | `Admin.CATALOG` | Administrative CRUD management for job categories (child protected). |
| `POST/PUT/PATCH/DELETE` | `/api/v1/admin/catalog/subcategories[/{id}]` | `Admin.CATALOG` | Administrative CRUD management for job subcategories. |
| `POST/PUT/PATCH/DELETE` | `/api/v1/admin/catalog/material-families[/{id}]` | `Admin.CATALOG` | Administrative CRUD management for material families (child protected). |
| `POST/PUT/PATCH/DELETE` | `/api/v1/admin/catalog/materials[/{id}]` | `Admin.CATALOG` | Administrative CRUD management for raw crafting materials. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`CatalogController`](CatalogController.java) | Public REST controller handling `/api/v1/catalog/**` read requests and delegating to `CatalogService`. |
| [`AdminCatalogController`](AdminCatalogController.java) | Administrative REST controller handling `/api/v1/admin/catalog/**` write requests requiring `Admin.CATALOG` permission. |

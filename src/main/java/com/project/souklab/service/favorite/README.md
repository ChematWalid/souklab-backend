# Favorite services

`ArtisanFavoriteService` manages client artisan favorites with visibility filtering, cap enforcement (default 500 per client), deduplication under pessimistic concurrency lock, and directory card mapping.

| Service | Responsibility |
| --- | --- |
| [`ArtisanFavoriteService`](ArtisanFavoriteService.java) | Contract for client artisan favorites addition, pagination, status checks, and removal. |
| [`ArtisanFavoriteServiceImpl`](ArtisanFavoriteServiceImpl.java) | Production implementation with transaction boundaries, pessimistic concurrency locking, and zero N+1 entity graphs. |

## Adding a New Favorite Type (Extensibility Recipe)

The favorites subsystem is designed for type isolation. To introduce a new favorite target (e.g. `FORMATION`, `MATERIAL`, `TECHNIQUE`):

1. **Add enum value**: Declare the new entry in [`FavoriteType`](../../model/FavoriteType.java).
2. **Database migration**: Create a new Flyway migration (e.g., `V17__client_favorite_formations.sql`) defining the dedicated table (e.g. `client_favorite_formations`) with `client_id`, target FK with `ON DELETE CASCADE`, unique constraint `(client_id, target_id)`, and appropriate indexes.
3. **Model entity**: Create `ClientFavoriteFormation` extending [`ClientFavorite`](../../model/ClientFavorite.java) with `@Entity`, `@Table`, and the target `@ManyToOne` relationship.
4. **Repository**: Create `ClientFavoriteFormationRepository` extending `JpaRepository<ClientFavoriteFormation, String>` with targeted query methods.
5. **DTOs**: Add target-specific request/response DTOs in `com.project.souklab.dto.favorite`.
6. **Service**: Create `FormationFavoriteService` interface and implementation in `com.project.souklab.service.favorite`.
7. **Controller**: Create `ClientFavoriteFormationController` mapped to `/api/v1/client/favorites/formations` secured by `@PreAuthorize("@accessControl.canManageFavorites(authentication)")`.

Existing favorite code (`ClientFavoriteArtisan`, `ArtisanFavoriteService`, `ClientFavoriteArtisanController`) remains completely untouched.

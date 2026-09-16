# Profile Service Package (`com.project.souklab.service.profile`)

Application service layer managing authenticated user profile lifecycles, craft taxonomy association, and partial profile patch mutations for artisans and clients.

---

## Key Capabilities

- **Profile Retrieval (`GET /me`)**: Returns account-type-specific profile DTOs (`ArtisanResponseDTO` or `ClientProfileResponseDTO`) via the polymorphic `ProfileResponse` contract.
- **Profile Completion (`POST /complete-profile`)**: Resolves foreign key taxonomy associations (region, craft subcategory, materials, techniques, epoques) for newly registered accounts.
- **Partial Patch Updates (`PATCH /me`)**: Processes strongly-typed `UserPatchDTO` using `PatchField<T>` semantics (omitted = keep existing, explicit null = clear field) with scalar and collection resolution.
- **Separation of Concerns**: Pure domain-to-DTO mapping logic is cleanly separated into `ProfileResponseMapper`.

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`ProfileService`](ProfileService.java) | Manages `getCurrentUser`, `completeProfile`, and `patchCurrentUser` workflows for artisan and client profiles. |
| [`ProfileResponseMapper`](ProfileResponseMapper.java) | Converts `User`, `Artisan`, and `Client` domain models into typed `ProfileResponse` DTO hierarchies. |

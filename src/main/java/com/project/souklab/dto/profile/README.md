# Profile DTO Package (`com.project.souklab.dto.profile`)

Representations for artisan public showcases, client account views, and self-service profile patches.

---

## Classes Reference

| DTO Class | Direction | Description |
| :--- | :---: | :--- |
| [`ProfileResponse`](ProfileResponse.java) | Interface | Polymorphic interface implemented by `ArtisanResponseDTO` and `ClientProfileResponseDTO` exposing shared user attributes (`id`, `email`, `accountStatus`, `permissions`). |
| [`ArtisanResponseDTO`](ArtisanResponseDTO.java) | Outbound | Detailed profile representation for authenticated artisans including bio, address, website, craft IDs, and verification status. |
| [`ClientProfileResponseDTO`](ClientProfileResponseDTO.java) | Outbound | Client profile representation including company name, client type, and preferences. |
| [`ArtisanPublicViewDTO`](ArtisanPublicViewDTO.java) | Outbound | Sanitized public view of an artisan profile. Includes contact information gating flags (`contactInfoLocked`). |
| [`UserPatchDTO`](UserPatchDTO.java) | Inbound | Strongly-typed JSON Merge Patch payload using `PatchField<T>` for artisan and client profiles. |

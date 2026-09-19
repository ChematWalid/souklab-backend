package com.project.souklab.dto.profile;

import com.project.souklab.dto.common.PatchField;
import com.project.souklab.model.ClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Strongly-typed JSON Merge Patch request DTO for {@code PATCH /api/v1/auth/me}.
 *
 * <p>Every field is wrapped in {@link PatchField}{@code <T>} to model three distinct
 * patch states without additional JSON pre-processing:
 * <ul>
 *   <li>{@link PatchField#undefined()} — the field was <em>absent</em> from the JSON payload; no change is applied.</li>
 *   <li>{@link PatchField#of(Object) PatchField.of(null)} — the field was present with an explicit JSON {@code null}; the entity field is cleared.</li>
 *   <li>{@link PatchField#of(Object) PatchField.of(value)} — the field was present with a non-null value; the entity field is set to that value.</li>
 * </ul>
 *
 * <p>All fields default to {@link PatchField#undefined()} so that an empty or partially
 * populated payload results in a no-op for every unspecified field.
 *
 * <p>Artisan-specific fields: {@code bio}, {@code city}, {@code address}, {@code website},
 * {@code regionId}, {@code subCategoryId}, {@code materialIds}, {@code techniqueIds}, {@code epoqueIds}.
 *
 * <p>Client-specific fields: {@code companyName}, {@code clientType}.
 *
 * <p>Shared fields: {@code bio}, {@code city}, {@code address}, {@code regionId}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPatchDTO {

    /**
     * Artisan or client biography text.
     * Max 5000 characters; enforced via {@code @Valid} at the controller boundary.
     */
    @Builder.Default
    private PatchField<String> bio = PatchField.undefined();

    /**
     * City of residence or business location.
     * Max 100 characters.
     */
    @Builder.Default
    private PatchField<String> city = PatchField.undefined();

    /**
     * Physical street address.
     * Max 255 characters.
     */
    @Builder.Default
    private PatchField<String> address = PatchField.undefined();

    /**
     * Public website URL.
     * Must be a valid URL; max 255 characters.
     */
    @Builder.Default
    private PatchField<String> website = PatchField.undefined();

    /**
     * Identifier of the {@code Region} entity to associate.
     * Artisan-specific; max 36 characters.
     */
    @Builder.Default
    private PatchField<String> regionId = PatchField.undefined();

    /**
     * Legacy region alias for backward compatibility with clients submitting 'region' instead of 'regionId'.
     */
    @Builder.Default
    private PatchField<String> region = PatchField.undefined();

    /**
     * Resolves the effective region identifier PatchField, preferring regionId if defined,
     * otherwise falling back to the legacy region alias.
     *
     * @return the resolved PatchField for region
     */
    public PatchField<String> resolveRegionField() {
        if (regionId != null && regionId.isDefined()) {
            return regionId;
        }
        return region != null ? region : PatchField.undefined();
    }

    /**
     * Identifier of the {@code JobSubCategory} entity to associate.
     * Artisan-specific; max 36 characters.
     */
    @Builder.Default
    private PatchField<String> subCategoryId = PatchField.undefined();

    /**
     * Ordered list of {@code Material} entity identifiers to associate.
     * Artisan-specific; replaces the current material collection atomically.
     */
    @Builder.Default
    private PatchField<List<String>> materialIds = PatchField.undefined();

    /**
     * Ordered list of {@code Technique} entity identifiers to associate.
     * Artisan-specific; replaces the current technique collection atomically.
     */
    @Builder.Default
    private PatchField<List<String>> techniqueIds = PatchField.undefined();

    /**
     * Ordered list of {@code Epoque} entity identifiers to associate.
     * Artisan-specific; replaces the current epoque collection atomically.
     */
    @Builder.Default
    private PatchField<List<String>> epoqueIds = PatchField.undefined();

    /**
     * Company or organisation name.
     * Client-specific; max 255 characters.
     */
    @Builder.Default
    private PatchField<String> companyName = PatchField.undefined();

    /**
     * Client classification type (e.g. {@code INDIVIDUAL}, {@code ENTERPRISE}).
     * Client-specific; max 50 characters.
     */
    @Builder.Default
    private PatchField<ClientType> clientType = PatchField.undefined();

    /**
     * Checks whether all patch fields in this DTO are undefined.
     *
     * @return true if no fields were defined in the patch payload
     */
    public boolean isEmpty() {
        return (bio == null || !bio.isDefined())
                && (city == null || !city.isDefined())
                && (address == null || !address.isDefined())
                && (website == null || !website.isDefined())
                && (regionId == null || !regionId.isDefined())
                && (region == null || !region.isDefined())
                && (subCategoryId == null || !subCategoryId.isDefined())
                && (materialIds == null || !materialIds.isDefined())
                && (techniqueIds == null || !techniqueIds.isDefined())
                && (epoqueIds == null || !epoqueIds.isDefined())
                && (companyName == null || !companyName.isDefined())
                && (clientType == null || !clientType.isDefined());
    }
}

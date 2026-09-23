package com.project.souklab.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utility for converting arbitrary Unicode display names into URL-friendly slug strings.
 * The algorithm mirrors the slug convention used throughout the catalog taxonomy:
 * <ol>
 *   <li>NFD-normalize the input so combining diacritics become standalone code points.</li>
 *   <li>Strip all combining-diacritic code points (Unicode category Mn).</li>
 *   <li>Lower-case using {@link Locale#ENGLISH} (avoids Turkish i/İ edge cases).</li>
 *   <li>Replace every run of characters that are not {@code [a-z0-9]} with a single hyphen.</li>
 *   <li>Trim leading and trailing hyphens.</li>
 * </ol>
 *
 * <p>Examples matching existing DataSeeder literals:
 * <ul>
 *   <li>{@code "Filigrane d'argent"} → {@code "filigrane-d-argent"}</li>
 *   <li>{@code "Période Zianide"}    → {@code "periode-zianide"}</li>
 *   <li>{@code "Tizi Ouzou"}         → {@code "tizi-ouzou"}</li>
 * </ul>
 */
public final class SlugUtils {

    private SlugUtils() { }

    /**
     * Converts a display name into a lowercase, hyphen-separated URL slug.
     *
     * @param name the raw display name (may contain accents, spaces, punctuation)
     * @return the normalized slug, or an empty string if the input is null or blank
     */
    public static String toSlug(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String preprocessed = name.replace("œ", "oe")
                .replace("Œ", "oe")
                .replace("æ", "ae")
                .replace("Æ", "ae");
        String normalized = Normalizer.normalize(preprocessed, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}

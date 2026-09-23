package com.project.souklab.config;

import com.project.souklab.util.SlugUtils;

/** Immutable reference-data seed for a material. */
record MaterialSeed(String displayName, String slug, String description, int displayOrder) {

    MaterialSeed(String displayName, String description, int displayOrder) {
        this(displayName, SlugUtils.toSlug(displayName), description, displayOrder);
    }
}

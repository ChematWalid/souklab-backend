package com.project.souklab.config;

import com.project.souklab.util.SlugUtils;

/** Immutable reference-data seed for a job sub-category. */
record SubCategorySeed(String displayName, String slug, String description, int displayOrder) {

    SubCategorySeed(String displayName, String description, int displayOrder) {
        this(displayName, SlugUtils.toSlug(displayName), description, displayOrder);
    }
}

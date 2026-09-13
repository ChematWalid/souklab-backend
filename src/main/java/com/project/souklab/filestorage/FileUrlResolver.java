package com.project.souklab.filestorage;

import com.project.souklab.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Central resolver for converting internal opaque storage keys into public or CDN accessible URLs.
 * Encapsulates file-serving prefix formatting and trailing slash normalization.
 */
@Component
@RequiredArgsConstructor
public class FileUrlResolver {

    private final AppProperties appProperties;

    /**
     * Resolves an internal storage key to its public URL representation.
     *
     * @param storageKey the raw storage key (e.g. UUID filename)
     * @return the fully qualified or relative public file serving URL, or null if key is null or blank
     */
    public String toUrl(String storageKey) {
        return appProperties.getStorage().toUrl(storageKey);
    }

    /**
     * Returns the configured file serving prefix normalized with a trailing slash.
     *
     * @return normalized file serving prefix ending with '/'
     */
    public String getPrefix() {
        return appProperties.getStorage().resolveFileServingPrefix();
    }
}

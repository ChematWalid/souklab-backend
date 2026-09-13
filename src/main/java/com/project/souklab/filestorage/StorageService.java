package com.project.souklab.filestorage;

import java.io.InputStream;

/**
 * Provider-agnostic file storage abstraction interface.
 * Decouples storage operations from specific backend providers (in-memory, local disk, MinIO, S3).
 */
public interface StorageService {

    /**
     * Stores file content under a generated storage key.
     *
     * @param content stream containing the file content
     * @param originalFilename original name of the uploaded file
     * @param contentType MIME type of the content
     * @param size size in bytes
     * @return result containing the generated storage key and metadata
     */
    StorageResult store(InputStream content, String originalFilename, String contentType, long size);

    /**
     * Retrieves stored file resource by its unique key.
     *
     * @param key the unique storage key
     * @return storage resource containing readable stream and metadata
     */
    StorageResource retrieve(String key);

    /**
     * Loads stored file resource by its unique key. Alias for {@link #retrieve(String)}.
     *
     * @param key the unique storage key
     * @return storage resource containing readable stream and metadata
     */
    default StorageResource load(String key) {
        return retrieve(key);
    }

    /**
     * Deletes a stored file by its key.
     *
     * @param key the unique storage key
     */
    void delete(String key);

    /**
     * Checks if a file exists under the given key.
     *
     * @param key the unique storage key
     * @return true if the file exists, false otherwise
     */
    boolean exists(String key);
}

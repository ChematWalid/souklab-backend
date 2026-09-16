package com.project.souklab.filestorage.controller;

import com.project.souklab.filestorage.StorageResource;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.service.storage.FileAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * REST controller for serving stored files.
 * Streams content directly from the underlying storage provider with immutable caching headers.
 */
@RestController
@RequestMapping(FileServingController.BASE_PATH)
@Slf4j
public class FileServingController {

    /**
     * Single source of truth for the file-serving route, referenced by AvatarService and FileRateLimitFilter to avoid drift.
     */
    public static final String BASE_PATH = "/api/v1/files";
    public static final String DEFAULT_FILE_SERVING_PREFIX = BASE_PATH + "/";
    private static final String PUBLIC_CACHE_CONTROL = "public, max-age=31536000, immutable";
    private static final String PRIVATE_CACHE_CONTROL = "private, max-age=31536000, immutable";

    private final StorageService storageService;
    private final FileAccessService fileAccessService;

    public FileServingController(StorageService storageService, FileAccessService fileAccessService) {
        this.storageService = storageService;
        this.fileAccessService = fileAccessService;
    }

    /**
     * Serves a stored file by its unique storage key.
     * Streams the content using {@link StreamingResponseBody} and sets Content-Type, Content-Length,
     * inline Content-Disposition, and immutable Cache-Control headers.
     * Guarantees that the underlying storage InputStream is closed upon completion.
     *
     * @param key the unique storage identifier
     * @return ResponseEntity containing the streaming response body and response headers
     */
    @GetMapping("/{key}")
    public ResponseEntity<StreamingResponseBody> serveFile(@PathVariable("key") String key) {
        boolean isPublic = fileAccessService.authorize(key);
        StorageResource resource = storageService.retrieve(key);

        MediaType mediaType;
        if (resource.contentType() != null && !resource.contentType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(resource.contentType());
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        String filename = (resource.originalFilename() != null && !resource.originalFilename().isBlank())
                ? resource.originalFilename()
                : resource.key();

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream is = resource.content()) {
                if (is != null) {
                    is.transferTo(outputStream);
                }
            }
        };

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(resource.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, isPublic ? PUBLIC_CACHE_CONTROL : PRIVATE_CACHE_CONTROL)
                .body(responseBody);
    }
}

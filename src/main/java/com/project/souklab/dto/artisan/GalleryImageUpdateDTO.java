package com.project.souklab.dto.artisan;

import lombok.Data;

/** Multipart metadata used to update an artisan gallery image. */
@Data
public class GalleryImageUpdateDTO {
    private String title;
    private String caption;
}

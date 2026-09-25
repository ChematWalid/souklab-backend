# Artisan Portfolio DTO Package (`com.project.souklab.dto.artisan`)

Outbound data transfer representations for artisan craft certifications and portfolio showcase gallery images.

---

## Classes Reference

| DTO Class | Direction | Description |
| :--- | :---: | :--- |
| [`CertificationResponseDTO`](CertificationResponseDTO.java) | Outbound | Professional certification record containing title, issuing organization, issue date, and credential URL. |
| [`CertificationUpdateDTO`](CertificationUpdateDTO.java) | Multipart input | Optional certification metadata fields used by the update endpoint. |
| [`GalleryImageResponseDTO`](GalleryImageResponseDTO.java) | Outbound | Showcase portfolio image representation containing image URL, caption, display order, and creation timestamp. |
| [`GalleryImageUpdateDTO`](GalleryImageUpdateDTO.java) | Multipart input | Gallery title and caption fields used by the update endpoint. |

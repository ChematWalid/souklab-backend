# Formation DTO Package (`com.project.souklab.dto.formation`)

Contracts and data transfer schemas for masterclass authoring, curriculum updates, peer enrollment, review moderation, and course document descriptors.

---

## Classes Reference

| DTO Class | Direction | Description |
| :--- | :---: | :--- |
| [`FormationCreateDTO`](FormationCreateDTO.java) | Inbound | Payload for creating a new formation masterclass draft. |
| [`FormationUpdateDTO`](FormationUpdateDTO.java) | Inbound | Payload for modifying an existing formation's curriculum, schedule, or pricing. |
| [`FormationResponseDTO`](FormationResponseDTO.java) | Outbound | Comprehensive formation dossier for instructors containing active files, review verdicts, and enrollment counts. |
| [`FormationSummaryDTO`](FormationSummaryDTO.java) | Outbound | Lightweight summary card used for paginated author dashboards and catalog listings. |
| [`FormationPublicViewDTO`](FormationPublicViewDTO.java) | Outbound | Publicly visible representation of a published masterclass with caller enrollment status and syllabus descriptors. |
| [`FormationAuthorDTO`](FormationAuthorDTO.java) | Outbound | Embedded instructor representation within formation views. |
| [`FormationFileResponseDTO`](FormationFileResponseDTO.java) | Outbound | Upload response holding course material storage metadata and resolved access URL. |
| [`FormationFileDescriptorDTO`](FormationFileDescriptorDTO.java) | Outbound | Public syllabus file descriptor exposing filename, MIME type, size, and download path. |
| [`FormationEnrollmentResponseDTO`](FormationEnrollmentResponseDTO.java) | Outbound | Confirmation payload returned after workshop enrollment or cancellation. |
| [`FormationEnrollmentDetailDTO`](FormationEnrollmentDetailDTO.java) | Outbound | Detailed enrollment history record for the authenticated artisan. |
| [`FormationReviewRequestDTO`](FormationReviewRequestDTO.java) | Inbound | Administrative review payload containing moderation decision (`APPROVED`/`REJECTED`) and comment. |
| [`FormationReviewResponseDTO`](FormationReviewResponseDTO.java) | Outbound | Historical audit representation of an administrative moderation review verdict. |

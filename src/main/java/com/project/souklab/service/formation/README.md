# Formation Service Package (`com.project.souklab.service.formation`)

Business logic and transactional state management for peer artisan formations, masterclasses, curriculum moderation, capacity tracking, and course materials.

---

## Key Capabilities

- **Authoring Lifecycle**: Manages masterclass draft creation (gated by `isTeacher`), full updates, review submission, and soft deletion. Reverts approved/published formations to `PENDING_REVIEW` if core scheduling or pricing fields are altered.
- **Media & Antivirus Integration**: Handles multipart showcase thumbnail uploads and syllabus document attachments with ClamAV stream inspection, 10-file quota enforcement, and physical storage compensation delete upon persistence error.
- **Peer Enrollment & Capacity Guard**: Enforces workshop capacity limits, blocks instructor self-enrollment, handles cancellation cutoff deadlines, and reactivates cancelled registrations.
- **Access-Controlled Downloads**: Restricts binary syllabus document streaming strictly to the authoring instructor and confirmed enrolled participants.
- **Administrative Moderation**: Manages the administrative pending queue, approval/rejection decisions with review auditing, and catalog publication.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`FormationService`](FormationService.java) | Masterclass authoring lifecycle, thumbnail & syllabus uploads, ClamAV scanning, and review submission. |
| [`FormationEnrollmentService`](FormationEnrollmentService.java) | Peer workshop catalog browsing, seat reservation, cancellation deadlines, and protected download streaming. |
| [`AdminFormationService`](AdminFormationService.java) | Administrative moderation queue, review verdicts (approve/reject), and catalog publication. |

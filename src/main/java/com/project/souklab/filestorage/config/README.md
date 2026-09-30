# Storage Configuration Package (`com.project.souklab.filestorage.config`)

Configuration properties and bean wiring for the object storage engine.

---

## Classes Reference

| Class | Type | Description |
| :--- | :---: | :--- |
| [`StorageProperties`](StorageProperties.java) | `@ConfigurationProperties(prefix = "storage")` | Binds provider, validation, endpoint, bucket name, access key, secret key, region, and path style access configurations. |
| [`StorageConfiguration`](StorageConfiguration.java) | `@Configuration` | Registers `S3Client`, `StorageService`, and `VirusScanner` beans based on active configuration profiles. |
| [`AntivirusHealthIndicator`](AntivirusHealthIndicator.java) | `HealthIndicator` | Spring Boot Actuator health contributor monitoring ClamAV daemon connectivity. |
| [`StorageHealthIndicator`](StorageHealthIndicator.java) | `HealthIndicator` | Spring Boot Actuator health contributor verifying S3/MinIO bucket access. |

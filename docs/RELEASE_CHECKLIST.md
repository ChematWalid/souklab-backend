# Production release checklist

- [ ] Hosted CI is green, including integration tests and dependency scan.
- [ ] Working tree is clean and image is built from the reviewed commit.
- [ ] Flyway migrations reviewed and upgrade path verified.
- [ ] Fresh Elasticsearch bootstrap profile completed, then normal app schema validation verified.
- [ ] No unresolved critical/high dependency vulnerability.
- [ ] Production env file is secret-backed, mode 600, and absent from Git.
- [ ] Backup artifact exists and is encrypted; latest restore drill is recorded.
- [ ] Daily backup systemd timer is installed, enabled, and has a successful recent run.
- [ ] Restore drill restored MariaDB and validated the encrypted object inventory; application smoke flow results recorded.
- [ ] Immutable image digest recorded.
- [ ] Readiness returns HTTP 200 after deployment.
- [ ] Prometheus scrape is up and critical flows pass.

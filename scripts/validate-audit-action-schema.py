#!/usr/bin/env python3
"""Ensure every typed Java audit action is representable in MariaDB."""

from __future__ import annotations

import re
from pathlib import Path

java = Path("src/main/java/com/project/souklab/model/AuditLogAction.java").read_text()
migration = Path("src/main/resources/db/migration/V15__admin_catalog_permission.sql").read_text()

java_values = set(re.findall(r'\("([A-Z][A-Z0-9_]+)"\)', java))
sql_values = set(re.findall(r"'([A-Z][A-Z0-9_]+)'", migration))
missing = sorted(java_values - sql_values)
if missing:
    raise SystemExit("audit actions missing from V15 enum: " + ", ".join(missing))

print(f"audit action schema synchronized: {len(java_values)} typed actions")

notification_java = Path("src/main/java/com/project/souklab/model/NotificationType.java").read_text()
notification_migration = Path("src/main/resources/db/migration/V5__phase9_subscriptions_payments.sql").read_text()
notification_values = set(re.findall(r'\("([A-Z][A-Z0-9_]+)"\)', notification_java))
notification_sql_values = set(re.findall(r"'([A-Z][A-Z0-9_]+)'", notification_migration))
notification_missing = sorted(notification_values - notification_sql_values)
if notification_missing:
    raise SystemExit("notification types missing from V5 enum: " + ", ".join(notification_missing))

print(f"notification schema synchronized: {len(notification_values)} typed types")

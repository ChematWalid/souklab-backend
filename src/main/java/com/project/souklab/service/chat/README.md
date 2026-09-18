# Direct messaging

The chat module owns private one-to-one conversations, participant archive/read state,
messages, validated attachment reservations, idempotency, and realtime event dispatch.
REST resources live under `/api/v1/conversations`. STOMP commands use the configured
application prefix and `/v1/conversations/{id}/...` paths. All message mutations recheck
account status, email verification, participant ownership, and `permission:message:send`.

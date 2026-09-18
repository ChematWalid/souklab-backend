# Chat controller

`ConversationController` exposes the authenticated `/api/v1/conversations` REST resource.
`ChatStompController` handles versioned realtime commands under the configured application
destination prefix. Authentication and participant/permission checks remain in the service
layer so REST and STOMP callers share the same policy.

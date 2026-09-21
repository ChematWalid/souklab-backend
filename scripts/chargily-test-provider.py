#!/usr/bin/env python3
"""Deterministic, credential-free Chargily-compatible checkout provider.

Verification only. It never contacts Chargily and does not move money.
Set CHARGILY_FAKE_MODE to success, validation, rate_limit, provider, or
malformed. The rate_limit mode returns 429 once, then succeeds.
"""
import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

HOST = os.getenv("CHARGILY_FAKE_HOST", "127.0.0.1")
PORT = int(os.getenv("CHARGILY_FAKE_PORT", "8787"))
MODE = os.getenv("CHARGILY_FAKE_MODE", "success")
PREFIX = os.getenv("CHARGILY_FAKE_ID_PREFIX", "local-checkout")
calls = 0


class Handler(BaseHTTPRequestHandler):
    server_version = "souklab-chargily-test-provider/1"

    def log_message(self, *_args):
        return

    def send_json(self, status, body):
        encoded = json.dumps(body, separators=(",", ":")).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)

    def do_POST(self):
        global calls
        if self.path != "/checkouts":
            self.send_json(404, {"error": "not_found"})
            return
        length = int(self.headers.get("Content-Length", "0"))
        try:
            request = json.loads(self.rfile.read(length) or b"{}")
        except json.JSONDecodeError:
            self.send_json(400, {"error": "invalid_json"})
            return
        calls += 1
        mode = MODE
        if mode == "rate_limit" and calls == 1:
            self.send_json(429, {"error": "rate_limited"})
        elif mode == "validation":
            self.send_json(422, {"error": "validation_failed"})
        elif mode == "provider":
            self.send_json(503, {"error": "provider_unavailable"})
        elif mode == "malformed":
            self.send_json(200, {"unexpected": True})
        else:
            metadata = request.get("metadata") or {}
            suffix = str(metadata.get("payment_id") or calls)
            checkout_id = f"{PREFIX}-{suffix}"
            self.send_json(200, {
                "id": checkout_id,
                "checkout_url": f"http://{HOST}:{PORT}/sandbox/{checkout_id}",
                "customer_id": metadata.get("customer_id"),
                "invoice_id": metadata.get("invoice_id"),
            })


if __name__ == "__main__":
    ThreadingHTTPServer((HOST, PORT), Handler).serve_forever()

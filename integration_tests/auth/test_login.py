"""
Integration test: POST /api/auth/login

Prerequisites:
  - A user account already exists in the DB (run test_register_patient.py first)

Run:
  GATEWAY_URL=https://... USERNAME=<username> PASSWORD=<password> \
    python3 integration_tests/auth/test_login.py

Requires:
  pip install requests
"""

import os
import sys
import requests


def require_env(name):
    val = os.environ.get(name)
    if not val:
        print(f"ERROR: {name} env var is required")
        sys.exit(1)
    return val


GATEWAY_URL = require_env("GATEWAY_URL")
USERNAME    = require_env("USERNAME")
PASSWORD    = require_env("PASSWORD")

payload = {
    "username": USERNAME,
    "password": PASSWORD,
}

print(f"POST {GATEWAY_URL}/api/auth/login")
print(f"  username: {USERNAME}")

response = requests.post(
    f"{GATEWAY_URL}/api/auth/login",
    json=payload,
    timeout=10,
)

print(f"  status: {response.status_code}")
print(f"  body:   {response.text}")

assert response.status_code == 200, f"Expected 200, got {response.status_code}"

body = response.json()
assert body.get("access_token"),  "Missing access_token"
assert body.get("refresh_token"), "Missing refresh_token"
assert body.get("token_type") == "Bearer", f"Unexpected token_type: {body.get('token_type')}"

print("PASS")
sys.exit(0)

"""
Integration test: POST /api/auth/register/patient

Prerequisites:
  - Gateway reachable at GATEWAY_URL
  - A patient record exists in the DB matching MRN / FIRST_NAME / LAST_NAME

Run:
  GATEWAY_URL=https://... MRN=<mrn> FIRST_NAME=<first> LAST_NAME=<last> \
    python3 integration_tests/auth/test_register_patient.py

Optional:
  PASSWORD=MyPass1@  (default: Password1@)

Requires:
  pip install requests
"""

import os
import sys
import time
import requests


def require_env(name):
    val = os.environ.get(name)
    if not val:
        print(f"ERROR: {name} env var is required")
        sys.exit(1)
    return val


GATEWAY_URL = require_env("GATEWAY_URL")
MRN         = require_env("MRN")
FIRST_NAME  = require_env("FIRST_NAME")
LAST_NAME   = require_env("LAST_NAME")
PASSWORD    = os.environ.get("PASSWORD", "Password1@")

suffix   = str(int(time.time() * 1000))
username = f"test_user_{suffix}"
email    = f"test_{suffix}@example.com"

payload = {
    "username":  username,
    "email":     email,
    "password":  PASSWORD,
    "mrn":       MRN,
    "firstName": FIRST_NAME,
    "lastName":  LAST_NAME,
}

print(f"POST {GATEWAY_URL}/api/auth/register/patient")
print(f"  username: {username}")

response = requests.post(
    f"{GATEWAY_URL}/api/auth/register/patient",
    json=payload,
    timeout=10,
)

print(f"  status: {response.status_code}")
print(f"  body:   {response.text}")

assert response.status_code in (200, 201), f"Expected 200 or 201, got {response.status_code}"

body = response.json()
assert body.get("access_token"),  "Missing access_token"
assert body.get("refresh_token"), "Missing refresh_token"
assert body.get("token_type") == "Bearer", f"Unexpected token_type: {body.get('token_type')}"

print("PASS")
sys.exit(0)

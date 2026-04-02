# JWT RS256 Key Pair — generated once, stored in Secret Manager
# Private key: PKCS8 format  (-----BEGIN PRIVATE KEY-----)
# Public key:  X.509 format  (-----BEGIN PUBLIC KEY-----)
# Both match exactly what JwtService expects.

resource "tls_private_key" "jwt" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

resource "google_secret_manager_secret_version" "jwt_private_key" {
  secret      = google_secret_manager_secret.jwt_private_key.id
  secret_data = tls_private_key.jwt.private_key_pem_pkcs8
}

resource "google_secret_manager_secret_version" "jwt_public_key" {
  secret      = google_secret_manager_secret.jwt_public_key.id
  secret_data = tls_private_key.jwt.public_key_pem
}


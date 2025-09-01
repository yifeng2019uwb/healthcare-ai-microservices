# Create public schema
resource "postgresql_schema" "public" {
  provider = postgresql.neon
  name     = "public"
}

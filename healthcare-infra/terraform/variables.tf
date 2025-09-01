# Variables for connecting to existing Neon database
variable "neon_host" {
  description = "Neon database host"
  type        = string
}

variable "neon_port" {
  description = "Neon database port"
  type        = number
  default     = 5432
}

variable "neon_database" {
  description = "Neon database name"
  type        = string
  default     = "medconnect-healthcase"
}

variable "neon_username" {
  description = "Neon database username"
  type        = string
}

variable "neon_password" {
  description = "Neon database password"
  type        = string
  sensitive   = true
}

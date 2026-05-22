# Variables for database table deployment
variable "supabase_host" {
  description = "Supabase database host"
  type        = string
}

variable "supabase_port" {
  description = "Supabase database port"
  type        = number
  default     = 5432
}

variable "supabase_database" {
  description = "Supabase database name"
  type        = string
}

variable "supabase_username" {
  description = "Supabase database username"
  type        = string
}

variable "supabase_password" {
  description = "Supabase database password"
  type        = string
  sensitive   = true
}
# Healthcare Infrastructure

> **Status**: Only Supabase PostgreSQL is implemented. Other infrastructure is under research.

## ✅ **What's Implemented**

**Supabase PostgreSQL Database** - Fully configured
- Location: `terraform/supabase/`
- 6 database tables configured
- Deployment scripts ready
- See: `CURRENT_INFRASTRUCTURE.md` for details

## ❌ **What's NOT Implemented**

### Deprecated Design (AWS-based)
- `INFRASTRUCTURE_DESIGN.md` - ⚠️ **DEPRECATED**
  - Contains AWS design (S3, IAM, CloudWatch)
  - **Not implemented** - Design docs only
  - Kept for reference

### Research Only (No Implementation Yet)
- Other infrastructure options (Railway, Azure, etc.) - Under research
- No implementation decisions made yet
- See `CURRENT_INFRASTRUCTURE.md` for details

## 📁 **Directory Structure**

```
healthcare-infra/
├── README.md                      # This file
├── CURRENT_INFRASTRUCTURE.md      # Actual implementation status
├── INFRASTRUCTURE_DESIGN.md       # ⚠️ Deprecated AWS design (reference only)
├── terraform/
│   └── supabase/                  # ✅ Only implemented infrastructure
│       ├── deploy-supabase.sh
│       ├── 01_users.tf
│       └── ... (other table files)
└── scripts/                       # Deployment scripts
```

## 🎯 **Next Steps**

1. ✅ Use Supabase PostgreSQL (already configured)
2. 🔍 Research deployment options (Railway, Docker, Azure, etc.)
3. 🔍 Research file storage options (when needed for Phase 2)
4. 🔍 Research monitoring options (when needed)

**No rush** - Take time to research and make informed decisions.

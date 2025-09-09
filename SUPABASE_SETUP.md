# Supabase Setup Guide

> **Quick setup for Supabase Database + Auth + Storage**

---

## 🚀 **Step 1: Create Supabase Account**

1. **Go to [supabase.com](https://supabase.com)**
2. **Click "Start your project"**
3. **Sign up with Email** (recommended for separate account management)
   - Use a dedicated email for Supabase projects
   - Create a strong password (save it securely!)
4. **Verify your email** if required

---

## 🏗️ **Step 2: Create New Project**

1. **Click "New Project"**
2. **Choose Organization** (or create one)
3. **Project Details:**
   - **Name**: `healthcare-ai-microservices`
   - **Database Password**: Generate a strong password (save it!)
   - **Region**: Choose closest to your location
4. **Click "Create new project"**
5. **Wait 2-3 minutes** for project to be ready

---

## 🔑 **Step 3: Get Database Connection String**

1. **Go to Settings → Database**
2. **Scroll down to "Connection string"**
3. **Copy the "URI" connection string**
4. **It looks like**: `postgresql://postgres:[YOUR-PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres`

---

## 📋 **Step 4: Save Your Credentials**

Create a file `supabase-credentials.txt` (don't commit this!):

```
# Supabase Account Credentials
Supabase Email: [YOUR-SUPABASE-EMAIL]
Supabase Password: [YOUR-SUPABASE-PASSWORD]

# Project Credentials
Project URL: https://[PROJECT-REF].supabase.co
Database URL: postgresql://postgres:[DATABASE-PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres
Database Password: [DATABASE-PASSWORD]
Project Reference: [PROJECT-REF]
```

---

## 🔧 **Step 5: Update Our Configuration**

1. **Open `services/shared/src/main/resources/application.yml`**
2. **Replace the database URL with Supabase URL:**
   ```yaml
   spring:
     datasource:
       url: "postgresql://postgres:[PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres?sslmode=require"
       username: postgres
       password: [YOUR-PASSWORD]
   ```

---

## 🐳 **Step 6: Update Docker Configuration**

1. **Open `docker/docker-compose.yml`**
2. **Update environment variables:**
   ```yaml
   environment:
     SPRING_DATASOURCE_URL: "postgresql://postgres:[PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres?sslmode=require"
     SPRING_DATASOURCE_USERNAME: postgres
     SPRING_DATASOURCE_PASSWORD: [YOUR-PASSWORD]
   ```

---

## ✅ **Step 7: Test Connection**

1. **Start Docker services:**
   ```bash
   cd docker
   docker-compose up --build
   ```

2. **Test patient registration:**
   ```bash
   curl -X POST http://localhost:8080/api/patients \
     -H "Content-Type: application/json" \
     -d '{
       "externalUserId": "user123",
       "firstName": "John",
       "lastName": "Doe",
       "email": "john.doe@example.com",
       "phone": "+15551234567",
       "dateOfBirth": "1990-05-15",
       "gender": "MALE",
       "streetAddress": "123 Main St",
       "city": "New York",
       "state": "NY",
       "postalCode": "10001",
       "country": "USA",
       "emergencyContactName": "Jane Doe",
       "emergencyContactPhone": "+15559876543"
     }'
   ```

---

## 🆓 **Free Tier Limits**

- **Database**: 500MB storage, 2GB bandwidth/month
- **Auth**: 50,000 monthly active users
- **Storage**: 1GB file storage, 2GB bandwidth/month

---

## 🔒 **Security Notes**

- ✅ **Separate account** - Use dedicated email/password for Supabase
- ✅ **Never commit passwords** to git
- ✅ **Use environment variables** in production
- ✅ **Enable Row Level Security** in Supabase dashboard
- ✅ **Set up proper database policies**
- ✅ **Keep credentials secure** - Store in password manager

---

## 🆘 **Troubleshooting**

### **Connection Issues:**
- Check if password is correct
- Verify project is fully created (wait 2-3 minutes)
- Ensure SSL mode is set to `require`

### **Authentication Issues:**
- Make sure you're using the correct database password
- Check if the project reference is correct in the URL

---

## 📚 **Next Steps**

1. **Set up database tables** using our Terraform scripts
2. **Configure Row Level Security** policies
3. **Set up Supabase Auth** for user management
4. **Configure file storage** for medical documents

---

*This guide will get you up and running with Supabase in under 10 minutes!*

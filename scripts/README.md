# Local CI/CD Testing Scripts

> **🧪 Test CI/CD Pipeline Locally**
>
> These scripts help you test and debug the CI/CD pipeline before pushing to GitHub.

## 📁 **Available Scripts**

### **`test-ci.sh` - Full CI Test**
Simulates the complete GitHub Actions CI pipeline locally.

**Usage:**
```bash
./scripts/test-ci.sh
```

**What it does:**
- ✅ Checks Java version (17+)
- ✅ Checks Maven availability
- ✅ Runs unit tests
- ✅ Builds the application
- ✅ Verifies JAR artifacts
- ✅ Checks test reports

### **`debug-ci.sh` - Debug CI Issues**
Helps identify what's failing in the CI pipeline.

**Usage:**
```bash
./scripts/debug-ci.sh
```

**What it does:**
- 🔍 Checks project structure
- 🔍 Verifies Java and Maven
- 🔍 Tests individual Maven commands
- 🔍 Identifies specific failures

## 🚀 **Quick Start**

### **1. Test Before Push**
```bash
# Run full CI test
./scripts/test-ci.sh

# If it passes, you're ready to push!
git add .
git commit -m "Your changes"
git push
```

### **2. Debug CI Failures**
```bash
# If CI fails on GitHub, run debug locally
./scripts/debug-ci.sh

# Fix any issues found
# Then test again
./scripts/test-ci.sh
```

## 🔧 **Requirements**

### **Local Environment**
- **Java 17+** - Required for building
- **Maven** - Build tool
- **Bash** - Script execution

### **Project Structure**
```
services/
├── pom.xml              # Main Maven project
├── gateway/             # Gateway service
├── auth/                # Auth service
├── patient/             # Patient service
├── provider/            # Provider service
└── appointment/         # Appointment service
```

## 📊 **Script Output**

### **Success Output**
```
🚀 Starting Local CI/CD Test...
==================================
✅ Found project structure
✅ Java version: openjdk version "17.0.2"
✅ Maven version: Apache Maven 3.8.6
✅ Unit tests passed
✅ Build successful
✅ Found 5 JAR file(s)
✅ Local CI/CD test completed successfully!
```

### **Error Output**
```
❌ Java 17+ required. Found: 11
❌ Unit tests failed
❌ Build failed
```

## 🛠️ **Troubleshooting**

### **Common Issues**

1. **Java Version**
   ```bash
   # Check Java version
   java -version

   # Install Java 17 if needed
   # macOS: brew install openjdk@17
   # Ubuntu: sudo apt install openjdk-17-jdk
   ```

2. **Maven Not Found**
   ```bash
   # Check Maven
   mvn -version

   # Install Maven if needed
   # macOS: brew install maven
   # Ubuntu: sudo apt install maven
   ```

3. **Project Structure**
   ```bash
   # Make sure you're in the project root
   ls services/pom.xml

   # Should show: services/pom.xml
   ```

### **CI-Specific Issues**

1. **Test Failures**
   - Run `./scripts/debug-ci.sh` to see specific test errors
   - Check test files in `services/*/src/test/java/`

2. **Build Failures**
   - Check for compilation errors
   - Verify all dependencies in `pom.xml`

3. **Missing Artifacts**
   - Ensure Maven build completes successfully
   - Check `services/*/target/` directories

## 🎯 **Best Practices**

### **Before Every Push**
1. Run `./scripts/test-ci.sh`
2. Fix any issues found
3. Push only when tests pass

### **When CI Fails**
1. Run `./scripts/debug-ci.sh`
2. Identify the specific failure
3. Fix the issue locally
4. Test again before pushing

### **Development Workflow**
```bash
# Make changes
# ... edit code ...

# Test locally
./scripts/test-ci.sh

# If successful, commit and push
git add .
git commit -m "Your changes"
git push
```

---

*These scripts help ensure your CI/CD pipeline works correctly before pushing to GitHub.*

# GitHub Actions CI/CD

> **🚀 Automated Build and Test Pipeline**
>
> This directory contains GitHub Actions workflows for automated build and testing.

## 📁 **Workflows**

### **`ci.yml` - Build and Test**
- **Triggers**: Push to `main`/`develop` branches, Pull Requests
- **Actions**:
  - ✅ **Unit Tests** - Run Maven unit tests
  - ✅ **Build** - Compile and package Java applications
  - ✅ **Test Reports** - Generate and upload test results
  - ✅ **Artifacts** - Upload JAR files for deployment

## 🔧 **Configuration**

### **Java Setup**
- **JDK Version**: 17 (Temurin distribution)
- **Build Tool**: Maven
- **Test Framework**: JUnit (via Spring Boot)

### **Caching**
- **Maven Dependencies**: Cached for faster builds
- **Cache Key**: Based on `pom.xml` file hash

### **Test Environment**
- **Profile**: `test` (Spring profiles)
- **Database**: Not required for unit tests
- **Dependencies**: All external dependencies mocked

## 📊 **Workflow Steps**

1. **Checkout Code** - Get latest source code
2. **Setup Java 17** - Install JDK and Maven
3. **Cache Dependencies** - Speed up build process
4. **Run Unit Tests** - Execute all test cases
5. **Build Application** - Compile and package
6. **Generate Reports** - Create test result reports
7. **Upload Artifacts** - Save JAR files for deployment

## 🎯 **Benefits**

✅ **Automated Testing** - Every commit is tested
✅ **Fast Feedback** - Know immediately if code breaks
✅ **Quality Assurance** - Prevent broken code from merging
✅ **Build Verification** - Ensure code compiles successfully
✅ **Artifact Generation** - Ready-to-deploy JAR files

## 🔄 **Usage**

### **Automatic Triggers**
- Push to `main` or `develop` branches
- Create or update Pull Requests

### **Manual Triggers**
- Go to Actions tab in GitHub
- Select "Build and Test" workflow
- Click "Run workflow"

### **View Results**
- Check Actions tab for build status
- Download artifacts from successful builds
- View test reports and coverage

---

*This CI/CD pipeline focuses on build and test automation to ensure code quality and reliability.*

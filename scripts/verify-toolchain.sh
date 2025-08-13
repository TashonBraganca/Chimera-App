#!/bin/bash
# Toolchain verification script for Chimera MVP development environment

set -e

echo "🔍 Verifying Chimera MVP Development Environment"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Track verification results
ERRORS=0
WARNINGS=0

check_command() {
    local cmd=$1
    local expected_version=$2
    local version_flag=${3:-"--version"}
    
    echo -n "Checking $cmd... "
    
    if command -v "$cmd" &> /dev/null; then
        local version_output=$($cmd $version_flag 2>&1 | head -n1)
        echo -e "${GREEN}✓${NC} Found: $version_output"
        
        if [ -n "$expected_version" ]; then
            if echo "$version_output" | grep -q "$expected_version"; then
                echo -e "  ${GREEN}✓${NC} Version matches expected: $expected_version"
            else
                echo -e "  ${YELLOW}⚠${NC} Version may not match expected: $expected_version"
                ((WARNINGS++))
            fi
        fi
    else
        echo -e "${RED}✗${NC} Not found"
        ((ERRORS++))
    fi
    echo
}

echo "Backend Development Tools:"
echo "-------------------------"
check_command "java" "21" "--version"
check_command "mvn" "3.9" "--version"
check_command "docker" "24" "--version"
check_command "psql" "16" "--version"

echo -e "\nAndroid Development Tools:"
echo "-------------------------"
check_command "adb" "" "version"

# Check Android SDK
if [ -n "$ANDROID_HOME" ]; then
    echo -n "Checking Android SDK... "
    if [ -d "$ANDROID_HOME" ]; then
        echo -e "${GREEN}✓${NC} Found at: $ANDROID_HOME"
        
        # Check for required API levels
        if [ -d "$ANDROID_HOME/platforms/android-34" ]; then
            echo -e "  ${GREEN}✓${NC} API 34 (Android 14) available"
        else
            echo -e "  ${YELLOW}⚠${NC} API 34 (Android 14) not found"
            ((WARNINGS++))
        fi
        
        if [ -d "$ANDROID_HOME/platforms/android-26" ]; then
            echo -e "  ${GREEN}✓${NC} API 26 (Android 8.0) available"
        else
            echo -e "  ${RED}✗${NC} API 26 (Android 8.0) not found (minimum required)"
            ((ERRORS++))
        fi
    else
        echo -e "${RED}✗${NC} Directory not found: $ANDROID_HOME"
        ((ERRORS++))
    fi
else
    echo -e "${YELLOW}⚠${NC} ANDROID_HOME environment variable not set"
    ((WARNINGS++))
fi
echo

echo -e "\nProject Structure Validation:"
echo "----------------------------"

# Check required directories
directories=("backend" "android" "docs" "scripts" "infra")
for dir in "${directories[@]}"; do
    echo -n "Checking $dir/... "
    if [ -d "$dir" ]; then
        echo -e "${GREEN}✓${NC} Exists"
    else
        echo -e "${RED}✗${NC} Missing"
        ((ERRORS++))
    fi
done

# Check required documentation
docs=("tech_stack.md" "architecture_overview.md" "health_observability_plan.md" "ci_cd_plan.md")
for doc in "${docs[@]}"; do
    echo -n "Checking docs/$doc... "
    if [ -f "docs/$doc" ]; then
        echo -e "${GREEN}✓${NC} Exists"
    else
        echo -e "${RED}✗${NC} Missing"
        ((ERRORS++))
    fi
done

# Check backend structure
echo -n "Checking backend/pom.xml... "
if [ -f "backend/pom.xml" ]; then
    echo -e "${GREEN}✓${NC} Exists"
    
    # Validate Java version in pom.xml
    if grep -q "<java.version>21</java.version>" "backend/pom.xml"; then
        echo -e "  ${GREEN}✓${NC} Java 21 configured"
    else
        echo -e "  ${YELLOW}⚠${NC} Java version may not be 21"
        ((WARNINGS++))
    fi
    
    # Validate Spring Boot version
    if grep -q "3.3." "backend/pom.xml"; then
        echo -e "  ${GREEN}✓${NC} Spring Boot 3.3.x configured"
    else
        echo -e "  ${YELLOW}⚠${NC} Spring Boot version may not be 3.3.x"
        ((WARNINGS++))
    fi
else
    echo -e "${RED}✗${NC} Missing"
    ((ERRORS++))
fi

# Check Android structure
echo -n "Checking android/build.gradle.kts... "
if [ -f "android/build.gradle.kts" ]; then
    echo -e "${GREEN}✓${NC} Exists"
else
    echo -e "${RED}✗${NC} Missing"
    ((ERRORS++))
fi

echo -e "\nRuntime Environment Check:"
echo "-------------------------"

# Check Docker is running
echo -n "Checking Docker daemon... "
if docker info &> /dev/null; then
    echo -e "${GREEN}✓${NC} Running"
else
    echo -e "${YELLOW}⚠${NC} Not running or not accessible"
    ((WARNINGS++))
fi

# Test Maven compilation
echo -n "Testing Maven compilation... "
if [ -f "backend/pom.xml" ]; then
    cd backend
    if mvn compile -q &> /dev/null; then
        echo -e "${GREEN}✓${NC} Successful"
    else
        echo -e "${RED}✗${NC} Failed"
        ((ERRORS++))
    fi
    cd ..
else
    echo -e "${RED}✗${NC} No pom.xml found"
    ((ERRORS++))
fi

# Test Gradle compilation (Android)
echo -n "Testing Gradle compilation... "
if [ -f "android/build.gradle.kts" ] && [ -f "android/gradlew" ]; then
    cd android
    if ./gradlew compileDebugKotlin -q &> /dev/null; then
        echo -e "${GREEN}✓${NC} Successful"
    else
        echo -e "${YELLOW}⚠${NC} Failed or incomplete Android setup"
        ((WARNINGS++))
    fi
    cd ..
else
    echo -e "${YELLOW}⚠${NC} Android project not fully configured"
    ((WARNINGS++))
fi

echo -e "\nSecurity Check:"
echo "---------------"

# Check for secrets in git history (basic check)
echo -n "Checking for potential secrets... "
if command -v git &> /dev/null; then
    if git log --all --grep="password\|secret\|key" --oneline | wc -l | grep -q "^0$"; then
        echo -e "${GREEN}✓${NC} No obvious secrets in commit messages"
    else
        echo -e "${YELLOW}⚠${NC} Found potential secrets in commit messages"
        ((WARNINGS++))
    fi
else
    echo -e "${YELLOW}⚠${NC} Git not available for check"
    ((WARNINGS++))
fi

# Check for .env files
echo -n "Checking for .env files... "
if find . -name ".env*" -type f | grep -q "."; then
    echo -e "${YELLOW}⚠${NC} Found .env files (ensure they're in .gitignore)"
    ((WARNINGS++))
else
    echo -e "${GREEN}✓${NC} No .env files found"
fi

echo -e "\nVerification Summary:"
echo "===================="

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✅ All checks passed! Development environment is ready.${NC}"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Environment mostly ready with $WARNINGS warnings.${NC}"
    echo "Please address warnings for optimal development experience."
    exit 0
else
    echo -e "${RED}❌ Environment setup incomplete: $ERRORS errors, $WARNINGS warnings.${NC}"
    echo "Please fix errors before proceeding with development."
    exit 1
fi
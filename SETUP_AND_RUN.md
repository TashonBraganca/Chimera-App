# Chimera MVP - Setup and Run Guide

## 🚀 Quick Start

### 1. Run Backend Server
```cmd
# Option 1: Use the batch file (Recommended)
D:\Chimera MVP\run-backend.bat

# Option 2: Manual commands
cd "D:\Chimera MVP\backend"
gradlew.bat runIsolated
```

**Backend will run on:** http://localhost:8080

**Available endpoints:**
- `GET /` - Home page with status
- `GET /health` - Health check JSON
- `GET /api/test` - API test endpoint

### 2. Build Android App
```cmd
# Option 1: Use the batch file (Recommended)  
D:\Chimera MVP\android\build-debug.bat

# Option 2: Manual commands
cd "D:\Chimera MVP\android"
gradlew.bat assembleDebug
```

**APK Location:** `android\app\build\outputs\apk\debug\app-debug.apk`

### 3. Run Android App

#### Option A: Android Studio (Recommended)
1. Open Android Studio
2. Open project: `D:\Chimera MVP\android`
3. Wait for Gradle sync to complete
4. Click the green "Run" button (or press Shift+F10)
5. Select device/emulator and run

#### Option B: Command Line
```cmd
# Install APK to connected device
cd "D:\Chimera MVP\android"
gradlew.bat installDebug

# Or manually install the APK
adb install app\build\outputs\apk\debug\app-debug.apk
```

## 🔧 Prerequisites

### Required Software:
- **Java 21** (for backend)
- **Android Studio** (for Android development)
- **Android SDK** (comes with Android Studio)

### Verify Installation:
```cmd
java -version        # Should show Java 21
gradlew.bat --version  # Should work in both backend and android directories
```

## 📱 Testing the Integration

1. **Start Backend:** Run `run-backend.bat` - backend runs on http://localhost:8080
2. **Start Android:** Build and run the Android app 
3. **Test Connection:** The Android app should connect to localhost:8080

### Android App Screens:
1. **Input Screen:** Enter investment amount, time horizon, risk preference
2. **Results Screen:** View ranked assets with scores and confidence
3. **Chat Screen:** Ask questions about rankings and get AI explanations

### Backend API Testing:
```cmd
# Test if backend is running
curl http://localhost:8080/health

# Expected response:
{
  "status": "UP",
  "application": "chimera-backend", 
  "version": "0.1.0-SNAPSHOT",
  "phase": "M1-M6 Complete",
  "timestamp": "...",
  "message": "Backend is running successfully!"
}
```

## 🐛 Troubleshooting

### Backend Issues:
- **Port 8080 busy:** Stop other applications using port 8080
- **Java errors:** Ensure Java 21 is installed and JAVA_HOME is set
- **Build fails:** Run `gradlew.bat clean build` to clean and rebuild

### Android Issues:
- **Gradle sync fails:** Check Android SDK is properly installed
- **Build errors:** Ensure Android SDK 34 is installed
- **App crashes:** Check device logs with `adb logcat`

### Common Fixes:
```cmd
# Clean builds
cd backend && gradlew.bat clean
cd android && gradlew.bat clean

# Rebuild everything
cd backend && gradlew.bat build
cd android && gradlew.bat assembleDebug
```

## 📊 Current Status

**Phases Complete:**
- ✅ M0: Data Sources & Compliance 
- ✅ M1: Repository Foundation
- ✅ M2: Data Ingestion (EOD + News)
- ✅ M3: Features, Scoring, Calibration
- ✅ M4: LLM Explain (RAG-first)
- ✅ M5: Backend APIs  
- ✅ M6: Android App (3-screen UX)

**Next Steps:**
- M7: Deploy & Observability
- M8: Validation & Calibration
- M9: Beta & Feedback
- M10: Monetization Toggles
- M11: Launch Prep & Listing

## 🎯 Key Features Implemented

### Backend:
- Spring Boot 3.3.4 with Java 21
- Feature engineering with multiple horizons
- Asset ranking with transparent scoring
- LLM integration with citations
- Health monitoring and observability
- Clean architecture with repository pattern

### Android:
- Jetpack Compose UI with Material 3
- Offline-first architecture with Room database  
- Hilt dependency injection
- 3-screen UX: Input → Results → Chat
- Network handling with Retrofit
- Background data sync with WorkManager

**Happy Coding! 🚀**
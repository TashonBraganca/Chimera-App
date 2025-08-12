# 🚀 Chimera MVP - Flutter App

Beautiful, AI-powered investment analysis app built with Flutter and Material 3.

## ✨ Features

- **Investment Input**: Intuitive form with amount, horizon, and risk preference
- **AI-Powered Rankings**: Real-time asset analysis with confidence scores
- **Smart Chat**: AI assistant with citations and explanations
- **Offline Support**: Cached data and mock fallbacks
- **Material 3 Design**: Modern, beautiful UI with glassmorphism effects

## 🏃‍♂️ Quick Start

### Prerequisites
- Flutter SDK (3.16.0 or higher)
- Dart SDK (3.2.0 or higher)
- Running backend on http://localhost:8080

### Setup & Run

```bash
# Get dependencies
flutter pub get

# Generate code
flutter packages pub run build_runner build

# Run the app
flutter run
```

### Backend Connection
The app connects to the Chimera backend running on `http://localhost:8080`.
Make sure your backend is running:

```bash
# In backend directory
gradlew.bat runIsolated
```

## 📱 App Structure

```
lib/
├── core/           # Theme, constants
├── data/           # Models, services, repositories  
├── presentation/   # Screens, widgets, providers
└── main.dart       # App entry point
```

## 🎨 Design System

- **Colors**: Material 3 with custom seed color (#1976D2)
- **Typography**: Inter font family
- **Components**: Glass morphism, smooth animations
- **Theming**: Automatic dark/light mode support

## 🔧 State Management

Using Riverpod for:
- Connection status tracking
- Investment form state
- Asset rankings data
- Chat message handling
- Offline caching

## 📦 Key Dependencies

- `flutter_riverpod` - State management
- `go_router` - Navigation
- `dio` - HTTP client
- `flutter_animate` - Smooth animations
- `shared_preferences` - Local storage

## 🌐 API Integration

### Endpoints:
- `GET /health` - Backend health check
- `POST /api/rank` - Get asset rankings
- `POST /api/chat` - AI chat responses

### Error Handling:
- Automatic retries for timeouts
- Graceful fallback to cached/mock data
- User-friendly error messages

## 🎯 Performance

- **Offline-first** architecture
- **Smart caching** with SharedPreferences
- **Optimized animations** at 60fps
- **Lazy loading** and pagination ready

## 🚀 Build & Deploy

```bash
# Build APK
flutter build apk --release

# Build for iOS
flutter build ios --release
```

Built with ❤️ for the Chimera MVP
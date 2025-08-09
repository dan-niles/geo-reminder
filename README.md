# GeoReminder 📍

A location-based reminder Android app built with Kotlin that alerts you when approaching your destination. Perfect for commuters who want to be notified before their bus/train stop.

## Features

- **Location-based Reminders**: Set alerts for specific locations with customizable radius
- **Geofencing Technology**: Uses Google's Geofencing API for accurate location detection
- **Background Monitoring**: Works even when the app is closed
- **Minimal UI**: Clean, Material Design interface
- **Battery Optimized**: Smart location tracking to preserve battery life
- **Custom Notifications**: Configurable alert messages and sounds

## Setup

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK with minimum API level 24 (Android 7.0)
- Google Play Services installed on target device

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd GeoReminder
   ```

2. **Get Google Maps API Key**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing one
   - Enable the following APIs:
     - Maps SDK for Android
     - Places API
     - Geocoding API (optional)
   - Create credentials (API Key)
   - Restrict the API key to your app's package name

3. **Configure API Key**
   - Copy `local.properties.example` to `local.properties`
   - Replace `your_google_maps_api_key_here` with your actual API key:
     ```
     MAPS_API_KEY=AIzaSyC...your_actual_key
     ```

4. **Build and Run**
   - Open project in Android Studio
   - Sync project with Gradle files
   - Run on device or emulator

## Permissions

The app requires the following permissions:
- **Location Access**: To detect when you approach saved locations
- **Background Location**: To work when app is closed (Android 10+)
- **Notifications**: To alert you about location reminders
- **Wake Lock**: To ensure notifications work properly

## Architecture

- **MVVM Pattern**: Clean separation of concerns
- **Repository Pattern**: Centralized data management
- **Room Database**: Local storage for reminders
- **Kotlin Coroutines**: Asynchronous operations
- **Google Play Services**: Location and geofencing services

## Usage

1. **Grant Permissions**: Allow location and notification access
2. **Add Reminder**: Tap the + button and set up your location reminder
3. **Set Location**: Choose your destination (currently simplified - uses preset location)
4. **Configure Radius**: Adjust how close you want to be before getting alerted
5. **Save**: The app will monitor your location and notify you

## Project Structure

```
app/src/main/java/com/example/georeminder/
├── data/           # Database entities, DAOs, Repository
├── service/        # Background geofencing service
├── receiver/       # Broadcast receivers for geofence events
├── ui/            # Activities, ViewModels, Adapters
├── utils/         # Helper classes and utilities
└── ...
```

## Future Enhancements

- Google Places integration for location search
- Route intelligence and learning
- Multiple stop warnings
- Offline maps support
- Transportation mode detection
- Recurring reminders for daily commute

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is open source and available under the MIT License.
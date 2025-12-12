# <img alt="app icon" src=".github/assets/app_icon.svg" width="48" /> mpvDroid

A media player for Android based on [mpv-android](https://github.com/mpv-android/mpv-android) with an integrated library browser and modern Material 3 UI.


## Key Features
- **Integrated Library Browser**: Browse your local media files with folder and video list views
- **Recently Played**: Quick access to your viewing history
- **Playlists**: Create and manage custom playlists
- **Network Streaming**: Support for SMB, FTP, HTTP, and WebDAV protocols
- **Subtitle Support**: Automatic subtitle search and download from OpenSubtitles
- **Modern UI**: Material 3 design with bottom navigation
- **Smart Permissions**: First-run permission flow with proper Android 13+ media access
- **Nicer Player UI**: Enhanced playback controls
- **Better Playback History**: Comprehensive history tracking
- **Sleep Timer & Speed Presets**: Convenient playback controls
- **Smooth Picture-in-Picture**: Optimized PiP experience

## Package Information
- **Application ID**: `com.uw.mpvDroid`
- **Version**: 1.0.0 (versionCode 13)
- **Minimum SDK**: Android 5.0 (API 21)
- **Target SDK**: Android 16 (API 36)



## Building from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/umeshwayakole27/mpvDroid_kt.git
   cd mpvDroid_kt
   ```

2. Build the app:
   ```bash
   ./gradlew assembleDebug    # For debug build
   ./gradlew assembleRelease  # For release build (requires signing)
   ```

3. Install on device:
   ```bash
   ./gradlew installDebug
   ```


## Third-Party Libraries
This project uses the following major dependencies:
- [mpv-android](https://github.com/mpv-android/mpv-android) - Core media playback
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Room](https://developer.android.com/topic/libraries/architecture/room) - Local database
- [Retrofit](https://square.github.io/retrofit/) & [Moshi](https://github.com/square/moshi) - Networking
- [Accompanist](https://google.github.io/accompanist/) - Compose utilities
- [smbj](https://github.com/hierynomus/smbj), [sardine-android](https://github.com/thegrizzlylabs/sardine-android), [commons-net](https://commons.apache.org/proper/commons-net/) - Network protocols
- [MediaInfo](https://mediaarea.net/en/MediaInfo) - Media file analysis
- [NanoHTTPD](https://github.com/NanoHttpd/nanohttpd) - Local proxy server
## Acknowledgments
- [mpv-android](https://github.com/mpv-android) for the base mpv library
- [mpvKt](https://github.com/abdallahmehiz/mpvKt) for the foundation and excellent player UI
- [mpvEx](https://github.com/jarnedemeulemeester/mpvEx) for the library browser and network features

> **Note**: This is a fork of [mpvKt](https://github.com/abdallahmehiz/mpvKt) with enhanced library features integrated from [mpvEx](https://github.com/jarnedemeulemeester/mpvEx).


## License
See [LICENSE](LICENSE) file for details.

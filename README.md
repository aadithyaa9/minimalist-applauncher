# Minimalist Launcher

A distraction-free Android launcher designed to promote intentionality, focus, and mindfulness. This launcher replaces the standard icon-heavy interface with a clean, text-only environment optimized for OLED displays.

## Core Philosophy

Modern smartphones are designed to capture and hold your attention through vibrant colors and recognizable icons. This launcher strips away those psychological hooks, forcing a more conscious interaction with your device.

## Key Features

### Absolute Black Theme
The interface uses a pure black background (#000000) throughout all screens. This not only reduces visual clutter but also significantly saves battery life on devices with OLED screens.

### Text-Only Interface
All application icons and branding colors are removed. Apps are represented strictly by their names in clean typography. This eliminates the "notification red" and "icon recognition" triggers that lead to mindless scrolling.

### Intentional Usage Timer
Before any application is launched, the system prompts for a time limit. Once the specified duration expires, the launcher automatically returns you to the home screen, acting as a digital nudge to maintain awareness of your time.

### Persistent Habit Tracking
A built-in habit tracker allows you to define and monitor daily routines. Changes are persisted across sessions using Jetpack DataStore, ensuring your progress is maintained through reboots and app updates.

### Integrated Scratch Pad
A minimalist note-taking surface is available directly on the home screen for capturing thoughts and reminders without the friction of opening a separate application.

### Mindfulness Integration
The home screen features a ticking clock and displays selected quotes from Vinland Saga focused on mindfulness and anger management. These quotes rotate hourly to provide consistent philosophical reminders.

## Navigation

- Swipe Up: Access the full application list.
- Long Press: Pin apps to the home screen or block usage tracking.
- Home Button: Always returns you to the minimalist home screen.

## Installation and Deployment

### Build from Source

1. Clone the repository to your local machine.
2. Open the project in Android Studio.
3. Synchronize Gradle files.
4. Build the project using the following command:

```bash
./gradlew assembleDebug
```

The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Publishing

To create a release on GitHub:

1. Navigate to the Releases section of your repository.
2. Click on Create a new release.
3. Upload the generated APK file as a binary asset.
4. Tag the version and publish.

## Technical Specifications

- Language: Kotlin
- UI Framework: Jetpack Compose
- Persistence: Jetpack DataStore
- Target SDK: 34
- Minimum SDK: 26


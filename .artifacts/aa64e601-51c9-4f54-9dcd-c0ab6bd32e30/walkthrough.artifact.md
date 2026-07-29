# Walkthrough - Background Notifications & Custom Scheme Fix

I have implemented the Foreground Service to keep notifications working in the background and fixed the custom scheme issue.

## Changes Made

### Background Notifications (Foreground Service)
- **New Service**: Created [WebNotificationService.kt](file:///C:/Users/ikrut/AndroidStudioProjects/Scam/app/src/main/java/com/example/scam/WebNotificationService.kt) which keeps the app process alive.
- **Persistent Notification**: Added a status bar notification to comply with Android's requirements for background services.
- **Manifest**: Added necessary permissions (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`) and declared the service.
- **Persistence**: Modified the `Back` button behavior in [MainActivity.kt](file:///C:/Users/ikrut/AndroidStudioProjects/Scam/app/src/main/java/com/example/scam/MainActivity.kt) so that pressing it on the main screen minimizes the app (`moveTaskToBack`) instead of closing it, keeping the `WebView` active.

### Custom Scheme Fix (`max://`)
- **URL Mapping**: Updated the logic in `shouldOverrideUrlLoading` and `handleIntent` to correctly handle links like `max://max.ru/path`.
- **Logic**: It now detects if the host `max.ru` is already present in the custom scheme and prevents it from being duplicated when converting to `https://web.max.ru/`.

## Verification Results

### Automated Tests
- Build successful: `gradlew app:assembleDebug` passed.

### Manual Verification Required
1. **Background**: Open the app, press "Back" or "Home". Verify the persistent notification "Фоновая работа" appears. Trigger a notification from the web side and verify it is shown.
2. **Link Test**: Open the link `max://max.ru/maMinenkov`. It should now correctly load `https://web.max.ru/maMinenkov` without a "Something went wrong" error.

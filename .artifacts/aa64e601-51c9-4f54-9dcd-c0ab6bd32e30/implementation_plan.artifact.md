# Implementation Plan - Background Notifications & Custom Scheme Fix

We will implement a **Foreground Service** to keep the application process alive and fix the broken URL mapping for the `max://` custom scheme.

## User Review Required

> [!CAUTION]
> **Important Limitations:**
> 1. A Foreground Service keeps the **process** alive, but if the user explicitly closes the app, the `WebView` UI component may still be destroyed.
> 2. Android aggressively throttles JavaScript in background. The notifications might still be delayed.
> 3. This will result in a **persistent notification** in the status bar.

## Proposed Changes

### app module

#### [NEW] [WebNotificationService.kt](file:///C:/Users/ikrut/AndroidStudioProjects/Scam/app/src/main/java/com/example/scam/WebNotificationService.kt)
- A `Service` class that will:
    - Create a persistent notification to enter the Foreground state.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/ikrut/AndroidStudioProjects/Scam/app/src/main/java/com/example/scam/MainActivity.kt)
- **Foreground Service**: Update `onCreate` to start the `WebNotificationService`.
- **Custom Scheme Fix**: Improve `shouldOverrideUrlLoading` and `handleIntent` to correctly map `max://max.ru/path` to `https://web.max.ru/path` instead of duplicating the host.
- **Background Persistence**: Modify `setupOnBackPressed` to move the task to background (`moveTaskToBack(true)`) instead of finishing, which keeps the `WebView` alive.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/ikrut/AndroidStudioProjects/Scam/app/src/main/AndroidManifest.xml)
- Add `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_SPECIAL_USE` permissions.
- Register the new `Service`.

#### [MODIFY] [strings.xml](file:///C:/Users/ikrut/AndroidStudioProjects/Scam/app/src/main/res/values/strings.xml)
- Add strings for the service notification.

## Verification Plan

### Automated Tests
- Build verification: `gradlew app:assembleDebug`.

### Manual Verification
1. **Background Notifications**: Launch app, minimize, trigger notification. Verify it appears.
2. **Custom Scheme**: Test the link `max://max.ru/maMinenkov`. Verify it opens `https://web.max.ru/maMinenkov` correctly.
3. **Back Button**: Press "Back" on the home page. Verify app minimizes but persistent notification stays.

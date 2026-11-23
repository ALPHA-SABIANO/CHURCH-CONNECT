# Implementation Plan - Church Connect 5 Screens UI

Implement all five designed screens for the **Church Connect** Android app using Jetpack Compose and Material 3, complete with interactive navigation between screens.

## User Review Required

> [!IMPORTANT]
> This plan covers all 5 screens shown in the design:
> 1. **Splash / Start Screen** (`SplashScrren`)
> 2. **Select Your Church Screen** (`SelectChurchScreen`)
> 3. **Church Space / Main App Screen** (`ChurchSpaceScreen` with Bottom Navigation and Tabs)
> 4. **Admin Account Screen / Auth** (`AdminAuthScreen` supporting Login / Register toggle)
> 5. **Manage Church Screen** (`ManageChurchScreen` for admin controls)

## Open Questions

- None. The visual design requirements and flow are clearly defined by the provided screenshots.

## Proposed Changes

### Navigation & Models
Create screen routing and UI state handling for navigating between all 5 screens.

#### [NEW] [ScreenRoutes.kt](file:///C:/Users/sabia/AndroidStudioProjects/CHURCHCONNECT/app/src/main/java/com/example/churchconnect/ui/navigation/ScreenRoutes.kt)
- Define sealed class or constants for navigation routes (`splash`, `select_church`, `church_space`, `admin_auth`, `manage_church`).

### UI Components & Screens

#### [NEW] [SplashScrren.kt](file:///C:/Users/sabia/AndroidStudioProjects/CHURCHCONNECT/app/src/main/java/com/example/churchconnect/ui/screens/SplashScreen.kt)
- Implement Splash / Start Screen matching design #1 with church icon, gradient background, app title, and "Get Started" button.

#### [NEW] [SelectChurchScreen.kt](file:///C:/Users/sabia/AndroidStudioProjects/CHURCHCONNECT/app/src/main/java/com/example/churchconnect/ui/screens/SelectChurchScreen.kt)
- Implement Select / Add Church Screen matching design #2 with selection cards and chevrons.

#### [NEW] [ChurchSpaceScreen.kt](file:///C:/Users/sabia/AndroidStudioProjects/CHURCHCONNECT/app/src/main/java/com/example/churchconnect/ui/screens/ChurchSpaceScreen.kt)
- Implement Main App Screen matching design #3 with top bar, church banner card, filter tabs, update/event cards, and bottom navigation.

#### [NEW] [AdminAuthScreen.kt](file:///C:/Users/sabia/AndroidStudioProjects/CHURCHCONNECT/app/src/main/java/com/example/churchconnect/ui/screens/AdminAuthScreen.kt)
- Implement Admin Account Screen matching design #4 with Login/Register toggle, text fields, password visibility icons, and submit buttons.

#### [NEW] [ManageChurchScreen.kt](file:///C:/Users/sabia/AndroidStudioProjects/CHURCHCONNECT/app/src/main/java/com/example/churchconnect/ui/screens/ManageChurchScreen.kt)
- Implement Manage Church Screen matching design #5 with admin action cards and Log Out button.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/sabia/AndroidStudioProjects/CHURCHCONNECT/app/src/main/java/com/example/churchconnect/MainActivity.kt)
- Set up `NavHost` to wire up all 5 screens with proper navigation actions.

## Verification Plan

### Automated Tests
- Build project successfully using `gradle_build("app:assembleDebug")`.

### Manual Verification
- Test all screen previews using Compose Previews.
- Run app on emulator/device to verify smooth navigation flow across all 5 screens.

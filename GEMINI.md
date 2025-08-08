# Project: RoadRelief - Master Blueprint

## 0. Guiding Principles & Rules

**IMPORTANT: Read and follow these rules for all generated code.**

1.  **Use Latest Stable Versions**: All dependencies (Jetpack Compose, Hilt, Room, CameraX, etc.) must use the latest stable versions available.
2.  **Avoid Deprecated APIs**: Before generating code, you must refer to the latest official Android developer documentation. Actively avoid any deprecated methods or classes. If a common pattern has been superseded (e.g., `startActivityForResult`), use the modern equivalent (e.g., `ActivityResultLauncher`).
3.  **Modern Kotlin Idioms**: All generated Kotlin code must be clean, idiomatic, and leverage modern features like coroutines, flows, and higher-order functions.
4.  **Well-Commented Code**: Add comments to explain complex logic, especially in ViewModels and utility functions.

## 1. Project Overview

- **App Name**: RoadRelief
- **Purpose**: A tool for users to document road damage, generate formal PDF notices, and get guidance on submitting claims.
- **Platform**: Android
- **Monetization**: One-time purchase.


## 2. Technical Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **State Management**: `StateFlow` in ViewModels, collected in Composables.
- **Navigation**: Jetpack Navigation for Compose.
- **Dependency Injection**: Hilt (with Singleton scoping for `SharedPreferences`).
- **Database**: Room.
- **Async**: Kotlin Coroutines & Flow.
- **Dependencies**:
  - CameraX (for in-app camera).
  - Google Play Services (for location/geotagging).
  - Android's built-in `PdfDocument` API for PDF generation.
- **Package Name**: `com.roadrelief.app`
- **minSdk**: 27
- **targetSdk**: 34

## 3. To-Do List (MVP Features)

### Phase 1: Foundation & Setup
- [x] **Task 1: Project & Dependency Setup**
- [x] **Task 2: Permissions Handling**
- [x] **Task 3: Navigation Graph**
- [x] **Task 4: Database Schema**

### Phase 2: Core Feature Implementation
- [x] **Task 5: User Profile Screen & Flexible Onboarding**
- [x] **Task 6: CameraX Screen & Geotagging**
- [x] **Task 7: New Case Screen**
- [x] **Task 8: Home Screen (Case List)**

### Phase 3: Finalization & Polish
- [x] **Task 9: Case Detail Screen**
  - [x] **Sub-task: Full-Screen Image Viewer**: Implemented a full-screen image viewer accessible from the Case Detail screen.
- [x] **Task 10: PDF Generation Service**
- [x] **Task 11: Submission Guidance Screen**

## 4. Completed Tasks

**Task 1-4: Foundation & Setup**
- All foundational tasks, including dependency setup, permissions handling, navigation graph, and database schema, are complete.
- Ensured `SharedPreferences` is provided as a Singleton via Hilt for consistent access across the app, resolving initial onboarding flow issues.
- **Evidence Entity Indexing**: Added an index to `EvidenceEntity` on the `caseId` column (`MIGRATION_3_4`) to optimize queries and resolve KSP warnings.

**Task 5: User Profile Screen & Flexible Onboarding**
- The user profile screen has been implemented. On first launch, the user is directed here.
- It allows users to save their name, address, and vehicle number, which is persisted in the local Room database.
- **Flexible Onboarding**:
    - Includes a "Skip for Now" button. If the user skips, they are navigated to the Home Screen.
    - The `firstLaunchComplete` flag in SharedPreferences is set to true regardless of whether the profile is filled or skipped, preventing the Profile screen from appearing mandatorily on every subsequent launch.
    - If the profile is skipped or incomplete, the `NewCaseScreen` will display a reminder (e.g., using placeholder text like "[Your Name]" in a prompt) to encourage the user to complete their profile later via the Home screen's Profile tab.
    - The "Skip for Now" button is only visible during the initial, mandatory profile setup.
    - A back button is correctly shown on the Profile screen if accessed after the initial setup (e.g., from the Home screen).

**Task 6 & 7: CameraX and New Case Screen**
- The CameraX screen is fully functional, capturing geotagged and timestamped photos.
- The New Case screen correctly receives the captured evidence from the camera.
- The screen includes fields for incident date, responsible authority (dropdown), road condition description, vehicle damage description, and compensation amount.
- A database migration was successfully added to include the `vehicleDamageDescription` field in the `cases` table.
- A reminder is displayed on the New Case Screen if the user's profile information is incomplete.
- **Critical Incident Location Refactor**: Implemented a dedicated incident location capture mechanism in `NewCaseScreen` (latitude/longitude fields and 'Get Location' button). `CaseEntity` now stores `incidentLatitude` and `incidentLongitude` directly. This required `MIGRATION_2_3`. This addresses a critical UX and data integrity flaw.
- **Navigation Fix**: Corrected navigation call in `NewCaseScreen` to use `Screen.Camera.route` to resolve runtime errors.

**Task 8: Home Screen**
- The home screen displays a list of all created cases.
- **UI Update**:
    - The Bottom App Bar has been removed.
    - "Profile" and "Submission Guide" are now accessible via `IconButton`s in the `RoadReliefTopAppBar`.
- A Floating Action Button (FAB) allows users to create a new case.

**Task 9 & 10: Case Detail and PDF Generation**
- The case detail screen displays all information for a selected case, including evidence photos.
- **Full-Screen Image Viewer**: Implemented `FullScreenImageScreen.kt` to display evidence photos full-screen. Updated `CaseDetailScreen.kt` to navigate to this screen when an image is tapped. Added the new screen to `Screen.kt` and `MainActivity.kt` (NavHost).
- The PDF generation service creates a comprehensive PDF report of the case.
- **Improved PDF Handling**: The PDF is saved to the app's internal cache directory, removing the need for `WRITE_EXTERNAL_STORAGE` permission. A `FileProvider` is used to securely share the generated PDF via a system share sheet.
- The `AppComponents.kt` file now includes an optional `actions` parameter for `RoadReliefTopAppBar` to support custom actions.

**Task 11: Submission Guidance Screen**
- **UI Revamp**:
    - The screen now uses a `Scaffold` with a `RoadReliefTopAppBar` that includes a back button for navigation.
    - Instructions are presented in a more structured and visually appealing manner using a new `GuideStep` composable, which utilizes `Card`s and `Icon`s.
    - The "Go to E-Daakhil Portal" button has been updated to use `.toUri()` for the intent URI, ensuring correct handling of the web link.
- The `MainActivity.kt` has been updated so that the `SubmissionGuideScreen` composable now accepts a `navController` parameter for managing navigation.


## 5. Testing Phase

- **[x] Test Profile Setup & Onboarding**:
  - Verify that user data is correctly saved and retrieved.
  - Test edge cases, such as empty fields and invalid input.
  - Verify the "Skip for Now" functionality:
    - User is navigated to Home.
    - `firstLaunchComplete` flag is set.
    - App opens to Home on subsequent launches.
    - Reminder appears on New Case screen if profile is skipped.
  - Verify "Skip for Now" button is hidden after first launch.
  - Verify back button functionality on Profile screen.
- **[x] Test New Case and Camera Flow**:
  - Ensure that the camera captures photos and location data correctly.
  - Verify that the captured evidence is correctly added to a new case.
  - Test the saving of a new case to the database.
  - **Verify dedicated incident location capture.**
- **[x] Test Full-Screen Image Viewing**: 
  - Verify that tapping an image in Case Detail Screen opens it in full-screen.
  - Test navigation back from the full-screen view.
- **[ ] Test PDF Generation and Sharing**:
  - Verify that the PDF is generated correctly with all the required information.
  - Test the sharing of the PDF to other apps.

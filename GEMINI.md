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
- **Dependency Injection**: Hilt.
- **Database**: Room.
- **Async**: Kotlin Coroutines & Flow.
- **Dependencies**:
    - CameraX (for in-app camera).
    - Google Play Services (for location/geotagging).
    - Android's built-in `PdfDocument` API for PDF generation.
- **Package Name**: `com.roadrelief.app`
- **minSdk**: 27
- **targetSdk**: 35

## 3. To-Do List (MVP Features)

### Phase 1: Foundation & Setup
- [x] **Task 1: Project & Dependency Setup**
- [x] **Task 2: Permissions Handling**
- [x] **Task 3: Navigation Graph**
- [x] **Task 4: Database Schema**

### Phase 2: Core Feature Implementation
- [x] **Task 5: User Profile Screen**
- [x] **Task 6: CameraX Screen & Geotagging**
- [x] **Task 7: New Case Screen**
- [x] **Task 8: Home Screen (Case List)**

### Phase 3: Finalization & Polish
- [x] **Task 9: Case Detail Screen**
- [x] **Task 10: PDF Generation Service**
- [x] **Task 11: Submission Guidance Screen**

## 4. Completed Tasks

**Task 1-4: Foundation & Setup**
- All foundational tasks, including dependency setup, permissions handling, navigation graph, and database schema, are complete.

**Task 5: User Profile Screen**
- The user profile screen has been implemented, allowing users to save their name, address, and vehicle number.

**Task 6 & 7: CameraX and New Case Screen**
- The CameraX screen is fully functional, capturing geotagged and timestamped photos.
- The New Case screen correctly receives the captured evidence from the camera and allows users to create a new case.
- Fixed a bug where the `NewCaseViewModel` was not correctly handling incoming evidence.

**Task 8: Home Screen**
- The home screen displays a list of all cases and provides a button to create a new case.

**Task 9 & 10: Case Detail and PDF Generation**
- The case detail screen displays all case information.
- The PDF generation service creates a PDF report of the case.
- **Improved PDF Handling**:
    - The PDF is now saved to the app's internal cache directory instead of public storage, removing the need for `WRITE_EXTERNAL_STORAGE` permission.
    - Implemented a `FileProvider` to securely share the generated PDF.
    - The `CaseDetailViewModel` now creates a share intent to be launched by the `CaseDetailScreen`.

**Task 11: Submission Guidance Screen**
- The submission guidance screen provides instructions for submitting the claim and includes a button to open the E-Daakhil portal.

## 5. Testing Phase

- **[ ] Test Profile Setup**:
    - Verify that user data is correctly saved and retrieved.
    - Test edge cases, such as empty fields and invalid input.
- **[ ] Test New Case and Camera Flow**:
    - Ensure that the camera captures photos and location data correctly.
    - Verify that the captured evidence is correctly added to a new case.
    - Test the saving of a new case to the database.
- **[ ] Test PDF Generation and Sharing**:
    - Verify that the PDF is generated correctly with all the required information.
    - Test the sharing of the PDF to other apps.

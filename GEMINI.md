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
- [x] **Task 1: Project & Dependency Setup**:
    - Create a new Android Studio project.
    - In the module-level `build.gradle.kts`, add the latest stable dependencies for Hilt, Room (KSP), CameraX, Jetpack Navigation Compose, and Compose ViewModel.
    - In the project-level `build.gradle.kts`, add the Hilt and KSP plugins.
    - Create the `MainApplication.kt` class, annotate it with `@HiltAndroidApp`.
    - Update `AndroidManifest.xml` to use this Application class.

- [x] **Task 2: Permissions Handling**:
    - Create a `PermissionManager` utility class.
    - Implement a composable function that uses the modern `rememberLauncherForActivityResult` with `ActivityResultContracts.RequestMultiplePermissions` to request `CAMERA` and `ACCESS_FINE_LOCATION`.
    - The function should gracefully handle cases where the user denies permissions.

- [x] **Task 3: Navigation Graph**:
    - Set up the basic navigation graph using Jetpack Navigation for Compose.
    - Define routes for: `Home`, `Profile`, `NewCase`, `CaseDetail`, `Camera`, and `SubmissionGuide`.
    - Create a `NavHost` in `MainActivity.kt`.

- [ ] **Task 4: Database Schema**:
    - Create the Room database entities with appropriate `@Entity` annotations and primary keys.
        - `UserEntity` (id, name, address, vehicleNumber)
        - `CaseEntity` (id, incidentDate, authority, description, compensation, status)
        - `EvidenceEntity` (id, caseId, photoUri, latitude, longitude, timestamp) - Use a `@ForeignKey` to link to `CaseEntity`.
    - Create the DAOs (`@Dao`) with functions for all CRUD operations (e.g., `insertCase`, `getCaseWithEvidence`, `getAllCases`).
    - Create the `AppDatabase` class annotated with `@Database`.
    - Set up a Hilt module to provide the database and DAO instances.

### Phase 2: Core Feature Implementation
- [ ] **Task 5: User Profile Screen**:
    - Create `ProfileViewModel` injected with the `UserDao`. Use `StateFlow` to hold the user profile state.
    - Create a `ProfileScreen` composable with `TextField`s for name, address, and vehicle number.
    - The screen should observe the ViewModel's state and have a "Save" button that calls a ViewModel function to insert/update the user data in the database.

- [ ] **Task 6: CameraX Screen & Geotagging**:
    - Create `CameraViewModel` to handle location logic.
    - Create a `CameraScreen` composable. It must use the `CameraX` `PreviewView` (via an `AndroidView`) and an `ImageCapture` use case.
    - Implement logic using the Fused Location Provider API to get a single, high-accuracy location update.
    - When the capture button is pressed, take a picture and immediately fetch the current location. Pass the resulting photo URI and location data back to the previous screen (`NewCaseScreen`).

- [ ] **Task 7: New Case Screen**:
    - Create `NewCaseViewModel` to manage the state of a new case draft.
    - Create `NewCaseScreen` with `TextField`s for all case details.
    - Include a button "Add Evidence" that navigates to the `CameraScreen`.
    - When returning from the camera, display a thumbnail of the captured photo. Allow capturing multiple pieces of evidence.
    - A "Save Case" button will trigger a ViewModel function to save the `CaseEntity` and all associated `EvidenceEntity` items to the database in a single transaction.

- [ ] **Task 8: Home Screen (Case List)**:
    - Create `HomeViewModel` that exposes a `StateFlow<List<CaseEntity>>` from the database.
    - Create `HomeScreen` that uses a `LazyColumn` to display the list of cases.
    - Each item should be clickable and navigate to the `CaseDetail` screen, passing the `caseId`.
    - Include a Floating Action Button (FAB) to navigate to the `NewCaseScreen`.

### Phase 3: Finalization & Polish
- [ ] **Task 9: Case Detail Screen**:
    - Create `CaseDetailViewModel` that takes a `caseId` and loads the full case with its evidence.
    - Create `CaseDetailScreen` to display all information immutably.
    - Show evidence photos as a small scrollable row of images.
    - Add a button "Generate PDF Report".

- [ ] **Task 10: PDF Generation Service**:
    - Create a `PdfGenerator` class.
    - Implement a method that takes the case data, creates a `PdfDocument`, and draws the content onto a `Canvas`. This includes drawing text for the legal notice and drawing the photo bitmaps onto the page.
    - Save the generated PDF to the app's public "Documents/RoadRelief" directory.

- [ ] **Task 11: Submission Guidance Screen**:
    - Create a simple, scrollable `SubmissionGuideScreen` composable with static `Text` elements containing the instructions.
    - Add a `Button` that uses an `Intent` to open the E-Daakhil portal URL in the user's browser.

## 4. Completed Tasks

**Task 1: Project & Dependency Setup**
- Updated `build.gradle.kts` (project-level) to include Hilt and KSP plugins.
- Updated `app/build.gradle.kts` (module-level) with the latest stable versions of Hilt, Room, CameraX, Navigation Compose, and ViewModel Compose.
- Created `MainApplication.kt` and annotated it with `@HiltAndroidApp`.
- Updated `AndroidManifest.xml` to use the `MainApplication` class.

**Task 2: Permissions Handling**
- Added `CAMERA` and `ACCESS_FINE_LOCATION` permissions to `AndroidManifest.xml`.
- Created `PermissionManager.kt` to check for permissions.
- Created `PermissionRequest.kt` with a composable to request permissions using the official Jetpack Compose Activity Result APIs.

**Task 3: Navigation Graph**
- Created `Screen.kt` to define all navigation routes as a sealed class.
- Created placeholder composable screens for `Home`, `Profile`, `NewCase`, `CaseDetail`, `Camera`, and `SubmissionGuide`.
- Configured the `NavHost` in `MainActivity.kt` with all the defined routes.

**Task 4: Database Schema**
- Created `UserEntity`, `CaseEntity`, and `EvidenceEntity` with foreign key constraints.
- Created `UserDao`, `CaseDao`, and `EvidenceDao` for database operations.
- Created the `AppDatabase` class to define the database.
- Set up a Hilt `DatabaseModule` to provide the database and DAO instances.

**Task 5: User Profile Screen**
- Created `ProfileViewModel` to manage user profile data using `StateFlow`.
- Implemented `ProfileScreen` composable with `TextField`s for user input and a "Save" button to persist data.

**Task 6: CameraX Screen & Geotagging**
- Created `CameraViewModel` to handle location logic and image capture.
- Implemented `CameraScreen` composable using CameraX `PreviewView` and `ImageCapture`.
- Integrated Fused Location Provider API to fetch current location upon photo capture.
- Passed captured photo URI and location data back to the previous screen via `SavedStateHandle`.

**Task 7: New Case Screen**
- Created `NewCaseViewModel` to manage the state of a new case draft, including handling evidence from the camera.
- Implemented `NewCaseScreen` composable with `TextField`s for case details and a button to navigate to the `CameraScreen`.
- Displayed thumbnails of captured photos and implemented logic to save the `CaseEntity` and associated `EvidenceEntity` items to the database.

**Task 8: Home Screen (Case List)**
- Created `HomeViewModel` that exposes a `StateFlow<List<CaseEntity>>` from the database.
- Implemented `HomeScreen` that uses a `LazyColumn` to display the list of cases.
- Each item is clickable and navigates to the `CaseDetail` screen, passing the `caseId`.
- Included a Floating Action Button (FAB) to navigate to the `NewCaseScreen`.

**Task 9: Case Detail Screen**
- Created `CaseDetailViewModel` to load a specific case and its associated evidence.
- Implemented `CaseDetailScreen` to display all case information immutably.
- Displayed evidence photos as a scrollable row of images.
- Added a placeholder button for "Generate PDF Report".

**Task 10: PDF Generation Service**
- Created `PdfGenerator` class to generate PDF reports from case data and evidence.
- Integrated `PdfGenerator` into `CaseDetailViewModel` to trigger PDF generation.
- Added `WRITE_EXTERNAL_STORAGE` permission to `AndroidManifest.xml` for saving PDFs.

**Task 11: Submission Guidance Screen**
- Implemented `SubmissionGuideScreen` composable with static text providing instructions.
- Added a button to open the E-Daakhil portal URL using an `Intent`.
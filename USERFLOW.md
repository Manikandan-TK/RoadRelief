# Complete User Flow for RoadRelief App

The RoadRelief app assists users in filing claims for road-related damages by guiding them through profile setup, case creation, evidence capture, PDF report generation, and submission guidance.

### 1. First Launch & Optional Profile Setup

**Purpose**: To collect the user's essential information, which will be used to pre-fill future legal notices. This step is now optional during the first launch.

**Steps**:
1.  On first launch, the user is directed to the **Profile Screen**.
2.  The user is presented with fields for their full name, address, and vehicle number.
3.  The user has two options:
    *   **Save Profile**: User enters their details and clicks "Save Profile". The app saves the information to the local database.
    *   **Skip for Now**: User clicks a "Skip for Now" button.
4.  Regardless of the choice (Save or Skip), the `firstLaunchComplete` flag is set, and the app automatically navigates the user to the **Home Screen**.

**Details**:
*   The Profile Screen will not be shown automatically on subsequent launches.
*   If the profile was skipped, the **New Case Screen** will display a reminder to complete the profile.
*   Profile information can be entered or edited later by tapping the "Profile" icon in the Home Screen's top app bar.

### 2. Home Screen

**Purpose**: The central hub for managing cases and navigating to other features.

**Components**:
*   A **Top App Bar** with:
    *   App title "RoadRelief".
    *   An icon button to navigate to the **Profile Screen**.
    *   An icon button to navigate to the **Submission Guide Screen**.
*   A list of previously created cases. If the list is empty, a message prompts the user to create their first report.
*   A Floating Action Button (FAB) with a `+` icon to navigate to the **New Case Screen**.

**Details**:
*   Each item in the case list is clickable and navigates to the **Case Detail Screen** for that specific case.
*   The previous Bottom Navigation Bar has been removed. Access to Profile and Submission Guide is now through the top app bar.

### 3. Create New Case

**Purpose**: To create a new damage claim by entering all relevant details, including a dedicated incident location.

**Steps**:
1.  From the Home Screen, the user taps the `+` FAB.
2.  The user is taken to the **New Case Screen**.
3.  **Profile Reminder**: If the user's profile is incomplete (e.g., skipped during onboarding), a message reminds them to fill out their profile from the Home screen's Profile tab for accurate claim details.
4.  User selects the **Incident Date** (defaults to the current date, modifiable via a date picker).
5.  **Capture Incident Location**:
    *   The screen displays an "Incident Location" section with a "Coordinates" field (initially "Location not set") and a "Get Location" button.
    *   User taps "Get Location". The app requests **Location permissions** (if not already granted).
    *   Upon granting permission, the app fetches the current GPS coordinates and displays them in the "Coordinates" field (e.g., "Lat: XX.XXXXX, Lon: YY.YYYYY").
    *   A small helper text clarifies that this location should be where the incident occurred and is separate from photo locations.
6.  User selects the **Responsible Authority** from a dropdown list (e.g., "City Council", "State Highway Dept").
7.  User enters a **description of the road condition** (e.g., "Deep pothole on Main Street").
8.  User enters a **description of the vehicle damage**.
9.  User enters the **requested compensation amount**.
10. User adds photo evidence (see next step).
11. User clicks "Save Claim". The app saves the case (including the specifically captured incident location) to the database and returns to the Home Screen.

### 4. Capture Evidence (Photos)

**Purpose**: To collect geotagged and timestamped photos as evidence for the claim. The location of each photo is stored as metadata *for that specific photo* and is independent of the main incident location.

**Steps**:
1.  On the **New Case Screen**, the user taps the "Add Photo from Camera" button.
2.  The app requests **Camera and Location permissions** (if not already granted for photo geotagging).
3.  The in-app **Camera Screen** opens.
4.  The user takes a photo. The photo is automatically timestamped.
5.  The app captures the device's current GPS location (latitude/longitude) *for that photo*.
6.  The user is automatically returned to the **New Case Screen**, where the captured photo appears as a thumbnail.
7.  A small helper text below the "Add Photo from Camera" button clarifies that photo location is recorded with each photo and is separate from the main incident location captured earlier.
8.  The user can repeat this process to add multiple photos. Each photo will have its own geotag and timestamp.

**Details**:
*   Permissions are handled gracefully with rationales and links to settings if permanently denied.
*   The primary **incident location** is captured and stored directly within the case details in the `NewCaseScreen`.
*   Each piece of **evidence (photo)** is stored locally with its *own timestamp and GPS coordinates*, linked to the corresponding case. This photo-specific location may differ from the main incident location.

### 5. View Case Details & Generate PDF

**Purpose**: To review all case information and generate a shareable PDF report.

**Steps**:
1.  From the Home Screen, the user taps on a case to open the **Case Detail Screen**.
2.  This screen displays all entered information: the main **incident location (latitude/longitude)**, authority, dates, descriptions, compensation, and evidence photos (with their individual metadata, including photo-specific timestamps and locations, if needed).
3.  The user clicks the **"Generate PDF Report"** button at the bottom of the screen.
4.  The app generates a PDF file containing all the case details (including the primary incident location) and photos.
5.  A system **share sheet** appears, allowing the user to send the PDF to another app (e.g., Gmail, WhatsApp, Google Drive).

**Details**:
*   The PDF is generated and stored in the app's private cache directory.
*   A `FileProvider` is used to grant secure, temporary access to the PDF for sharing, which means no `WRITE_EXTERNAL_STORAGE` permission is needed.

### 6. Submission Guidance

**Purpose**: To provide the user with instructions on how to formally submit their claim.

**Steps**:
1.  From the Home Screen, the user taps the "Submission Guide" icon in the top app bar.
2.  The user is navigated to the **Submission Guide Screen**.
3.  This screen displays structured, step-by-step instructions for uploading the generated PDF to the E-Daakhil portal, presented in individual cards with icons.
4.  A button is provided that opens the E-Daakhil portal URL in the user's default web browser.
5.  A back arrow in the top app bar allows navigation back to the Home Screen.

### 7. Manage Cases

**Purpose**: To allow users to review their claim history.

**Features**:
*   **View Cases**: The Home Screen lists all cases with their ID, status, and date.
*   **View Details**: Tapping a case opens the detailed view.
*   **Editing/Deleting**: *Currently not implemented in the UI.* The user can only add new cases and view existing ones.


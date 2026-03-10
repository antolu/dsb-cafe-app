# DSB Café

Android app for tracking coffee consumption at the DSB café. People tap their NFC badge on a shared tablet to increment their count. A leaderboard shows everyone's totals.

## How it works

- Tap an NFC badge → coffee count incremented in Firestore
- First tap with a new badge → prompted to enter your name
- Optional "make it a double?" dialog after each tap (auto-dismisses after 10s)
- Leaderboard sorted by count with gold/silver/bronze rank badges
- Admin menu hidden behind 5 taps on the total count: reset all counts, email stats, delete a user

## Stack

- Kotlin 2.1.10
- Jetpack Compose + Material Design 3 (CERN blue `#0033A0`)
- Firebase Firestore (data) + Firebase Auth (Google Sign-In)
- Credential Manager API for Google authentication
- MVVM — ViewModel, Repository, StateFlow

## Project structure

```
app/src/main/java/com/lua/dsbcafe/
├── auth/                  Google Sign-In + FirebaseAuth
├── data/
│   ├── model/             Person data class
│   └── repository/        Firestore operations
├── nfc/                   NFC foreground dispatch
├── ui/
│   ├── components/        PersonItem, AdminMenu, dialogs
│   ├── screen/            LoginScreen, MainScreen
│   └── theme/             CERN blue M3 color scheme
├── viewmodel/             State + business logic
└── MainActivity.kt        Thin shell: NFC wiring, auth routing
```

## Setup

### Prerequisites

- Android Studio Meerkat or later
- A Firebase project with:
  - **Authentication** → Google Sign-In enabled
  - **Firestore** database created
  - Your debug SHA-1 fingerprint registered under Project settings → Your apps

### Firebase config

Download `google-services.json` from the Firebase console (Project settings → Your apps → Android app) and place it at `app/google-services.json`. This file is gitignored.

### Firestore security rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /persons/{personId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Build

```bash
./gradlew assembleDebug
```

## CI

GitHub Actions runs lint, build, and unit tests on every push and pull request to `main`.

Add your `app/google-services.json` content as a repository secret named `GOOGLE_SERVICES_JSON` (Settings → Secrets and variables → Actions).

## Admin access

Tap the large coffee count at the top of the screen 5 times within 5 seconds to reveal the admin speed-dial. Requires being signed in.

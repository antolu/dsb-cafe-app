# AGENTS.md

Guidelines for agentic coding agents working in this repository.

## What this app does

Android NFC coffee counter for a shared café tablet. People tap their NFC badge to increment their coffee count in Firestore. One admin signs in with Google to access the app. A leaderboard shows all counts. Admin menu (5 taps on the total) allows resetting counts, emailing stats, and deleting users.

---

## Build, lint, test commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run lint (required to pass before committing)
./gradlew lintDebug

# Run all unit tests
./gradlew testDebugUnitTest

# Run a single test class
./gradlew testDebugUnitTest --tests "com.lua.dsbcafe.ExampleUnitTest"

# Run a single test method
./gradlew testDebugUnitTest --tests "com.lua.dsbcafe.ExampleUnitTest.methodName"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedDebugAndroidTest

# Run lint + unit tests together
./gradlew lintDebug testDebugUnitTest

# Auto-fix lint issues where safe
./gradlew lintFix
```

`google-services.json` must be present at `app/google-services.json` for any build to succeed. It is gitignored. Get it from the Firebase console (Project settings → Your apps → Android app).

---

## Architecture

MVVM. Strict layer separation — no Firestore or Android SDK imports in the ViewModel except `Context` for launching intents.

```
auth/           Google Sign-In + FirebaseAuth wrapper
data/model/     Data classes only — no logic
data/repository/ All Firestore operations — suspend fns + Flow
nfc/            NFC foreground dispatch — no business logic
viewmodel/      StateFlow-based state + all business logic
ui/screen/      Full screens — consume ViewModel state
ui/components/  Reusable composables — no ViewModel dependency
ui/theme/       Color, Type, Theme — no logic
MainActivity    Thin shell: NFC lifecycle, auth routing, setContent only
```

---

## Language and Kotlin conventions

- Kotlin only. No Java.
- Kotlin `2.1.x`. Target JVM 17.
- Use `data class`, `sealed interface`, `data object` appropriately.
- Prefer `val` over `var`. Only use `var` when mutation is genuinely needed.
- Use expression bodies for single-expression functions.
- Use named arguments when calling functions with multiple parameters of the same type.
- No `!!` operator. Use `?: return`, `?: return null`, or `checkNotNull` with a message.
- Use `copy()` on data classes rather than mutating fields.
- Trailing commas on multi-line argument lists and collections.

---

## Coroutines and async

- All Firestore operations use `kotlinx-coroutines-play-services` `.await()` — no `addOnSuccessListener` callbacks.
- Repository functions are `suspend` or return `Flow`. No callbacks leak out of the repository layer.
- ViewModel launches coroutines with `viewModelScope.launch { }`.
- Wrap all suspend calls in `try/catch(e: Exception)` inside the ViewModel. Propagate errors via `UiMessage.Error`.
- Use `SharingStarted.WhileSubscribed(5_000)` when converting `Flow` to `StateFlow` in the ViewModel.

---

## State management

- All UI state lives in `MainViewModel` as `StateFlow`.
- Private backing field pattern: `private val _foo = MutableStateFlow(...)` exposed as `val foo = _foo.asStateFlow()`.
- Dialog state is a `sealed interface DialogState` — one active dialog at a time, `None` when idle.
- User-facing messages use `sealed interface UiMessage` (Info / Error), consumed via `SnackbarHostState`. Clear after showing with `viewModel.clearMessage()`.
- Composables observe state with `collectAsState()` or `collectAsStateWithLifecycle()`.

---

## Jetpack Compose

- Every screen is wrapped in `DSBCafeTheme { }` (applied once at the top in `MainActivity`).
- Use `Scaffold` with `SnackbarHost` for all screens — no `Toast`.
- Use M3 `AlertDialog` for all dialogs — no View-based `AlertDialog.Builder`.
- Use `ListItem` for list rows. Use `HorizontalDivider` between items.
- Use `CenterAlignedTopAppBar` or `TopAppBar` — no raw `Text` as a title substitute.
- Composable functions: PascalCase, no side effects in the function body, side effects in `LaunchedEffect`.
- Previews are encouraged for components but not required.
- No `@Composable` functions inside Activity/ViewModel classes — extract to separate files.

---

## Imports and formatting

- Kotlin official code style (`kotlin.code.style=official` in `gradle.properties`).
- Imports: no wildcard imports. One import per line. Sorted alphabetically within groups (Android, androidx, com.google, com.lua, kotlinx, java).
- No unused imports.
- Max line length: 120 characters.
- Indentation: 4 spaces.
- Opening braces on the same line. Trailing commas in multi-line expressions.

---

## Naming conventions

| Element | Convention | Example |
|---|---|---|
| Classes / interfaces | PascalCase | `PersonRepository`, `DialogState` |
| Functions | camelCase | `onNfcTagRead`, `resetCounts` |
| Properties / variables | camelCase | `totalCount`, `isLoading` |
| Private backing StateFlow | `_` prefix | `_dialogState` |
| Constants | SCREAMING_SNAKE | `TAG` |
| Composables | PascalCase | `PersonItem`, `AdminMenu` |
| Packages | lowercase, no underscores | `com.lua.dsbcafe.data.repository` |
| Resource IDs | snake_case | `default_web_client_id` |

---

## Error handling

- Repository functions let exceptions propagate to the caller (ViewModel).
- ViewModel catches all exceptions and sets `_message.value = UiMessage.Error(...)`.
- Never swallow exceptions silently.
- Use `Result<T>` for operations that can fail at the call site (e.g. `AuthManager.signIn`).
- Auth errors: show in `Snackbar` on `LoginScreen`. Never crash.

---

## Firebase / Firestore

- All Firestore access requires `request.auth != null` (enforced by security rules).
- Collection: `persons`. Document ID: NFC badge hex string. Fields: `name`, `coffeeCount`, `badgeId`.
- Use Firestore batch writes for multi-document operations (`resetAllCounts`, `deletePerson`).
- Real-time updates via `collection.snapshots()` Flow — do not poll.
- `google-services.json` is always gitignored. Never commit it.

---

## Git and commits

- Conventional commits: `feat:`, `fix:`, `refactor:`, `build:`, `ci:`, `docs:`, `test:`.
- Commit messages are short and factual — describe what changed, not what you did.
- Do not commit `google-services.json`, `*.jks`, `*.keystore`, or any secrets.
- Do not use `git add -A`. Stage files explicitly.
- Do not commit with `--no-verify` unless explicitly instructed.

---

## CI

GitHub Actions runs on every push and PR to `main`: lint → build → unit tests.
The `GOOGLE_SERVICES_JSON` secret must be set in the repo settings (raw JSON content of `app/google-services.json`).

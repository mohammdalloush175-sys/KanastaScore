# KanastaScore

KanastaScore is an Android companion app for recording, calculating, and analyzing scores from real-world Canasta matches. It replaces manual score sheets with guided round entry, automatic validation, match history, statistics, backup tools, and shareable result cards.

## Highlights

- Create and manage multiple Canasta matches.
- Configure teams, players, and the target score.
- Detailed scoring mode for individual melds.
- Direct scoring mode for faster round entry.
- Automatic calculation of:
  - Meld points
  - Red and black Canasta bonuses
  - Red-three bonuses or penalties
  - Going-out bonus
  - Cards remaining in hand
  - No-Canasta penalty
- Input validation and warnings for unusual values.
- Round history with edit and delete support.
- Completed-match summaries and winner detection.
- Overall match and team statistics.
- Export, import, merge, and replace local backups.
- Share match results as an image.
- System, light, and dark themes.
- Responsive layouts for landscape and larger screens.
- Built-in GitHub update page with manual and daily checks.
- Android notification when a newer GitHub release is available.
- Arabic-first interface with RTL support.

## Scoring rules implemented

- Red Canasta: 300 points
- Black Canasta: 200 points
- Going out: 100 points
- Red threes: 100, 200, 300, or 800 points for four
- No Canasta: fixed -300 penalty
- Red threes become penalties when the team has no Canasta
- Meld and hand-card values are excluded when no Canasta is recorded

## Tech stack

- Kotlin
- Android Views and XML layouts
- View Binding
- AndroidX ViewModel and LiveData
- Material Components
- RecyclerView
- Gson
- SharedPreferences-based local persistence
- JUnit unit tests

## Requirements

- Android 7.0 or newer (API 24+)
- Android Studio with Android SDK 36
- JDK 17 recommended

## Build

```bash
./gradlew assembleDebug
```

Run unit tests with:

```bash
./gradlew test
```

The repository also includes helper scripts:

```bash
./scripts/run_tests.sh
./scripts/build_and_install.sh
```

## Project structure

```text
app/src/main/java/com/mohammdalloush/kanastascore/
├── engine/       # Models, scoring rules, calculation, and validation
├── ui/           # Match, round, history, statistics, and settings screens
├── ui/adapters/  # RecyclerView adapters
├── update/       # GitHub release checks and update notifications
├── utils/        # Local storage, backup, and result sharing
└── viewmodel/    # Application state and match workflow
```

## Testing

The unit-test suite covers scoring calculations, validation, red-three synchronization, match storage behavior, statistics, and ViewModel workflows.

## Privacy

KanastaScore stores match data locally. Internet access is used only to read public release metadata from the official GitHub repository when checking for updates. See [PRIVACY.md](PRIVACY.md).

## Developer

Developed by **Mohammd Aloush**.


## App updates

The Updates screen reads public releases from the official GitHub repository, compares the published tag with `BuildConfig.VERSION_NAME`, and opens the APK asset or release page in the browser. Automatic checks run approximately once per day when enabled. Android still asks the user to confirm installation; the app never performs silent installs.

# Walkthrough - KanastaScore Major Enhancements

This update brings a complete set of features for managing rounds, matches, and data in KanastaScore.

## Key Changes

### 1. Detailed Round Review & Undo
- **Round Preview:** Before recording, see a detailed breakdown of scores for each team, including bonuses, penalties, and progression.
- **Duplicate Prevention:** Recording buttons are disabled during saving, and unique IDs prevent multiple entries of the same round.
- **Undo Feature:** A Snackbar appears after recording a round, allowing you to undo it instantly and restore your inputs for correction.

### 2. Fast Input Experience
- **Shortcuts:** Added quick-entry buttons (+50, +100, +200) for door points and hand cards.
- **Canasta Counters:** Easy +/- buttons for red and black canastas.
- **Red Threes Selector:** Fast 0-4 toggle buttons that automatically sync between teams.
- **Sticky Summary:** A fixed header at the top of the input screen shows current totals and target score while scrolling.

### 3. Match Completion & Re-match
- **Match End Screen:** Once the target score is reached, a celebration screen shows the winner, final results, and match statistics.
- **Re-match:** Quickly start a new game with the same team names and target score.
- **Optional Naming:** Give your matches custom names like "Thursday Session" or "Family Night".

### 4. Results Sharing & Data Management
- **Image Sharing:** Share a beautiful, branded image of the match result directly to social media or messaging apps.
- **Backup & Restore:** Securely export all your matches as a JSON file and import them back on any device using the System File Picker.
- **Statistics Dashboard:** Track your overall performance, win rates, and average scores across all matches.
- **Rules Page:** An in-app reference for the scoring rules used by the application.

## Verification Results

### Automated Tests
- `testDebugUnitTest`: Passed all 13 tests (covering score calculation, validation, and core logic).
- `lintDebug`: Passed without blocking errors.
- `assembleDebug`: Successfully generated the APK.

### Manual Verification Required
- Verify "Undo" Snackbar behavior after recording a round.
- Test "Re-match" copies names correctly.
- Check "Export/Import" with the system file picker.
- Share an image and verify the generated PNG looks correct in other apps.

> [!TIP]
> Use the new shortcut buttons in the Round Input screen to speed up your scoring process significantly!

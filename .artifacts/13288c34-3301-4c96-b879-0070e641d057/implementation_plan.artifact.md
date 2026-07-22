# Implementation Plan - Completing KanastaScore Features

This plan outlines the steps to add advanced features to the KanastaScore app, including detailed round reviews, undo functionality, input shortcuts, match end statistics, sharing results as images, backup/restore, and a statistics dashboard.

## User Review Required

> [!IMPORTANT]
> The "Undo" functionality will be available via a Snackbar immediately after recording a round. Once the Snackbar disappears or a new action is taken, the round can still be edited or deleted from the history screen as before.

> [!NOTE]
> Backup and Restore will use the System File Picker (SAF) to ensure no broad storage permissions are required.

## Proposed Changes

### 1. Round Review & Recording Flow [Phase 1]

#### [MODIFY] [ScoreBreakdown](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/engine/GameModels.kt)
- Ensure all components of the score (red canasta count, black canasta count, etc.) are available for the detailed breakdown UI.

#### [MODIFY] [ResultFragment](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/ui/ResultFragment.kt)
- Redesign UI to show a table-like breakdown for both teams.
- Use green for additions (+), red for subtractions (-).
- Show a "No Canasta" warning card if applicable.
- Add "Record Round" with duplicate prevention (disabling button and ID check).
- Add "Back to Edit" button.

#### [MODIFY] [GameViewModel](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/viewmodel/GameViewModel.kt)
- Add `undoLastRound()` logic to remove the last round and restore the draft.
- Add state to track if a round is currently being recorded to prevent duplicates.

### 2. Input Enhancements & Shortcuts [Phase 2]

#### [MODIFY] [layout_team_round_input.xml](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/res/layout/layout_team_round_input.xml)
- Add horizontal button groups for quick score entry (+50, +100, +200, Clear).
- Add similar buttons for "Remaining Points" (Burning).
- Add +/- buttons for Canasta counts.

#### [MODIFY] [RoundInputFragment](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/ui/RoundInputFragment.kt)
- Implement logic for shortcut buttons.
- Implement Snackbar with "Undo" action after recording a round.

#### [MODIFY] [fragment_round_input.xml](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/res/layout/fragment_round_input.xml)
- Add a sticky summary card at the top showing current total scores and target score.

### 3. Match End & Re-match [Phase 3]

#### [NEW] [MatchEndFragment](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/ui/MatchEndFragment.kt)
- Display winner, final scores, and match statistics (total rounds, canastas, max round score).
- Buttons: Re-match with same teams, New Match, View History, Share Result.

#### [MODIFY] [GameViewModel](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/viewmodel/GameViewModel.kt)
- Add `reMatch()` to create a new match copying team names and target score.

### 4. Sharing Result as Image [Phase 4]

#### [NEW] [layout_share_card.xml](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/res/layout/layout_share_card.xml)
- A clean, branded layout for the shareable image.

#### [MODIFY] [MainActivity](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/MainActivity.kt)
- Set up `FileProvider` in manifest and provide a utility method to share Bitmaps.

### 5. Backup, Restore & Data Management [Phase 5]

#### [MODIFY] [StorageHelper](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/utils/StorageHelper.kt)
- Add methods to export `MatchesStore` to JSON string and import from JSON string.
- Support "Merge" and "Replace" modes.

#### [NEW] [SettingsFragment](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/ui/SettingsFragment.kt)
- Options for Theme, Backup/Restore, Statistics, Rules, and "Delete All Data".

### 6. Statistics & Rules [Phase 6]

#### [NEW] [StatisticsFragment](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/ui/StatisticsFragment.kt)
- Calculate and display global stats (total matches, rounds, canastas).
- Show team-specific stats for recurring team names.

#### [NEW] [RulesFragment](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/ui/RulesFragment.kt)
- Scrollable text view explaining the scoring system and rules.

## Verification Plan

### Automated Tests
- `ScoreCalculatorTest`: Verify detailed breakdown components.
- `GameViewModelTest`: Test Undo logic and Re-match logic.
- `StorageHelperTest`: Test JSON export/import (Merge vs Replace).
- `StatisticsCalculatorTest`: Verify stats calculation from a sample `MatchesStore`.

### Manual Verification
- Verify the "Undo" Snackbar appears and works after recording a round.
- Test "Re-match" preserves team names and starts at Round 1.
- Share a result and verify the generated image looks correct.
- Export backup, clear data, and import backup to verify data integrity.
- Verify RTL support in all new screens.
- Check Day/Night mode transitions.

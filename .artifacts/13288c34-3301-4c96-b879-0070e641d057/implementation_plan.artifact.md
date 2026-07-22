# Implementation Plan - Red Threes Flex Rule & UI Responsiveness

This plan covers updating the Red Threes rule to allow totals less than 4 with smart suggestions, and a comprehensive review of the UI to ensure it works perfectly across all screen sizes and orientations.

## User Review Required

> [!IMPORTANT]
> The Red Threes sum check will now be `<=` instead of `==`. This allows scenarios where not all Red Threes have appeared before the round ended.

> [!NOTE]
> We will add "Manual" vs "Auto" tracking for Red Threes to provide helpful suggestions without overwriting user intentions.

## Proposed Changes

### 1. Red Threes Logic & Engine Updates

#### [MODIFY] [GameModels.kt](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/engine/GameModels.kt)
- Add `isRedThreesManual: Boolean = false` to `TeamRoundInput` to track user interaction.

#### [MODIFY] [GameViewModel.kt](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/viewmodel/GameViewModel.kt)
- Update `setRedThrees` to:
    1. Mark the target team as manual.
    2. If the other team is still "Auto", update its value to `4 - selectedCount`.
- Add `resetRedThrees()` to clear manual flags and reset both to 0.

#### [MODIFY] [ScoreValidator.kt](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/engine/ScoreValidator.kt)
- Change sum check from `== 4` to `<= 4`.
- Ensure error message is user-friendly.

---

### 2. UI Updates for Red Threes

#### [MODIFY] [strings.xml](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/res/values/strings.xml)
- Add labels for "Auto-suggested", "Manually edited", and the sum error.
- Add helper text explaining the suggestion logic.

#### [MODIFY] [layout_team_round_input.xml](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/res/layout/layout_team_round_input.xml)
- Add a status badge (TextView) next to "Red Threes" label.
- Add helper text below the toggle group.

#### [MODIFY] [RoundInputFragment.kt](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/ui/RoundInputFragment.kt)
- Implement badge visibility logic.
- Dynamically enable/disable the "Preview Result" button based on validation.

---

### 3. Comprehensive Layout Responsiveness

#### [REVIEW & FIX] All Layouts
- **Scrollability:** Ensure `NestedScrollView` wraps content in all fragments.
- **Fixed Sizes:** Replace fixed `layout_width` and `layout_height` (except icons) with `match_parent`, `0dp`, or `wrap_content`.
- **ConstraintLayout:** Fix any missing constraints or inefficient nesting.
- **Font Scaling:** Test with `2.0x` font size and ensure text doesn't overflow or overlap.

#### [NEW] [dimens-sw600dp.xml](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/res/values-sw600dp/dimens.xml)
- Adjust spacings and text sizes for tablets.

#### [NEW] [layout-land/fragment_round_input.xml](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/res/layout-land/fragment_round_input.xml)
- Optimize round input for horizontal orientation (possibly side-by-side teams).

#### [MODIFY] [MainActivity.kt](file:///home/mohammd/Workspace/Android/kanastaScore/app/src/main/java/com/mohammdalloush/kanastascore/MainActivity.kt)
- Double-check Window Insets implementation for notched devices and gesture navigation.

---

## Verification Plan

### Automated Tests
- Add unit tests in `ScoreValidatorTest` for:
    - Sum < 4 (Valid).
    - Sum == 4 (Valid).
    - Sum > 4 (Invalid).
    - Negative or > 4 per team (Invalid).
- Add `GameViewModelTest` for auto-suggestion logic.

### Manual Verification
1.  **Red Threes:** Change Team 1 to 3 -> Team 2 should become 1 (Auto). Change Team 2 to 0 -> Team 2 should become 0 (Manual).
2.  **Small Screen:** Run on 320dp device (e.g. Jelly Bean style emulator or small Pixel).
3.  **Tablet:** Run on 10-inch tablet emulator.
4.  **Landscape:** Flip device in all major screens.
5.  **Large Font:** Set system font to Largest and check for layout breaks.
6.  **Lint:** Run `./gradlew lintDebug` and resolve layout warnings.

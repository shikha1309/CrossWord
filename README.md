# Word Puzzle - Connect & Solve Crosswords

A premium, production-ready Word Connect mobile game built natively in Kotlin using **Jetpack Compose** and modern **Android Architecture Components (MVVM)**.

---

## 🎮 Gameplay Features & Mechanics

* **Crossword Grid & Letter Wheel**: A clean, responsive crossword grid at the top and a custom interactive letter wheel at the bottom.
* **Continuous Gesture Swipe**: Smooth, bug-free touch gestures that connect letters on the wheel with a dynamic line that follows the player's finger.
* **Smart Word Validation**:
  * **Valid Grid Word**: Bounces and reveals matching tiles on the grid in yellow, awards coins, and plays success audio.
  * **Bonus Word**: Awards extra coins and populates the bonus list for words not in the main grid.
  * **Invalid Word**: Triggers a shake animation on the preview bar in red and plays an error sound.
* **Automated Level Progression**: Once all words in the crossword grid are solved, a beautiful Level Cleared trophy overlay is displayed, and the game automatically transitions to the next level after 2 seconds.
* **Satisfying SFX Audio**: Features dynamic sound effects for success, error, and victory using `SoundPool` (with system-level audio fallbacks) for satisfying tactile gameplay.
* **Animated GIF Backgrounds**: Smooth, high-performance animated backgrounds on both the Home and Game screens.

---

## 🛠️ Technical Architecture

The app is built using clean, production-level Android architecture patterns:

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│   (HomeScreen, LevelScreen, GameScreen, Compose UI)    │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│                    ViewModel Layer                     │
│  (HomeViewModel, LevelViewModel, GameViewModel)        │
│          - Manages UI State via StateFlow              │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│                   Repository Layer                     │
│                   (GameRepository)                     │
│   - Acts as the single source of truth for progress    │
└─────────────────┬──────────────────┬───────────────────┘
                  ▼                  ▼
┌────────────────────┐     ┌────────────────────┐
│   Local Source     │     │    Data Source     │
│ (SharedPreferences) │     │    (JSON Asset)    │
│  - Solved Words    │     │  - levels.json     │
│  - Unlocked Level  │     └────────────────────┘
│  - Coin Count      │
└────────────────────┘
```

### 1. State Retention & Persistence
* **SharedPreferences**: Solved words for each level, maximum unlocked levels, and coin balances are saved locally to persistent preferences. Progress survives screen rotation, app backgrounding, and back stack transitions.
* **Android Auto Backup**: Configured with `android:allowBackup="true"`. The local SharedPreferences file is automatically backed up to the user's Google Drive, restoring their progress automatically if they uninstall and reinstall the app.
* **Screen Orientation Lock**: Locked to `portrait` mode in the manifest to ensure coordinate calculation remains bug-free and layouts look premium.

### 2. Back Stack & Navigation Hygiene
* Built using **Jetpack Compose Navigation**.
* Back navigation is handled cleanly with system back-button presses without duplicates or memory leaks.
* Uses precise stack popping (e.g. `popUpTo(Screen.Game.route) { inclusive = true }`) during level transition to ensure the backstack doesn't accumulate completed screens.

---

## 🎨 Custom Swipe & Gesture Logic

The letter wheel is a custom component drawn on a Canvas. The gesture logic is handled in [LetterWheel.kt](file:///c:/Users/Shikha%20yadav/AndroidStudioProjects/PeopleFn/app/src/main/java/com/example/peoplefn/ui/game/components/LetterWheel.kt):

1. **Pointer Coordinates**: Using `pointerInput` with `detectDragGestures`, we track the current drag coordinate of the user's finger.
2. **Hit Detection**: During dragging, we calculate the distance between the finger coordinates and the center coordinates of each letter node on the circle:
   $$\text{distance} = \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2}$$
   If the distance is less than the node radius, the letter is selected.
3. **Dynamic Lines**: We draw a line from the first selected letter to the second, and from the last selected letter directly to the user's current drag coordinates.
4. **Validation Lock**: When a word is completed and submitted, the wheel interactions are locked during validation. The lines and circles turn green (success) or red (error) based on validity.
5. **Compose Stale-Closure Fix**: Wrapped callback lambdas inside `rememberUpdatedState` to ensure that gesture callbacks always read up-to-date state values on finger release, resolving classical pointer input state bugs.

---

## 📂 Level Data Structure

Levels are loaded from [levels.json](file:///c:/Users/Shikha%20yadav/AndroidStudioProjects/PeopleFn/app/src/main/assets/levels.json):

```json
{
  "id": 2,
  "gridWidth": 5,
  "gridHeight": 5,
  "wheelLetters": ["D", "O", "G"],
  "words": [
    { "id": "w2_1", "text": "DOG", "startRow": 0, "startCol": 0, "direction": "HORIZONTAL" },
    { "id": "w2_2", "text": "GOD", "startRow": 0, "startCol": 2, "direction": "VERTICAL" }
  ],
  "bonusWords": []
}
```

* **Grid Coordinates**: Every word specifies a `startRow`, `startCol`, and a `direction` (`HORIZONTAL`/`VERTICAL`). 
* **Dynamic Grid Building**: When a level loads, the app builds a dynamic `GridCell` matrix:
  * Intersecting letters are calculated automatically.
  * A cell is revealed if any of the intersecting words are solved.

# Compose A11y Toolkit

Accessibility bugs like missing content descriptions and undersized touch targets usually get caught in QA, if they get caught at all. Not at build time, while the composable is still sitting right there on screen.

## Architecture

```
compose-a11y-toolkit/
├── toolkit/
│   └── src/main/java/com/a11ytoolkit/toolkit/
│       ├── components/
│       │   ├── A11yButton.kt
│       │   ├── A11yCard.kt
│       │   └── A11yTextField.kt
│       └── audit/
│           ├── AuditRule.kt                  # MissingContentDescriptionRule, MinTouchTargetRule
│           ├── AccessibilityAuditor.kt        # findAccessibilityIssues(): walks the real semantics tree
│           └── AccessibilityAuditOverlay.kt   # debug-only Canvas overlay
└── sample/
    └── src/
        ├── main/java/com/a11ytoolkit/sample/
        │   ├── MainActivity.kt
        │   └── SampleScreen.kt               # toolkit components + 3 intentionally-broken raw elements
        └── testDebug/java/com/a11ytoolkit/sample/
            └── AccessibilityAuditTest.kt      # runs the real rule engine against the real screen
```

## How the audit actually works

`AccessibilityAuditOverlay` doesn't know which composables are "supposed" to be broken. Every frame it's enabled, it grabs the host view's `RootForTest.semanticsOwner`, walks the same merged semantics tree TalkBack sees, and runs each `AuditRule` against every node's real measured bounds and resolved semantics config:

- **`MissingContentDescriptionRule`** flags an interactive node (one with a click, expand, or collapse action) that has no content description, no visible text, and no state description.
- **`MinTouchTargetRule`** flags an interactive node whose measured window bounds fall under 48dp in either dimension.

Violations get drawn as red outlines on a `Canvas` positioned over the real content, translated from window coordinates into the overlay's local space via `LayoutCoordinates.windowToLocal`.

## Demo

Toggle "Audit Mode" in the sample app: the three raw, toolkit-bypassing elements at the bottom of the screen (an icon button with no content description, a 24dp clickable box, and a raw clickable icon with no content description) get outlined in red. Everything built with `A11yButton`, `A11yCard`, and `A11yTextField` stays clean.

## Metric

`AccessibilityAuditTest` renders the real `SampleScreen` under Robolectric plus `compose-ui-test`, runs the exact production `findAccessibilityIssues` function against its real semantics tree, and asserts the result. It's the same code path the on-screen overlay uses, not a separate reimplementation.

Overlay correctly flagged **3/3** intentionally-broken elements in the sample screen, with 0 false positives against the toolkit-built components. See `sample/src/testDebug/java/com/a11ytoolkit/sample/AccessibilityAuditTest.kt`.

## Stack

Jetpack Compose, Compose Semantics APIs (`SemanticsOwner`, `SemanticsNode`, `RootForTest`), Material3.

## Try it yourself

### Prerequisites

- JDK 17
- Android SDK with `compileSdk 35` platform + build-tools installed (Android Studio installs this for you; command-line-only setups need `sdkmanager "platforms;android-35" "build-tools;36.0.0"`)
- An emulator or a physical device with USB debugging enabled, if you want to actually see the overlay in action. The audit logic itself is verified headlessly (see below), but toggling "Audit Mode" and watching it highlight elements is the whole point of the demo.

### Clone and open

```
git clone https://github.com/ranab4b/compose-a11y-toolkit.git
cd compose-a11y-toolkit
```

Point Gradle at your SDK, either by setting `ANDROID_HOME`/`ANDROID_SDK_ROOT`, or by creating `local.properties` (gitignored) in the repo root:

```
sdk.dir=/path/to/Android/sdk
```

Then either open the folder in Android Studio, which syncs automatically, or use the command line. The wrapper is already committed, so there's no need to install Gradle locally:

```
./gradlew :sample:installDebug
```

That builds and installs the sample app on whatever device or emulator is connected (check with `adb devices`). Launch "A11y Toolkit Sample" from the app drawer, then flip the **Audit Mode** switch at the top. Three broken elements sit at the bottom of the screen, a heart icon button, a tiny delete target, and a share icon, and all three get outlined in red. Flip the switch off and the outlines disappear. Nothing else about the screen changes.

### Verify the metric yourself

No device needed for this one. It's a headless Robolectric test that renders the real screen and runs the real rule engine against it:

```
./gradlew :sample:testDebugUnitTest --tests "com.a11ytoolkit.sample.AccessibilityAuditTest"
```

Pass `--info` if you want to see the actual issues printed (which rule fired, and the pixel bounds of the flagged node).

### Using the toolkit components in your own project

This isn't published to Maven Central. The `toolkit` module is meant to be copied into or included in your own project, either via `include(":toolkit")` in `settings.gradle.kts` plus a git submodule, or by just copying the sources over, whichever fits how you work. Once it's on your classpath:

```kotlin
A11yButton(onClick = { /* ... */ }, text = "Save")

A11yCard(
    contentDescription = "Open the article: Accessibility isn't optional",
    onClick = { /* ... */ },
) {
    Text("Accessibility isn't optional")
}

A11yTextField(
    value = note,
    onValueChange = { note = it },
    label = "Add a note",
)

// Anywhere near the root of a screen, in debug builds:
AccessibilityAuditOverlay(enabled = auditModeEnabled) {
    YourScreenContent()
}
```

`A11yCard`'s clickable variant and `A11yTextField` both throw immediately (via `require`) if you forget the accessible label. That's intentional. Better the mistake surfaces in your own debug build than in a screen reader session months later.

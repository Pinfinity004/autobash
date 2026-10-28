# AutoBash 7.4

On-device screen recognition and input automation for Android. Save a crop of any UI
element as a **target**; AutoBash watches the screen through an `AccessibilityService`,
finds that element with OpenCV template matching, and performs the action you assigned —
tap, swipe, back or home — at the match location.

Grew out of an earlier prototype called ImageTap, then Skiptap. This repository is the
source of release **7.4**.

> **Not for Google Play.** AutoBash injects input via `AccessibilityService`. Play policy
> disallows accessibility-based automation, so this app is for personal use and sideloading.
> It will not pass Play review, and targets it is well suited to (ad "skip"/"close" buttons)
> are covered by ad-network terms of service. The app requires no `INTERNET` permission —
> the manifest removes it explicitly — and processes everything on-device.

## Features

- **Targets** — crop from a screenshot or import an image; up to 100 saved, shown in a
  reorderable grid.
- **Recognition** — OpenCV template matching with multi-scale search, per-region accuracy
  thresholds, transparent-pixel masking, and a confirmation step before a tap fires.
- **Scan engine** — parallel recognition (`ScanEngine`) with Balanced/Fast modes, worker
  caches, recent-location hints, and fair rotating/interleaved scheduling so targets at the
  end of a preset are not starved.
- **Actions** — tap, swipe in four directions, back, home, plus a **pointer marker** to fire
  a tap or swipe on an interval with no detection at all.
- **Auto Tap** — 1–20 movable numbered targets on a timer, with play/pause and saved layouts.
- **Sessions** — start/pause/resume, quick save, checkpoint and recovery that preserves
  action count and timed-plan progress across process death.
- **UI** — cream/navy theme, neural-brain logo, pulsing nerve background, and floating
  controls resizable from 75 % to 150 % independently for simple and advanced popups.

## Build

Requirements: **JDK 17**, Android SDK **platform 35** and **build-tools 35.0.0**.

```bash
./gradlew assembleDebug
```

The debug build is signed with a local development key; see [`signing/README.md`](signing/README.md)
for how that is wired up and how to supply your own. Release APKs are attached to GitHub
Releases rather than committed here.

## Project facts

| | |
|---|---|
| `applicationId` | `com.pratik.imagetap.v7` |
| `namespace` | `com.pratik.imagetap` |
| Version | `7.4` (code `24`) |
| minSdk / targetSdk / compileSdk | 30 / 35 / 35 |
| Language | Java 17 |
| Android Gradle Plugin | 8.9.2 |
| Vision | `org.opencv:opencv:4.12.0` |
| Tests | 155 passing, as reported for this release |

`applicationId` deliberately differs from `namespace`, which is why this build installs
alongside earlier versions instead of replacing them.

## Layout

```
app/src/main/java/com/pratik/imagetap/
  MainActivity.java        dashboard, session controls, settings
  TapService.java          AccessibilityService: capture -> match -> gesture
  VisionMatcher.java       template matching, scaling, evidence scoring
  ScanEngine.java          parallel scan scheduling
  FrameOrder.java          fair rotating target order
  TargetOrder.java         persistent drag-to-reorder grid order
  SessionCheckpoint.java   crash-safe session state
  AutoTap*.java            timed tap/swipe runner, store, overlay, UI
  Brain*.java, Nerve*.java brand artwork and animated background
  Ui.java, TargetGrid.java, PresetEditor.java, Region*.java, PopupSize.java
app/src/test/              Robolectric and JVM unit tests
tests/                     standalone pure-Java and native benchmark harnesses
```

## Documentation

| | |
|---|---|
| [`docs/CHANGELOG.md`](docs/CHANGELOG.md) | Version-by-version history, 0.x → 7.4 |
| [`docs/HISTORY.md`](docs/HISTORY.md) | The original release index, newest first |
| [`AUTOBASH_7.4.md`](AUTOBASH_7.4.md) | Current release notes; earlier versions sit beside it |
| [`VALIDATION.md`](VALIDATION.md) | Test and lint results per release |
| [`REVIEW.md`](REVIEW.md) | Independent review notes |
| [`docs/RECOVERY_STATUS.md`](docs/RECOVERY_STATUS.md) | Provenance of this source tree |

## Status and limits

Automated suite: 155 tests passing, build and lint clean. **No physical-device test was
performed**, and the release notes say so plainly. On-device behaviour — accessibility
permission grants, file-provider access for importing images, capture timing on real
hardware, and OEM-specific battery restrictions — should be verified on your own phone
before relying on it.

## License

See [`LICENSE`](LICENSE).

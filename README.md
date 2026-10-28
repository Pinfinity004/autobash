<div align="center">
<a href="https://raw.githubusercontent.com/Pinfinity004/autobash/main/docs/media/logo-banner.png">
<img src="https://raw.githubusercontent.com/Pinfinity004/autobash/main/docs/media/logo-banner.png" width="620" alt="AutoBash — circuit-brain logo">
</a>
</div>

<p align="center"><b>It sees the button. It presses it.</b></p>

<p align="center">
Show AutoBash an image — a skip button, a close cross, a play control, a stubborn
dialog — and it finds that exact thing on your screen, then acts on it. Tap. Swipe.
Back. Home.
</p>

<p align="center">
On-device. No account. No cloud. <b>No internet permission at all.</b>
</p>

<p align="center">
<img alt="version" src="https://img.shields.io/badge/version-7.6-FFD166?style=flat-square">
<img alt="android" src="https://img.shields.io/badge/Android-11%2B-3DDC84?style=flat-square&logo=android&logoColor=white">
<img alt="java" src="https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white">
<img alt="opencv" src="https://img.shields.io/badge/OpenCV-4.12.0-5C3EE8?style=flat-square&logo=opencv&logoColor=white">
<img alt="tests" src="https://img.shields.io/badge/tests-168%20passing-2ea44f?style=flat-square">
<img alt="network" src="https://img.shields.io/badge/network-none-critical?style=flat-square">
<img alt="license" src="https://img.shields.io/badge/license-MIT-blue?style=flat-square">
</p>

<p align="center">
<a href="https://raw.githubusercontent.com/Pinfinity004/autobash/main/docs/media/auto-tap.png"><img src="https://raw.githubusercontent.com/Pinfinity004/autobash/main/docs/media/auto-tap.png" width="320" alt="AutoBash Auto Tap screen"></a>
</p>

---

## The idea

Most auto-clickers are blind. They tap coordinates in a loop and hope the world has not
moved. AutoBash is the other thing: it **looks** first.

You crop the thing you want — a tiny 40-pixel skip arrow, a grey close cross, a specific
game tile — straight out of a screenshot. AutoBash keeps that as a *target*. Then it
watches the live screen through Android's `AccessibilityService`, hunts for your target
with OpenCV template matching, and presses it the moment it appears.

Size changes. Backgrounds change. The ad network ships a new layout. AutoBash searches
across scales, masks out the background noise you cropped by accident, and scores every
candidate before anything is touched.

## What makes it not-just-another-clicker

| | |
|---|---|
| 🧠 **Real matching, not coordinates** | Multi-scale template search, transparent-pixel masking, per-region accuracy thresholds, and a confidence score before any tap fires. |
| 🎯 **Five actions, not one** | Tap, swipe left/right/up/down, back, home. A quick-save menu asks which one you meant. |
| 👀 **Look twice** | Optional two-frame confirmation: the same target must appear in a second capture before AutoBash commits. Stale frames never trigger a tap. |
| ⚡ **Parallel eyes** | The scan engine checks targets concurrently with Balanced/Fast modes and worker caches — a 51-target pass went from **4443 ms to 1625 ms** on the tiny-symbol benchmark. |
| 🔄 **Nothing gets starved** | Fair rotating target order, so target #51 is not permanently checked last. |
| 💾 **Survives death** | Session checkpoints keep your action count, timed-plan progress and floating controls across a process kill. |
| 🧭 **Guided help** | Step-by-step guides with progress on every tab, which explain but never start automation on their own. |
| 🗄️ **Whole-app backup** | Export all 7 actions × 3 preset slots — images, regions, timelines and Auto Tap layouts — into one file, and restore it on another phone. |
| 🤖 **Auto Tap** | Up to 20 numbered targets on a timer with play/pause, saved layouts and an on-device tap test — no image detection at all. |
| 🎨 **Actually designed** | Cream-and-navy, a hand-drawn circuit brain, pulsing nerve background, resizable floating controls. |

## How it works

```
   ┌──────────────┐    ┌───────────────┐    ┌──────────────┐    ┌────────────┐
   │  CAPTURE     │───▶│   PROPOSE     │───▶│   VERIFY     │───▶│    ACT     │
   │  screenshot  │    │  fast candidate│    │ full-res match│    │ gesture    │
   │  + timestamp │    │  search @ 720px│    │ + mask + score│    │ dispatch   │
   └──────────────┘    └───────────────┘    └──────────────┘    └────────────┘
          │                                          │                 │
          └── fresh? (≤2s) ─────────────────────────┘── ≥ threshold? ──┘
```

Every screenshot carries Android's own `uptimeMillis` timestamp. If a scan took too long,
its result is treated as **stale** and not allowed to authorise a tap — a matched but old
position is a wrong tap, and wrong taps are worse than no taps.

## Screens

<p align="center">
<img src="https://raw.githubusercontent.com/Pinfinity004/autobash/main/docs/media/auto-tap-controls.png" width="230" alt="Auto Tap floating controls">
&nbsp;&nbsp;
<img src="https://raw.githubusercontent.com/Pinfinity004/autobash/main/docs/media/auto-tap-landscape.png" width="360" alt="Auto Tap in landscape">
</p>

## Install

Grab the APK from **[Releases](https://github.com/Pinfinity004/autobash/releases)**.

1. Install `AutoBash_7.6.apk` (allow install from unknown sources).
2. Open AutoBash and follow the **Android access** card on Home → enable **AutoBash recognition**.
3. Add a target: screenshot → crop the thing you want → confirm.
4. Start a session. **Leave Tap on detection off for the first minute** and just watch the
   green candidate brackets to check it is looking at the right thing.

Requires Android 11+ (API 30), `arm64-v8a`.

> ⚠️ **This is the debug build and it is not on Google Play.** It ships debuggable, and Play
> policy forbids accessibility-based automation anyway. Sideload it, use it on your own
> device, and understand what it does before you trust it unattended.

## Build

**JDK 17**, Android SDK **platform 35**, **build-tools 35.0.0**.

```bash
git clone https://github.com/Pinfinity004/autobash.git
cd autobash
./gradlew assembleDebug
```

The debug variant is signed with a local development key — see
[`signing/README.md`](signing/README.md) to supply your own. Release APKs belong in
Releases, not in the tree.

## Under the hood

| | |
|---|---|
| `applicationId` | `com.pratik.imagetap.v7` |
| `namespace` | `com.pratik.imagetap` |
| Version | `7.6` (code `26`) |
| minSdk / target / compile | 30 / 35 / 35 |
| Android Gradle Plugin | 8.9.2 |
| Vision | `org.opencv:opencv:4.12.0` |
| Tests | 168 passing |

```
app/src/main/java/com/pratik/imagetap/
  MainActivity.java        dashboard, session control, settings
  TapService.java          AccessibilityService: capture → match → gesture
  VisionMatcher.java       template matching, multi-scale, masking, scoring
  ScanEngine.java          parallel scan scheduling
  FrameOrder.java          fair rotating target order
  TargetOrder.java         persistent drag-to-reorder grid order
  SessionCheckpoint.java   crash-safe session state
  AutoTap*.java            timed tap/swipe runner, store, overlay, UI
  Brain*.java, Nerve*.java brand artwork and the animated background
```

## Documentation

| | |
|---|---|
| [`docs/CHANGELOG.md`](docs/CHANGELOG.md) | Version history, 0.x → 7.6 |
| [`docs/HISTORY.md`](docs/HISTORY.md) | Original release index |
| [`AUTOBASH_7.6.md`](AUTOBASH_7.6.md) | Current release notes |
| [`VALIDATION.md`](VALIDATION.md) | Test and lint results per release |
| [`REVIEW.md`](REVIEW.md) | A candid review of what works and what does not |
| [`docs/RECOVERY_STATUS.md`](docs/RECOVERY_STATUS.md) | Provenance of this source tree |

## Honest limits

- **No device testing was performed.** All 168 tests are automated (Robolectric + JVM +
  native OpenCV). Real phones behave differently: OEM battery managers kill accessibility
  services, secure windows refuse screenshots, and gesture delivery is not guaranteed.
- **A match is not an intention.** Finding a shape that looks like a skip button does not
  prove it skips anything. Constrain each target to a region and check in preview mode.
- **Small, low-contrast, rotated or animated targets are hard** for template matching.
  It is not a neural network and does not generalise to unseen designs.
- **Accessibility automation is a policy minefield.** Not for Play Store distribution, and
  the ad-skip use case sits against more than one ad network's terms.

## License

MIT — see [`LICENSE`](LICENSE).

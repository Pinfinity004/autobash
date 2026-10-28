# AutoBash 7.3 — Auto Tap
Version code 23. Android 11 or newer. Update over the existing AutoBash installation to retain presets; do not uninstall first.

## Start
Open **Auto Tap**, enable **Allow actions**, then choose **Open Auto Tap controls**. The new popup is separate from recognition and timeline controls. Opening it stops the other action engine so gestures cannot compete.

Choose **Number** for 1–20 targets. Drag the numbered circles into position while paused; tap a circle to edit exact screen-percentage coordinates or its timing. **Targets** shows/hides placement circles. **Play** removes marker windows before tapping; **Pause** retains progress; **Stop** closes controls. Move the popup away from every target.

Targets run sequentially: 1, 2, …, N. One cycle visits every target once. Multiple targets are not simultaneous fingers.

## Speed and useful extras
- **Fastest:** 1 ms hold, 0 ms added gap, next dispatch after completion.
- **Reliable:** 100 ms hold, 100 ms gap; useful when a receiving app ignores very short touches.
- Custom hold 1–1000 ms; gap and start delay 0–60000 ms.
- Per-target hold/gap overrides, reset when applying a speed preset.
- Continuous operation or a whole-cycle limit.
- Three named saved layouts, independently stored from image presets.
- Movable controls with persisted position, visible tap/cycle counts and completed gestures per second.
- Play/Stop stay above scrollable details for shorter displays.
- **Test taps on this phone** opens a pad that counts actual touch-up events received by its View. Put targets inside it, move the menu outside, press Play, and compare received taps with completed gestures. Its rate is measured from the first received tap; reset before comparing settings.

Android's GestureDescription.StrokeDescription accepts a positive integer duration in milliseconds, so 1 ms is the smallest expressible hold. It is not a guarantee of 1000 taps/second. Android scheduling, display/input sampling, accessibility dispatch and the receiving app affect throughput. No physical-device maximum was measured for this release. A completed gesture does not prove that another app accepted a click.

Only one gesture is in flight. Sending new gestures before completion can cancel earlier ones, so this version avoids overlapping dispatch and catch-up bursts. There is an 80 ms settling allowance on Play/Resume to remove target windows; it is not added between taps.

## Pause, stop and recovery
Pause/Stop prevent new dispatches. A stroke already sent to Android may finish (up to the configured hold, at most one second). A cancelled/rejected gesture or missing callback pauses instead of retrying blindly. Returning to the editor, screen lock, or rotation also pauses. Check positions before resuming.

Layout changes start a new run. If a gesture is pending, settings changes wait for its completion before applying. Dragging a marker preserves current progress.

Service recovery restores controls/progress **paused**, never silently replays actions. Active progress is checkpointed approximately once per second and on pause/close, so abrupt process death may lose the most recent second of counts. An interrupted gesture's outcome can be unknown. Saved layouts/positions survive separately.

## Validation
Final result: **150 tests passed, zero failed, zero skipped**. The debug APK build and Android lint completed successfully (zero errors; 105 non-blocking warnings, including localization/accessibility style suggestions and intentional synchronous saves). Portrait and landscape UI previews were inspected.

Automated validation results are included with the source in tests/release-7.3-evidence. Tests cover Fastest timing, 20-target coverage, pause/resume, cycle limits, rejection, cancellation, watchdog timeout, delayed/duplicate callbacks, Stop, recovery, UI controls, marker removal, layout persistence and a 10,000-completion virtual scheduler soak. Desktop OpenCV regression checks cover the existing recognition engine.

Robolectric and desktop tests are not real-phone throughput tests. Use the included received-tap test pad to tune speed for your device; games may need a longer hold than the pad.

## Research used
Android API contracts:
- https://developer.android.com/reference/android/accessibilityservice/GestureDescription.StrokeDescription
- https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/accessibilityservice/GestureDescription.java

Auto-clicker product listings reviewed for practical features:
- https://play.google.com/store/apps/details?id=com.doun.automouse
- https://play.google.com/store/apps/details?id=com.autoclicker.tap

Useful patterns adopted: numbered sequential targets, individual hold/interval settings, start delay, cycle limits and named layouts. Store marketing claims are not evidence of achievable tap rates.

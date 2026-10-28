# AutoBash 6.7

Install the APK over AutoBash 6.6 without uninstalling. The package and signing key remain the same, so your targets, presets, and settings are preserved.

## Changes

1. **Scan pulse: 0.1–10 seconds.** The app requests the configured cadence without overlapping scans. When Android rejects screenshots as too frequent, capture pacing adapts instead of stopping the session. The requested pulse is not a guarantee of ten completed scans per second: capture limits and recognition work can make the actual rate slower. The previous 6.6 hybrid recognition engine is unchanged.
2. **Compact target grid.** Three columns on a typical phone, two on narrower screens, and more on tablets. The header and add/preset toolbar take less space. Tap a tile or image to edit its name, enabled state, source width, and search area. Each tile keeps its delete button and confirmation. Pagination remains 30 images, with up to 500 per preset.
3. **Duplicate threshold: 80–100%.** Find duplicates now has a slider, initially 99%, and remembers the selected value. At 100%, original-pixel identity is required. Lower values suggest looser groups. Review and select the image to keep before merging; existing backup/undo behavior remains.
4. **Detailed launcher switch.** Settings → Appearance → Detailed launcher defaults to On. Off shows only a 64 dp brain with a central Stop ×; drag the brain to move it, tap to stop. There are no counters, status messages, or extra actions in this mode. To regain Quick save and Pause/Resume, turn Detailed launcher back on. Changes apply when leaving the app settings for the running session. Focus guides and touch-ring effects remain separately controlled by their existing settings.
5. **Enabled-setting glow.** Enabled switches use a soft green text glow, including Tap on detection, Touch pulse, image inclusion, and region exclusion controls. Off removes the glow. Switch positions and labels remain available as non-color indicators. Active targets also show a small green status label.
6. **Preset import/export.** Open Images → Presets. Export current writes a single `.autobash.zip` file through Android's file picker. You can then use Share file or share the saved file from Files. The recipient uses Presets → Import file in AutoBash 6.7 or later.

## Preset sharing

The file includes all images in the selected preset, their transparency masks, names, enabled states, source screenshot widths, and per-target search areas. Global regions and device-wide settings stay on each phone, matching the existing preset model.

Import shows the incoming name and image count, then lets you choose one of the three slots. **Replace slot & import** replaces that slot and opens the imported preset; the other slots remain intact. Cancel leaves the existing presets unchanged. Transfers temporarily pause an active scanning session and preserve a pre-existing manual pause. Scanning also remains paused while the export share chooser is open.

Files are validated before the slot changes: format version, image count, paths, PNG checksums/completeness, dimensions, and per-image options. Sharing supports up to 500 images, 32 MiB per PNG, 256 MiB expanded archive contents, and 20 million pixels per image. Invalid archives are rejected without replacing the destination preset.

## Validation

60 Robolectric Android tests passed with zero failures, including preset round trips with masks/options, rejection of malformed/oversized/truncated files, cancelled and conflicting imports, file-picker intents, grid actions, active-state glow, compact dragging/stopping, launcher switching, and screenshot-throttle adaptation. Pure-Java and native recognition regression suites also passed. Lint: 0 errors, 70 warnings (including existing UI/localization warnings). APK signature verified; package `com.pratik.imagetap.v7`, version code 15, version 6.7. The recognition-engine source is byte-for-byte unchanged from 6.6.

Native UI renders were inspected for the target grid, compact brain control, active-setting glow, and import preview. Physical-phone file-provider behavior, capture throttling, and performance still require device testing. No new on-device speed claim is made for this release.

The Android screenshot API explicitly reports overly frequent requests with `ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT`: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT

The project includes the Gradle build, signing configuration, unit tests, pure-Java/native matcher tests, and prior release notes. Build with JDK 17, Android SDK 35, and Gradle 8.11.1.

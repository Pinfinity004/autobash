# AutoBash 6.5

AutoBash is the new name and visual identity of Skiptap. Install AutoBash_6.5.apk over Skiptap 6.4.1 without uninstalling. The package and signing key remain the same, so existing images, presets and settings remain in place. Android 11+, ARM64 / x86_64. Version code 13.

## Design and navigation

- Deep navy surfaces with cream text and primary controls; soft green is reserved for neural signals, scan state and touch feedback.
- An original hollow circuit-brain mark is drawn natively and used on the home screen, launcher icon and floating controls. It is vector artwork, so it remains sharp at different sizes. Your references informed the direction; no screenshot imagery or third-party stock brain artwork is embedded in the app.
- Tap the large home-screen brain to open the circular menu: Images, Regions, Presets and Settings. Tap the brain again or press Back to close the menu. The open state survives ordinary activity recreation/rotation.
- Labeled bottom navigation remains as a quick, accessible alternative. Tap the small brain in the header to return home.
- The home summary shows the current preset, number of enabled targets and configured scan pulse. Tap the summary to switch or manage presets.
- The old “Ready when you are” heading and marketing text have been removed. Start session stays reachable at the bottom; smaller screens can scroll the remaining home content.

## Nerve animation

The home background uses faint circuit/nerve paths with a soft green travelling signal about every 4.2 seconds. Touches restart a shorter response wave. The central brain has a restrained glow. These are decorative home animations, not indications of recognition accuracy or semantic understanding.

Settings → Reduce motion stops ambient movement and menu transitions. Android's system animation setting is also respected. Keep Reduce motion off to see the new pulses. Drawing schedules further animation only while the relevant view is shown and its window is visible.

## Floating control

The expanded panel has a softly scalloped, brain-inspired outline and small stem. The compact form uses the same circuit-brain logo. Tap the brain to collapse/expand; drag it to move the control.

Quick save, Pause/Resume and Stop retain their existing behavior. The status areas have reserved heights so changing messages do not make the panel grow and shrink. The nerve strip follows the real scan-progress bar; it uses a travelling signal while processing is indeterminate and stops when paused. A separate badge shows the session's completed gesture count.

“Android completed” and the tap counter refer to Android's gesture callback. They do not prove the destination game accepted the touch or performed the intended action. The persistent tap-status line from 6.4.1 remains available to explain confirmation, delay, rejection or cancellation.

## Existing features retained

Three presets, up to 500 images per preset, source-size calibration, per-image areas, global accuracy/exclusion regions, same-frame collection scans, crop/mask tools, Quick save auto-pause, independent target confirmation and adjustable touch duration remain. This update does not change the recognition algorithm or add AI training.

## Validation and limits

- assembleDebug, lintDebug and testDebugUnitTest passed.
- 40 Robolectric tests passed on Android API 34 with native graphics: the existing 33 tests including gesture dispatch/callback handling, plus seven new design checks for brain-menu navigation, presets/Back behavior, state retention on rotation, compact-screen targets, reduced motion, timed/touch nerve response and launcher rendering.
- Native renders of home, expanded circular menu, compact menu, launcher icon and floating panel were inspected.
- The matcher, target-confirmation helper, target/preset stores, capture gate and full-frame ordering files were compared with 6.4.1 and remain unchanged. The service changes concern visual controls, feedback and animation binding.
- Lint: zero errors, 60 warnings. Remaining warnings include legacy Android View conventions, localization and custom-drawing/accessibility guidance.
- APK label AutoBash, version 6.5, version code 13 and signing compatibility were verified. Package com.pratik.imagetap.v7; certificate SHA-256 a864d8961f62522dcc7dd8db9ac1f62cd6ab7d8955988f4428b9aea0f8f67a72.

No physical phone or emulator was available. Real-device animation smoothness, battery cost, launcher rendering across OEMs, accessibility capture and gesture behavior still need phone validation. The provided PNG previews are renders of the actual native app views, with reduced motion enabled for repeatable screenshots; they are not concept mockups.

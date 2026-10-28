# AutoBash 6.9

Install AutoBash_6.9.apk over the existing installation. Do not uninstall first. The package ID and signing key are unchanged; existing image collections, presets, settings and masks are retained.

## Quick save

Quick save pauses the session and captures the current screen before opening any editor controls. A new destination menu then asks which action should receive the image: Tap, any swipe direction, Back or Home. Each choice displays that action's active image preset. After choosing, crop/mask the image and save it as usual. Cancelling returns without saving. Closing Quick save preserves an existing manual pause.

## General: repeating actions without images

Open Actions, choose an action, then select **General · timed actions**. Add a timed action, name it if useful, set an interval, and choose **Place marker on screen**. AutoBash opens the app selected in Settings → Launch app, or moves itself into the background if no app is selected.

Drag the Tap marker to the desired position. Swipes have Start and End markers; the end follows the chosen direction. Tap the marker editor's heading to move its controls between the top and bottom, making covered areas available. Save position returns to the action editor; press Save there to commit the action. Back and Home use Android navigation and need no marker. Marker placement requires the existing AutoBash Accessibility service.

Choose **Start General mode**, or select General in the Home run-mode selector before starting. All enabled General rules across the action collections run together. They do not require images. Each interval is the waiting time after that rule's previous action completes; the first interval is the delay before its first execution. Actions share one execution lane, so simultaneous deadlines are serialized rather than sending overlapping gestures. Missed repetitions of the same rule are not queued for catch-up.

General rules are separate from image preset slots. Switching image presets does not change them.

## Timeline: ordered steps

The new Timeline tab lets you add, edit, enable/disable, delete and move steps up or down. Choose an action, its position (or swipe endpoints), and the delay before it. The first delay starts when the session is running and unblocked; subsequent delays start when the preceding action completes.

Example:

1. Tap at your marker after **3.4 seconds**.
2. Swipe down after **1.2 seconds** following completion of that tap.
3. Back after another chosen delay.

Enable **Repeat timeline** to start again after the last step, including the first step's delay. Otherwise the sequence finishes once and leaves the Stop control visible with “Timeline complete.” Disabled steps are skipped. Cancelling or rejecting a gesture does not advance the sequence; the same step retries after a short delay.

For taps and swipes, completion means Android's gesture callback. For Back and Home, Android provides acceptance of the navigation request rather than a gesture-completion callback. Add enough delay for the next app screen to load; AutoBash cannot infer that loading has finished in a timed sequence.

## Modes, timing and controls

Home now offers Image recognition, General and Timeline. Only one runs at a time, preventing detection-based actions from interfering with a timed sequence. A running session uses a snapshot of its selected mode and timed actions. Restart to apply edits to a running plan.

The shared enable switch is now called **Allow actions**, retaining its previous saved value. Turning it off previews image matches and freezes timed actions. Pause, Quick save, the marker editor, opening AutoBash, and screen lock/off freeze timed countdowns; Resume does not discard the remaining delay. A gesture already sent can finish while paused, but no following step is sent until resumed. Stop invalidates the session and ignores late callbacks.

The detailed floating panel displays a countdown and progress bar for timed actions. Existing Tap/Swipe/Back/Home visual effects are retained. Focus guides show the next marked position when enabled. Move the floating controls if they overlap a target or swipe path. Very short swipes are blocked rather than converted into accidental taps.

Delays support 0.1–3600 seconds; touch/swipe duration supports 40–1000 milliseconds. Up to 100 General rules and 100 Timeline steps can be stored. Markers use relative screen coordinates; recheck positions if screen orientation or the target app's layout changes. Timed modes do not use image-region accuracy/exclusion settings or the image scan-pulse slider.

If Android restarts the Accessibility service, a previously active General session restarts its timers without replaying missed repetitions. A Timeline session is restored paused at the beginning because its exact in-flight progress cannot be recovered safely; review it before resuming. Android scheduling and the target app can introduce delays; timing is not a hard real-time guarantee.

## Persistence and sharing

General rules and the Timeline are saved locally between sessions. Image preset import/export remains unchanged; it does not include these new timed plans.

## Validation

Build, lint (0 errors, 85 warnings), and all 92 automated Android tests passed with no failures or skips. Native recognition regression tests also passed, including same-frame 51-image coverage, masked targets, regional rules and strongest-instance selection. The recognition engine is unchanged from 6.8.

New tests cover completion-relative delays, pause/resume, completion during pause, non-overlapping timers, repeat behavior, rejected-action retries, separate saved plans, invalid input, all timer dispatch gates, Stop ignoring late callbacks, navigation requests, the destination menu, Timeline UI and marker movement/saving.

No physical phone was available for testing. Live overlay positioning, app switching, Android gesture delivery and device-specific timing still need confirmation on your phone. Test a short sequence first and adjust delays for the target app's loading time.

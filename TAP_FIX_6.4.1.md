# Skiptap 6.4.1 — detected targets and touch delivery

Install this APK over 6.4 without uninstalling. The package and signing certificate are unchanged; saved presets, targets and settings remain. Version code 12.

## Fixes

- Confirmation now belongs to each target. Previously a single counter was shared across detections; another target, including an already-tapped target when repetition was off, could reset confirmation. Each target now accumulates evidence independently across distinct screenshot timestamps. One screenshot cannot confirm itself twice. Missing targets, pauses and session invalidation clear the appropriate history.
- The default touch lasts **100 ms**, replacing 25 ms. A very brief gesture can be less suitable for some apps, although this has not been verified as the cause on your phone. **Settings → Touch duration** adjusts it from 40–250 ms. This is separate from the delay between taps.
- The floating control has a fixed-height tap-status line separate from scan progress. It explains confirmation, configured delay, preview mode, old screenshots, pause/editor state, already-tapped targets, and controls covering the tap point. It also reports gesture submission, rejection, cancellation and Android completion.
- Dispatch exceptions and rejected gestures release the pending state so later scans can retry. Pausing invalidates old results and clears the pending flag; late callbacks cannot increment the current session's tap count.

## What the indicators mean

Green candidate brackets mean a visual match was found. They do not prove a gesture was sent.

The fading touch ring appears after Android reports gesture completion. **Android completed** means the accessibility gesture callback completed; it does not establish that the destination game accepted the input or performed the intended action.

Examples of the persistent status:

| Status | Meaning / next step |
|---|---|
| Confirming 1/2 frames | Waiting for the same target in another captured frame. |
| Preview · taps off | Tap on detection is currently disabled. |
| Waiting for configured tap delay | Recognition succeeded, but the tap interval has not elapsed. |
| Already tapped · Repeat visible targets off | One tap per appearance is enabled. Turn repetition on if repeated taps are intended. |
| Android rejected touch | The accessibility API did not accept the request. Check that Skiptap's Accessibility service remains enabled; reconnect it if needed. |
| Android cancelled touch | The gesture was cancelled; subsequent scans can retry. |
| Android completed | Android finished the injected gesture. If the game still ignores it, try 150–200 ms Touch duration and verify that the highlighted center is the actual actionable button. |

Protected capture restrictions, a locked screen, a paused session, configured exclusions and stale-coordinate safeguards remain. The three presets and same-frame collection scanning from 6.4 remain.

## Verification performed

- Android assembleDebug, lintDebug and testDebugUnitTest passed.
- **33 Robolectric tests passed**, including nine new tests that exercise the production match-to-gesture decision path with a recording gesture adapter: default duration and completion, competing targets, same-frame confirmation prevention, already-tapped target interference, preview/stale blocking, cancellation/rejection retries, duration clamping/exception recovery, one accepted gesture per frame, and pause invalidation of late completion.
- Eight pure Java regression programs and two desktop native OpenCV programs passed. The recognition implementation itself is unchanged by this patch.
- Fixed-height popup render visually inspected, including the new persistent tap-status line.
- Lint: zero errors, 56 warnings. Existing legacy View, localization and accessibility/drawing warnings remain.
- APK signature and version verified. Package com.pratik.imagetap.v7; version 6.4.1 / code 12. Certificate SHA-256: a864d8961f62522dcc7dd8db9ac1f62cd6ab7d8955988f4428b9aea0f8f67a72.

No physical Android phone or emulator was available. The adapter tests verify the app's dispatch decisions and callback handling, not real input injection into your game. Your exact observed device failure cannot be conclusively identified from code alone. After installing, use the persistent tap-status line to distinguish an app-side block from an Android-completed gesture that the game did not act on.

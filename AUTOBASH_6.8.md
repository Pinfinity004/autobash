# AutoBash 6.8

Install AutoBash_6.8.apk over the existing app. Do not uninstall first. The package ID and signing key are unchanged. Existing images, all three presets, image options, masks, merge backups and settings remain under Tap; no file migration or deletion is needed.

## Actions and collections

The Actions tab opens seven collections: Tap, Swipe left, Swipe right, Swipe up, Swipe down, Back and Home. Each has the same target grid, crop/mask editor, duplicate merge tools, and three named preset slots. Each slot supports up to 500 images. One preset per action is active at a time. All enabled targets in those seven active presets participate in every screenshot scan, regardless of which collection you are viewing.

Tap touches the matched object's centre. A swipe starts at that centre and moves in the named direction. Enable “Swipe from screen centre” to use the image only as a trigger and start at the display centre instead; this helps when the trigger is near an edge. Settings provide Swipe distance (10–70% of display width/height; default 35%) and Swipe duration (150–1000 ms; default 350 ms). Gestures shorten near screen edges. A swipe with less than 12 pixels of available travel is blocked with an explanation. Move the floating control if it overlaps a swipe path.

Back and Home use Android's native accessibility navigation actions. Home requests the launcher; Back requests the current app's normal Back behavior. Android may reject an unavailable navigation action. These actions do not tap a guessed navigation-bar coordinate.

The home switch is now “Actions on detection.” Its existing value is retained. Off previews matches without performing any action. Action delay, two-frame confirmation, pause/resume, automatic Quick-save pause, and Stop apply to all actions. Quick save and ordinary cropping include an action destination selector.

## Strongest verified match

The matcher no longer accepts a tracked location immediately when a better instance may exist elsewhere. It compares verified candidates across the permitted search area. The service waits for all active targets in the screenshot to be checked and selects the highest similarity score among eligible matches. A pending confirmation cannot outrank a stronger match. Equal scores use stable collection/file order, starting with Tap. Only one action can be accepted for a screenshot.

Each candidate must pass its normal similarity threshold, image area and global region rules. Two-frame confirmation remains enabled by default. With Repeat visible targets off, targets already acted on are ineligible until absent in two checked scans. These rules can intentionally prevent the highest visible match from acting.

Similarity is an image-comparison score, not a statistical probability. Candidate search remains bounded and approximate, so this release does not promise an exhaustive optimum over every pixel and possible scale. Multiple identical-looking images assigned to different actions are ambiguous: use distinct targets or search areas.

Waiting for the complete scan removes first-found action dispatch. It can increase latency relative to early dispatch, especially with many active collections or alternate-size search. Captures more than two seconds old remain blocked rather than acting on stale coordinates. If the console repeatedly reports an old frame, reduce active targets or narrow their search areas. The pulse setting is a requested interval, not a guaranteed scan duration.

## Visual feedback

- Tap: fading green rings at the touch point.
- Swipe: green directional trail with sparkle particles along the gesture path.
- Back: cream double chevrons.
- Home: pale-blue home outline with an expanding halo.

The Action effects switch retains the old Touch pulse setting. Reduce motion uses static marks that fade. Feedback is hidden before screenshots so it does not become a recognition target. The session badge counts actions. Taps/swipes are counted after Android's completion callback; Back/Home are counted when Android accepts the request. Neither response proves that a particular game's content changed.

## Sharing presets

New exports include their action type and use preset format version 2, requiring AutoBash 6.8 or later. Import previews identify the action and only offer that action's three destination slots. Existing version-1 exports from 6.7 import as Tap. PNG masks, names, source screenshot widths and per-image search areas are preserved. Global regions, swipe settings and other device settings remain local. Import validates files before replacing the chosen slot; the other collections remain untouched.

## Validation

Build and lint passed (0 lint errors; 73 warnings). All 73 Robolectric Android tests passed with no failures or skips. Test evidence is included in the source archive under tests/release-6.8-evidence. Native OpenCV tests include a weaker tracked duplicate and a stronger distant instance in both search modes, 51-image same-frame coverage, masked icons, region exclusions, exact matching, cancellation and hybrid-search parity.

Android-side tests exercise all four gesture directions, Back/Home routing, highest-score arbitration, stable ties, preview/pause gates, rejected navigation, independent presets, legacy import compatibility, action-aware exports, screen bounds and effect rendering/fading, in addition to existing regressions.

No physical Android phone testing was available. Gesture acceptance, screen capture cadence and app-specific responses still need testing on the user's device.

# Changelog

Reconstructed from the build transcript of the original ChatGPT session
(2026-10-22 → 2026-10-28). Early versions were not individually tagged, so the
0.x/ImageTap rows are grouped.

## AutoBash 7.6 — 2026-10-28

- Floating controls rebuilt: dedicated Pause/Stop row, separate full-width Quick save,
  15 sp labels on 52 dp buttons, and reserved space for status and statistics so the panel
  no longer jumps as text changes.
- Auto Tap keeps Play/Pause and Stop above secondary controls; visibility, count, speed and
  layouts use a consistent two-column layout, and secondary controls scroll on short displays.
- One help entry per form. Auto Tap dialogs and the crop editor gain help; the duplicate
  guide buttons are gone. Number selection shows an explicit readable count.
- 180 ms fade for content transitions; dialogs and action buttons do not slide while in use.
- **Export everything**: whole-setup backup covering all 7 actions × 3 preset slots, target
  images and options, names/order, active slots, regions, timed actions, timelines, Auto Tap
  plan and saved layouts, plus portable recognition and appearance settings. Restore
  validates, warns before replacing, and leaves actions **off**. An interrupted restore rolls
  back from a persistent journal.
- 168 automated Android/Robolectric tests; see `USABILITY_7.6.md`.

## AutoBash 7.5 — 2026-10-28

- Home reorganised around three plain-language tasks plus an Android access/setup card that
  leads into Practice.
- `How to use this · ?` on every main tab. Guides advance one step at a time with Next, Back,
  Close and a topic index; help never starts automation or enables Allow actions.
- Contextual guides inside Settings groups, Auto Tap dialogs and the crop editor.
- Advanced session controls collapsed by default so the main choices stay clear.
- Usability review scored 9.1/10 provisionally — an internal heuristic walkthrough, not a
  user study. See `USABILITY_7.5.md`.
- 160 automated tests passed.

## AutoBash 7.4 — 2026-10-27

- Resizable overlay popups, in both simple and advanced mode. Size is persisted per
  screen and clamped to the display bounds.
- Final deliverable of the original session.

## AutoBash 7.3 — 2026-10-27

- Auto-tap section: place N numbered tap targets, configure interval and action,
  play/pause the runner independently of target scanning.

## AutoBash 7.2 — 2026-10-27

- Session checkpointing and recovery. Ongoing session state survives the process
  being killed and restarts, instead of silently resetting to the start.

## AutoBash 7.1 — 2026-10-27

- Non-linear preset traversal. Previously targets at the end of the preset were always
  checked last and could be starved indefinitely; scanning now covers all positions.

## AutoBash 7.0 — 2026-10-27

- Parallel scan engine (`ScanEngine`): targets checked concurrently rather than in a
  sequential loop, targeting the 4.5 s full-pass time down toward 1–2 s.
- Fixed a stall where the UI sat on a stale frame showing "new frame required" while a
  valid target was on screen.

## AutoBash 6.11 — 2026-10-27

- Fresh-frame recheck fix — stale frame rejection no longer drops valid detections.

## AutoBash 6.10 — 2026-10-26

- UI/UX pass: spacing normalisation, varied typography, grouped sections, sharper
  per-section layout, redesigned launcher icon.

## AutoBash 6.9 — 2026-10-26

- Target grid: targets shown as boxes in rows/columns instead of a long scroll.
- Pulse slider minimum lowered to 0.1 s.
- Merge-duplicates threshold slider extended.

## AutoBash 6.8 — 2026-10-26

- Multi-action support: tap, swipe left/right/up/down, back, home.
- Best-match-only tapping — when several elements match, the highest-confidence one is
  chosen instead of an arbitrary one.
- Quick-save now opens an action menu (tap / back / other).
- Manual marker button to place a tap or swipe on screen, fired on an interval.

## AutoBash 6.7 — 2026-10-26

- Scan engine work; version identity moved into `setTitle`.

## AutoBash 6.6 — 2026-10-25

- Continued scan-speed work on the hybrid matcher.

## AutoBash 6.5 — 2026-10-25

- Rebrand to **AutoBash**. Cream + dark-navy palette.
- Hollow digital-punk brain logo; pulsing green nerve background reacting to touches.
- Logo interaction and redesigned home screen.

## Skiptap 6.4.1 / 6.4 — 2026-10-24

- Target-pack workflow: batch crops of ad skip/close buttons exported as transparent PNGs.
- Independent-evidence confirmation (`TapConfirmation`) so a match is verified before the
  tap fires, reducing mis-taps.

## Skiptap 6.2 — 2026-10-24

- Renamed to **Skiptap 6.2**.
- Removed the "smart plan" training feature — it was not worth the effort.
- Added a pulse slider controlling how often the screen is scanned.

## ImageTap 7 — 2026-10-23

- Ad-skip heuristics: recognise likely skip/close affordances rather than requiring an
  exact match for every possible ad variant.

## ImageTap 0.1 – 0.7 / 1.x — 2026-10-22

- Initial prototype: save image targets, detect on screen, tap the location.
- Up to 100 targets; zoomable crop confirmation.
- Accuracy floor lowered to 75–100; click-delay slider.
- Removed the auto-pause/restriction states — the session now runs until explicitly stopped.
- Per-region accuracy (loose at top, 100 % in the middle).
- Merge-duplicates feature for targets matching at 99–100 %.

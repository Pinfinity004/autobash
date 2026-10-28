# AutoBash 7.5 usability review
This is a developer's heuristic review using a first-time-user task script. It is not an independent user study, child testing, or proof that everybody will find the app easy. I cannot literally forget how I built the app.

## Rubric (10 points)
Five categories, each worth two points: discover the right task; know the next step; understand terms; recover/stop safely; read without clutter. Partial scores reflect remaining friction.

## Iteration 1: existing 7.4 — 6.5/10
The brain navigation and run-mode chooser assumed prior knowledge. A beginner needed to understand targets, presets and pulses before doing a simple tap. Help was scattered among descriptions.
Changes: task-based Home, persistent contextual help on all six main destinations, step-by-step guides with Back/Next/Close.

## Iteration 2: first guide design — 8.4/10
Task choices were clearer, but Android access was still a hidden prerequisite. Editing dialogs needed help without returning Home. One large tutorial would overwhelm the user.
Changes: explicit access card, a practice shortcut once access is available, help inside Auto Tap dialogs and the crop editor, topic index for presets/sharing/troubleshooting. Existing session details move into a folded section instead of competing with task choices.

## Final walkthrough — 9.1/10 (heuristic)
- Discover the task: 1.9/2. Home uses goals: repeated taps, picture-triggered actions, sequences.
- Next step: 1.8/2. Access setup and ordered guides explain the path. Android's external permission screen varies by device.
- Terminology: 1.9/2. Guides explain target, preset, cycle, pulse, hold, gap, and milliseconds with examples.
- Recovery/control: 1.8/2. Guides explain Pause vs Stop, master Allow actions, preview, exclusions and missed taps. Device-specific capture failures still require judgment.
- Readability/clutter: 1.7/2. Help is on demand, one step at a time, scrollable; section controls fold. The full advanced feature set still has a learning curve.

## Task script and evidence
1. Find repeated tapping from Home → Auto Tap; read its five steps.
2. Read setup → open Android accessibility settings. Actual OEM settings cannot be tested in Robolectric.
3. Find practice from the ready-access card or Auto Tap → Test taps on this phone.
4. Learn picture action → Actions guide → crop help → preview instructions.
5. Learn sequence → Timeline guide, including relative wait-time example.
6. Adjust popup size → Settings → Appearance help.
7. Find preset/import/export/duplicate guidance → Help & troubleshooting → Presets & sharing.
8. Stop or recover → Getting started and Session & permissions guides.
9. Replay help, move Next/Back, close without changing Allow actions or starting a session.

Automated checks verify task choices, contextual help on every main tab, all guide topics opening, Next/Back order and no action-enabling side effects. UI renders are inspected separately. Existing regression tests cover the underlying actions.

Remaining validation: a genuinely new person should complete these tasks on a phone without verbal help. Record completion time, wrong turns and requests for help. Do not present this self-review score as a measured score from children or independent users.

Final validation: 160 tests passed, zero failures/errors. Build and lint succeeded. Home and guide renders inspected.

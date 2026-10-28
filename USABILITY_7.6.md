# AutoBash 7.6 usability review

## Method and limits

I reviewed the source and rendered Android views, walked through first-time tasks without relying on hidden controls, and added automated behavior checks. This is a developer heuristic walkthrough, not a genuinely independent person or child usability study. A prior high self-rating did not reflect the user's actual experience; it should not be treated as validation. The scores below are subjective and provisional.

## Iterations

1. Initial assessment: 6.5/10. Small recognition labels, three competing actions in one row, inconsistent dialog locations, setup buried among advanced controls, and preset-scoped export made essential tasks difficult to discover.
2. First redesign: 8.4/10. Larger controls, dedicated playback row, global backup and setup shortcuts improved the flow. Walkthrough still found repeated guide buttons, a large Home heading pushing setup down, long backup dialog labels, and low-contrast number selection.
3. Revised design: 9.1/10 provisional. Removed repeated help; shortened the heading; used separate backup choices; added explicit readable target count; standardized Auto Tap forms and guide placement. Final guide steps open a useful destination. This exceeds 9 on this internal rubric, but it does not establish that real users will score it above 9.

## Final rubric

| Area | Score / 10 | Evidence and remaining limitation |
|---|---:|---|
| Find the starting steps | 9.0 | Home exposes access, action permission, Practice and Popup setup; task cards explain purpose. Android permission screens remain outside the app's control. |
| Read and operate popups | 9.0 | Dedicated Pause/Stop row, separate Quick save, larger default labels, fixed status space; Auto Tap primary controls stay above scrolling details. Extreme user resizing/font settings still need device checks. |
| Understand and recover | 9.0 | Preview/paused states, Stop, practice and contextual guides. Recognition failures can still require understanding crop/accuracy. |
| Follow guidance | 9.0 | Step progress, Back/Next, one help entry per form, final destination links. No first-time human study performed. |
| Move a complete setup | 9.5 | Global 21-slot backup, validation, replacement warning, actions disabled after restore, interrupted-restore rollback test. OS file picker varies across phones. |

Mean: **9.1/10**, an internal assessment only.

## Walkthrough tasks and checks

- Find Allow actions, Android access, Practice and Popup setup from Home.
- Find whole-app backup and open the export file flow.
- Read recognition status; distinguish Pause, Stop and Quick save without crowded labels.
- Change Auto Tap target count; find exactly one guide button in the dialog.
- Follow a guide to its final step and open the relevant destination.
- Export/restore every action's three slots, retaining images, names, settings and Auto Tap layouts.
- Reject malformed backup entries without changing existing collections.
- Recover an interrupted restore, restore original preferences and leave actions off.
- Run the existing gesture, pause/resume, recovery, preset, crop, timing, overlay-size and recognition regression suite.

Visual inspection used Robolectric-rendered views at phone dimensions, including a 360 dp-wide Home screen and recognition controls. Existing tests cover short landscape Auto Tap controls and resizing. NumberPicker rendering in Robolectric does not perfectly reproduce the real device wheel; an explicit count label makes the selection readable independently.

## Release checks

168 automated Android/Robolectric tests; final results recorded in release evidence. Android lint and APK build also run. No physical Android device was connected. Actual game tap acceptance, OEM accessibility restrictions, TalkBack interaction, very large text and keyboard overlap are not claimed as verified.

Recommended next validation: give a new user the APK without explaining it; ask them to create two tap points, pause/resume, save an image and export/restore. Record completion and hesitation before revising this provisional score.

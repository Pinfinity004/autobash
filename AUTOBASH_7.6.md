# AutoBash 7.6 — clearer controls and whole-app backups

Install AutoBash_7.6.apk over the existing AutoBash installation. Do not uninstall first: uninstalling deletes local data. Android 11 or newer; ARM64 and x86_64. Package com.pratik.imagetap.v7, version code 26. This is a personally signed debug build, not a Play Store release.

## Floating controls

Recognition now has a dedicated Pause/Stop row and a separate full-width Quick save button. Default action labels are 15 sp with 52 dp buttons. Status and statistics have reserved space to avoid jumping as text changes. The navy/cream surface has consistent rounded edges and spacing.

Auto Tap keeps Play/Pause and Stop above secondary controls. Target visibility, count, speed and layouts use a consistent two-column layout. Secondary controls scroll on short displays. Existing independent simple/advanced popup size settings remain available.

Auto Tap forms and guides open in a consistent bottom position, with a width cap and keyboard resize. Duplicate help buttons were removed. Number selection includes an explicit readable count. Content transitions use a short 180 ms fade; dialogs and action buttons do not slide while being operated. Reduce motion remains available.

## Start here and help

Home brings Allow actions, Android access, Practice and Popup setup into the initial setup flow. Task cards explain repeated tapping, image-triggered actions and ordered timelines. Advanced session controls remain collapsible.

Guides show a step count and progress bar, with Next, Back, Close and All guides. The final step opens the relevant section; the introductory guide leads to Practice. Help stays available on demand instead of filling every screen with instructions.

## Export everything

Home → Back up or restore everything → Export everything to a file.
The collections/presets export button now also exports the entire setup.

A global backup includes all 7 actions × 3 preset slots, target images and options, names/order, active slots, screen regions, general timed actions, timelines, Auto Tap plan and saved layouts, and portable recognition/appearance settings. Existing single-preset files can still be imported using Import preset.

Restore everything replaces the whole setup after validation and confirmation. Export your existing setup first if you want to keep it. Running sessions stop when opening backup tools. Actions are OFF after restore, so review settings before running.

Android permissions, selected installed-app package, session checkpoints and live tap counters are intentionally not transferred. These belong to the receiving phone or a live session, not a portable configuration. Archive size is capped at 1 GiB overall, with existing per-preset limits. Interrupted restores use a persistent rollback journal. Previous unreferenced image directories may remain after restore; automatic destructive cleanup is intentionally avoided in this release.

## Validation

See USABILITY_7.6.md and tests/release-7.6-evidence for the review and automated checks. Device-specific overlays, keyboard behavior, Android permission screens and actual received taps still need a physical-phone check.

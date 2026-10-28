# AutoBash 7.4 — adjustable floating controls
Install over 7.3 without uninstalling to retain your presets and layouts.

Open Settings → Appearance → Floating control & effects.
- Simple popup size: 75–150%, in 5% steps, for the compact brain/Stop popup.
- Advanced popup size: 75–150%, in 5% steps, for the detailed console and Auto Tap popup.

Sizes are stored independently. Return to the session to apply. Auto Tap resizing preserves its current progress and target coordinates. Larger requested sizes are limited to fit the display. The app scales text, spacing, controls and actual window bounds together, keeping its no-tap area aligned. Drag the popup to reposition it after resizing if needed.

Validation: regression tests plus new checks for independent size preferences, scaled text/geometry, short-screen limits and window bounds. Automated checks do not replace testing on your phone.

Final automated result: 155 tests passed. Build and lint completed; no physical-device test performed.

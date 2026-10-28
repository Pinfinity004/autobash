# AutoBash 6.10 — interface refinement

A focused native Android UI release, retaining the cream/navy identity and green nerve animations.

## What changed

- Bundled Space Grotesk for expressive headings and Manrope for readable controls/body text. Both are open-source fonts; their SIL Open Font Licenses ship with the app. Fonts work offline.
- Corrected a shared styling bug that replaced authored button margins. Standardized 12 dp grid/card gaps and explicit toolbar spacing.
- Settings grouped into five collapsible sections: Recognition, Gestures, Detection rules, Appearance, and Session & permissions. Expansion state is remembered. Recognition summarizes pulse and similarity.
- Added a bottom navigation drawer through the header menu. Existing bottom navigation and brain menu remain available.
- Different section accents: warm gold for action collections, green for regions, periwinkle for timeline and cool blue for preferences. Color supplements labels rather than replacing them.
- More restrained active-label glow; greater distinction between headings, descriptions and primary actions.
- Action cards grow with their content. Header branding truncates rather than wrapping into controls. Bottom navigation grows with font scale.
- Timeline reorder controls gain descriptive accessibility labels; delete controls use the destructive-action color.
- Refined preset slot spacing, separated sharing section, crop toolbar spacing, and zoom accessibility labels.

## Research and rationale

Google's Material 3 Expressive work emphasizes grouping, containment, meaningful color and clear focal points. Its 2026 CHI research abstract reports faster task completion in a controlled study of 48 participants across ten applications. These results support the direction, but are not a measured speed improvement for AutoBash.

- https://design.google/library/expressive-material-design-google-research
- https://research.google/pubs/usability-hasnt-peaked-exploring-how-expressive-design-overcomes-the-usability-plateau/
- https://blog.google/products-and-platforms/platforms/android/material-3-expressive-android-wearos-launch/

Applied to AutoBash: distinct task groups, stronger typography, restrained accents and large controls. Avoided heavy animated transitions and background blur that could compete with an automation app's recognition workload. No new UI framework dependency or network permission.

## Compatibility

Package remains com.pratik.imagetap.v7; versionCode 18, versionName 6.10. Install over the existing signed AutoBash app without uninstalling to retain saved data. Recognition, frame freshness checks, dispatch behavior, image presets and timed-action storage remain unchanged. This release does not fix the previously discussed old-frame timing issue.

## Validation

Validation: 96 Android/Robolectric tests passed, with zero failures or skips. Android lint reported zero errors and 85 warnings. Added checks cover preserved layout margins, remembered settings expansion, drawer navigation and scalable action cards. See the release evidence directory for build, lint and test results. Native UI renders are generated with Robolectric, not screenshots from a physical phone. Device testing is still needed for actual font rendering, overlays, gesture delivery and Android-specific window insets.

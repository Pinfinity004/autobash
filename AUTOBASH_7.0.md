# AutoBash 7.0 — parallel recognition

## Enable and use

Install over your existing AutoBash app without uninstalling. Open Settings → Recognition → Fast performance mode. Off is Balanced (up to two workers); On is Fast (up to four). The phone's available CPU count, low-memory classification and app memory class can lower these limits. Fast may use more battery and generate more heat. Mode changes apply on the next full scan.

The requested pulse remains 0.1–10 seconds. Capture rate and processing time still determine the effective rate; scans never overlap. Start with a short pulse if you want frequent scans, and watch the detailed launcher's actual timing rather than assuming the slider is the achieved rate.

## Pipeline changes

- Checks independent target groups concurrently against the same immutable captured screenshot. Each worker owns its mutable frame state, native image data and prepared-template cache.
- Waits for all matching workers before choosing the highest eligible score. Completion order cannot choose the winning action.
- Uses bounded, persistent worker caches for decoded templates, scaled templates, masks and edges. Cache invalidation releases prepared entries before their source native images are evicted.
- Checks a recent target location first. A full-screen search is skipped only after retained RGB pixels match exactly, yielding the maximum score. A merely similar tracked match still competes with the broader search, so a stronger distant match is not hidden.
- Consolidates searches for byte-identical image files with identical scale and search-region settings. All original entries and action assignments remain intact. Different scales or regions remain separate.
- Similar-but-not-identical templates are deliberately not merged automatically: their verification details can differ. The existing user-reviewed duplicate tool remains available.
- Retains small-target full-resolution handling, masks, global exclusions, per-target search regions, strict 100% matching, two-frame confirmation and the 6.11 fresh-frame stall fix.
- Shows Capture, Prep and Search timings, target count, unique search count and actual worker count in the detailed floating control. Search includes worker-side preparation and candidate verification; the separate fresh-frame check reports its own elapsed time when needed.
- A worker error propagates only after all native tasks have joined; the source screenshot is not released while another worker is still reading it. Cancelled generations cannot dispatch actions.

No automatic region restriction is inferred from past locations. Existing user-defined regions continue to reduce search work without excluding new locations unexpectedly.

## Benchmark

Synthetic desktop OpenCV test, 720×1280 screenshot, 51 unique 48×32 templates. Seven measured rounds per configuration after warm-up, rotating measurement order. No Android capture, gesture delivery, phone thermal behavior or UI cost is included. The serial comparison uses one target-processing worker and two OpenCV threads, approximating the earlier serial configuration rather than benchmarking the old APK on a phone.

| Scenario | Serial | Balanced: 2 workers | Fast: 4 workers |
|---|---:|---:|---:|
| 51 visible targets, median | 384 ms | 224 ms | 115 ms |
| Only one target visible, median | 177 ms | 96 ms | 57 ms |

Balanced with all 51 exact targets at remembered locations: 12 ms median. This favorable repeat case does not represent a scene where most targets are absent. Actual speed depends on the phone, image sizes, masks, alternate-size searches and search regions; no sub-second phone guarantee is made.

Reproducible benchmark: tests/PerformanceBenchmark.java. Raw output is in tests/release-7.0-evidence/benchmark.log. It should be run without another build or benchmark competing for CPU.

## Validation

- 105 Android/Robolectric tests passed, zero failures or skips.
- Native regression suite passed, including serial/parallel result parity, full target coverage, repeated-scan cache reuse, duplicate result fan-out, independent regions, cancellation during a scan, worker-error recovery and an 8×8 target at full screenshot resolution.
- Existing tests passed for strongest-match selection, masked icons, exact pixels, fresh-frame verification, action dispatch gates, presets and UI controls.
- APK built and signed using the existing development certificate. No new network permission.
- Physical-device capture-to-action performance, thermal behavior and gesture delivery still need testing.

Version name 7.0, version code 20. Package com.pratik.imagetap.v7. Saved images, presets, regions, timed actions and timelines remain compatible.

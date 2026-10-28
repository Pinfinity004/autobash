# AutoBash 6.6 — faster recognition

Install `AutoBash_6.6.apk` over the current app without uninstalling. The package ID and signing key are unchanged, so this is an update that preserves saved targets, presets, and settings.

## What changed

- Every active target still sees the same captured screen frame. Targets are not rotated across separate screenshots.
- Fast candidate search is on by default. A smaller shared screen image proposes locations; the original detailed matcher verifies those areas at its previous working resolution. The candidate score alone never authorizes a tap.
- Template colors, grayscale, masks, silhouettes, and pixel arrays are prepared once and cached between frames. Prepared data has a 32 MiB / 1,024-entry budget, with a single oversized entry allowed. The original image cache retains its 48 MiB / 500-entry budget, likewise allowing one oversized image.
- Tiny symbols and thin retained strokes use the exhaustive path. Crowded high-scoring candidate maps also fall back to exhaustive search. These fallbacks protect detection but can make some collections slower.
- Similarity thresholds, exact retained-pixel checks at 100%, exclusion areas, target-specific regions, and two-frame confirmation remain in effect. The two-second stale-frame tap guard remains unchanged.
- The floating controls now keep the most recent complete scan time visible, including while the next scan runs. Its height remains fixed as the numbers change.
- Settings → Fast candidate search can be turned off for comparison or if an unusual target performs better with exhaustive matching.

## Phone test

1. Keep **Fast candidate search** on and **Two-frame confirmation** on.
2. Set **Scan pulse** to 500–1,000 ms. This is the minimum interval between scan starts; it cannot force a slow scan to finish sooner.
3. For the most direct comparison, leave **Search alternate sizes** off unless your targets need it. That option adds additional scale searches for unmatched targets.
4. Use the same 51 targets and scene as before. Read **All 51 checked · … ms** after a few scans, allowing the cache to warm up.
5. Compare with Fast candidate search switched off, keeping everything else the same. Test a moving scene as well as a still one, including thin skip/close symbols.

The displayed time runs from screenshot receipt through processing completion. It excludes screenshot acquisition latency, the configured idle interval, and Android gesture delivery. Two-frame confirmation requires another scan, so a complete scan time is not the same as tap-response time.

## Validation

The native matcher suite covers exact pixels, brightness tolerance, wrong colors/text, changing backgrounds, source-size calibration, alternate scales, tracking, still screens, 8×8 targets, regions/exclusions, and cancellation. Added hybrid tests cover 16 textured-background placements and 30 parity cases for masked crosses from 16–48 pixels, including one-pixel strokes. All 30 exhaustive hits were preserved in those parity cases.

The 51-target desktop benchmark uses generated targets and a 1080×2340 screen, two OpenCV threads, one warm-up followed by five measured scans. Normal targets use the prior 720-pixel working width; tiny targets use native resolution. Baseline is the unchanged 6.5 matcher included under `tests/benchmark-reference/`. The optimized benchmark retains its prepared cache between frames, as the app does. Scripts and results are included with the source.

| 51-target workload | AutoBash 6.5 | AutoBash 6.6 | Speedup |
| --- | ---: | ---: | ---: |
| Regular opaque templates | 1,036 ms | 362 ms | 2.86× |
| Tiny masked templates | 4,443 ms | 1,625 ms | 2.73× |

The last target was found on all six runs of both workloads with both engines. The accepted-target bitmasks also matched between old and new on these fixtures; this is a parity check, not proof of zero false positives in real-world ads.

These are desktop results, not measurements from your phone or your actual saved collection. The goal is 1–2 seconds for your 51 targets, but that is not yet verified on-device. Tiny/thin targets, many alternative sizes, large crops, thermal throttling, and dense repeated patterns can reduce the speedup. Candidate generation is heuristic and cannot guarantee identical recall for every possible screen; the exhaustive comparison option is retained for that reason.

Android build / lint / UI and tap tests: 41 tests passed, zero failures. Lint: 0 errors and 60 existing warnings. The signed APK was verified as package `com.pratik.imagetap.v7`, version code 14, version 6.6, with the same signing certificate as 6.5. No physical phone or Android emulator timing/gesture test was available.

## Reproduce

Use JDK 17, Android SDK 35, Gradle 8.11.1, and the project wrapper to run `assembleDebug lintDebug testDebugUnitTest`. The app uses OpenCV Android 4.12.0.

For native desktop tests, set `OPENCV_DESKTOP_JAR` to the OpenPnP OpenCV 4.9.0-0 JAR and run `sh tests/run-tests.sh`. Run `sh tests/run-benchmark.sh` for the old/new comparison. Desktop timing excludes Android UI/capture overhead and is not a substitute for device testing.

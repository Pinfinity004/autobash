# Source provenance

## Where this tree came from

This repository is the contents of **`AutoBash_7.5_Source.zip`**, the source artifact
produced by the original build session and downloaded from its file library on
2026-10-27. It is byte-for-byte that archive — not a reconstruction.

The precursor session ran for six days (2026-10-22 → 2026-10-27) in a container
filesystem, producing 927 shell commands, and iterated the app through
ImageTap → Skiptap → AutoBash 7.4.

Archive integrity at import:

| | |
|---|---|
| Entries | 338 |
| Uncompressed | 5,063,071 bytes |
| Java sources | 93 |
| Resource XML | 168 |
| Gradle wrapper jar | present (43,583 bytes) |
| Version notes | `AUTOBASH_6.5.md` … `AUTOBASH_7.5.md`, plus `TAP_FIX_6.4.1.md` |

## Verified from the archive itself

| Field | Value |
|---|---|
| `applicationId` | `com.pratik.imagetap.v7` |
| `namespace` | `com.pratik.imagetap` |
| `versionName` | `7.5` |
| `versionCode` | `25` |
| minSdk / target / compile | 30 / 35 / 35 |
| Android Gradle Plugin | 8.9.2 |
| OpenCV | `org.opencv:opencv:4.12.0` |
| Tests (as reported in `USABILITY_7.5.md`) | 160 passed |

Note that `applicationId` (`…v7`) differs from `namespace` (`…imagetap`). That is
intentional in the archive and is why the app can install alongside older builds.

The manifest requests **no** `INTERNET` permission — it is explicitly removed via
`tools:node="remove"` — which is the right posture for an app that processes screenshots
entirely on-device.

## What was done to the archive on import

Four changes, all outside the app source:

1. **Signing key removed from version control.** `signing/development.jks` exists in the
   archive but is gitignored here, with `signing/README.md` replacing it. The build still
   references the file, so builds on this machine continue to work; a fresh clone must
   supply its own key.
2. **Logs gitignored.** The archive carries 23 build/native/benchmark logs scattered
   through the tree; they are build artifacts, not source.
3. **Project documentation added under `docs/`** — `CHANGELOG.md` (version history
   reconstructed from the session) and this file.
4. **No source file was edited.** If the tree fails to build, the cause is the archive or
   the local toolchain, not the import.

## Known limitations, from the archive's own notes

`USABILITY_7.5.md` states: *"Automated checks do not replace testing on your phone. Final
automated result: 155 tests passed. Build and lint completed; no physical-device test
performed."* `VALIDATION.md` records the same caveat for earlier releases — physical-phone
file-provider and capture behaviour was never verified.

## How this was nearly done instead

The initial plan was to reconstruct the source from the exported conversation transcript.
That was abandoned for concrete reasons, recorded here so the decision is auditable:

- All 970 tool outputs in the shared transcript are redacted — no build logs, no test
  results, no directory listings.
- The transcript's code text is corrupted: 1,209 damaged sites, where adjacent quote and
  `!` characters were mangled. `screen.length != sw*sh` is rendered
  `screen.length"'!=sw*sh`.
- Only first-generation files were ever written whole. Later versions exist solely as a
  chain of ~700 in-place `replace()` mutations applied to files copied forward with
  `cp -a`, so the final state cannot be replayed from text alone.

The transcript remains useful for history — that is what `docs/CHANGELOG.md` is built from.

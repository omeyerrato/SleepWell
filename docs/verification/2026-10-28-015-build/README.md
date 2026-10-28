# 0.1.5: independent frozen package audit

Source **`ef69408258e900a50274d6f2488e859647138714`**, version **0.1.5 / code 6**. Independently read and hashed Root's frozen `SleepWell-ef69408258e900a50274d6f2488e859647138714.apk`: **163,200,933 bytes**, SHA-256 **`41447d6bce55abb20ce504d44dd21dfee949a88f68ee7e7633e160cebef4cdde`**. The app tree is `791ff991bd19eb2ff72d8933499ed30b106baf92`; the distinct app/src/main tree is `435a3f890533d2a4c4554ab77aa2b5dbdccfb682`. [Independent result](independent-candidate.json), [unaltered release-owner frozen record](owner-frozen-record.json).

SDK 36 `aapt` confirms `org.sleepwell.app`, minSDK 30, targetSDK 36 and **arm64-v8a only**. `apksigner verify --verbose --print-certs` independently verifies both this APK and frozen 0.1.4 with one v2 signer, certificate SHA-256 **`49eeeb7f74344335321800e3f78b9bb9ab25c5fff059332c458133dca0da0e1c`**. [Current badging](package-badging.txt), [current signer](package-signer.txt), [baseline badging](baseline-badging.txt), [baseline signer](baseline-signer.txt). Aapt's badging differs only in the package/version line. The complete compiled [current manifest](package-manifest-tree.txt) and [baseline manifest](baseline-manifest-tree.txt) differ only in versionCode 5→6 and versionName 0.1.4→0.1.5: **no permission, component, exported/access attribute, feature, launcher declaration or SDK drift**.

Root's assembleDebug log is [copied without alteration](build-original.txt), SHA-256 `14b67aeb4f9d36596d9ee9310f631d0b043bcc5c30566617c827bb266add0d26`. It prints **BUILD SUCCESSFUL in 3m 6s**, 34 tasks / 15 executed / 19 up to date. Root separately reported 188.375 seconds wall time. This auditor did not run or time a build; the source/package association is pinned to the owner's frozen record, not proved by source-to-DEX equivalence.

## Retention and actual ZIP delta

Frozen 0.1.4 baseline `08d3911e90af666d79a1f57fb0ea1326e91c3929` was independently rehashed: **167,067,516 bytes**, SHA-256 `3b6c698fbfc1d71a21fa2e9b5420b7cf08ad39632608648e226482d11709a024`, matching [its prior audit](../2026-10-28-014-final-build/README.md).

| Actual package entries | Comparison with frozen 0.1.4 |
|---|---|
| All **65 assets** | Same names and exact uncompressed bytes; no additions, removals or changes. Audio, models, fonts and licenses are retained. |
| All **8 native libraries** | Same names and exact uncompressed bytes, all under `lib/arm64-v8a/`. |
| Central-directory entries | **653 → 947**; 372 added, 78 removed, 64 changed and 511 content-identical. No duplicate entry names. |
| `res/` entries | **481 → 775**. No claim that resource counts or resource bytes are unchanged. |
| Added entries | **372 PNGs**: 365 for 73 glyph names at five densities, five notification-moon variants and two launcher rasters. Every added APK PNG's decoded RGBA pixels equal its corresponding source PNG. |
| Removed entries | **78 compiled XML entries**: 72 old Phosphor glyph XMLs plus six notification/launcher entries, including three anonymous inline-vector derivatives. |
| Changed entries | `AndroidManifest.xml`, `classes2.dex`, `classes3.dex`, `resources.arsc`, and 60 compiled resource XMLs. Other DEX and retained content compare equal. |

[All retained asset/library hashes](retained-assets-native.json) and [the complete ZIP-entry delta](zip-entry-delta.json) are computed from actual uncompressed entry bytes. ZIP reads verify CRCs; original APK signature verification passes. No APK was repacked or modified.

The 60 changed resource XMLs were individually dumped through `aapt xmltree`. Only application numeric resource IDs were replaced with their independently dumped resource names for each package; no other normalization was applied. **55 become exactly equal**, including the existing dependency/widget layouts. The five remaining semantic changes are the monochrome wrapper and four adaptive launcher XMLs, recorded in [the resolved XML diff](owned-resource-xml.patch). The launcher foreground becomes transparent while its generated background carries the artwork; monochrome uses the generated raster inset. [Per-entry raw/resolved hashes and all 372 PNG pixel checks](resource-resolution.json). These are package-structure checks, not launcher-mask or in-app rendering acceptance.

The final APK is **3,866,583 bytes smaller**, despite **1,038,829 more compressed payload bytes**. [Physical ZIP accounting](package-layout.json) reconciles both file sizes: frozen 0.1.4 has a 4,967,956-byte gap before signing that is absent from 0.1.5; referenced local-header/name/extra bytes increase by 32,228 and central-directory/end bytes by 30,316. Both signing blocks are 4,096 bytes. No retained asset/library loss is inferred from the smaller total.

## Source boundaries

[Source inventory](source-inventory.json) records each exact resource path/status and named blob hashes. The baseline contains **72** Phosphor vector glyphs; the additional Headphones glyph makes **73 raster names / 365 density PNGs** in 0.1.5. The other intended changes are notification/launcher resources and four period-timeline action IDs compiled into `resources.arsc`. Source and compiled resource counts are distinguished because AAPT creates anonymous inline-vector entries and resolves API variants.

The complete source `AndroidManifest.xml`, `Store.java` and `Backup.java` are **byte-identical** to frozen 0.1.4. SQLite remains **schema 17** and the records/alarms portable backup remains **format 14**. Source app assets are unchanged. Build dependencies and build configuration differ only in versionCode/versionName. No app-owned resource XML still contains a `<vector>` tag; unchanged dependency resources may retain their existing vector implementations. No data migration, import/export or user-data action was executed by this audit.

## Limits

This archive performs **no rebuild, test, lint, CI dispatch, native rerun, device/ADB operation, output, install, release or latest-metadata update**. It changes only this audit folder in an isolated branch. It does not prove source-to-DEX equivalence, new runtime behavior, all tint/state/accessibility paths, launcher masks, all-device rendering, all 36 reference screens, OS permissions/providers or real alarm/tracking/audio/haptic/light/overnight behavior. Earlier Contactless/Report/Tonight/Automatic screenshots predate this raster migration; the current 18-frame native raster check is separately owned and is not claimed by this package audit. Publication and any phone update remain Root-owned. The copied owner record describes its frozen pre-publication checkpoint, not a continuing distribution-status assertion.

`SHA256SUMS.txt` hashes every file in this compact archive except itself. All raw tool outputs, the owner record and the original build transcript retain their original bytes.

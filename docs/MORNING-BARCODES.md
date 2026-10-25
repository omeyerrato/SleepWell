# Offline barcode morning codes

The existing morning-code task now has an explicit **QR code / Barcode** enrollment mode. Barcode mode requires selecting exactly one of **EAN-13, EAN-8, UPC-A, UPC-E, Code 128 or Code 39**. This deliberately bounded set covers common printed retail and user-generated one-dimensional codes using the already pinned Apache-2.0 ZXing core and CameraX dependencies. There is no product-catalogue lookup, web request, URL launch, camera file or new dependency.

## Exact saved identity and compatibility

QR enrollment remains the same three portable JSON fields (`label`, `hash`, `fallback`) and SHA-256 of the exact UTF-8 text, byte for byte. Old enrollments, backup formats and reusable defaults remain valid. No absent field silently changes an old QR reader into a barcode reader.

Barcode enrollment adds one strict `format` field. The saved hash is SHA-256 of a versioned domain prefix, that selected format and the validated normalized payload. Raw text/product numbers and images are never persisted. Leading zeros, case and meaningful spaces are retained; text is neither trimmed nor converted to another code kind. EAN/UPC values require the complete expected ASCII digit count and a valid check digit. UPC-E number systems 0/1 are supported and checked via the pinned library's UPC-E expansion. Code 128 accepts 1–80 printable ASCII characters. Standard Code 39 accepts its uppercase alphabet and punctuation; extended full-ASCII Code 39, GS1/FNC control content and other formats are outside this increment.

Unknown format names, nontext format fields and extra portable fields are rejected before restore replacement. Explicit QR format metadata is not introduced: QR remains canonical legacy three-field JSON. Removing a format from a barcode document produces a distinct legacy-QR identity; an ordinary scan of that barcode cannot supply the QR hash. A hash is an identifier, not encryption or proof that a photographed code is inaccessible.

The single legacy routine category remains `QR code` for compatibility; its editor explicitly offers QR or barcode and its task detail shows the selected barcode format. Both contribute one routine unit. Scanner request/proof, saved-state recreation, current active task and returned result now check the format as well as occurrence, unit, nonce and fingerprint. Changing the enrolled type invalidates stale callbacks even if a portable file retains the same 64-character hash.

UPC-A and an EAN-13 symbol with a leading zero share the same physical bar pattern. The explicitly selected ZXing reader applies that standard representation (12 UPC-A digits or 13 EAN digits), and SleepWell fingerprints that selected representation separately. The app cannot distinguish two physically identical symbols by a printed human-readable label; it does not guess a product identity.

## Actual camera behavior

The selected reader is fixed for the camera session. Changing enrollment mode/format first closes its owned session and clears captured proof; it does not relabel an old capture. Selecting the already active Barcode tab preserves its selected format. Label and Math fallback remain in the draft. Cancellation leaves saved alarms unchanged.

Frame admission remains at most 4 MiB and at most five camera frames per second. One-dimensional scanning adds bounded rotated/inverted decoding with one reused transform buffer; four decode attempts cover normal/inverted and quarter-turn orientations. Row reversal handles the other half turns. Unsupported kinds are not silently searched. Camera generation fences, permission denial/settings/retry, camera unavailable/in-use states and ownership cleanup reuse the existing implementation. No camera access is requested while ringing: the explicit existing Math alternative remains available.

## Verification contract

`MorningBarcodeTest` adds six pure cases: actual generated decoder fixtures for all six formats and rejection by the QR-only reader; quarter turns/inversion; selected-format and domain separation; exact text/leading-zero preservation; bounds/checksum/encoding rejection; and typed proof result/occurrence/unit/format/fallback fences. The symbols are generated from original test strings using the existing licensed writer, not downloaded assets.

`BarcodeCodeStorageTest` adds three native methods/0 PNG: legacy QR three-field round-trip; strict typed metadata/equality; and actual private alarm/defaults/backup restore/reopen with no raw payload and no mutation on invalid import.

`BarcodeCodeFlowTest` adds one actual native configuration method with fake access/camera ports and a real generated-and-decoded Code 128 proof. It exercises explicit type/format choice, permission denial, cancel without persistence, scan, clearing proof on mode change, stale callback rejection, recreation, routine save, exact reopen and unavailable-camera state. It never starts CameraX, `AlarmActivity`, sound, tracking or hardware. Its five original screenshot names are:

- `barcode-code-mode.png`
- `barcode-code-captured.png`
- `barcode-code-recreated.png`
- `barcode-code-routine.png`
- `barcode-code-reopened.png`

Existing QR configuration and decoder/proof/session tests remain applicable. Compilation and generated/fake fixtures do not certify physical camera focus, glare, print quality, low-light/OEM performance or a ringing transition.

Primary codec references: [ZXing UPC-E reader and canonical expansion](https://github.com/zxing/zxing/blob/zxing-3.5.3/core/src/main/java/com/google/zxing/oned/UPCEReader.java), [ZXing Code 128 writer](https://github.com/zxing/zxing/blob/zxing-3.5.3/core/src/main/java/com/google/zxing/oned/Code128Writer.java).

Author verification on 2026-10-25: direct JDK/JUnit compilation and all 6 new pure tests passed in 0.504 sec, including actual generated pixel decoding for all six formats and orientation/inversion. Per root coordination this source increment is committed before Android/Gradle compilation so it can join the next combined candidate. The four new native methods and Android Activity/CameraX edits are uncompiled and unexecuted at this handoff. No physical camera, phone, alarm/output or external request was tested.

CI 40156141382 subsequently passed all three storage methods, but the first UI run stopped after `barcode-code-mode.png`: its immediate global scroll fallback threw because a compact native picker had no scrollable accessibility node. The fixture now waits for the requested target in the focused owned app window, scrolls that actual owner only if clipped, requires a fully visible 48 dp enabled clickable area and a committed frame, and uses a real device-coordinate click. Text entry explicitly closes only a visible keyboard. All enrollment, decoding, persistence and cancellation assertions remain; a failure now captures PNG/XML and names the pending action. This narrow fixture correction has not yet been Android-compiled or rerun at author handoff.

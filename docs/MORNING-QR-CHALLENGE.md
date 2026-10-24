# Offline morning QR code

Board04 enrollment is a real private CameraX preview and offline ZXing QR decoder.
The location label and SHA-256 digest of the exact UTF-8 QR text (maximum 2048 bytes)
are saved per routine task; raw text/images are never persisted, opened as URLs, or
sent over a network. A hash is a matching identifier, not secret encryption.

Enrollment requests camera access only after Allow camera. Denial, unavailable
hardware, camera-in-use/start errors and Android settings launch failures have
visible retry states. CameraX closes every ImageProxy, limits analysis to 5 frames/s,
packs padded Y planes, and unbinds owned use cases on pause. Scanner generation
fences discard frames after pause/retry/ownership loss. The blank preview state is
explicit; no staged bathroom photograph is presented as a live camera.

The QR step contributes exactly one ordered routine unit. At ringing time camera
permission is never requested: missing access uses the saved Math fallback. A wrong
code remains unaccepted and offers retry or the same fallback. The fallback is one
question with saved complexity, operators and answer mode. Scanner result nonces,
occurrence, unit, enrolled hash and live occurrence are revalidated before advancing
one unit. Stale/duplicate callbacks cannot skip the next task. This is deterrence,
not guaranteed protection against force-stop, hardware shutdown or OS intervention.

Scalar legacy QR strings keep their previous Math fallback; actual QR tasks require
explicit enrolled metadata. Backup decode rejects invalid hashes, labels, arithmetic
settings, missing code and QR rounds other than one before replacement. Malformed
legacy routine JSON remains retained by existing Alarm behavior. No schema changes.

## Validation

Pure tests decode actual generated QR images including UTF-8, all rotations and
inversion; exercise crop/row/pixel stride and bounds; reject invalid enrollment;
exercise camera release/retry/current-owner loss and occurrence/unit proof fences.
QrCodeConfigurationTest has one inert UI method and twelve planned PNGs:
permission, invalid, denied, access-error, scanning, camera-error, captured, fallback,
recreated, routine, reopened, unavailable (diagnostics are additional). It injects
camera/access ports, uses a real generated-and-decoded QR hash, and verifies draft
Cancel/recreate/save/reopen, private backup strictness, other alarm/global preference
preservation. It never launches AlarmActivity or starts any physical camera/output.
The full local debug/test-APK/pure/lint build passed in 7m16 and the final
incremental validation in 1m04: 401 pure tests / zero failures (14 new), lint zero
errors / 86 warnings (six added pinned-dependency version notices). Generated
UTF-8/rotation/inversion decoder fixtures all passed.
Compilation is not native test execution. Actual CameraX device compatibility,
low-light focus and protected ringing transition require later explicitly authorized
hardware validation and are not marked verified.

## Primary references

- https://developer.android.com/media/camera/camerax/analyze
- https://developer.android.com/media/camera/camerax/architecture
- https://developer.android.com/jetpack/androidx/releases/camera (pinned 1.5.3)
- https://developer.android.com/jetpack/androidx/releases/activity (pinned 1.11.0)
- https://github.com/zxing/zxing/tree/zxing-3.5.3/core
- https://sleep.urbandroid.org/docs/alarms/captcha.html

The installed CameraX skill supplies lifecycle/frame-cleanup guidance. Its ML Kit
Analyzer preference is not used because this flow explicitly needs a pinned offline
ZXing-only decoder; the same single-frame ownership and lifecycle cleanup apply.

Exact-head native configuration: QrCodeConfigurationTest1 passes24.619s at
a8362a1/CI40047180910 with12 individually reviewed original images. Actual
generated QR decoding, fake camera/access transitions and real private JSON/
backup/recreate/save/reopen boundaries pass. Supporting compositions are usable;
full board04 fidelity and true CameraX/ringing transition remain unverified.
Fallback operator switches also differ from the compact Math operator grid.
No real camera, AlarmActivity, microphone or output was executed.

Operator-control consistency follow-up14c4d5a replaces the four large fallback
switches with the same accessible compact cards as a Math task. Both routes
share MathOperatorGrid, preserving checked semantics and the56dp minimum target.
The native QR journey additionally rejects removal of its last operator,
retains the selected value through recreation/save/reopen, checks all card
edges stay inside the rows and requires a fallback-error original. This is a
13-frame source candidate; the current frozen9188e78 run still has12 frames.
Scoped debug/native APK and lint pass1m23s. Fresh UI/visual proof is pending;
no actual camera, alarm or output was run.

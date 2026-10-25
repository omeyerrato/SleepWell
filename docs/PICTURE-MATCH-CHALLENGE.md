# Picture match challenge (bounded F028 subfeature)

**Picture match** is an original offline visual-similarity task, not semantic object recognition, face recognition, identity verification or liveness detection. The user explicitly captures a detailed reference, names it, then points the visible camera at a similar view during the task. Three fresh stable matching descriptors complete exactly one routine unit. A printed picture or screen showing a similar image can match. Different lighting, composition, viewpoint, rotation, blur or limited texture can prevent a match; reframe or use Math.

## Real enrollment and matching

Alarm editor → Challenges → Configure Picture match opens private `PictureMatchActivity`. Camera access starts only from the explicit camera action. Runtime permission, permanent-denial settings, no-camera/error, retry, pause, freshness/quality failure, empty reference and cancel states remain visible. The user must capture a fresh detailed frame before saving a new reference. Editing settings or returning from a preview cannot enable or schedule an alarm; only the existing Save routine / Save alarm flow writes it.

The user-selected label and compact signature survive recreation, routine editing, reusable defaults and portable backup/reopen. There is no captured-photo thumbnail because no photograph is stored. Signatures still reveal coarse visual characteristics and are not encryption; they are included in a user-exported backup/default template with the task. A changed reference invalidates an old challenge proof. Ordinary existing tasks/QR/barcode/voice payloads remain unchanged; Picture match has one explicit typed reference and one unit. Missing/corrupt metadata cannot silently turn into a completed camera task.

After enrollment, **Try picture match** runs the same match/hold component in a silent private preview. It does not launch `AlarmActivity`, schedule anything or emit sound. While ringing, **Open picture camera** opens a private visible camera screen, and **Use Math instead / Use Math fallback** select an actual Math task for the current occurrence/unit. They do not directly dismiss the alarm.

## Fixed original pixel policy

The policy below was declared before evaluating the generated fixtures. It uses Java and the existing Apache-2.0 CameraX dependencies; no model, AAR or new dependency is added.

- Center-square luminance crop, oriented using CameraX rotation metadata, area-averaged to 32×32 grayscale.
- 8×8 low-frequency DCT perceptual hash: discard DC; threshold the 63 AC coefficients at their median. This is a local similarity estimate, not an object label or calibrated probability.
- 16-bin luminance histogram summing to 1,024 and 8×8 coarse neighboring-gradient means.
- Admission quality: mean brightness 35–230, standard deviation at least 18 and mean neighboring gradient at least 10.
- All match checks must pass: hash Hamming distance at most 8, histogram total-variation distance at most 0.25, average coarse-edge difference at most 18 and mean-brightness difference at most 25.
- At least three accepted frames spanning 400 ms; no gap over 400 ms. A miss, unavailable frame, duplicate timestamp, stale/future timestamp or pause resets the hold. Descriptors must match both the enrolled reference and the preceding accepted descriptor.

`PictureSignature` rejects incomplete/unbounded input, unsupported rotations and low-detail frames. Inputs are at least 64×64 and at most 4 MiB. CameraX uses a square ViewPort shared by Preview/ImageAnalysis, a preferred 640×480 analysis resolution, KEEP_ONLY_LATEST backpressure, at most five admitted analyses per second and one worker. Only live in-memory luminance is processed; copied raw pixels are cleared after descriptor creation. There is no image file, network request, image URI grant, torch or microphone use.

Camera timestamps must map to a supported elapsed/monotonic clock within 800 ms; otherwise that frame cannot count. Callbacks are generation-fenced. All match attempts stop after 60 seconds unless the user explicitly retries. Pause/recreation releases only the owned Preview/ImageAnalysis bindings, resets live proof, and requires starting the camera again. The adapter never calls `unbindAll` or edits QR camera use cases. Camera-open failures do not count as a successful reference/match.

The private result checks run, unit, launch nonce, reference key, active task and a fresh elapsed capture time; a proof is consumed once. The existing routine completion persistence owns the completed unit. No raw signature callback can directly dismiss an alarm. Closed-session/old-generation callbacks and changed occurrence/reference state cannot complete a later unit.

## Portable format and tests

The new strict v1 reference contains exactly eight fields: version, label, 16-character lowercase perceptual hash, mean, contrast, gradient, 16 histogram integers and 64 edge integers. Values are bounded, histogram totals validated and descriptor arrays copied. Unknown fields, wrong types, invalid dimensions/quality, multiple rounds or a reference on another task type are rejected. A legacy alarm lacking Picture match metadata serializes as before. No SQLite migration is needed.

Author direct JDK/JUnit verification passed **10 pure methods** in 0.299 sec:

- `PictureSignatureTest` (5): actual original deterministic pixels, exact/+8 brightness/small-noise positives, different and quarter-turn negatives, blank/dark/corrupt rejection, rotation metadata, descriptor-copy/type bounds.
- `PictureHoldTest` (5): actual three-frame hold/span, reset/duplicate/stale/future/gap cases, camera timestamp mapping, current request/reference/unit freshness/one-use fences and an owned Math fallback.

The source candidate also adds native fixtures, **not yet compiled/executed at author handoff**:

- `PictureNativeTest`: 3 methods / 0 PNG. Actual pixel processing on Android is separate from fake camera UI; strict reference/routine metadata; real private SQLite/default template/backup validation/restore/reopen without images.
- `PictureMatchFlowTest`: 1 method / 5 PNG. Real native input and private save/reopen, fake access/camera ports, actual generated signatures; permission denial, cancel without persistence, low-quality/stale frame rejection, explicit capture, recreation, silent three-frame match, old-camera callback rejection, cleanup and unavailable-camera reopen. The test forbids actual challenge/AlarmActivity launches.

Frames are `picture-reference-invalid.png`, `picture-reference-captured.png`, `picture-reference-recreated.png`, `picture-reference-matched.png` and `picture-reference-reopened.png`. Failure PNG/XML preserve the pending UI action. Fake camera fixtures do not certify physical camera framing, OEM timestamp sources, focus, low-light performance, texture discrimination, ringing transitions or real device accuracy. No phone, camera hardware, alarm/output, microphone or external service was tested.

Implementation follows the installed CameraX, intent-security and permissions guidance narrowly: visible consent, runtime permission recheck, owned lifecycle/use cases, bounded off-main analysis and private explicit component/results. Original DCT/histogram/gradient mathematics avoids a model licensing/telemetry prerequisite. Broader semantic object-recognition and Smile remain separate missing features.

## Native cancellation observation correction

CI 40166582977 passed all three `PictureNativeTest` methods. The enrollment flow stopped after the explicit Back action: its failure PNG/XML shows the correct Challenges routine page at the retained scroll position, with Configure Picture match and Save routine visible. The Challenges header is above the viewport, so the accessibility-only title lookup did not observe it. The fixture now requests the actual owned text view into view, waits for complete visibility and a committed frame, then retains the original exact accessibility-text assertion. Action clicks, permission callback counts, cancel-without-persistence assertions and all later capture/recreation/match/save/reopen checks remain unchanged. This scoped correction has not yet been compiled or rerun on Android; the remaining four expected success frames are still unverified.

# Graded arithmetic and typed answers (F021 / F107 / F109)

Math now has independent per-task complexity, operators, answer method and rounds. Existing scalar alarms and routine arrays without arithmetic metadata keep Medium/addition/four choices; their existing difficulty number still means rounds. No schema change, migration or default rewrite is involved. The optional `math` object is omitted when settings equal the old default, preserving existing valid routine JSON.

## Actual behavior

Easy addition/subtraction uses operands 2–9; Medium uses the original 2–19; Hard uses 20–99. Multiplication factors and division divisor/quotient ranges are 2–5, 2–12 and 10–19 respectively. Division is generated from a product and a nonzero factor, so its answer is always a whole number. Subtraction orders the operands to avoid negative answers. Any nonempty subset of addition, subtraction, multiplication and division is supported. “Hard” means these documented larger operands, not a clinical or guaranteed wakefulness claim. There are two operands per question; no powers, decimals or multi-step equations are claimed.

Choices supplies four unique nonnegative nearby options with exactly one correct answer. Typed mode shows the same generated question without choice hints and validates the entire trimmed integer string, including zero answers. Expressions, decimal/prefix matches, signs, empty text and oversized input are not accepted. Leading zeros are allowed. The runtime shows an owned themed numeric entry and an explicit Check answer button; keyboard Done invokes the same check. Completion still enters the existing occurrence/unit-fenced routine advance, never dismisses subsequent tasks early, and preserves the configured number of rounds.

The persisted question includes its configuration, operator, operands and stable choice order. Runtime restoration reuses only a structurally valid question matching the current task's settings. Corrupt or incompatible cached questions regenerate, and old six-field addition caches remain supported. Typed input survives Activity recreation with a key containing occurrence, task unit and exact question, so an earlier task's text cannot fill a new question. The original five seeded addition outputs were captured from unmodified `bb582cf` and are frozen as compatibility fixtures. Distractor generation also has a bound, preventing a pathological constant random source from looping forever.

## Configuration and data preservation

Open Alarm editor → Challenges → an existing Math task or Configure Math. The supporting Math screen follows board 04's cream/Inter/brown grouped controls: Easy/Medium/Hard selected segments, explicit independently checked operator tiles, Choices/Type answer segments, the existing owned rounds picker and a fixed Use Math footer. A generated example comes from the actual arithmetic generator and is read-only; it is not a running alarm or interactive challenge preview. Empty operator selection keeps the last operator enabled and shows a visible footer error. Back cancels task edits, Use Math updates the routine draft, Save routine updates the alarm draft, and only Save alarm writes data.

New nondefault routine metadata is `math: {level: "easy"|"medium"|"hard", operators: ["add"|"subtract"|"multiply"|"divide", ...], answer: "choices"|"typed"}`. It requires all three fields, unique known operators, a nonempty set and exact string values. Null/coerced values, unknown fields, unsupported modes and arithmetic settings on a different task type are rejected. Alarm.read preserves the raw invalid routine and marks it invalid; existing Backup validation rejects it before replacing storage. Other task types, order, targets, per-task rounds, alarms, history and preferences remain intact. Sensor/NFC fallback Math keeps the old Medium/addition/choices behavior.

## Verification and limits

Ten new `GradedMathTest` cases cover frozen legacy outputs, all grades/operators/modes, 9,600 generated valid questions, every enabled operator mask, exact typed validation, validated stable cache roundtrips, incompatible settings, tampering, constant-random termination and independent routine rounds/reorder. Together with the existing two MathTaskTest and ten ChallengeRoutineTest cases, direct javac/JUnit passed 22 tests in 0.884 seconds before Android compilation.

`org.sleepwell.app.GradedMathConfigurationTest` has one emulator-CI-only method: `perTaskGradeOperatorsTypedModeCancelRecreatePersistAndBadImportsStayInert`. It uses a private SQLite database and injected disabled-alarm scheduler. A lifecycle guard rejects any Activity other than AlarmEditorActivity. It exercises actual controls, cancelling an initial grade edit, a distinct second Math task, operators, rounds, typed mode, screen and open-picker recreation, all draft/save boundaries, Save/reopen, last-operator error and strict malformed Backup.restoreData calls with database equality afterward. It checks original global preferences and another alarm are unchanged; no alarm, challenge, sensor, microphone, permission dialog, audio, vibration, flashlight or power action is invoked.

Expected seven native screenshots: `math-config-default`, `math-config-operators`, `math-config-typed`, `math-config-recreated`, `math-config-ordered`, `math-config-reopened`, `math-config-error`. Native execution and strict original-image comparison remain pending until a fresh CI run. Ringing-window rendering, keyboard behavior during a live alarm and physical wakefulness remain untested under the user's prohibition on alarm tests. Compilation alone must not be reported as runtime verification.

Integration is already within the existing task route and runtime `AlarmActivity.math`: `ChallengeRoutine.Task.math` → immutable MathSettings → MathTask generation/validation. No Main/CI/matrix edits or new navigation hook are required. The integrating agent should add the named native class and seven expected frames to its CI gate.

[Sleep as Android CAPTCHA documentation](https://sleep.urbandroid.org/docs/alarms/captcha.html) distinguishes simple multiple-choice Math and typed Math, and describes larger numbers at higher difficulty. This feature is a separate native implementation with explicit ranges; it does not claim all CAPTCHA types or full app parity.

Local Android verification: full Gradle testDebugUnitTest (378 tests, zero failures/errors/skips), debug APK, Android-test APK and lint passed in 2 minutes 43 seconds with max-workers=1/build cache. Lint reports zero errors and 80 warnings, including one additional literal-text translation warning from the typed-answer feedback. Native configuration methods are compiled only; the seven original screenshots and ringing behavior remain unverified.

Regional typed-answer correction: validation now consumes Unicode decimal-digit code points (Nd), normalizing Arabic-Indic, Eastern Arabic, Devanagari, Thai, fullwidth and other decimal scripts to a single integer before exact-answer comparison. Mixed decimal scripts and leading zeros remain valid. Signs, decimal separators, fractions, Roman/circled/superscript numeric glyphs, malformed surrogate input and values exceeding the five-digit input bound are rejected. The parser also guards integer overflow before accumulation. Two additional pure regressions cover those cases; question generation, private cache format and the native seven-frame configuration gate are unchanged. No ringing window or keyboard was opened.

The regional-digit follow-up passed direct javac/JUnit: twelve GradedMath, two existing MathTask and ten existing ChallengeRoutine tests, 24 total with zero failures (0.430 seconds). No local Gradle or native execution was run while the primary integrated build held the slot.

Latest native configuration evidence: GradedMathConfigurationTest1 passes
22.360s at12c8c0f/CI40042183746 with all7 original frames and retained
cancel/recreation/private-DB/save/reopen/invalid-import invariants. These
settings assertions do not execute AlarmActivity. Strict review finds clipped
upper operator-card borders in six detail frames; later top-alignment and
geometry assertions need fresh native captures before visual acceptance.

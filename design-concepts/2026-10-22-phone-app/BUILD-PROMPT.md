# SleepWell — implementation brief for ChatGPT / Codex

Build SleepWell as a real, open-source Android sleep and alarm app. Work in the existing SleepWell repository; the project discussed here is https://github.com/omeyerrato/SleepWell. Inspect its current code, build instructions and implementation status first. Extend working behavior rather than discarding it to produce a new demo.

## What the attached design bundle contains

- Twelve numbered PNG boards containing thirty-six intended phone screens.
- gallery.html for visual browsing.
- SCREEN-MAP.md for screen names, routes and documentation sources.
- PROMPTS.md for how the visual studies were generated.
- manifest.json for the image and screen inventory.

Extract the bundle and visually inspect every board. Reading filenames or prompt text is not a substitute for viewing the images. If you cannot access or inspect the images, say exactly what is missing before inventing a new appearance.

## Product and scope

Help people stop scrolling at night, track their sleep, and get out of bed using reliable alarms and challenges. All SleepWell features are free, without feature paywalls, subscriptions or advertising.

**Wearables are excluded for now.** Do not build watch companions, fitness-band integrations, Bluetooth oximeter support, wearable settings, or heart-rate/oxygen screens. Do not invent these measurements from a phone. Optional smart lights and sleep-record exchange are separate from wearable support and can remain in scope.

Use https://sleep.urbandroid.org/docs/ as the functional reference for the phone-capable features. Independently implement equivalent behavior with original code and original or appropriately licensed assets. The manual is a feature inventory; current official Android documentation is the technical authority for permissions, background restrictions, scheduling and system APIs.

Do not copy vendor APK code, proprietary sound packs, artwork, brand identity, research algorithms or hosted services. A free SleepWell feature does not make an external provider's paid service free.

The thirty-six screens cover main flows, not every permission, validation, empty, error or advanced setting state. Add required states using the same design system. Do not drop a required feature simply because it lacks a dedicated mockup.

## Visual contract

The supplied SleepWell designs are the visual reference. Do not substitute a generic Material dashboard or a new design direction.

- Changing light is the base: deep plum at night, muted violet before sunrise, warm amber for dawn, and cream with warm brown ink for daytime information.
- Use the neutral Inter-style typography direction. Bundle a properly licensed font and use a consistent type scale, weights and tabular time numerals. The raster references approximate type; implement actual text, not pictures of text.
- Use the Uber-inspired grouped floating dock on root destinations: Tonight, Report, Settings. Its selected capsule surrounds the icon and label together. Match the supplied proportions, icon weight and spacing.
- Keep detail screens on back navigation; keep active tracking and wake-up flows focused on their actions.
- Match composition, spacing, surfaces, control shapes, charts, selection states, hierarchy and small purposeful illustrations.
- Use consistent vector icons. Avoid emoji icons, oversized marketing headers, generic slogans, repeated rounded cards, random pills, neon edges and decorative scores.
- Build real native Android screens and controls. Never use a flattened phone mockup as the application UI, place a full screenshot behind invisible controls, or deliver a website/WebView substitute.
- Draw charts from actual records with readable labels, gaps for missing signals and appropriate estimate disclosures. Generated chart coordinates are illustrative, not a dataset.
- Create production illustration assets separately from the screen mockups. Keep controls, labels and data outside illustrations.
- Support Samsung Galaxy S23 Ultra portrait safe areas, Android back behavior, at least48dp touch targets, readable contrast, font scaling, accessibility semantics and reduced motion.
- Implement purposeful touch feedback and restrained time-of-day transitions. Honor reduced-motion settings. Keep nighttime brightness subdued.
- Keep content and the primary action clear of floating navigation and the Android system navigation area.

If a mockup has a concrete usability or technical problem, identify it and make the smallest consistent correction. Do not silently redesign a whole screen.

## Required feature inventory

First turn the manual, this brief, existing docs/FEATURES.md if present, and the screen map into a persistent feature matrix. Include subfeatures and additional phone-capable manual features, not just the headings below.

1. **Alarms:** multiple alarms; weekday and supported repeat rules; per-alarm settings and reusable defaults; skip/reschedule occurrence; labels; local-time, time-zone and daylight-saving behavior; reboot rescheduling; appropriate exact-alarm and full-screen handling.
2. **Wake behavior:** smart movement-based wake windows with a fixed latest alarm deadline; goal-aware early waking; gentle volume ramp; independent vibration/sound timing; local tones and chosen audio; screen sunrise; bounded snoozing; escalation/backup; post-dismiss wake-up checks.
3. **Challenges:** configurable difficulty, counts and sequences; math, memory, retype, motion/walking, QR/barcode and NFC enrollment and verification; other applicable phone-only challenge categories discovered in the manual. Include previews, fallback behavior and accessibility alternatives.
4. **Bedtime:** goals and reminders; wind-down schedule; voluntary app blocking with user-selected apps and a temporary pause; useful sleep-goal/bank progress. Emergency calling and legitimate system controls must remain available. Do not claim an ordinary app can defeat hardware shutdown or all force-stops.
5. **Sleep tracking:** manual start/pause/resume/stop; automatic start modes and reviewable missed-night estimates; phone accelerometer; ambient-light readings; contactless sonar with real calibration and signal processing where supported; placement help; battery-aware behavior; clear missing-sensor handling.
6. **Sleep analysis:** movement/awake estimates and honestly labeled stage estimates; actual start/end times and elapsed duration; no invented clinical certainty. An estimated record without active sensors must not receive fabricated movement or audio graphs.
7. **Audio:** actual microphone analysis; triggered clips; event library/playback/filtering/favorites; retention and export; properly licensed on-device recognition for relevant sound classes; optional previewable snoring cues; noise values must not be called calibrated dB without calibration.
8. **Sleep sounds:** an original or licensed ambient library, real playback/mixing, timers and fade-out, local audio sources and supported optional external sources.
9. **History and insights:** history/search, naps and multiple sessions, trends, regularity, chronotype when enough data exists, social jetlag, goal gaps, tags/notes/dream entries, graph edits, awake-period edits and manual record creation.
10. **Other applicable phone flows:** nap/power-nap scheduling, supported multi-nap routines, phone-supported dream/light cues and travel-related settings. Research feasibility and accuracy rather than treating a toggle as a finished algorithm.
11. **Data and services:** local-first storage; backup/restore and CSV/JSON/audio exports with clear contents; optional user-controlled cloud storage; sleep-record Health Connect exchange; calendar rules; supported smart lights; explicit Tasker/Intent/webhook/MQTT event integrations.
12. **Reliability and appearance:** readiness checks, optional permissions with explanations, battery guidance, quiet alarm preview, time-of-day theme, text size, high contrast, reduced motion, retention, deletion and privacy controls.

No feature is implemented merely because a screen, toggle, sample graph or placeholder callback exists. Sonar requires a functioning signal-processing implementation. Sound classification requires a working model and evaluated labels. Integrations require real permissions, data exchange and error handling.

Separate device limitations, external-account prerequisites and implementation gaps. If an external prerequisite blocks a feature, record the exact blocker and keep progressing on independent work. Do not call the app complete while in-scope core behavior is still missing.

## Execution process

1. Inspect the existing repository and attached designs. Build the app in its current state before assuming what works.
2. Create or update a feature-to-screen-to-code matrix and a short persistent implementation plan. Record current status as implemented, partial, missing or blocked with evidence.
3. Establish shared typography, colors, spacing, shapes, icons and navigation components from the designs.
4. Implement real vertical flows incrementally: an alarm that rings and completes its task; a night that records data and produces its report; a setting that persists and changes behavior.
5. Preserve working alarm logic and user data. Follow the existing architecture where practical; avoid an unnecessary wholesale rewrite.
6. Keep platform scheduling, storage, sensors/audio, analysis and UI separated enough to test and maintain.
7. Handle foreground/background transitions, screen-off/Doze, restart/reboot, time changes, overlapping alarms, interrupted audio, denied permissions, unavailable sensors, import failures and empty data states.
8. Use monotonic elapsed time for relative timers and appropriate wall-clock scheduling for calendar alarms. Store enough provenance to distinguish tracked, imported, estimated and user-edited records.
9. Persist progress and document decisions so another coding turn can continue without redoing research. Continue implementation rather than stopping at scaffolding or a plan.
10. Commit coherent, verified milestones when repository write access is available, preserving existing user changes and reporting actual commit hashes.

## Design and functional verification

For each implemented screen:

- Capture an actual screenshot at the target S23 Ultra viewport.
- Compare it side by side with the corresponding reference. Correct font size/weight, spacing, alignment, icon shape, surface colors, dock geometry, selection state and safe-area clearance.
- Use real example records for charts and controls; keep demonstration fixtures out of production user data.
- Exercise the main action and confirm navigation, state persistence and resulting behavior.
- Check accessibility labels, touch targets, font scaling and empty/error states.

Screenshots of static sample content do not prove functionality. Conversely, passing tests does not excuse a visibly different interface.

Run meaningful unit/integration tests and relevant Android lint/build checks. Exercise screen-off, reboot, snooze, backup and challenge flows on an emulator or authorized connected phone. Use controlled quiet previews for routine alarm tests. Do not start unrequested overnight recording or surprise loud alarms.

## Definition of finished

- The agreed phone-only feature matrix has evidence-backed implementation and honest status.
- The required screens and supporting states follow the supplied designs with actual screenshot comparisons.
- Core alarm, bedtime guard, tracking, audio, challenge and report flows work with persisted data.
- Advanced algorithms and external integrations are real, not placeholder successes.
- The project builds; relevant checks pass; an APK and clear install/run instructions are available.
- Any remaining device/account restrictions or unvalidated analysis are clearly documented, rather than hidden behind a completion claim.
- README, feature matrix, permissions/privacy notes and handoff instructions reflect the actual code.

Start by inspecting the repository and design bundle, then proceed with the first complete vertical flow. Ask for missing access or truly blocking choices only. Do not ask me to repeat information already in the attachments.


# Challenge, narration and bedroom-light features

This note describes the current production source batch. Its evidence level is **production-build-only; runtime behavior remains unverified** for the requested speech engines, microphone challenges and physical Hue bridge/light. Writing this note involved source reads only: no new tests, devices, providers, TTS engines or network calls were exercised. This is not a claim of vendor feature parity, recognition accuracy or finished UI design.

## Installed offline narration voice

**Path:** Tonight options → **Guided wind-down**, or Sleep sounds → **Guided wind-down** → **Offline narration voice**.

The chooser lists installed speech-service packages and lets the user explicitly refresh engine-declared offline English voices, select one, then **Save voice selection**. Opening the chooser, refreshing it and saving do not synthesize speech or download a voice. Narration starts only through **Play narration** in the running guide. The original guide remains English; selecting a locale does not translate it.

The saved identity is the requested engine package plus exact voice name and locale, in separate device-local preferences. Rate and pitch remain independent. **Auto** retains the existing automatic offline-English selection. At explicit Play, a selected identity is frozen, the requested package is passed to Android's public TTS constructor, and the returned voice inventory, language support, exact name/locale, offline flag and installed status are checked before use. A missing selected voice produces an error rather than silently selecting a different voice.

Android may fall back to another engine during initialization; its public API does not prove engine-package binding. The UI therefore says **requested engine**, not guaranteed engine isolation. No remote voice fallback is introduced. Leaving/pausing the guide stops narration; engine cleanup failures remain visible rather than admitting another uncertain owner.

Source: [guide UI](../app/src/main/java/org/sleepwell/app/GuidedWindDownActivity.java), [voice chooser](../app/src/main/java/org/sleepwell/app/NarrationVoiceActivity.java), [installed-voice discovery](../app/src/main/java/org/sleepwell/app/NarrationVoiceDiscovery.java), [saved identity](../app/src/main/java/org/sleepwell/app/NarrationVoice.java), [narration runtime](../app/src/main/java/org/sleepwell/app/GuidedWindDownNarration.java).

## Saved random challenge pools

**Path:** Alarms → an alarm → **Challenges** → **Routine mode** → **Random selection**. Pool rows open their actual task settings. **Use random routine** returns a draft; the parent **Save alarm** action is still required.

A pool contains 1–8 distinct task configurations and a saved task count no greater than its pool size. The editor can preview an example selection. The occurrence selection is separate from that preview and the saved pool. Current supported pool tasks are Math, Memory, Sequence, Retype, Visual search, Counting, Flags, Mirror text, Dream journal, Jumping, Type code, Shake, Walk, NFC, QR code, Tilt maze, Light and Spin around. Microphone and image-recognition tasks are not implicitly added to this pool.

Eligibility is checked without requesting permission or starting hardware, then checked again when the occurrence is delivered. Hardware entries require their actual prerequisites: for example, Walk needs activity permission and a step sensor; QR needs an enrolled target, camera and existing camera permission; NFC needs an enabled reader and enrolled tag. Unavailable entries are excluded with reasons. Selection is without replacement among eligible entries. If fewer entries are eligible than the saved count, actual one-round **Math alternatives fill the missing positions**, preserving that requested count.

The concrete selection and its notice are persisted with the owned occurrence. Reopening or recreating the alarm screen does not reroll it. Snoozing carries that exact concrete selection into the successor run; a new main occurrence selects anew. **Random selection details** exposes the selected-count and exclusion notice. An unresolved or corrupt active wrapper cannot authorize an arbitrary child task; recovery uses the saved materialized selection or a Math alternative.

Source: [pool editor](../app/src/main/java/org/sleepwell/app/RandomChallengeActivity.java), [pool codec, eligibility and occurrence ledger](../app/src/main/java/org/sleepwell/app/RandomChallenges.java), [editor entry](../app/src/main/java/org/sleepwell/app/AlarmEditorActivity.java), [accepted-occurrence selection](../app/src/main/java/org/sleepwell/app/Scheduler.java), [alarm task dispatch](../app/src/main/java/org/sleepwell/app/AlarmActivity.java).

## Laughter and relative loud-sound challenges

**Path:** Alarms → an alarm → **Challenges** → a **Laughter** or **Loud sound** task → save the alarm. During that task, **Open microphone challenge** opens the dedicated flow; capture requires explicit **Listen**. **Use Math instead** remains available subject to confirmed capture cleanup.

| Task | Actual acceptance rule | Meaning and limits |
| --- | --- | --- |
| Laughter | The existing on-device YAMNet policy must return the laughter family for two consecutive complete windows. Family indices 13–18 use the maximum score, not a sum; the score gate remains 0.65 with a 0.15 margin against unrelated classes. A nonmatching window resets the consecutive count. | Experimental audio-content estimate. It does not establish emotion, identity or liveness. Bedroom accuracy and the previously unresolved independent laughter coverage remain unproven. |
| Loud sound | Three complete quiet baseline windows establish the maximum normalized PCM RMS. Then two consecutive windows must exceed `max(0.025, baseline × 3.1622776602)` with at most 1% clipped samples. An already loud/clipped baseline is rejected. | Relative microphone amplitude only: no calibrated decibels, instrument identification or semantic sound recognition. The instructions ask for a comfortable sound, not shouting or dangerous volume. |

The shared capture uses 15,600-sample windows at 16 kHz (about 0.975 seconds). It saves no raw recording. Explicit visible admission, one-use attempt identity, the shared microphone lease and a bounded attempt prevent these tasks from opening a second tracking/sonar microphone. The matching alarm's audible output is quieted for listening and restored only for the still-current occurrence after confirmed capture cleanup. Permission, model or capture failure is visible. An unconfirmed close stays blocked for cleanup rather than pretending the microphone is released. These are fixed-routine tasks, not additions to random-pool eligibility.

Source: [task rules and copy](../app/src/main/java/org/sleepwell/app/AudioChallengeMode.java), [YAMNet family policy](../app/src/main/java/org/sleepwell/app/SoundAnalysis.java), [capture runtime](../app/src/main/java/org/sleepwell/app/SpeakRuntime.java), [explicit challenge screen](../app/src/main/java/org/sleepwell/app/SpeakChallengeActivity.java), [capture service](../app/src/main/java/org/sleepwell/app/SpeakCaptureService.java), [owned completion proof](../app/src/main/java/org/sleepwell/app/SpeakProof.java).

## Direct Philips Hue bedroom light

**Path:** Settings → **Appearance** → **Smart lights** → **Direct Philips Hue bridge**. Home Assistant remains a separate connection path.

The user enters a private LAN IPv4 address, the bridge's exact TLS identity and an **independently verified SHA-256 leaf-certificate pin**. Pairing requires explicit verification and confirmation of the physical bridge button. The implementation uses HTTPS on port 443 and the Hue **v1** light API only, with TLS name/date checks and the exact certificate pin. It performs no discovery, cloud login, HTTP fallback or automatic trust-on-first-use. Hostnames, IPv6, v2-only resources, groups/scenes and color control are outside this implementation.

After pairing, **Load dimmable lights** and **Check selected light** are read-only requests. The user chooses one actual light, a sunrise duration and maximum brightness, then **Save Hue selection**. The credential is sealed locally and excluded from portable settings profiles. Pairing creates a bridge app user even before local Save; cancelling or **Disconnect locally** does not revoke that bridge-side user.

**Use for future real alarms** is off by default. Save never changes the lamp. Only an admitted real alarm can start the saved sunrise; quiet test alarms are excluded. Direct Hue and Home Assistant are mutually exclusive: if both are enabled, direct Hue does not start, and the Hue-enabled path does not silently fall back to Home Assistant. Phone alarm behavior remains independent of bridge availability.

The owner ramps only the chosen light's on/off and brightness. A lamp already above the target is left unchanged. Before further commands it checks whether another controller changed the observed state; such a change relinquishes control. Optional restoration is conditional on the same configuration revision and matching last-owned brightness/on state plus unchanged color/effect metadata. It restores the observed original on/off and brightness, not a global light reset. Off, disconnect or configuration changes revoke further control and may leave the last requested level. In-flight commands cannot be recalled; process death, network failure or an uncertain restore cannot guarantee restoration.

There is a separately consented **Request low saved-light test**, capped at 5%, requesting conditional restoration after about three seconds. It is real lamp output when explicitly used; it was not invoked for this documentation or claimed as validated. Compatible bridge firmware, reachable LAN and independently obtained identity/pin remain prerequisites.

Source: [entry path](../app/src/main/java/org/sleepwell/app/AppearanceActivity.java), [Hue setup](../app/src/main/java/org/sleepwell/app/HueBridgeActivity.java), [configuration bounds](../app/src/main/java/org/sleepwell/app/HueBridgeConfig.java), [HTTPS client](../app/src/main/java/org/sleepwell/app/HueBridgeClient.java), [private saved settings](../app/src/main/java/org/sleepwell/app/HueBridgeSettings.java), [alarm owner](../app/src/main/java/org/sleepwell/app/HueBridgeAlarm.java), [ramp and conditional restore](../app/src/main/java/org/sleepwell/app/HueBridgeRun.java), [alarm integration](../app/src/main/java/org/sleepwell/app/AlarmService.java).

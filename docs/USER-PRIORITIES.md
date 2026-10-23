# User priorities — 23 October 2026

These priorities steer the full phone-app goal; the 36 reference screens and
remaining feature matrix still apply. Lullabies are lower priority.

| Priority requested | Current authoritative scope |
| --- | --- |
| Prevent leaving / power off on all alarms | Optional global accessibility protection; best-effort source implementation with eleven pure policy tests and passing native configuration/recreation flow. Actual alarm/menu operation remains untested by request. See ALARM-PROTECTION.md. |
| Extra loud toggle | Runtime source now connects the per-alarm toggle to maximum device alarm volume while retaining the normal percentage. Fake output/ownership tests pass; native configuration/save/reopen now passes at 69298aa; actual output remains unverified. No hardware boost claim. |
| Sleep noise analysis | Bundled offline YAMNet estimates and stored events are connected. Actual Android CPU synthetic-input inference passed; event linking and configuration pass. Real human fixture evaluation failed to persist positives; compatible-label corrections require a fresh run. Microphone capture remains unverified. |
| Automatically detect sleeping | Explicitly armed bedtime/smart-window/phone-idle modes and optional sound consent are coded, with pure tests and first native configuration pass. Actual Service/sensor/overnight behavior remains unverified; idle-phone evidence is an estimate. |
| Better graphs and charts | Actual measurement, pause/gap and trend models exist; remaining design and feature acceptance stays open. |
| Compact alarm list, settings on alarm click | Repeated list settings removed; native time/name opens editor, which keeps Alarm on and Save visible. Actual configuration captures and silent navigation pass; other editor features retain their own incomplete gates. |
| Gentle volume increase | Runtime candidate replaces the coarse ramp with continuous player gain and gain/volume-before-start ordering, shared planned chart and optional sound delay. Fake lifecycle/fallback tests pass. Native duration-picker/recreation/save/reopen/cancel flow now passes at 69298aa; actual alarm output is untested. |
| Sunrise wake-up | Runtime candidate changes only the alarm Window brightness using occurrence time, with pause/restore/resume and separate lifecycle ticker. Fake light tests pass; native configuration and bounded control-layout review pass at 69298aa; actual sunrise is untested. Smart lights remain missing. |
| Flashlight wake-up | Runtime candidate uses the public torch API, preserves initially lit lights and handles ownership/cleanup/unavailability with fake tests. Native configuration and bounded control-layout review pass at 69298aa; actual phone torch operation is untested by request. |
| Anti-snoring | Default-off sound/vibration cue source follows model snoring estimates with bounded cooldown/budget and settings. Fake-output tests pass; native configuration, hardware operation and effectiveness remain unverified. |
| Jet-lag prevention | Saved IANA zone/date plan and opt-in silent daylight reminders are integrated candidates. Native configuration/retry/reopen evidence pending; no clinical prevention or flashlight-therapy claim. |

No physical alarm, power-menu, flashlight, vibration, microphone or tracking test
is authorized during the user's sleep restriction. Configuration review and
fake-clock/fake-output tests can progress without activating these effects.
Do not mark the full goal complete based on the narrower milestones.

Latest bounded review: CI 39967552077 passed all five silent emulator flows at
69298aa. The strict reviewer inspected all 39 original screenshots and accepted
the new configuration controls and curves. Full Alarm sound board 03 still
fails reference fidelity. Offscreen helper copy and large-text wake settings
remain visually unverified. This does not certify native alarm effects.

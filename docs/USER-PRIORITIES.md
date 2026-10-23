# User priorities — 23 October 2026

These priorities steer the full phone-app goal; the 36 reference screens and
remaining feature matrix still apply. Lullabies are lower priority.

| Priority requested | Current authoritative scope |
| --- | --- |
| Prevent leaving / power off on all alarms | Optional global accessibility protection; best-effort source implementation with eleven pure policy tests and passing native configuration/recreation flow. Actual alarm/menu operation remains untested by request. See ALARM-PROTECTION.md. |
| Extra loud toggle | Not shipped. Intended maximum device alarm volume; no hardware loudness boost claim. |
| Sleep noise analysis | Relative microphone amplitude and triggered WAV recording exist; classification and evaluated snoring/talking labels are missing. |
| Automatically detect sleeping | Missing; movement tracking is not proof of sleep onset. |
| Better graphs and charts | Actual measurement, pause/gap and trend models exist; remaining design and feature acceptance stays open. |
| Compact alarm list, settings on alarm click | Repeated list settings removed; native time/name opens editor, which keeps Alarm on and Save visible. Actual configuration captures and silent navigation pass; other editor features retain their own incomplete gates. |
| Gentle volume increase | Existing coarse stream ramp has a known initial-volume ordering issue. Continuous envelope/runtime integration remains unfinished. |
| Sunrise wake-up | Missing; shared screen/audio-delay design requires runtime integration and silent fake-output verification. |
| Flashlight wake-up | Missing; device/resource ownership and cleanup must be handled before acceptance. |
| Anti-snoring | Missing; requires a real evaluated detector, then optional cue behaviour. An amplitude threshold is not snore classification. |
| Jet-lag prevention | Travel/light-plan flow remains missing. Do not claim it prevents jet lag without appropriate evidence. |

No physical alarm, power-menu, flashlight, vibration, microphone or tracking test
is authorized during the user's sleep restriction. Configuration review and
fake-clock/fake-output tests can progress without activating these effects.
Do not mark the full goal complete based on the narrower milestones.

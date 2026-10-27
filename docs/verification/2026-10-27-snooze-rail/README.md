# Snooze rail: bounded native correction

Source: `ecd1c7ec5667708c37dbe884b5d1c3f586ba1c37`.
[Run 40550003988](https://github.com/omeyerrato/SleepWell/actions/runs/40550003988) completed on 2026-10-27. **The overall job FAILED**: Gentle wake-up and Privacy failed. The unaltered combined summary also records Health Connect's two passing methods. This folder accepts only the scoped Snooze correction: `org.sleepwell.app.AdvancedSnoozeFlowTest#longMaximumAndPerPressChoicePersistCancelAndRecreateWithoutRuntime`, **38.75 seconds, OK (1 test), all ten Snooze originals**.

All ten PNGs were opened individually against board03's right-hand screen at `design-concepts/2026-10-22-phone-app/03-sound-and-snooze.png`. Original image bytes, scoped native log, environment and combined summary are unchanged CI artifacts. `run.json` is the read-only GitHub metadata export collected for this archive. SHA256SUMS covers every retained file except itself.

## Observed rail correction

The earlier scoped evidence in [snooze-backup](../2026-10-27-snooze-backup/README.md) showed a pale rail regardless of value. The corrected actual native images show a fully brown active rail at 100% and a brown leading portion/pale remainder at 35%; draft recreation and saved reopening retain that rendering and the Chime/35% configuration. Main controls remain above Save. The extent follows the actual SeekBar's 10..100 range, so a value of 35 is normalized as `(35-10)/(100-10)`; the change does not redefine the setting or slider semantics.

The same existing native method now verifies the real primary drawable level, an independent secondary-progress layer, full track/primary bounds and the actual drawable rendered to a bitmap: brown active-center pixels and pale inactive-center pixels. This is a native drawable check plus separately reviewed screenshots, not a hardware output measurement. The source correction gives Android's secondary-progress refresh its own transparent layer so it cannot reset the whole track's level after primary initialization. Dimensions, touch/range semantics and native RTL mirroring remain unchanged.

| Original | Inspected result |
| --- | --- |
| `advanced-snooze-initial.png` | Pulse/100%, fully brown active rail; full primary rows fit above Save. |
| `advanced-snooze-options.png` | Supporting options, 90 min cap and per-press off; advanced controls remain discoverable. |
| `advanced-snooze-options-recreated.png` | Options parent and per-press on survive recreation. |
| `advanced-snooze-sound-picker.png` | Real Daybreak/Pulse/Chime selector, Pulse selected, Cancel present. |
| `advanced-snooze-sound-picker-recreated.png` | Open selector, selected Pulse and parent state retained after recreation. |
| `advanced-snooze-draft.png` | Chime/35%, brown leading rail and pale remainder; count/shortening/escalation/check changes remain visible. |
| `advanced-snooze-recreated.png` | Same actual draft values and rail after recreation. |
| `advanced-snooze-reopened.png` | Saved Chime/35% and the active rail survive reopening. |
| `advanced-snooze-invalid.png` | Unreadable block marked Needs review; fallback guidance and explicit repair path retained. |
| `advanced-snooze-repaired-draft.png` | Confirmed Pulse/100% repair renders a full brown rail in the draft without overwriting stored Chime/35% before Save. |

## Scope and limits

Environment records API 36 emulator, `sdk_gphone64_x86_64`, 1080 x 2316 override at 420 dpi, host output disabled and no allowlisted native playback output. The method also retains its settings/Save/cancel/default/portable checks and actual Playback/WakePolicy/VolumeLease exercised through fake Sink/Port/Journal only. It does not construct native AudioSink output, play sound, write the hardware alarm stream, run AlarmService delivery or verify physical loudness/restoration. Requested percentages are not acoustic measurements.

The repeated compatibility checks include omitted legacy/default Pulse 100 settings, SQLite 17 unchanged, format 14 export/restore, legacy 13 import and strict malformed-block rejection before replacement. The format 14 gate makes older apps reject newer exports rather than silently discard custom backup settings; no downgrade roundtrip is claimed. Partial caller-field patches retain the hidden configuration without adding caller columns or URI grants.

Accepted here: the observed pale-to-brown rail correction, represented normal-text main-control fit, configuration persistence and this method's fake-policy checks. This is not all 36-screen or pixel-perfect acceptance. Shared glyphs, the plain Backup sound value/chevron versus the reference pill, other control treatments and conditional repair scrolling remain. The reopened frame also has a pale gesture handle on cream; this archive does not accept all system-bar states. No independent RTL/device/large-text campaign, actual sound, hardware alarm behavior, personal-phone operation or later Gentle/Privacy fix is established. The separate failed methods remain failed in the combined metadata.

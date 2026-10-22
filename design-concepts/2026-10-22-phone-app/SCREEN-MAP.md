# SleepWell — phone app design set

Created 22 October 2026 using the built-in image generation tool. **36 screen mockups in 12 boards.** Open [gallery.html](gallery.html) to browse them, or open an image directly. [BUILD-PROMPT.md](BUILD-PROMPT.md) is the implementation brief to give a coding assistant.

## Direction

Changing light is the base: plum at night, cream for daytime information and amber for waking. Typography follows the neutral Inter-style direction; the images approximate the family. Root navigation uses the Uber-inspired grouped floating dock, with selection surrounding the icon and label. Detail screens use back navigation and a clear action instead of a destination bar. The active alarm and tracking flows remain focused on their immediate actions.

## Scope

Wearables, watch companions, fitness bands, heart-rate/oxygen hardware and associated setup pages are excluded at the user's request. The set concentrates on phone motion, microphone/sound analysis, ambient light, contactless sonar, QR/NFC challenges, optional smart lights, sleep-record exchange and automation. No wearable feature has been added by this work.

These are static design artifacts, including proposed features not yet implemented in SleepWell. Data, diagrams, sensor statuses and permissions shown are fictional samples. They do not reflect live phone readings. Controls do not execute. This work did not edit or install the app, change permissions, set alarms or record audio. [The existing feature implementation map](../../docs/FEATURES.md) remains separate from this design coverage. Plot coordinates are illustrative; implementation must derive charts from actual records and render native accessible controls.

## Screens

| Board | Screen IDs | Screens | Image |
|---|---|---|---|
| 01 Everyday | 01–03 | Tonight; Sleep report; Wake-up challenge | [Open](01-everyday.png) |
| 02 Alarms | 04–06 | Alarm list; Edit alarm; Smart wake-up | [Open](02-alarms.png) |
| 03 Alarm sound & backup | 07–09 | Choose alarm sound; Gentle wake-up; Snooze & backup | [Open](03-sound-and-snooze.png) |
| 04 Wake-up challenges | 10–12 | Challenge routine; QR / NFC enrollment; Wake-up check | [Open](04-challenges.png) |
| 05 Wind-down & bedtime | 13–15 | Wind-down; Blocked apps; Sleep goals | [Open](05-bedtime.png) |
| 06 Phone sleep tracking | 16–18 | Tracking setup; Sonar calibration; Live tracking | [Open](06-tracking.png) |
| 07 Sleep sounds & recordings | 19–21 | Sleep sound mixer; Sound-event library / player; Anti-snoring cues | [Open](07-sleep-audio.png) |
| 08 History & editing | 22–24 | Sleep history; Weekly trends; Edit sleep record | [Open](08-history-and-trends.png) |
| 09 Rhythm, naps & travel | 25–27 | Sleep regularity; Nap timer; Travel / light-cue setup | [Open](09-rhythm-and-naps.png) |
| 10 Automatic tracking & room signals | 28–30 | Automatic tracking; Phone motion test; Room conditions | [Open](10-phone-sensors.png) |
| 11 Settings & integrations | 31–33 | Settings home; Health Connect (sleep only); Automation rules | [Open](11-settings-and-integrations.png) |
| 12 Reliability, privacy & appearance | 34–36 | Alarm readiness; Backup & privacy; Appearance / smart-light setup | [Open](12-privacy-and-appearance.png) |

## Navigation and flows

- Tonight → Alarms → Edit alarm → Smart wake-up / Sound / Snooze & backup / Challenge routine.
- Challenge routine → QR/NFC enrollment → preview. Active wake-up challenge → wake-up check.
- Tonight → Wind-down → Bedtime guard / Sleep goals / Sleep sounds.
- Start tracking → Phone sensor setup → motion or contactless sonar → live tracking → Report.
- Report → Sound events / History / Trends / Edit record / Sleep rhythm. Nap and Travel are additional planning flows.
- Settings → Automatic tracking / Sensor test / Room conditions / Integrations / Reliability / Data & privacy / Appearance.

## Source mapping

Features were reviewed in the official [Sleep as Android manual](https://sleep.urbandroid.org/docs/). Layouts and copy are adapted for SleepWell; vendor graphics, hosted services and paid account access are not reproduced. Bedtime app blocking extends the original SleepWell brief.

### 01 Everyday

- [sleep tracking](https://sleep.urbandroid.org/docs/sleep/sleep_tracking.html)
- [captcha](https://sleep.urbandroid.org/docs/alarms/captcha.html)

### 02 Alarms

- [alarm settings](https://sleep.urbandroid.org/docs/alarms/alarm_settings.html)
- [smart wake up](https://sleep.urbandroid.org/docs/sleep/smart_wake_up.html)

### 03 Alarm sound & backup

- [ringtone](https://sleep.urbandroid.org/docs/alarms/ringtone.html)
- [snooze](https://sleep.urbandroid.org/docs/alarms/snooze.html)
- [backup](https://sleep.urbandroid.org/docs/alarms/backup.html)

### 04 Wake-up challenges

- [captcha](https://sleep.urbandroid.org/docs/alarms/captcha.html)

### 05 Wind-down & bedtime

- [bedtime notification](https://sleep.urbandroid.org/docs/alarms/bedtime_notification.html)
- [goals](https://sleep.urbandroid.org/docs/sleep/goals.html)

### 06 Phone sleep tracking

- [sleep tracking](https://sleep.urbandroid.org/docs/sleep/sleep_tracking.html)
- [sensors](https://sleep.urbandroid.org/docs/sleep/sensors.html)

### 07 Sleep sounds & recordings

- [lullaby](https://sleep.urbandroid.org/docs/sleep/lullaby.html)
- [sleep noise recording](https://sleep.urbandroid.org/docs/sleep/sleep_noise_recording.html)
- [anti snoring](https://sleep.urbandroid.org/docs/sleep/anti_snoring.html)

### 08 History & editing

- [statistics](https://sleep.urbandroid.org/docs/sleep/statistics.html)
- [charts](https://sleep.urbandroid.org/docs/sleep/charts.html)
- [graph edit](https://sleep.urbandroid.org/docs/sleep/graph_edit.html)
- [tags](https://sleep.urbandroid.org/docs/sleep/tags.html)

### 09 Rhythm, naps & travel

- [chrono jetlag](https://sleep.urbandroid.org/docs/sleep/chrono_jetlag.html)
- [naps](https://sleep.urbandroid.org/docs/alarms/naps.html)
- [jetlag](https://sleep.urbandroid.org/docs/sleep/jetlag.html)

### 10 Automatic tracking & room signals

- [automatic sleep tracking](https://sleep.urbandroid.org/docs/sleep/automatic_sleep_tracking.html)
- [sensors](https://sleep.urbandroid.org/docs/sleep/sensors.html)
- [light level](https://sleep.urbandroid.org/docs/sleep/light_level.html)

### 11 Settings & integrations

- [health connect](https://sleep.urbandroid.org/docs/services/health_connect.html)
- [automation](https://sleep.urbandroid.org/docs/services/automation.html)

### 12 Reliability, privacy & appearance

- [permissions](https://sleep.urbandroid.org/docs/getting_started/permissions.html)
- [backup data](https://sleep.urbandroid.org/docs/services/backup_data.html)
- [privacy](https://sleep.urbandroid.org/docs/general/privacy.html)
- [personalize](https://sleep.urbandroid.org/docs/ux/personalize.html)
- [smart light](https://sleep.urbandroid.org/docs/devices/smart_light.html)

## Review notes

The final pass corrected the sample year to2026, smart-wake/sleep-goal explanations, automatic-tracking helper copy, the distinction between phone motion and inferred sleep, and readiness wording when a battery check needs attention. The sample week averages about7h43min with a total2h gap to an8h nightly goal. Chronotype remains an incomplete-data state rather than an invented result. Stage labels remain estimates. Wearables are outside this design set.


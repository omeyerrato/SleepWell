# Reusable alarm behavior defaults

From an ordinary alarm editor, **New alarm defaults** opens an explicit review/save screen. Saving changes future **Add alarm** drafts only. Restore factory defaults removes the template. Opening, cancelling or recreating the review screen performs no write; a retained, single-flight worker completes an already requested save across recreation.

Included: sound and its imported document name, volume/gentle delays, vibration, sunrise/flashlight, extra loud, smart window, challenge routine/pinning, snooze/count/shortening, backup escalation and wake-up check. Each created alarm receives an independent draft and its own positive identity. Time remains 07:00, days empty and enabled false. Alarm label, nap, skip, schedule errors and occurrence authority never transfer. Existing alarms remain unchanged.

Missing private settings retain exact old defaults. Invalid/type-confused/oversized templates show an error and fall back without replacing saved bytes. The private versioned codec accepts only enumerated behavior fields; authority fields are rejected. This is a local preference, excluded from portable night/alarm backups (as with other app-wide preferences); actual saved alarms still serialize all of their behavior normally. No database schema/backup-format change.

Imported tones retain their existing document URI; availability remains subject to the provider and its persisted grant. Import-discard cleanup checks templates as well as saved alarms and the sleep-sound mix, with template save/release serialized. Saving a template does not open the provider, allocate audio, request permissions, schedule or enable alarms.

## Evidence contract

- `AlarmDefaultsTest`: four host policy tests, including immutable snapshots, excluded authority, complete behavior transfer and legacy defaults.
- `AlarmDefaultsStorageTest`: three private emulator-only methods: strict JSON/settings/reset; malformed/authority-bearing input; template grant references and unchanged real SQLite/portable alarm rows.
- `AlarmDefaultsFlowTest`: two configuration-only methods with private settings and forbidden recording-store factory, no output or runtime service starts. Checks explicit save/recreate/reopen/reset and held-save/recreate/failure/retry, disabled busy actions and unchanged global preferences. Nine required originals: `alarm-defaults-{initial,recreated,saved,reopened,reset,saving,saving-recreated,save-error,retry}.png`.

Native execution and these originals are pending parent-owned CI. No physical alarm/audio test is authorized or claimed.

# Native home widget and app shortcuts

F083 adds a classic Android RemoteViews home-screen widget and three native app shortcuts: **Add alarm**, **Sleep tracking**, and **Sleep history**. This is a Java/native implementation; it does not add a Compose migration or a new framework dependency.

## Actual saved content

The widget reads actual enabled alarm records and persisted future alarm deadlines. Pending nap timers use their saved relative timer projection without creating a timer identity or rescheduling anything. It displays the earliest saved future deadline; no future deadline, disabled alarms, invalid/unreadable alarm data and missing exact-alarm access have distinct states. It does not manufacture a scheduled alarm from an unsaved clock time. Current snooze delivery is not separately projected when no saved primary deadline exists; open the app for the complete running/snooze state.

The record panel uses the same effective completed-record projection as history, including actual edits, awake periods, pause/active-duration bounds and imported Health Connect awake periods. Active unfinished recordings are excluded. It shows duration/date and interruption/manual provenance. Notes, tags, sound data and guessed stages are never sent to the launcher.

Snapshot reads are bounded to 128 alarm rows and a single completed record with at most 2048 pause rows. Larger/unreadable data produces an unavailable state instead of an invented summary. SQLite/provider contention can still delay a read; no accuracy claim is based on a cached demonstration value.

## UI-only actions

Alarm taps open Alarms, or Reliability when exact-alarm access is missing. Record taps open the actual record report, falling back to History for an empty state. Add alarm opens an unsaved editor draft using the existing saved alarm defaults. It does not save, enable or schedule that draft. Refresh requests a new snapshot. Widget settings open the Home screen settings page.

All widget PendingIntents are explicit and immutable. `HomeWidgetProvider` is nonexported and accepts only expected widget/system/refresh actions. The exported `HomeLaunchActivity` exists so launchers can open a small allowlist of screens. It reconstructs private target intents; it never forwards incoming intents, URI grants, alarm commands, sensor/audio flags or nested intent payloads. Unknown actions/routes are rejected. Deleted reports show the existing unavailable-record state instead of opening another record silently.

## Refresh and launcher integration

Store alarm saves/deletes, recording end/recovery/erase, record create/edit/reset/delete and scheduler completion request a coalesced worker refresh. Main reopening and protected boot/date/time/time-zone/package events also refresh. At most one snapshot read and one coalesced follow-up are queued. Database and widget-host update work run off the main thread, exceptions are contained, and transaction-owned notifications are deferred until outside the transaction or Store close. The widget never holds an alarm/recording transaction waiting for a host. A receiver completion watchdog bounds its asynchronous broadcast ownership.

No exact alarms, foreground services, periodic housekeeping timers, microphone, light output or playback are used. `updatePeriodMillis` is zero. The explicit Refresh control is available when an OS/launcher event is delayed or another process changes state.

On Main reopening, dynamic shortcuts are published only when missing, subject to launcher limits/rate limits. Settings → Home screen offers explicit requests to pin the widget or one shortcut. Android/your launcher must support the relevant pin API and the user must confirm it. Merely opening settings never pins anything. A submitted pin request is not described as a completed installation. Existing shortcuts/widgets are not automatically installed on the phone by tests or by an app update.

The native layout uses the existing pinned Phosphor icons and the identical bundled Inter font through a RemoteViews font resource. It supports native host resizing, accessibility descriptions and 48 dp action targets. Widget contents are visible on the user's home screen. Device/launcher sizing, large text and actual pinning/rendering remain unverified until a separately authorized launcher test; inert view inflation is not proof of those behaviors.

## Verification contract

`HomeWidgetNativeTest` has seven isolated native methods: fresh/disabled/access-needed states; earliest saved deadline/effective manual duration/reopen without mutation; bad alarm JSON with a valid report; actual inert RemoteViews inflation/accessibility; 50-to-1 fake-host off-main coalescing; explicit allowlisted widget/shortcut action resolution and manifest export boundaries; and an Add alarm draft that leaves SQLite unchanged. It uses UUID preferences/database, fake host/reader ports and actual package resources. No launcher host is bound, no widget/shortcut is pinned and no pending intent is sent. No original screenshot contract is added by this fixture.

Android build/native execution results must be recorded separately. These tests cannot certify real launcher pinning, home-host layout, actual alarm delivery, physical device behavior or pixel fidelity.

Primary references: [Android native widget components and RemoteViews](https://developer.android.com/develop/ui/views/appwidgets), [ShortcutManager dynamic and user-confirmed pin APIs](https://developer.android.com/reference/android/content/pm/ShortcutManager).

Author verification on 2026-10-25: :app:assembleDebug, :app:assembleDebugAndroidTest and :app:lintDebug passed (5 min 43 sec; lint 0 errors, 118 warnings, 1 hint). The seven new native methods compile; they have not yet been executed in this worktree. No physical phone, real launcher pin/bind or output test was performed.

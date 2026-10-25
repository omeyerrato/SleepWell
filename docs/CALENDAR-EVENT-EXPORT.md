# Review a saved sleep period in your calendar app

Report options → Add to calendar displays the actual saved effective start/end,
zone and recorded duration. The event covers the whole period, including pauses
and awake intervals; its description distinguishes this from effective duration
and measured sleep. Notes are excluded by default. The note choice survives screen
recreation but resets when the review is opened again.

Only **Open calendar** sends a fresh Android `ACTION_INSERT` event draft. Android's
[calendar handoff](https://developer.android.com/identity/providers/calendar-provider)
lets the user review and save in their chosen calendar app without granting
SleepWell calendar-write access. No source record identifier, account, recording,
URI grant or nested Intent is sent. Missing/denied calendar apps show an error.

This is an explicit event draft export, not automatic synchronization. SleepWell
does not know whether the calendar app saved the event. Returning never marks it
saved, and repeating the action may create another event. Automatic record write,
update/delete reconciliation and calendar-provider compatibility remain separate.

Three pure boundary/disclosure tests, one native saved-record/Intent contract and
one native review/note/recreation/missing-app/retry flow with four originals are
authored. Build and native execution remain pending. Tests replace the external
launcher; no actual calendar event, phone alarm or audio output has run.

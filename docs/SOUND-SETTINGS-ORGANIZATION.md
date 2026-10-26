# Sound settings organization

This is a bounded organization pass over the existing sound sensitivity, audio
retention and snoring cue settings. It preserves existing policies, saving jobs,
permission requests and explicit preview behavior. Existing Changing light,
Inter, Phosphor icons and board07/11 hierarchy remain the visual reference;
there is no replacement design system or generated sample data.

## Where each control lives

| Existing control/action | Current location | Behavior preserved |
| --- | --- | --- |
| Detected categories, all6 switches | Sound sensitivity → Detected categories | One shared draft; at least1 category; custom selection disables clips next night |
| Snoring/speech/cough/baby crying/laughter minimum durations | Sound sensitivity → Event duration | Same1/2/3 accepted-window policy; model scores unchanged |
| Restore defaults | Sound sensitivity main | Resets categories and durations in draft; Save still required |
| Save sensitivity and read/save/error states | Footer on all sensitivity pages | Original retained immutable save job; confirmed result only |
| Keep recordings1/3/7/14/30days or all | Audio retention main | Same choices and retained save/prune job |
| Keep favorites | Audio retention main | Favorite estimate keeps shared audio file |
| Expiration/preserved-history explanation | Audio retention → Retention details | Main page still states that applying permanently deletes expired audio |
| Save & apply retention and cleanup retry/errors | Footer on both retention pages | Existing job and exact result text; failed cleanup stays visible |
| Snoring cues master, alert type, intensity, repeats, detection sensitivity | Snoring cues main | Same draft values and production policy |
| Requires sound analysis / microphone permission | Snoring cues main | Permission requested only from explicit enabling action; Save validation unchanged |
| Contactless vibration and sound opt-ins | Snoring cues → Contactless & output | Same two flags; future-night behavior; no output merely opening route |
| Sound output | Contactless & output → existing Sound output activity | Existing independent route-save flow retained; parent draft survives |
| About snoring cues | Contactless & output → named help | Existing limits, media-volume and estimate explanation |
| Preview cue | Snoring cues main | Still only explicit Preview may allocate output; same low intensity, budget and tracking/alarm guards |
| Save settings and persisted cue/route errors | Footer on both cue pages | Exact existing preference keys and permission guards |

## Navigation and feedback

Named pages have chevrons, and choice rows have down arrows. Category switches
remain switches; segmented alert/intensity/repeat/sensitivity selections expose
radio semantics and selected state. User input uses existing restrained native
feedback; render, restoration and route initialization do not emit haptics or
claim a successful save. Haptics retain the shared quiet/capture/review guards.
There is no additional animation.

All child pages return to their parent with both toolbar and Android Back.
Routes, existing draft values, local error messages and open owned choice dialogs
survive recreation. The sensitivity editor also accepts the old saved categories
flag. Retained save/retention operations keep their original ownership and busy
control disabling. The cue help dialog is restored, and current output-route
summary refreshes on resume. Only explicit Save writes the parent draft.

## Optional private search routes

Use string extra `soundSettingsPage` with these exact allowlisted values:

| Activity | Value | Visible destination |
| --- | --- | --- |
| SoundSensitivityActivity | `categories` | Detected categories |
| SoundSensitivityActivity | `durations` | Event duration |
| SoundLibrarySettingsActivity | `details` | Retention details |
| SnoringCuesActivity | `options` | Contactless & output |

Unknown or missing values open the normal main page. No route requests a
permission, changes a preference, starts capture or invokes a preview. These
activities retain their existing manifest exposure; no manifest change was made.

## Verification boundary

Source inspection checks every former control/action against the mapping above,
existing preference writes, permission flow and preview admission. `git diff
--check` is clean. No unit-test batch, app build, emulator, physical phone,
microphone, alarm, audio or vibration preview ran for this pass. Root performs
the combined build. Pixel comparison and device Back/recreation interaction
verification remain unverified; this source organization is not a claim of final
visual parity.

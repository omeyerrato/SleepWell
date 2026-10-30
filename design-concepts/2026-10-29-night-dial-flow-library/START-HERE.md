# SleepWell — Night dial flow library

The attached Night dial board is the selected visual source. This library extends that design rather than the later monochrome alternatives. It contains 43 flow boards with 172 identified screen states, including pages, open sheets, menus, empty/error states and task interfaces. These are static visual prototypes, not clickable screens or implemented behavior.

## Start with the everyday journey

1. [Plan tonight — 02](<boards/02.png>) → tap Begin night → [Choose sounds and settle in — 18](<boards/18.png>).
2. Adjust Timer or Fade; choose sensors → [Start, pause and finish tracking — 20](<boards/20-v3.png>).
3. Alarm rings → [Alarm to dismissal — 17](<boards/17.png>) → configured challenge → [Wake-up check — 16D](<boards/16.png>).
4. End & review → [Your recorded night — 26](<boards/26.png>) → [History — 27](<boards/27.png>) or [Trends — 28](<boards/28.png>).

Use the state IDs such as 18C to refer to a precise panel. A board can show alternative branches; it is not always a left-to-right sequence. The route tables below are the navigation specification, including corrections to generated arrow captions. Device settings, file choosers, share sheets, permission prompts and launchers remain Android-owned handoffs.

## Browse the journeys

[Visual navigation map](<FLOW-MAP.md>)

| Board | Journey | Where the user enters |
| --- | --- | --- |
| 01 | [First night](<boards/01.png>) | Settings / Reliability; first launch |
| 02 | [Plan tonight](<boards/02.png>) | Tonight |
| 03 | [Wind down and guard](<boards/03.png>) | Tonight → Settle in → Bedtime guard |
| 04 | [Create and manage alarms](<boards/04.png>) | Alarms tab |
| 05 | [Dates and smart waking](<boards/05-v2.png>) | Alarms → Daybreak → Schedule |
| 06 | [Tone and gentle wake](<boards/06.png>) | Alarm editor → Sound |
| 07 | [Snooze, backup, input](<boards/07.png>) | Alarm editor → Snooze / Backup |
| 08 | [Build a wake routine](<boards/08.png>) | Alarm editor → Wake challenges |
| 09 | [Enroll morning codes](<boards/09.png>) | Wake challenges → QR / NFC |
| 10 | [Screen challenges I](<boards/10.png>) | Routine → Practice; actual alarm uses same task geometry |
| 11 | [Screen challenges II](<boards/11.png>) | Task catalog → Screen |
| 12 | [Writing and light tasks](<boards/12.png>) | Task catalog → Screen / Sensors |
| 13 | [Movement challenges](<boards/13.png>) | Task catalog → Movement |
| 14 | [Camera challenges](<boards/14.png>) | Task catalog → Camera |
| 15 | [Sound challenges](<boards/15.png>) | Task catalog → Sound |
| 16 | [Notes, camera light and fallback](<boards/16.png>) | Task catalog → other; active alarm → Emergency |
| 17 | [Alarm rings to dismissal](<boards/17.png>) | Enabled alarm → Ringing |
| 18 | [Choose sounds and settle in](<boards/18.png>) | Tonight → rain card / Begin night |
| 19 | [Advanced audio sources](<boards/19.png>) | Sounds → More sound settings |
| 20 | [Start, pause and finish tracking](<boards/20-v3.png>) | Settle in → Start tracking |
| 21 | [Automatic tracking](<boards/21-v2.png>) | Settings → Sleep & bedtime → Automatic |
| 22 | [Sensors and calibration](<boards/22.png>) | Settings → Sleep & bedtime → Sensors |
| 23 | [Room, awake and experimental signals](<boards/23.png>) | Settings → Sleep & bedtime → Room conditions |
| 24 | [Sound analysis and recordings](<boards/24.png>) | Settings → Sound & recordings |
| 25 | [Gentle cues and dream tools](<boards/25.png>) | Settings → Sound & recordings → Cues |
| 26 | [Review a recorded night](<boards/26.png>) | Report tab → latest night |
| 27 | [History, manual records and import](<boards/27.png>) | Report → History |
| 28 | [Trends, rhythm and context](<boards/28.png>) | Report → Trends |
| 29 | [Sleep plan, bank and milestones](<boards/29.png>) | Tonight → More → Sleep plan |
| 30 | [Naps and timers](<boards/30.png>) | Tonight More → Naps; Alarms More |
| 31 | [Travel and calendar schedules](<boards/31.png>) | Settings → Sleep & bedtime → Travel |
| 32 | [Find and personalize](<boards/32-v2.png>) | Settings root |
| 33 | [Reliability and alarm protection](<boards/33.png>) | Settings → Reliability / Alarms → Protection |
| 34 | [Health and calendar exchange](<boards/34.png>) | Settings → Connected apps |
| 35 | [Lights and automation](<boards/35.png>) | Settings → Connected apps → Automations |
| 36 | [Local data, export and restore](<boards/36.png>) | Settings → Data & privacy |
| 37 | [Cloud versions, profiles and conflicts](<boards/37.png>) | Data & privacy → Backup folder |
| 38 | [Imported signals, sharing and access](<boards/38-v2.png>) | Report source details / Data & privacy |
| 39 | [Home shortcuts and system handoffs](<boards/39-v2.png>) | Settings → Home screen |
| 40 | [Recovery and details across flows](<boards/40-v2.png>) | Any feature → failure / Back / menu |
| 41 | [Tags, confirmation and custom values](<boards/41.png>) | Report editor / Alarm editor / custom selector |
| 42 | [Physical challenge and permission branches](<boards/42.png>) | Ringing alarm → QR / NFC; task setup → permissions |
| 43 | [Empty, search and loading states](<boards/43-v2.png>) | Report / Settings search / sensor details |

## Navigation contract

- Four persistent root destinations: Tonight, Alarms, Report and Settings. Sound library, plans, naps and guard are reached from Tonight or their Settings group; they do not add another permanent tab.
- Detail pages replace the root content and use Back. A sheet belongs to its visible parent; closing returns to that parent and retains scroll and draft state. Do not show a selected Tonight tab on a nested alarm editor.
- Final Save alarm commits the entire alarm draft. Selecting a sound, editing a routine, changing snooze or changing a schedule updates that draft until final Save. Preview and quiet tests never save, start an actual alarm or start overnight recording.
- A selected sound has a radio selection; its preview has a separate play/pause action. Playback can continue while browsing. Only one preview at a time. An unavailable or expired file keeps metadata readable and offers a replacement/retry.
- Save automatic tracking settings and Arm tonight are separate actions. Cancel wait disarms that wait. Opening the settings page starts no sensors or outputs. Paused recording resumes the same session and preserves an explicit gap.
- A denied optional permission preserves the rest of the app. Microphone permission is requested only after sound is chosen. Analysis and saved clips are separate choices. Canceling a native picker preserves existing state.
- QR, NFC and camera enrollment is performed in setup/practice. Active challenges use the enrolled target. Successful tasks advance only to the next configured task/round; tasks shown beside each other on a board are alternatives, not mandatory steps.
- An active alarm retains emergency access and a configured fallback. System controls remain reachable. There is no claim of defeating shutdown, force-stop or uninstall.
- Missing nights and unavailable sensors remain unknown. No invented REM/light/deep stages, oxygen, heart-rate or clinical score appears as measured data. Charts describe recorded duration and actual source signals.
- Destructive actions name their scope. Audio-only cleanup preserves night records; restore shows a review before writing; imported alarms start disabled. Research sharing is a proposed explicit export review, not an active hosted service.

## Screen-by-screen routes

### 01 — First night

Entry: Settings / Reliability; first launch.

[Open visual board](<boards/01.png>)

| State | What happens next |
| --- | --- |
| 01A | Set up my night →01B; Explore first →02A |
| 01B | Review Notifications →01C; Continue →02A with readiness status retained |
| 01C | Continue →Android notifications →01B if granted,01D if denied; Not now →01B |
| 01D | Open settings →Android →01B; Use app for now →02A |

### 02 — Plan tonight

Entry: Tonight.

[Open visual board](<boards/02.png>)

| State | What happens next |
| --- | --- |
| 02A | Bedtime handle →02B; Wake handle/alarm →02C; rain →18A; Begin night →18B; tabs →04A/26A/32A |
| 02B | Save bedtime →02A, not automatic alarm edit; Cancel/Back →02A |
| 02C | Edit alarm →04B with existing draft; Use alarm commits reviewed choice →02A; Cancel →02A |
| 02D | Add wake-up →04B; More →29A/30A/32D; Begin night →18B without creating an alarm |

### 03 — Wind down and guard

Entry: Tonight → Settle in → Bedtime guard.

[Open visual board](<boards/03.png>)

| State | What happens next |
| --- | --- |
| 03A | Reminder/bedtime time rows use02B-style selector; Apps →03B; Save →parent |
| 03B | Save selected apps →03A; Back restores selection draft |
| 03C | Continue →Android Accessibility →03A with actual access; Not now →03A access missing |
| 03D | Pause five minutes →paused variation; Resume early →active guard; Return →02A |

### 04 — Create and manage alarms

Entry: Alarms tab.

[Open visual board](<boards/04.png>)

| State | What happens next |
| --- | --- |
| 04A | New alarm →04B; existing row →04B existing; row More →04D |
| 04B | Repeat →04C; Sound →06A; Challenges →08A; Snooze →07A; More →05A/07B/07C; Save →04A |
| 04C | Use repeat →04B draft; Cancel/Back →04B unchanged |
| 04D | Edit →04B; Skip →04A skipped+Undo; Duplicate →04B copy; Delete →41C confirmation |

### 05 — Dates and smart waking

Entry: Alarms → Daybreak → Schedule.

[Open visual board](<boards/05-v2.png>)

| State | What happens next |
| --- | --- |
| 05A | Next only →05B; Smart wake →05C; Relative/Speak/Sunrise →05D; Calendar →31D |
| 05B | Save once →05A draft with reset date; Clear override →05A normal recurrence; Cancel →05A unchanged |
| 05C | Save →04B draft; sensitivity →22B/choice; default-follow restores inheritance; fixed deadline stays |
| 05D | Apply reviewed time →05A draft; Cancel →05A; spoken entry requires reviewed transcript |

### 06 — Tone and gentle wake

Entry: Alarm editor → Sound.

[Open visual board](<boards/06.png>)

| State | What happens next |
| --- | --- |
| 06A | Preview changes preview only; select row changes draft; Gentle →06B; missing file →06D; Use sound →04B |
| 06B | Timing row →06C; quiet sample starts only after tap, Stop ends; Save →06A draft |
| 06C | Use duration →06B draft; Cancel →06B old value |
| 06D | Choose file →Android document picker →06A; fallback Daybreak →06A; final Save alarm at04B |

### 07 — Snooze, backup, input

Entry: Alarm editor → Snooze / Backup.

[Open visual board](<boards/07.png>)

| State | What happens next |
| --- | --- |
| 07A | Duration chooser →07A open; Done returns underlying page; Save →04B |
| 07B | Wake check →07D; Save options →04B draft |
| 07C | Hardware key selector →07C open; Use →same page draft; Back →actual invoking07A/04B |
| 07D | Save →07B draft; Preview is explicitly non-alarm; Save defaults affects new alarms only |

### 08 — Build a wake routine

Entry: Alarm editor → Wake challenges.

[Open visual board](<boards/08.png>)

| State | What happens next |
| --- | --- |
| 08A | Add task →08B; task row →configuration; Practice →selected10–16 state; Save →04B draft |
| 08B | Math →08C; QR/NFC →09A; other task →matching10–16 configuration/practice |
| 08C | Preview →10A practice; Use task →08A draft; Cancel →08A unchanged |
| 08D | Use random →08A draft; Discard →08A old; Keep editing stays08D |

### 09 — Enroll morning codes

Entry: Wake challenges → QR / NFC.

[Open visual board](<boards/09.png>)

| State | What happens next |
| --- | --- |
| 09A | Enroll QR →09B; NFC →09C; printable code →Android print/share review |
| 09B | Use code →09D; denied camera →Android settings or08B alternative; Back →09A |
| 09C | Save enrolled tag →09D; Retry repeats reader admission; unavailable hardware →08B |
| 09D | Test →practice; Rename returns09D; Remove →09D confirmation; Remove and set Math →08A draft |

### 10 — Screen challenges I

Entry: Routine → Practice; actual alarm uses same task geometry.

[Open visual board](<boards/10.png>)

| State | What happens next |
| --- | --- |
| 10A | Correct →next configured round/task; wrong stays10A; Emergency →16C |
| 10B | Matched pair stays10B until all; complete →next configured task; Emergency →16C |
| 10C | Watch →input; correct →next configured task; mismatch restarts same round; Emergency →16C |
| 10D | Check exact target →next task; mismatch stays10D; Emergency →16C |

### 11 — Screen challenges II

Entry: Task catalog → Screen.

[Open visual board](<boards/11.png>)

| State | What happens next |
| --- | --- |
| 11A | Correct sleeping target →next configured task; wrong remains11A |
| 11B | Correct count →next task; wrong →same11B inline feedback |
| 11C | Correct flag →next task; wrong →same11C |
| 11D | Target reached →next task; Recenter →same task; no sensor →10A fallback |

### 12 — Writing and light tasks

Entry: Task catalog → Screen / Sensors.

[Open visual board](<boards/12.png>)

| State | What happens next |
| --- | --- |
| 12A | Correct typed unmirrored target →next task; mismatch →same12A |
| 12B | Save at minimum characters →next task and journal; short entry stays12B |
| 12C | Correct code →next code then next task; mismatch stays12C |
| 12D | Light target met →next task; unavailable →10A; no collecting after leaving |

### 13 — Movement challenges

Entry: Task catalog → Movement.

[Open visual board](<boards/13.png>)

| State | What happens next |
| --- | --- |
| 13A | Progress target →next task; Emergency →16C |
| 13B | Activity permission →Android →13B; target reached and listener stopped →next task; unavailable →10A |
| 13C | Complete target →next task; alternative →10A; Emergency →16C |
| 13D | One continuous measured turn →next task; unavailable →10A; emergency route16C |

### 14 — Camera challenges

Entry: Task catalog → Camera.

[Open visual board](<boards/14.png>)

| State | What happens next |
| --- | --- |
| 14A | Enroll/retake reference in practice →14A setup; Compare match →next task; no match retry |
| 14B | One explicit configured object match →next task; Retry →14B; Use Math →10A |
| 14C | Sustained valid smile →next task; missing face retry; denied camera →settings or10A |
| 14D | Valid hands-raised target →next task; denied access →settings/10A; no override by merely viewing preview |

### 15 — Sound challenges

Entry: Task catalog → Sound.

[Open visual board](<boards/15.png>)

| State | What happens next |
| --- | --- |
| 15A | Listen →bounded capture; enough admitted speech →next task; Stop stays; Math →10A |
| 15B | Explicit Listen →bounded offline phrase recognition; valid transcript →next task; Retry stays; unavailable10A |
| 15C | Listen and pass relative threshold →next task; Stop ends capture; Math →10A |
| 15D | Retry →new bounded estimate; sufficient accepted outcome →next task; uncertain outcome →retry or10A |

### 16 — Notes, camera light and fallback

Entry: Task catalog → other; active alarm → Emergency.

[Open visual board](<boards/16.png>)

| State | What happens next |
| --- | --- |
| 16A | Play reference only after tap; valid pitch →next task; no mic →10A |
| 16B | Relative camera-light target →next task; unavailable →10A |
| 16C | System dialer handoff; configured fallback →10A; Stop →explicit stop confirmation; cancel →same active task |
| 16D | Confirm before deadline →02A awake state; timeout →17A renewed check alarm; confirmation ends timer |

### 17 — Alarm rings to dismissal

Entry: Enabled alarm → Ringing.

[Open visual board](<boards/17.png>)

| State | What happens next |
| --- | --- |
| 17A | Start wake-up →first configured10–16 task; Snooze →17B; Emergency →16C |
| 17B | Confirm →17C chosen deadline; Keep ringing/Back →17A |
| 17C | Countdown ends →17A; End snooze now →17A; Android notification same session |
| 17D | Start wake-up →configured routine; Emergency →16C; successful routine →16D when enabled |

### 18 — Choose sounds and settle in

Entry: Tonight → rain card / Begin night.

[Open visual board](<boards/18.png>)

| State | What happens next |
| --- | --- |
| 18A | Select sound →18B; Import →Android document picker →19A; mini-player persists during browsing |
| 18B | Timer →18C; Fade →18D; Guard →03A; Sensors →20A/22A; Start →20A review →20B |
| 18C | Use timer →18B with playing-time duration; Cancel →18B old duration |
| 18D | Use fade →18B; Cancel restores old; errors →40C; play/pause changes playback, not selected source |

### 19 — Advanced audio sources

Entry: Sounds → More sound settings.

[Open visual board](<boards/19.png>)

| State | What happens next |
| --- | --- |
| 19A | Add local audio →Android picker; Save →18A; reorder/shuffle retained as draft |
| 19B | Test is explicit bounded request; error Retry stays19B; Save source →18A |
| 19C | Installed voice selection retained; Preview/Stop explicit; use →18B; no installed voice →system voice settings |
| 19D | Save →18B; denied external media access →disabled feature; Cancel old; no automatic recording/audio from menu |

### 20 — Start, pause and finish tracking

Entry: Settle in → Start tracking.

[Open visual board](<boards/20-v3.png>)

| State | What happens next |
| --- | --- |
| 20A | Start tracking →20B; choose another sensor →22A; mic review →24A |
| 20B | Pause →20C; End →20D; More →sampling/flip/delay options; no bottom nav |
| 20C | Resume →20B same session excluding pause; End →20D |
| 20D | Keep tracking →prior active/paused state; End and review →26A; discard needs40B-style confirmation |

### 21 — Automatic tracking

Entry: Settings → Sleep & bedtime → Automatic.

[Open visual board](<boards/21-v2.png>)

| State | What happens next |
| --- | --- |
| 21A | Start method →21B/21C; Save stores configuration; Arm tonight →21D after prerequisites |
| 21B | Save conditions →21A; Cancel preserves prior; Google input permission →system handoff |
| 21C | Save →21A; microphone opt-in →24A; Cancel old |
| 21D | Cancel wait →21A disarmed; valid trigger →20B; false-start →29D review; no silent rearm |

### 22 — Sensors and calibration

Entry: Settings → Sleep & bedtime → Sensors.

[Open visual board](<boards/22.png>)

| State | What happens next |
| --- | --- |
| 22A | Motion →22B; Contactless →22C; Partner/PIR →22D; Room →23A |
| 22B | Begin samples →progress →valid Apply next night →22A; invalid separation →Retry; does not alter active night |
| 22C | Start test →bounded signal; Stop closes capture; weak echo →22A movement; denied mic →40D |
| 22D | Connect →permission/peer handoff →status; Test explicit; disconnect invalidates source; unavailable →22A |

### 23 — Room, awake and experimental signals

Entry: Settings → Sleep & bedtime → Room conditions.

[Open visual board](<boards/23.png>)

| State | What happens next |
| --- | --- |
| 23A | Optional microphone preview →consent →live relative level; Stop on exit; dim preview explicit |
| 23B | Save triggers →parent; review estimated awake interval →26D; original readings retained |
| 23C | Start bounded experiment explicit; low quality/insufficient →same empty state; choose movement →22A |
| 23D | Back →23A/26A actual parent; unavailable metrics stay unavailable, not synthetic graphs |

### 24 — Sound analysis and recordings

Entry: Settings → Sound & recordings.

[Open visual board](<boards/24.png>)

| State | What happens next |
| --- | --- |
| 24A | Enable analysis →Android mic consent if needed; clips separate opt-in; Categories →24B; Continue movement →20A |
| 24B | Save →24A; Cancel restores; category duration changes next night config |
| 24C | Play pauses previous; Favorite stays24C; correct →26C; Export →36B audio; expired branch40C |
| 24D | Delete recordings only →named count confirmation; confirmed removes clips not nights; preserve favorites policy explicit |

### 25 — Gentle cues and dream tools

Entry: Settings → Sound & recordings → Cues.

[Open visual board](<boards/25.png>)

| State | What happens next |
| --- | --- |
| 25A | Choose cue →25B; Save stores only; browsing never outputs |
| 25B | Preview tap →bounded cue; Stop ends; headphones-only disconnected gives no output; Use →25A |
| 25C | Save cue window →parent; preview explicit; no REM trigger authority |
| 25D | Choose file →Android; Analyze →progress/cancel →estimated outcomes; Retry stays; never auto-run on night start |

### 26 — Review a recorded night

Entry: Report tab → latest night.

[Open visual board](<boards/26.png>)

| State | What happens next |
| --- | --- |
| 26A | Graph →26B; events →26C; Edit →26D; History →27A; Trends →28A; check-in Save stays26A |
| 26B | Choose plotted metric keeps night; select point shows tooltip; Back →26A |
| 26C | Correct label →sheet →Save →26A event; Play/Favorite stays; export36B |
| 26D | Save changes →26A; Back dirty →40B discard modal; Restore original →41D review |

### 27 — History, manual records and import

Entry: Report → History.

[Open visual board](<boards/27.png>)

| State | What happens next |
| --- | --- |
| 27A | Calendar →27B; Add missed night →27C; Import →27D; result tap →26A |
| 27B | Open existing date →26A; empty date Add period →27C; cancel27A |
| 27C | Save manual period →27A →26A labeled Manual; invalid bounds inline; Cancel27A |
| 27D | Choose file system →preview; Import selected →27A provenance retained; invalid file stays Retry; Cancel27A |

### 28 — Trends, rhythm and context

Entry: Report → Trends.

[Open visual board](<boards/28.png>)

| State | What happens next |
| --- | --- |
| 28A | Week/Month/Year changes same plot; point →26A; Rhythm →28B; tags →28C |
| 28B | Coverageinsufficient stays unknown; daypoint26A; Chronotype →28D |
| 28C | Tag group →27A filtered; quantity →41A; rename/delete →41B; night records retained |
| 28D | Tip →underlying observed nights; insufficient history stays unknown; Back28A |

### 29 — Sleep plan, bank and milestones

Entry: Tonight → More → Sleep plan.

[Open visual board](<boards/29.png>)

| State | What happens next |
| --- | --- |
| 29A | Save plan →02A; linked alarm changes require05A/04B review |
| 29B | Start goal →29C; Cancel29A |
| 29C | Stop goal →confirmation →stopped history; Keep goal returns; missing date27C |
| 29D | Milestone →contributing nights27A; estimate Use/Edit→27C manual provenance; Discard→27A missing |

### 30 — Naps and timers

Entry: Tonight More → Naps; Alarms More.

[Open visual board](<boards/30.png>)

| State | What happens next |
| --- | --- |
| 30A | Options →06A/08A using nap draft; Start →30C; power →30B |
| 30B | Start power nap →30C fixed deadline preserved; Cancel30A |
| 30C | Pause/Resume same timer; End →confirmation→26A if tracked; expiry17A nap alarm |
| 30D | Save schedule →30A; Timer tab starts separate exact-second countdown; Cancel restores |

### 31 — Travel and calendar schedules

Entry: Settings → Sleep & bedtime → Travel.

[Open visual board](<boards/31.png>)

| State | What happens next |
| --- | --- |
| 31A | Choose timezones/dates →31B |
| 31B | Review →31C; Save trip does not silently change alarm |
| 31C | Apply once →05B reviewed override; Keep current →31B |
| 31D | Grant calendar access →Android →31D; preview and Save →05A/parent; deny allows manual dates |

### 32 — Find and personalize

Entry: Settings root.

[Open visual board](<boards/32-v2.png>)

| State | What happens next |
| --- | --- |
| 32A | Tap group →its family entry; search →32B; bottomnav roots preserved |
| 32B | Result →real setting with breadcrumb; Back restores query; alarm target choice required |
| 32C | Save →32A; preview changes draft; Cancel old; highcontrast/text/motion settings test individually |
| 32D | Save layout →02A; Reset shows review; system language unchanged until explicit OS handoff |

### 33 — Reliability and alarm protection

Entry: Settings → Reliability / Alarms → Protection.

[Open visual board](<boards/33.png>)

| State | What happens next |
| --- | --- |
| 33A | Review →system setting →33A actual status; Quiet test explicit Start/Stop |
| 33B | Saved choices separate from granted access; explanation →33C; removable admin optional |
| 33C | Continue →Android →33B updated status; Not now →33B missing; test disabled |
| 33D | Retry →33A/33B; remove admin viaAndroid; diagnose opens local detail; no guaranteed bypass prevention |

### 34 — Health and calendar exchange

Entry: Settings → Connected apps.

[Open visual board](<boards/34.png>)

| State | What happens next |
| --- | --- |
| 34A | Health →34B; Calendar →31D/34D; Lights/Automations →35A/35B; paired22D |
| 34B | Continue →Android permission →34C; Not now34A; Manage access system; Disconnect confirmation |
| 34C | Import selected →27A provenance; duplicates skip; remove SleepWell writes named review |
| 34D | Insert once →system/provider exchange result; Future export separate opt-in →review; deny →manual export36B |

### 35 — Lights and automation

Entry: Settings → Connected apps → Automations.

[Open visual board](<boards/35.png>)

| State | What happens next |
| --- | --- |
| 35A | Connect/Test explicit; Save config →34A; fail remains retry; choosing not connected never says successful |
| 35B | Add →35C; existing row edit35C; test explicit; delete named confirm |
| 35C | Event and destination choices preserve draft; Test →result; Save →35B; Cancel old |
| 35D | Test broker explicit; Save35B; permission review commands →Allow/Reject; revoke →38C |

### 36 — Local data, export and restore

Entry: Settings → Data & privacy.

[Open visual board](<boards/36.png>)

| State | What happens next |
| --- | --- |
| 36A | Export →36B; Restore →36C; Recordings24D; Erase →36D; Folder →37A |
| 36B | Choose destination →Android picker →receipt36A; cancel returns no receipt; format scoped |
| 36C | Restore confirmation →transaction/result →36A; invalid file retry; imported alarms disabled |
| 36D | Confirm selected destructive scope →36A outcome; Cancel returns; All local data requires explicit final confirmation |

### 37 — Cloud versions, profiles and conflicts

Entry: Data & privacy → Backup folder.

[Open visual board](<boards/37.png>)

| State | What happens next |
| --- | --- |
| 37A | Choose folder →Android; enable only after confirmed folder; Backup now →37B |
| 37B | Select version →36C preview; offline Retry; no restore on row browsing |
| 37C | Review choice →36C diff →explicit Restore; cancel37B; no silent last-write-wins |
| 37D | Export selected →Android; Import profile review →Apply selected →32A; no grants/secrets imported |

### 38 — Imported signals, sharing and access

Entry: Report source details / Data & privacy.

[Open visual board](<boards/38-v2.png>)

| State | What happens next |
| --- | --- |
| 38A | View originals →source chart; Exact export →36B unchanged values; Back26A |
| 38B | Preview →Share system chooser only aftertap; Cancel26A; notes opt-in separate |
| 38C | Allow once →20A sensor review; Reject stays; revoke named confirmation |
| 38D | Review fields →export preview only; no upload without destination+explicit confirmation; unsupported39D |

### 39 — Home shortcuts and system handoffs

Entry: Settings → Home screen.

[Open visual board](<boards/39-v2.png>)

| State | What happens next |
| --- | --- |
| 39A | Add widget →Android launcher; preview size stays; tap widget routes02A/20A |
| 39B | Add shortcut →Android; Start tracking shortcut →20A consent review |
| 39C | Save request →04A saved alarm; Reject →source with no writes; voiceparse must be reviewed |
| 39D | Choose supported alternative →34A/36B/05A; Back realparent; largetext controls maintain order |

### 40 — Recovery and details across flows

Entry: Any feature → failure / Back / menu.

[Open visual board](<boards/40-v2.png>)

| State | What happens next |
| --- | --- |
| 40A | Review retained →26A; Restart →20A new session; discard →named confirmation |
| 40B | Save →04A/26A invoking editor; Discard →unchanged parent; Keep editing →draft |
| 40C | Retry →same source; choose file →Android; expired event remains without play |
| 40D | Settings →Android →20A; Movement only →20A; empty Report is43A |

### 41 — Tags, confirmation and custom values

Entry: Report editor / Alarm editor / custom selector.

[Open visual board](<boards/41.png>)

| State | What happens next |
| --- | --- |
| 41A | Use quantity →26D draft; Cancel old; global tag list returns parent |
| 41B | Save rename updates tag identity; delete named tag only after confirmation; Cancel old |
| 41C | Confirm deletion →04A removed + Undo; Cancel restores04D; Undo restores alarm without duplicate |
| 41D | Restore reviewed original →26A; Keep edits →26D; no original measurement deletion |

### 42 — Physical challenge and permission branches

Entry: Ringing alarm → QR / NFC; task setup → permissions.

[Open visual board](<boards/42.png>)

| State | What happens next |
| --- | --- |
| 42A | Recognized enrolled QR →next configured task or16D; retry stays42A; fallback16C |
| 42B | Correct enrolled tag →next configured task or16D; wrong tag stays; Math10A |
| 42C | Open settings →Android →practice retry; Choose alternative08B; no camera started on denial |
| 42D | Review voice settings →Android →retry readiness; Math10A; Cancel08A; no off-device speech implied |

### 43 — Empty, search and loading states

Entry: Report / Settings search / sensor details.

[Open visual board](<boards/43-v2.png>)

| State | What happens next |
| --- | --- |
| 43A | Start tonight →02A; Add manual →27C; History empty list remains accessible; no fake metric |
| 43B | Clear query →32A categories; Browse group →its entry; Back restores prior settings |
| 43C | Admitted data →22B diagnostic; cancel stops test and returns22A; unavailable sensor →22A alternative |
| 43D | Retry refresh same26A record; Back retains last-good value; no duplicate night or stale overwrite |

## All 28 challenge types

| Challenge | Interface state |
| --- | --- |
| Math | [10A](<boards/10.png>) |
| Memory | [10B](<boards/10.png>) |
| Sequence | [10C](<boards/10.png>) |
| Retype | [10D](<boards/10.png>) |
| Shake | [13A](<boards/13.png>) |
| Walk | [13B](<boards/13.png>) |
| NFC | [42B](<boards/42.png>) |
| QR code | [42A](<boards/42.png>) |
| Visual search | [11A](<boards/11.png>) |
| Counting | [11B](<boards/11.png>) |
| Tilt maze | [11D](<boards/11.png>) |
| Flags | [11C](<boards/11.png>) |
| Mirror text | [12A](<boards/12.png>) |
| Dream journal | [12B](<boards/12.png>) |
| Speak | [15A](<boards/15.png>) |
| Laughter | [15D](<boards/15.png>) |
| Loud sound | [15C](<boards/15.png>) |
| Picture match | [14A](<boards/14.png>) |
| Jumping | [13C](<boards/13.png>) |
| Smile | [14C](<boards/14.png>) |
| Pose | [14D](<boards/14.png>) |
| Light | [12D](<boards/12.png>) |
| Type code | [12C](<boards/12.png>) |
| Camera light | [16B](<boards/16.png>) |
| Spin around | [13D](<boards/13.png>) |
| Musical note | [16A](<boards/16.png>) |
| Object recognition | [14B](<boards/14.png>) |
| Spoken phrase | [15B](<boards/15.png>) |

## Files and scope

[172-feature coverage](<FEATURE-COVERAGE.md>) preserves source status separately from design coverage. [Visual review and remaining polish](<REVIEW.md>) records limitations. Generation prompts and machine-readable navigation are saved alongside the boards. No app source, dependencies, installed build or user data changed.



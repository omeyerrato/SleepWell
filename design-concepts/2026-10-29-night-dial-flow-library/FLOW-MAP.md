# SleepWell — navigation and flow map

This map specifies intended routes between the visual states. Board captions are illustrative; the route tables in START-HERE.md govern Save, Back and alternative branches.

## Everyday night

```mermaid
flowchart LR
  A["02A Tonight"] -->|Begin night| B["18B Settle in"]
  A -->|Rain card| C["18A Sound library"]
  C -->|Open mix| B
  B -->|Timer| T["18C Timer sheet"]
  T -->|Use / Cancel| B
  B -->|Fade| F["18D Fade sheet"]
  F -->|Use / Cancel| B
  B -->|Start tracking| R["20A Sensor review"]
  R -->|Start| L["20B Tracking"]
  R -->|Optional sound access denied| D["40D Recovery"]
  D -->|Movement only| R
  L -->|Pause| P["20C Paused"]
  P -->|Resume| L
  L -->|End| E["20D End confirmation"]
  P -->|End| E
  E -->|Keep tracking| L
  E -->|End and review| N["26A Your night"]
  N -->|History| H["27A History"]
  N -->|Trends| V["28A Trends"]
```

If End was opened while paused, Keep tracking returns to the paused state rather than silently resuming. The state table preserves that branch.

## Build an alarm and get up

```mermaid
flowchart TD
  A["04A Alarms"] -->|New / Edit| B["04B Alarm draft"]
  B -->|Repeat| C["04C Repeat sheet"]
  B -->|Schedule| S["05A Schedule and dates"]
  B -->|Sound| D["06A Sound"]
  D -->|Gentle wake| G["06B Gentle options"]
  B -->|Snooze / Backup| Z["07A–D Options"]
  B -->|Challenges| R["08A Routine"]
  R -->|Add task| K["08B Task catalog"]
  K -->|Configure Math| M["08C Math options"]
  K -->|Enroll QR / NFC| Q["09A–D Enrollment"]
  K -->|Practice| PR["10–16 Task interfaces"]
  B -->|Save alarm| A
  A -->|Deadline| X["17A Ringing"]
  X -->|Snooze| Y["17B Snooze sheet"]
  Y -->|Confirm| W["17C Snoozed"]
  W -->|Deadline / End snooze| X
  X -->|Start wake-up| CH["Configured task"]
  CH -->|QR| QR["42A Active QR"]
  CH -->|NFC| NFC["42B Active NFC"]
  CH -->|Emergency / Fallback| EM["16C Emergency options"]
  EM -->|Configured Math fallback| MT["10A Math"]
  CH -->|All configured tasks completed| WC["16D Wake-up check"]
  WC -->|I am awake| TN["02A Tonight"]
```

A child Save/Use updates the alarm draft. Final Save alarm commits it. Practice is a separate non-alarm mode. An ignored main alarm can independently reach 17D backup ringing.

## Find advanced features

```mermaid
flowchart LR
  S["32A Settings"] --> A["Alarms: 04–09, 33"]
  S --> B["Sleep and bedtime: 03, 20–23, 29–31"]
  S --> C["Sound and recordings: 18–19, 24–25"]
  S --> D["Appearance and feedback: 32C–D"]
  S --> E["Reliability: 01B–D, 33"]
  S --> F["Connected apps: 22D, 34–35, 38C"]
  S --> G["Data and privacy: 36–38"]
  S --> H["Home screen: 39A–B"]
  S --> Q["32B Search"]
  Q --> R["Real feature destination with parent breadcrumb"]
```

## Review, corrections and data

- Your night 26A → graph 26B / sound event 26C / edit 26D.
- Edit 26D → tags/quantity 41A–B; Save returns to the same night; dirty Back opens 40B.
- Restore original 41D shows a comparison before changing the displayed edits.
- History 27A → calendar 27B / manual period 27C / import review 27D.
- Trends 28A → rhythm 28B / tag comparison 28C / chronotype availability 28D.
- Privacy 36A → export review 36B / restore review 36C / erase scope 36D.
- Backup folder 37A → versions 37B → conflict review 37C → restore preview 36C.
- Share 38B opens the Android chooser only after review. Research sample 38D exports a file; it does not establish a hosted research service.
- Recovery 40A/C/D and empty/loading states 43A–D return to their actual invoking parent.



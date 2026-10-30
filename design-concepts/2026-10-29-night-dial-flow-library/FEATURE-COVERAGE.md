# Feature-to-flow coverage

Every source inventory entry has a design destination. SourceStatus is the status declared in the repository, not new implementation or runtime verification. Partial, missing and blocked entries remain conceptual or show availability/recovery states.

| ID | Feature | Source status | Design boards |
| --- | --- | --- | --- |
| F001 | Multiple alarms | implemented | [04](<boards/04.png>) |
| F002 | Repeating weekday alarms | implemented | [04](<boards/04.png>) |
| F003 | Per-alarm configuration | implemented | [04](<boards/04.png>) |
| F004 | Smart wake-up | partial | [05](<boards/05-v2.png>) |
| F005 | Do not wake before duration goal | implemented | [02](<boards/02.png>), [05](<boards/05-v2.png>), [29](<boards/29.png>) |
| F006 | Movement sensitivity | partial | [05](<boards/05-v2.png>), [22](<boards/22.png>) |
| F007 | Backup alarm | implemented | [07](<boards/07.png>), [17](<boards/17.png>) |
| F008 | Gentle volume ramp | partial | [06](<boards/06.png>) |
| F009 | Extra-loud mode | partial | [06](<boards/06.png>) |
| F010 | Vibration | implemented | [06](<boards/06.png>) |
| F011 | Built-in sounds | partial | [06](<boards/06.png>) |
| F012 | Device ringtone | partial | [06](<boards/06.png>) |
| F013 | Local songs | implemented | [06](<boards/06.png>) |
| F014 | Playlists / shuffle / silent alarm | partial | [06](<boards/06.png>), [19](<boards/19.png>) |
| F015 | Internet radio / Spotify | partial | [06](<boards/06.png>), [19](<boards/19.png>) |
| F016 | Sunrise alarm | partial | [06](<boards/06.png>) |
| F017 | Snooze duration and count | implemented | [07](<boards/07.png>), [17](<boards/17.png>) |
| F018 | Decreasing snooze / total snooze cap | partial | [07](<boards/07.png>), [17](<boards/17.png>) |
| F019 | Post-dismiss wake check | partial | [07](<boards/07.png>), [17](<boards/17.png>) |
| F020 | Challenge difficulty / rounds | implemented | [08](<boards/08.png>) |
| F021 | Math | partial | [10](<boards/10.png>) |
| F022 | Retype | implemented | [10](<boards/10.png>) |
| F023 | Memory pairs | implemented | [10](<boards/10.png>) |
| F024 | Sequence | implemented | [10](<boards/10.png>) |
| F025 | Shake | implemented | [13](<boards/13.png>) |
| F026 | NFC | implemented | [09](<boards/09.png>), [42](<boards/42.png>) |
| F027 | QR/barcode sequences | partial | [09](<boards/09.png>), [42](<boards/42.png>) |
| F028 | Object/picture task | partial | [14](<boards/14.png>) |
| F029 | Smile task | partial | [14](<boards/14.png>) |
| F030 | Pose task | partial | [14](<boards/14.png>) |
| F031 | Other CAPTCHA pack tasks | partial | [11](<boards/11.png>) |
| F032 | Prevent leaving the alarm | partial | [01](<boards/01.png>), [16](<boards/16.png>), [33](<boards/33.png>) |
| F033 | Prevent uninstall / shutdown | partial | [01](<boards/01.png>), [16](<boards/16.png>), [33](<boards/33.png>) |
| F034 | Lock-screen/full-screen alarm | implemented | [01](<boards/01.png>), [17](<boards/17.png>), [33](<boards/33.png>) |
| F035 | Nap timer | partial | [30](<boards/30.png>) |
| F036 | Power nap | partial | [30](<boards/30.png>) |
| F037 | Polyphasic routine | partial | [30](<boards/30.png>) |
| F038 | Voice time entry / alternate pickers | partial | [05](<boards/05-v2.png>), [39](<boards/39-v2.png>) |
| F039 | Alarm-only layout / skip occurrence / holiday skipping | partial | [04](<boards/04.png>), [39](<boards/39-v2.png>) |
| F040 | Bedtime reminder | partial | [02](<boards/02.png>), [03](<boards/03.png>) |
| F041 | Smart bedtime | partial | [02](<boards/02.png>), [03](<boards/03.png>) |
| F042 | Anti-scrolling guard | partial | [03](<boards/03.png>) |
| F043 | Manual sleep tracking | implemented | [20](<boards/20-v3.png>), [40](<boards/40-v2.png>) |
| F044 | Automatic tracking | partial | [21](<boards/21-v2.png>), [40](<boards/40-v2.png>) |
| F045 | Missing-night estimation | partial | [21](<boards/21-v2.png>), [27](<boards/27.png>) |
| F046 | Accelerometer tracking | implemented | [20](<boards/20-v3.png>) |
| F047 | Contactless sonar | partial | [22](<boards/22.png>) |
| F048 | PIR tracking | partial | [22](<boards/22.png>) |
| F049 | Sleep-phase estimation | missing | [23](<boards/23.png>) |
| F050 | Awake detection | partial | [23](<boards/23.png>) |
| F051 | Light levels | implemented | [23](<boards/23.png>) |
| F052 | Breathing rate | partial | [23](<boards/23.png>) |
| F053 | Noise amplitude | implemented | [24](<boards/24.png>) |
| F054 | Triggered sound recordings | implemented | [24](<boards/24.png>) |
| F055 | Automatic sound labels | partial | [24](<boards/24.png>) |
| F056 | Noise threshold | implemented | [24](<boards/24.png>) |
| F057 | Noise library | partial | [24](<boards/24.png>), [40](<boards/40-v2.png>) |
| F058 | Retention / favorite album / alternate formats | partial | [24](<boards/24.png>), [36](<boards/36.png>) |
| F059 | Anti-snoring cues | partial | [25](<boards/25.png>) |
| F060 | Pair tracking | partial | [22](<boards/22.png>) |
| F061 | Sleep sounds / lullabies | partial | [18](<boards/18.png>) |
| F062 | Binaural beats / meditation | partial | [19](<boards/19.png>) |
| F063 | Lucid dreaming | partial | [25](<boards/25.png>) |
| F064 | Jet-lag light intervention | partial | [31](<boards/31.png>) |
| F065 | DND / airplane / screen dimming | partial | [03](<boards/03.png>) |
| F066 | Tracking pause / standby / battery-adaptive sampling | partial | [20](<boards/20-v3.png>) |
| F067 | Sensor diagnostics | partial | [22](<boards/22.png>), [33](<boards/33.png>), [43](<boards/43-v2.png>) |
| F068 | Night graphs | partial | [26](<boards/26.png>) |
| F069 | History | implemented | [27](<boards/27.png>), [40](<boards/40-v2.png>), [43](<boards/43-v2.png>) |
| F070 | Manual journal / tags / ratings | implemented | [26](<boards/26.png>), [41](<boards/41.png>) |
| F071 | Graph editing / manual records | implemented | [26](<boards/26.png>), [27](<boards/27.png>), [41](<boards/41.png>) |
| F072 | Duration goal | partial | [02](<boards/02.png>), [29](<boards/29.png>) |
| F073 | Multi-metric sleep score | partial | [23](<boards/23.png>) |
| F074 | Statistics | partial | [28](<boards/28.png>) |
| F075 | Duration/deficit/regularity charts | partial | [28](<boards/28.png>) |
| F076 | Chronotype / social jetlag / SRI | partial | [28](<boards/28.png>) |
| F077 | Tag impact analysis | implemented | [28](<boards/28.png>) |
| F078 | Personalized advice | partial | [28](<boards/28.png>) |
| F079 | Achievements | implemented | [29](<boards/29.png>) |
| F080 | Goal bank / wagers | implemented | [29](<boards/29.png>) |
| F081 | Dashboard / tabs | partial | [02](<boards/02.png>), [32](<boards/32-v2.png>) |
| F082 | Global search | partial | [32](<boards/32-v2.png>), [43](<boards/43-v2.png>) |
| F083 | Home widgets / shortcuts | partial | [39](<boards/39-v2.png>) |
| F084 | Localization / accessibility | partial | [01](<boards/01.png>), [32](<boards/32-v2.png>), [39](<boards/39-v2.png>), [40](<boards/40-v2.png>), [42](<boards/42.png>), [43](<boards/43-v2.png>) |
| F085 | Sleep Phaser PIR lamp | blocked | [22](<boards/22.png>) |
| F086 | Hue / IKEA smart lights | partial | [35](<boards/35.png>) |
| F087 | Health Connect | partial | [34](<boards/34.png>) |
| F088 | Google Fit / Samsung Health | blocked | [34](<boards/34.png>), [39](<boards/39-v2.png>) |
| F089 | Local data backup | partial | [36](<boards/36.png>) |
| F090 | Cloud backup / SleepCloud | partial | [37](<boards/37.png>) |
| F091 | Calendar | partial | [31](<boards/31.png>), [34](<boards/34.png>) |
| F092 | Tasker / Automate / Intent API | partial | [35](<boards/35.png>) |
| F093 | Custom webhooks | partial | [35](<boards/35.png>) |
| F094 | MQTT | partial | [35](<boards/35.png>) |
| F095 | IFTTT | partial | [35](<boards/35.png>) |
| F096 | Assistant voice actions | partial | [35](<boards/35.png>), [39](<boards/39-v2.png>) |
| F097 | Content provider / CSV compatibility | partial | [36](<boards/36.png>), [38](<boards/38-v2.png>) |
| F098 | Community research sharing | partial | [38](<boards/38-v2.png>) |
| F099 | Reusable alarm defaults | implemented | [04](<boards/04.png>), [07](<boards/07.png>) |
| F100 | Separate sound/vibration delay | partial | [06](<boards/06.png>) |
| F101 | Flip snooze / hardware-key controls | partial | [07](<boards/07.png>) |
| F102 | Backup duration / repeat / timeout | partial | [07](<boards/07.png>) |
| F103 | Date / sunrise / goal-derived alarm | partial | [02](<boards/02.png>), [05](<boards/05-v2.png>), [31](<boards/31.png>) |
| F104 | Skip and restore next occurrence | partial | [04](<boards/04.png>), [05](<boards/05-v2.png>) |
| F105 | Dynamic snooze choices / until deadline | partial | [07](<boards/07.png>), [17](<boards/17.png>) |
| F106 | Wake-check prompt / confirmation deadline / retries | partial | [07](<boards/07.png>), [16](<boards/16.png>), [17](<boards/17.png>) |
| F107 | Multi-task routine / reorder / per-task difficulty | partial | [08](<boards/08.png>) |
| F108 | Lost code fallback / travel bypass | partial | [09](<boards/09.png>), [16](<boards/16.png>), [42](<boards/42.png>) |
| F109 | Typed arithmetic / operators / complexity | partial | [08](<boards/08.png>), [10](<boards/10.png>) |
| F110 | Sleeping-target visual search | partial | [11](<boards/11.png>) |
| F111 | Dream diary / mirror text task | partial | [12](<boards/12.png>) |
| F112 | Laughter / instrument microphone task | partial | [15](<boards/15.png>), [16](<boards/16.png>) |
| F113 | Tilt maze / jumping / flags / counting tasks | partial | [11](<boards/11.png>), [12](<boards/12.png>), [13](<boards/13.png>) |
| F114 | Walking task | partial | [13](<boards/13.png>) |
| F115 | Pause/resume within one tracked session | partial | [20](<boards/20-v3.png>) |
| F116 | Charging condition / standby mode | partial | [21](<boards/21-v2.png>) |
| F117 | After-fall-asleep / false-start rejection | partial | [21](<boards/21-v2.png>) |
| F118 | Missed-night estimate review / provenance | partial | [21](<boards/21-v2.png>), [27](<boards/27.png>), [29](<boards/29.png>) |
| F119 | Awake triggers from interaction/light/snooze | partial | [23](<boards/23.png>) |
| F120 | Flip pause / delayed tracking start | partial | [20](<boards/20-v3.png>) |
| F121 | Phone breathing / disturbance estimates | partial | [23](<boards/23.png>) |
| F122 | Sonar calibration / signal quality / fallback | partial | [22](<boards/22.png>) |
| F123 | Per-class sound sensitivity | implemented | [24](<boards/24.png>) |
| F124 | Correct sound labels / favorites / export | partial | [24](<boards/24.png>), [26](<boards/26.png>), [36](<boards/36.png>) |
| F125 | Headphone-only cues / intensity / repeat | partial | [25](<boards/25.png>) |
| F126 | Sound mixing / gains / fade / local playlist | partial | [18](<boards/18.png>), [19](<boards/19.png>) |
| F127 | Meditation / stereo binaural generator | partial | [19](<boards/19.png>) |
| F128 | Dream and travel light cues | partial | [25](<boards/25.png>), [31](<boards/31.png>) |
| F129 | Sleep-day cutoff / naps / date filtering | implemented | [27](<boards/27.png>) |
| F130 | Tag quantities / rename / delete / impact | implemented | [28](<boards/28.png>), [41](<boards/41.png>) |
| F131 | Awake edits / trim / manual record / provenance | implemented | [26](<boards/26.png>), [41](<boards/41.png>) |
| F132 | Dated goal bank | implemented | [29](<boards/29.png>) |
| F133 | SRI / circular midpoint clustering | partial | [28](<boards/28.png>) |
| F134 | Own Intent and data-provider contracts | partial | [35](<boards/35.png>), [38](<boards/38-v2.png>) |
| F135 | Calendar holiday skip / meeting shift / record write | partial | [31](<boards/31.png>), [34](<boards/34.png>) |
| F136 | Health Connect sleep provenance / dedup / deletion | partial | [34](<boards/34.png>) |
| F137 | User-controlled cloud / conflict / audio backup | partial | [36](<boards/36.png>), [37](<boards/37.png>) |
| F138 | Tasker events / webhook retries / MQTT transport | partial | [35](<boards/35.png>) |
| F139 | Appearance options alter behavior | implemented | [32](<boards/32-v2.png>) |
| F140 | Phone flashlight wake-up | partial | [06](<boards/06.png>) |
| F141 | Recorded rhythm / social jetlag | partial | [28](<boards/28.png>) |
| F142 | Chronotype estimation | partial | [28](<boards/28.png>) |
| F143 | Motion sensitivity configuration | partial | [05](<boards/05-v2.png>), [22](<boards/22.png>) |
| F144 | Retained record date calendars | partial | [27](<boards/27.png>) |
| F145 | Speak microphone challenge | partial | [15](<boards/15.png>) |
| F146 | Odd/even ISO weeks and anchored day intervals | partial | [04](<boards/04.png>) |
| F147 | One-occurrence alarm rescheduling | partial | [05](<boards/05-v2.png>), [31](<boards/31.png>) |
| F148 | Manual wake-up sensor FIFO batching | partial | [05](<boards/05-v2.png>), [21](<boards/21-v2.png>) |
| F149 | On-device spoken-phrase challenge | partial | [12](<boards/12.png>), [15](<boards/15.png>), [42](<boards/42.png>) |
| F150 | Per-alarm orientation lock | partial | [07](<boards/07.png>) |
| F151 | Tracking-owned movement-estimate audio fade | partial | [18](<boards/18.png>) |
| F152 | Opted-in external media-stream stillness fade | partial | [19](<boards/19.png>) |
| F153 | Optional removable uninstall administrator | partial | [33](<boards/33.png>) |
| F154 | AAC interval and collection export | partial | [24](<boards/24.png>), [36](<boards/36.png>) |
| F155 | Reviewed direct calendar record insertion | partial | [34](<boards/34.png>) |
| F156 | Portable reviewed settings profiles | partial | [32](<boards/32-v2.png>), [37](<boards/37.png>) |
| F157 | Source-preserving Sleep as Android CSV append | partial | [27](<boards/27.png>), [38](<boards/38-v2.png>) |
| F158 | Configurable optional Tonight rows | partial | [02](<boards/02.png>), [32](<boards/32-v2.png>) |
| F159 | Reviewed incoming tracking Intent requests | partial | [35](<boards/35.png>), [38](<boards/38-v2.png>) |
| F160 | Confirmed outbound lifecycle event catalog | partial | [35](<boards/35.png>), [38](<boards/38-v2.png>) |
| F161 | Imported CSV source signal charts and exact export | partial | [38](<boards/38-v2.png>) |
| F162 | Opted-in future completed-night calendar export | partial | [34](<boards/34.png>) |
| F163 | Saved occurrence-frozen random challenge routines | partial | [08](<boards/08.png>) |
| F164 | Relative microphone sound-level wake task | partial | [15](<boards/15.png>) |
| F165 | Requested installed offline narration voice selection | partial | [19](<boards/19.png>) |
| F166 | Imported CSV original event annotation timeline | partial | [38](<boards/38-v2.png>) |
| F167 | Historical movement-night sound analysis outcomes | partial | [24](<boards/24.png>) |
| F168 | Inline retained Report sound-event playback and waveform | partial | [24](<boards/24.png>), [26](<boards/26.png>) |
| F169 | User priority: interaction feedback | partial | [32](<boards/32-v2.png>), [40](<boards/40-v2.png>), [43](<boards/43-v2.png>) |
| F170 | User priority: organized settings and menus | partial | [32](<boards/32-v2.png>), [40](<boards/40-v2.png>) |
| F171 | Explicit offline recorded-audio supplementary estimates | partial | [23](<boards/23.png>), [24](<boards/24.png>), [25](<boards/25.png>) |
| F172 | Foreground room light and optional sound-level preview (supplied Board10/right) | partial | [23](<boards/23.png>) |


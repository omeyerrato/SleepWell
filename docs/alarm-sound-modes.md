# Alarm sound modes

Alarm sound options now open **Sound playback**. **Single sound** preserves the existing source. **Silent** allocates no alarm player, volume lease or audible fallback, including backup escalation. Its notification is explicitly silent; vibration, lights, challenges and ringing timeout retain their own settings.

**Playlist** accepts two to eight distinct built-in, device or document sources. Import a file in Alarm sound, then add the selected file to the list. Imported names survive reordering, defaults and portable metadata. Shuffle creates one deterministic permutation per occurrence; the complete list repeats in that order.

The real adapter disables player looping and advances from MediaPlayer completion callbacks. It does not guess durations. Backup interrupts the list; primary return resumes the interrupted position from its beginning. An occurrence/policy/index checkpoint is committed on the decoder worker immediately before each track starts. Restart resumes the last durably started track from its beginning; an interrupted or just-completed track may repeat after a crash. This is not exactly-once or gapless audio. New occurrences or changed playback policies start at zero.

Each failed source is excluded for the rest of that occurrence, including later cycles and backup return, so a blocked provider is not repeatedly opened. Inaccessible sources and decoder failures skip a bounded number of entries, then use the existing Daybreak fallback. A working previous player may remain if its replacement fails. Explicit Silent never falls back to sound. Quiet test clones clear the policy. Completed/replaced player threads retire, and old callbacks cannot advance a replaced current player.

Strict optional `soundMode` JSON stores mode, source list, shuffle and imported names. Missing metadata preserves old behavior. Invalid metadata stays available for review and is rejected before save or portable replacement. Defaults carry valid modes; saved playlists and templates protect their referenced document grants. URI permission itself is not portable: restored alarms are inactive, and unavailable sources are skipped. No schema or portable-format bump.

Validation contract: `AlarmSoundModeTest` has fourteen pure cases, with all eighteen existing playback cases retained. `AlarmSoundModeStorageTest` has four private native SQLite/JSON/grant/journal methods. `AlarmSoundModeFlowTest` has one configuration method and seven originals: `alarm-mode-{initial,silent,playlist,configured,recreated,reopened,single}.png`.

Native tests compile locally and run only in parent-owned inert CI. No actual alarm components, audio or personal-phone operations are executed. Real EOF/error delivery, speaker behavior and service restart delivery remain unverified on hardware. Document-provider stalls remain bounded by the existing preparation timeout; sequential changes may have audible gaps.

Primary references: [MediaPlayer state/resources](https://developer.android.com/media/platform/mediaplayer/state-resources), [SharedPreferences commit/apply](https://developer.android.com/training/data-storage/shared-preferences).

Local validation: debug and native-test APKs, 57 focused tests and lint passed (0 errors, 125 warnings). The final two-file failed-source circuit amendment then passed all 32 direct JDK mode/playback tests; parent integration must recompile that final delta. Native settings/storage execution and all seven actual originals remain pending.

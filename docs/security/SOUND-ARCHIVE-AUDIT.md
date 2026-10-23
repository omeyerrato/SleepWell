# Sound archive security alignment

Scope: the private sound-archive Activity, retained worker, WAV reads, private staging cache and existing audio-unlink helper. No permissions, dependencies, exported components or network destinations were added. The Activity remains explicitly exported=false and receives only internal record IDs; Android's document picker returns a content URI. No incoming nested Intent is launched and no private cache FileProvider exists.

| Area | Priority | Confirmed issue | Change |
| --- | --- | --- | --- |
| Source identity | Medium | An equal-size/equal-duration WAV replacement passed the original preflight. | SHA256 pinned during preparation and verified while copying from an owned descriptor. |
| Concurrent cleanup | Medium | Validation and later pathname reopen could race deletion; holding a deletion lock during provider I/O would block cleanup. | Bounded immutable private snapshot under the deletion lock; release database/lock before provider access. Legacy unlink uses that lock too. |
| Lifecycle/idempotence | Medium | A duplicate result could reset WRITING to ERROR and enable another export. A closed job still immediately notified a newly attached observer. | Ignore out-of-phase results; reject closed observation/queued writes; cancel pending work and suppress late results. |
| Resource isolation | Medium | Stalled providers could accumulate export work/snapshots. | Single active snapshot permit,64MiB source cap,60clips,3600events and4MiB serialized metadata cap. Identity-scoped cache cleanup never traverses unrelated files. |
| Error disclosure | Low | Raw file-open errors could expose private paths in the preparation message. | Generic user-facing preparation failure, without raw provider/file exception text. |

Modified production files: NightAudioArchive, AudioArchiveWorkspace, SoundArchiveJob, ClipWave and FileSafety. No manifest or dependency change was required. Exact unified diffs are generated in the ignored session artifact `.android-local/security-alignment.md` alongside the implementation commit, as required by the Android intent-security skill.

Focused verification: NightAudioArchiveTest3 existing native methods; AudioArchiveConcurrencyTest3 new real-file/SQLite methods; SoundArchiveJobTest2 new delayed fake-work methods; SoundArchiveUiTest1 fake SAF/library flow. Native execution is pending CI. No phone, recorder, audio player, vibration, flashlight, alarm or external provider was exercised. Actual provider cancellation remains best effort; a blocked provider may remain pending until it returns or the process exits. Closing can leave a partial destination; originals remain untouched. A ZIP with a final manifest still needs a valid central directory, all listed bytes and matching hashes before any consumer treats it as complete.

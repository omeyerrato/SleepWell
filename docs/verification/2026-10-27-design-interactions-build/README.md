# Report, menus, Appearance and interaction update

[Published development APK](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-design-interactions-20261027-795ab556). Production source `795ab556f843b1570809e5bf85fd387b9ee63260`; arm64, original signer, 166,676,096 bytes. Build passed in 4m19s. The exact remote tag and both uploaded asset digests match local files: [distribution](distribution.json), [candidate](candidate.json), [build](production-build.txt), [signer](signer.txt).

Five focused, host-muted API36 emulator journeys pass seven methods. All 47 original Android screenshots were inspected individually:

| Journey | Evidence | Captures |
| --- | --- | --- |
| Report calendar, edit and spacing | [Report](../2026-10-27-report-spacing/README.md) | 11 |
| Sound events, PCM, filters and persistence | [Event library](../2026-10-27-sound-goal-reference-f1f839ce/event-library/README.md) | 9 |
| Sleep plan and restorable supporting options | [Goal plan](../2026-10-27-sleep-plan-options-795ab556/README.md) | 14 |
| Shortcut cancellation, obscured touch and calendar recovery | [Interaction/security](../2026-10-27-interaction-security-ci40517817995/README.md) | 3 |
| Appearance, theme persistence and full large-text action reach | [Appearance](../2026-10-27-appearance-layout/README.md) | 10 |

The relevant production modules are unchanged between those evidence commits and this APK. Every packaged offline asset matches the preceding installed `32f064e2` APK byte-for-byte; [asset hashes](preserved-assets.sha256.json). Sleep schema17, backup13, private automation2 and recorded-result1 are unchanged.

The phone remains `32f064e2`; this release was not installed or run there. These bounded flows do not establish all 36 reference screens, physical alarm/haptic/audio/flashlight behavior, overnight accuracy, actual hostile-overlay exploitation or external-provider exchange. Broader design and interaction/security work remains open. Editable backup sound/volume is a separate pending increment and is not in this APK.

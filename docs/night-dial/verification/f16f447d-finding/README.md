# Broader native review: f16f447d

Run [41187865479](https://github.com/omeyerrato/SleepWell/actions/runs/41187865479), job 124498349689, artifact 13037439163. ZIP SHA-256: `936db2864e7eec29869b4aef9ec8480aeebd601064a98156f60678c012e0f48f`.

Eighteen of twenty native methods passed in 512.038 seconds; 80 focused unit methods passed. There are 175 captured PNGs. All 45 secondary root destinations, 11 actual cognitive workspaces, larger-text/reduced-motion roots, nap cancellation/accessibility/recreation, and the prior fourteen journeys passed. The review remains failed and is not a delivered APK.

Two failures:

- System Back left the diary editor without its new dirty-draft review. The Activity registered predictive Back but lacked the legacy Back override used elsewhere in this app. Corrected to route both through the same existing draft gate.
- The all-task configuration fixture opened AlarmEditorActivity without its required serialized alarm draft. It finished instead of becoming a native input owner. Corrected to pass an off, unsaved alarm through the real editor entry contract.

Visual findings from the actual captures: the miniature appearance dial's internal labels crowd its center; the lamp illustration is too bright; the guide benefits from Read/Breathe modes and folded narration; the media library needs a consistent nighttime photographic treatment. Follow-up changes address these findings and keep their native controls separate from artwork. Memory backs gain their own card treatment and a short user-triggered reveal, independent of task completion.

These selected original captures and raw results preserve the findings. The full ZIP remains in the local review directory. Root coverage does not establish every permission, provider, output, lifecycle or physical-device branch.

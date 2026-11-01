# 35B named saved-rule deletion

Deletion now names the saved rule in the title and identifies its saved receiving
app or endpoint origin in the message. It does not substitute an unsaved editor
rename or URL. The immutable reviewed identity is captured during the existing
saved-rule load and retains its ID/revision through recreation. Only the safe
identity fields enter this additional saved state; endpoint paths, query tokens,
usernames and credentials are not copied into the confirmation. If the saved
destination cannot be decoded, the saved row name and explicitly unavailable
transport remain identifiable. If a usable reviewed identity is missing, deletion
requires reopening the saved rule.

The existing focused-dialog, current-ID/revision, foreground, single-operation
and retired-dialog guards remain. The delete worker now calls a revision-checked
store overload: deleting the exact reviewed rule and its owned delivery queue is
one transaction. A changed/missing revision rolls back before removing queued
events. An in-flight external request remains subject to the existing disclosure;
this change cannot recall it.

The existing native method
`AutomationStorageTest#configReopensWithoutPlaintextCredentialsInRows` now checks:

- Named saved identity despite different unsaved name, URL and credential.
- The same copy after rotation and Cancel preserving the rule.
- A revision changed after review preserving the new rule and its queued event.
- A retained stale positive-button view unable to delete a newly reviewed rule.
- Actual confirmed deletion of the current named revision and its private queue.

The existing fake scheduling port performs no delivery. Its expected count is now
five: Off, On, the original private Test queue, Save and the current Delete. The
stale Delete adds no scheduling call. All ten original screenshot names/counts
remain unchanged; no new screenshot is required for this narrow copy/authority
increment. The named text is asserted in the actual native dialog.

No build, emulator method, unit campaign or physical output ran in this worktree.
Root owns the consolidated build/lint and this existing silent native method;
source changes and authored assertions are not a passing runtime claim.

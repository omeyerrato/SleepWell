# Automation rule navigation and draft ownership

Future source increment from `ef69408258e900a50274d6f2488e859647138714` on
`codex/automation-rules-board11`. This work is separate from the frozen 0.1.5
package; no root workspace, device or release was changed. The later focused
CI execution is recorded below.

Ordered commits before this note:

- `c145f2da726867e10f8aa2c46f4fd0fda784b59d`: Activity flow and owned operations.
- `a2c72f329bbffd54b6bd511d81fa0c3c368ad309`: disable row navigation during a load.
- `ffcf88f8062ef1c0e0d93a8eb8a7b2e12ff190dc`: existing private-store method extension.
- `08e2126cafcc26cc31a0d9e809e40742e5370070`: automatic-delivery/uncertain-test copy.

## Inspected reference and concrete findings

[Board 11 right](../design-concepts/2026-10-22-phone-app/11-settings-and-integrations.png)
was opened individually. Its hierarchy puts saved rules, their real choices,
Add rule and destination routes first. The current tracked UI acceptance marks
screen 33 pending. No comparable tracked AutomationActivity original PNG was
found; historical AutomationStorageTest results are data-layer evidence, not
visual proof of this screen.

The previous Activity placed incoming-request setup ahead of Your rules, had
non-actionable destination text and long delivery paragraphs on the main,
and stacked Save/Test/Delete under the editor. Its current-type chooser
unconditionally reset endpoint, topic, username and credential even when the
user selected the same transport. A pending row load had no page-generation
check and could replace an Add draft. Header Back returned to the list while
system Back exited. Busy state and confirmations were not retained. A scheduler
exception after `store.save` was reported as a failed save despite a committed
rule; deletion failures had no result UI.

## Bounded Activity change

Only [AutomationActivity](../app/src/main/java/org/sleepwell/app/AutomationActivity.java)
changes in production. The main shows actual saved rules, stored enable/status
values, three real destination-help routes and a fixed Add rule action. Rules
remain default Off. Destination help opens an explicit editable draft and never
probes a destination or infers that a remote service is connected.

Automation options contains the explicit incoming-settings route, Delivery &
privacy and Refresh saved rules. The editor keeps fields, When, Destination
type, Enabled and primary Save. Supporting rule options retain event help,
Tasker action/extras, delivery limits, explicit Send-test review and Delete.
Long values use stacked subtitles rather than squeezing the main label.

Event/type choices are actual radio pages. Current-type reselection is a no-op;
a different type clears only destination-specific draft fields as before.
Page history, scroll positions, draft values, errors and owned confirmation
identity survive recreation. Both kinds of Back follow the same page history;
leaving the editor discards its unsaved changes. Late row reads are fenced by
page/navigation identity; Add and saved-rule controls are disabled while a
reader owns the load. A failed refresh keeps an explicit error and Refresh
route rather than presenting failed reads as an empty saved-rule list.

A package-private Activity-pinned Port delegates production store, vault,
status, scheduling and recovery to the existing platform implementations.
Accepted Save/Delete/enable/test work uses existing PlanOperations with an
application-context task and operation token. It holds no Activity. The new
owner observes the retained result; a missing result does not replay work.
A committed change followed by a scheduling exception is reported as committed
with scheduling attention required. Nothing starts merely from restoring a
page or confirmation.

Test review pins rule identity/revision and requires the currently focused
owned confirmation. The test occurrence UUID is fixed before acceptance; the
existing delivery unique key remains unchanged. Destroyed dialog callbacks
cannot queue work. A recreated pending operation observes the existing token.
If the process no longer has that result, the UI warns that a previous test may
already have been queued and requires destination review before another send.
No request is auto-sent on recreation. An in-flight external request still
cannot be recalled; no new cancellation protocol is claimed.

Credential state continues through the existing Vault encryption contract.
Plain EditText hierarchy saving is disabled; the draft's Bundle holds the
sealed credential. A restoration failure requires explicit editing/clearing
before Save. No credentials enter portable profiles or sleep backups.

AutomationStore/Rule/Secrets, Automations, Event/Dispatch/Transports,
IncomingAutomation and its Activities, PlanOperations, manifests and permissions
are unchanged. All 14 event labels/help and transport labels remain verbatim
(SHA256 `218566408226aa83083adc511932d5bfb55e5c4a06fd802660aca097d25f3071`).
Targeted app broadcasts, HTTPS/TLS, MQTT, queue bounds, revisions, credentials,
retry behavior and incoming default-off authorization contracts remain intact.
The UI changes do not establish remote or vendor compatibility.

## Existing inert native method extension

Exact selector:
`AutomationStorageTest#configReopensWithoutPlaintextCredentialsInRows`.
There are still 10 existing @Test methods; no method or unit batch was added.
The original encrypted-row/reopen assertions remain, followed by one actual UI
journey using the same UUID private SQLite database and existing fake vault.
The pinned fixture status/recover/schedule port never touches JobScheduler or a
remote recipient. Every input checks that this exact private owner remains
installed. Visual-review flags suppress optional interaction feedback.

The extension verifies real native navigation, full 48dp bounds, actual
focused windows and committed frames; default-Off Add/cancel; page recreation;
current-type no-op with retained URL/credential; original saved values before
Save; old confirmation rejection; Test/Delete confirmation cancellation; one
explicit private test event across accepted-operation recreation; Save/reopen;
committed-save scheduling warning; load error/retry; and unsaved Back discard.
Readers and accepted tasks drain before restoring the prior Port and removing
private fixtures. Exact original global preference values/key absence and
absence of capture/playback/alarm runtime are asserted.

The ten required original names are:

- `automation-rules-main.png`
- `automation-rules-options.png`
- `automation-rules-destination.png`
- `automation-rule-type-recreated.png`
- `automation-rule-draft.png`
- `automation-rule-test-confirmation-recreated.png`
- `automation-rule-busy-recreated.png`
- `automation-rules-saved-notice.png`
- `automation-rules-load-error.png`
- `automation-rule-reopened.png`

The method and changed Activity compile against Android API36 / Java release17.
Focused `automation-rules` [CI40773076249](https://github.com/omeyerrato/SleepWell/actions/runs/40773076249)
passed at `805704af`: **31.979 seconds, one existing method / ten required originals**.
All ten originals were individually inspected against Board11 right; exact
assertions, source/scope hashes, the first failed run and limits are in the
[verification archive](verification/2026-10-29-automation-rules/README.md).
Both optional-output flags were false. The native-only correction waits for
current owned-window handoff; no production change followed the source commits
listed above. All42 previous scope mappings/selectors/arrays/defaults remain.

The fake Vault is not an Android Keystore runtime claim. Private SQLite queue
writes and fake scheduling do not prove real event delivery, recipient actions,
network/TLS, JobScheduler, process-death behavior or physical output. Wider
security and the full 36-screen acceptance remain separate and incomplete.

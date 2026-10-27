# Tonight and challenge routine update

[APK and checksum](https://github.com/omeyerrato/SleepWell/releases/tag/v0.1.0-tonight-report-20261027-32f064e2) target **32f064e247430e9ebcff373e921a97675d39d815**. arm64-v8a APK166,649,432bytes;
build PASS11m19s. Original signer, exact Git tag and both uploaded sizes/digests
verified. All packaged assets match33cb5693 byte-for-byte.

Installed on the connected S23 Ultra with `adb install -r --streaming`.
Installed APK checksum matches. Every original database row, preference file
and user file is preserved. Two previously absent, empty integration-access
policy files initialized on package replacement; Android ProfileVerifier cache
updated its package timestamp/profile size. No previous grant existed.
Raw differences and private before/after backups remain local.
[Preservation result](phone-update.json). No app launch, unsuspend, alarm, sound,
flashlight, haptic or functional phone test was performed.

## Accepted bounded changes

- Tonight: clearer continuous plan arc and pinned outline settings icon;
  direct controls use the shared saved feedback policy. Open rest-window help
  survives Activity recreation. CI[40451121212](https://github.com/omeyerrato/SleepWell/actions/runs/40451121212)
  at14bc693e passes45.786s/9 originals, all individually inspected against board01.
  Start/midpoint/end/next-plan/weekly progression, actual foreground timer update,
  help restoration and large-text action reach pass. Relevant production files
  are unchanged in the release. Physical haptic feel remains unverified.
- Challenges: selected task cards first; mode/pinning in More; four available
  tasks open configuration, with the entire catalog/duplicates under More task
  types. CI[40467733318](https://github.com/omeyerrato/SleepWell/actions/runs/40467733318)
  at32f064e2 passes48.33s/6 originals, all individually inspected against board04.
  Real drag/cancel/reorder/remove, settings/catalog Back after recreation,
  draft preservation and final Save/reopen pass. The three-card catalog row is
  completely visible above Save; no clipped explanatory subtitle remains.
  Full-routine preview is still absent; no placeholder was added.

These are two separate existing single-method journeys, not full-app acceptance.
Their original environments/logs/summaries/15 PNGs and hashes are retained here.
There was no new unit-test or broad lint/testing campaign.

## Report remains incomplete

Included source refines measured chart bars, gridlines and proportions; the
Trends calendar icon opens a real date picker. History remains under Options.
The latest native flow[40467045608](https://github.com/omeyerrato/SleepWell/actions/runs/40467045608)
failed: an immediate pending-date accessibility assertion preceded its updated
tree, and a History reader-shutdown timeout masked that original assertion.
The diagnostic calendar later shows the selected date. This is useful diagnosis,
not full calendar/edit/persistence acceptance. Its original log/error/XML and
two completed captures plus failure screenshot are in `report-incomplete/`.
Report is explicitly not marked finished. Corrections remain subsequent work.

Earlier failed runs40453741994/40456920687/40460905567 diagnosed a CI focus-dump
parser, an Activity-only screenshot helper used for a Dialog, and a stale
accessibility object after rerendering. No app assertions were removed to publish.

## Remaining scope

All36 screens/full reference fidelity, remaining interactions/security, reliable
extra sound recognition, phone stages/REM/breathing/apnea, provider/vendor parity,
real alarm outputs/protection/challenges and overnight/battery/integration
acceptance remain open. Breathing implementation work is separate and is not
included in this APK. Sleep schema17/backup13/private automation2/result1 unchanged.

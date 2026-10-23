# Report, calendar and routine native evidence

All 21 PNGs are original emulator captures at d6389dc from run40027277488.
Their three allowlisted native classes pass actual interaction, saved private
SQLite/JSON, recreation and reopening checks. SHA256.json records original bytes;
no screenshot was retouched or generated. Each file was individually inspected
against the supplied boards; see ../../STRICT-VISUAL-GATE-2026-10-23.md and
../../NATIVE-REVIEW-40027277488.md for bounded findings and remaining differences.

Report: eight originals, actual history/trend/night navigation, sound estimates,
awake edit and tag/note save/reopen. Calendar: seven originals, pending nested
time/date state, cancellation and real DST-resolved saved instants. Routine: six
originals, actual add/configure/reorder/remove/recreate/save/reopen and invalid
backup rejection. No alarm challenge, service or hardware output ran.

The overall run fails PrivacyHistoryFlowTest at late journal reentry; these
passing classes do not establish full app acceptance. Reference gaps include
missing QR/silent routine preview, nested time-form styling and report notes near
the viewport edge. Clock-axis, Walk and headphone source added later is outside
this evidence. Full36-screen/feature acceptance remains incomplete.

# Walk, headphone routing and privacy evidence

Run40033681541, source64ae8d2, API36 emulator at1080x2316/density420.
All16 PNGs are original isolated-fixture captures, individually opened by the
strict reviewer. SHA256.json records exact original bytes. No phone data is
included. Walk configuration1, headphone route configuration1, fake-output2 and
privacy cleanup1 methods pass. Source same-head total:30/31 classes,44 passing
methods and148 PNGs; ChartClockFlowTest fails its initial requestFocus assertion,
so none of the required four chart images or their visual acceptance exists.

Walk's six states exercise actual target settings, permission/error/unsupported
copy, recreation, Save and reopened SQLite JSON. No alarm or step sensor ran.
Five sound-route states exercise distinct unavailable/unsupported/accepted
endpoint data with a fake provider and saved/recreated/reopened preference.
Two native worker tests use a fake sink only, never AudioTrack or Vibrator.

Privacy's five states cover confirmation, retained disabled busy work, actual
SQLite/file failure and queued-audio retry preserving a new night. The later
journal-reentry assertion also passes in the raw class log, but has no separate
PNG. Four privacy captures intentionally scroll to the bottom and therefore do
not certify the offscreen title. The checked gray favorites switch is a known
later state-sync candidate, not represented as visually fixed here.

These are narrow configuration/storage/fake-output gates. Full36-screen parity,
physical routing/sensor recognition and alarm/sound effectiveness remain open.
Later trend, preview and exact-seconds styling is outside this evidence.

# Sunrise and automatic-backup native increment

Source **741d63c**, [CI40164774515](https://github.com/omeyerrato/SleepWell/actions/runs/40164774515).
The whole **29-class/76-method** run **FAILS** on one automatic-backup UI method.
Of91 required named originals,89 were produced; the retry/disabled backup frames
are absent. One additional failure diagnostic is retained (90 raw PNGs total).
The failed summary and original logs remain intact.

New sunrise Storage6/Flow1/all4 originals and ToneWaveArrival1 pass. The previously
failing delay Storage4/Flow2/all9, barcode Storage3/Flow1/all5 and flip Storage3/Flow2/
all7 pass. These use private records, fake capture/provider/output/sensor ports;
no actual alarm or hardware effects are exercised.

Automatic-backup Storage12 and its first save/reopen UI method pass. The uncertain
write confirmation method fails before retry because its dialog helper supplies
window coordinates to a screen-coordinate tap. A subsequent test-only correction
retains visibility,48dp,focused-frame and all state/file assertions; it needs a fresh
native run. The other passing classes do not make this increment green.

Original PNGs, test logs, CPU fixture JSON, environment and failed summary were
copied byte for byte. SHA256SUMS covers raw evidence. This is functional fixture
evidence; no design approval, actual calendar/cloud exchange, real microphone,
foreground-service delivery, speaker/torch/vibration/DND, phone power control or
overnight reliability is certified. Independent laughter accuracy remains failing.

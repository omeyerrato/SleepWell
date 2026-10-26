# Phone sleep-stage implementation gap

Reviewed 26 October 2026. F049 remains missing. The brief asks for honestly labelled
estimates; it does **not** require clinical certification before an experimental
feature can exist. The remaining problem is an appropriate implementable model and
input contract, not an added clinical-approval gate.

SleepWell currently retains minute movement/light summaries and optional short
16 kHz classification windows. YAMNet sound labels, event clips, inactivity and
clock cycles do not establish REM/light/deep estimates. Imported provider stages
are explicitly attributed source data, not locally inferred stages.

## Primary-source feasibility findings

- [HomeSleepNet](https://www.jmir.org/2023/1/e46216/) demonstrates smartphone
  nocturnal-audio staging at home and describes contextual audio features. Its
  study recordings are not public; no redistributable trained checkpoint was
  identified in the inspected article or supplement links. A published model
  architecture alone is not an Android inference implementation.
- [Oxford asleep](https://github.com/OxWearables/asleep) provides a classifier for
  wrist accelerometer data, including downloadable model weights. That input and
  sensor placement differ from a phone resting on a mattress and from saved minute
  RMS values. Its [Academic Use Licence](https://github.com/OxWearables/asleep/blob/main/LICENSE.md)
  restricts use to internal academic research and excludes consumer/product use;
  it is not a redistributable dependency for this app.

These findings establish possible research directions, not impossibility and not
an exhaustive search of all available models. No third-party weights, proprietary
APK assets or study recordings were copied; no vendor was contacted.

## Work still needed

1. Obtain a compatible redistributable trained phone-input model with its exact
   preprocessing contract, or train original weights on licensed synchronized
   signals and stage labels. Preserve model/code/data licences and provenance.
2. Implement the required bounded feature capture and Android inference with real
   gaps, source timestamps, missing-input handling and explicit estimate labels.
   Existing minute summaries cannot be silently treated as the richer input.
3. Persist model/version/coverage provenance; render only supported estimate
   intervals and document measured limitations with independent examples. An
   experimental label does not justify an invented hypnogram.

Breathing/apnea inference has its own signal/model requirements and remains open.
This note neither removes the requested feature nor marks it implemented.

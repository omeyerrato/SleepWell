# Piano challenge candidate rejected

No Piano/Instrument task is enabled by this experiment. The fixed two-source trial rejected both positives (0/2). The failed observations are retained; no threshold adjustment, source replacement or further source hunting followed.

Sources were selected from uploader descriptions and verified CC0 primary pages before downloading or inference: [Tesabob2001 chromatic scale](https://freesound.org/people/Tesabob2001/sounds/203494/) and [fnakez grand piano](https://freesound.org/people/fnakez/sounds/788058/). Declaration commit `0ed1093` fixes both sources, seven speech/snore/cry negatives and the rule. Commit `064eb5d` pins whole-preview PCM and original preview hashes before inference. Model, HTML, preview and PCM SHA-256 values are preserved in the manifest/evidence. Both whole MP3 previews were decoded with existing FFmpeg8.1.1 into16k mono PCM16, without gain, cropping, repetition or padding. The test assets are CC0; no app asset or model changed.

The exported model class map identifies Music132, Musical instrument133, Keyboard147, Piano148, Electric piano149. The [official AudioSet ontology](https://github.com/audioset/ontology/blob/master/ontology.json) identifies these exact ancestors; only132/133/147 were excluded as compatible evidence. Every other label, including other instruments, remained an unrelated competitor. Maximum Piano/Electric piano score had to reach0.65 with0.15 unrelated margin for two consecutive full15,600-sample windows. This conservative preset was declared before evaluating, not fitted to results.

| Source | Whole full windows | Maximum Piano/Electric piano score | Qualified windows | Detected |
|---|---:|---:|---:|---|
| Chromatic scale |34|0.4140625|0|No|
| Grand piano |28|0.19921875|0|No|
| Existing mixed music |57|0.5|0|No (context, no solo-piano truth assigned)|

All seven frozen speech/snore/cry sources were rejected. This does not compensate for no positive sensitivity. Full521 scores for every window and all outcomes are in [host-evaluation.json](verification/2026-10-25-piano-challenge/host-evaluation.json). This uses the existing pinned ai-edge-litert2.3.0 CPU runtime and isolated experimental Java policy; it is not Android execution or microphone/device evidence. No physical playback or capture occurred. The production night classifier, sensitivity defaults, Speak rule, model and existing independent laughter gate remain unchanged.

Reproduction (no downloads or automatic dependency installation):

```
python scripts/evaluate_piano_challenge.py --runtime-dir <existing-litert2.3.0-runtime> --jdk <jdk> --output <evidence.json>
```

Exit2 intentionally reports rejected candidate after saving all evidence. The Java policy is analysis-only under scripts/evaluation, not compiled into the app.

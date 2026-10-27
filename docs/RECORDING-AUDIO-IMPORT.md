# Preparing selected audio for recording analysis

This source increment adds AAC/M4A, MP3, Ogg Vorbis/Opus, FLAC and PCM16 WAV preparation to the existing explicit recorded-sound analysis. Compressed formats depend on the phone's installed Android decoders; unavailable, protected, multiple-track, incomplete and changing-format files fail visibly. The two models and their recognition limits are unchanged.

`RecordedRecognitionJob` copies only the explicitly selected content URI into memory, at most16MiB. It retains its30second preparation deadline, cancellation and alarm/tracking admission. No URI access is persisted and no temporary audio file, playback, microphone capture or network request is created. The document provider itself may need a connection to supply its bytes.

`RecordedAudioDecoder` uses a bounded [MediaDataSource](https://developer.android.com/reference/android/media/MediaDataSource), [MediaExtractor](https://developer.android.com/reference/android/media/MediaExtractor) and [MediaCodec](https://developer.android.com/reference/android/media/MediaCodec) path. It accepts one unprotected audio track, no video/other tracks, mono or stereo at8–96kHz, and PCM16 decoder output. Actual decoded frames cannot exceed120seconds, independently of declared duration. Complete end-of-stream is required; no first-two-minutes excerpt is silently selected. Timestamp discontinuities exceeding2ms or format changes are rejected rather than joined. This tolerance covers packet timestamp rounding, not a licence to fill missing audio.

PCM16 RIFF/WAVE uses a strict chunk parser, bounded to128chunks with coherent format/alignment/length. Stereo is the arithmetic mean of the two signed channels; no source peak normalization is performed. Selected mono16kHz PCM remains bit-identical. Other rates use the original193tap Blackman-windowed sinc resampler described in [analysis contract](RECORDED-SOUND-ANALYSIS.md). Its unsupported edge centers are omitted without padding or repeated samples. This is file preparation, not a change to retained-night capture, sonar, YAMNet or live event analysis.

The separate saved-estimate source JSON records `inputMime`, `inputRate`, `inputChannels`, `decoder`, actual `decodedFrames`, `firstPresentationUs`, stereo `downmix`, `resampling`, `firstOutputFrame`, `omittedStartMs` and `omittedEndMs`. `firstOutputFrame/16000` is the exact analysis-copy origin relative to decoded samples; millisecond omission fields round down. Result-window positions refer to that prepared copy. Source hashes retain actual encoded bytes (`sourceSha256` and `fileSha256` for compressed imports); PCM hashes identify actual prepared samples. Existing WAV sources retain `wavSha256`. No URI, file path or document filename is saved. Retained-WAV revalidation and original result JSON/database schema remain compatible.

The bounded native AAC flow below exercises decoding, actual analysis/Save/reopen/delete and original data preservation. It does not establish other device codec coverage, real document-provider behavior, physical capture, reliable automatic laughter/instrument recognition or sleep stages.

## Native AAC milestone and export correction

The existing recorded-analysis journey passes at3359881e in
[CI40444367323](https://github.com/omeyerrato/SleepWell/actions/runs/40444367323):
40.27seconds, actual platform encoding/decoding and bundled CPU inference,
Save/reopen/delete, original rows/preferences/audio preservation and five
individually inspected original Android screenshots. Selection uses a narrowly
read-granted fixture content URI; this does not verify DocumentsUI or real
external document providers. Host audio is disabled; no personal phone or
microphone/playback output is used.

The source contains218,720 real PCM samples (13.670seconds). Native AAC produces
216 packets; decoding yields221,184 samples (13.824seconds). The additional154ms
is actual codec padding, retained and disclosed rather than guessed away. Both
10second and3.824second model windows remain Uncertain. Source and prepared PCM
hashes, packet timestamps and actual decoded metadata are retained in the
separate saved-result proof; no recognition-accuracy improvement follows.

The earlier failed runs caught a genuine38ms jump in our generated AAC tail.
Sending EOS with the last real input buffer did not eliminate it. For the AOSP
`c2.android.aac.encoder` only, export now verifies the output AudioSpecificConfig
as AAC-LC, matching rate/channels and1024sample frames, rejects partial output,
and clocks each encoded access unit from that known continuous source frame
count. Encoded bytes and original PCM are unchanged; arbitrary vendor encoders
retain their original timestamps. The import decoder still rejects timestamp
gaps over2ms. This correction never joins, normalizes or repairs imported files.
Other codecs, rates/channels, physical arm64 and provider/lifecycle coverage
remain unverified by this one AAC fixture flow.

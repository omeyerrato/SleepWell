package org.sleepwell.app;

/** Frozen development-selected policy for explicit recorded analysis only. No live cue authority. */
final class RecordedSoundPolicy {
    static final String VERSION = "fsd50k-dev-v2-pann-instrument-035-015";
    static final String PANN = "panns-mobilenetv2-f9dd5647";
    static final String EFFICIENT = "efficientat-mn10-as-7bb42270";

    static final class Estimate {
        final double score, competitor;
        final boolean qualifies;
        final int index;
        Estimate(double score, double competitor, boolean qualifies, int index) {
            this.score = score; this.competitor = competitor; this.qualifies = qualifies; this.index = index;
        }
    }
    static final class Decision {
        final Estimate laughter, instrument, efficientInstrument, pannInstrument;
        final String candidate, instrumentModel;
        Decision(Estimate laughter, Estimate efficient, Estimate pann) {
            this.laughter = laughter; efficientInstrument = efficient; pannInstrument = pann;
            // Keep every accepted EfficientAT output, including its score and strongest family label.
            boolean fallback = !efficient.qualifies && pann.qualifies;
            instrument = fallback ? pann : efficient;
            instrumentModel = fallback ? PANN : EFFICIENT;
            candidate = laughter.qualifies ? "laughter" : instrument.qualifies ? "instrument" : "uncertain";
        }
    }

    static Decision decide(float[] pann, float[] efficient) {
        return new Decision(estimate(pann, false, .10, .025),
                estimate(efficient, true, .65, .15), estimate(pann, true, .35, .15));
    }
    private static boolean instrumentIndex(int i) {
        return (i >= 138 && i <= 199) || (i >= 208 && i <= 215);
    }
    private static Estimate estimate(float[] scores, boolean instruments, double threshold, double margin) {
        double score = 0, competitor = 0;
        int index = instruments ? 138 : 16;
        for (int i = 0; i < scores.length; i++) {
            boolean member = instruments ? instrumentIndex(i) : i >= 16 && i <= 21;
            if (member) { if (scores[i] > score) { score = scores[i]; index = i; } }
            else if (!instruments || i != 137) competitor = Math.max(competitor, scores[i]);
        }
        return new Estimate(score, competitor, score >= threshold && score - competitor >= margin, index);
    }
    private RecordedSoundPolicy() {}
}

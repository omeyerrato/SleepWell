package org.sleepwell.app;

import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;

/** Exact, separate 32 kHz frontends for the two supplementary AudioSet models. */
final class RecordedSoundFrontend {
    private final float[] fir, pannWindow, efficientWindow, pannMel, efficientMel;
    RecordedSoundFrontend(float[] fir, float[] pannWindow, float[] efficientWindow,
                          float[] pannMel, float[] efficientMel) {
        if (fir.length != 41 || pannWindow.length != 1024 || efficientWindow.length != 1024
                || pannMel.length != 513 * 64 || efficientMel.length != 513 * 128)
            throw new IllegalArgumentException("Invalid sound frontend constants");
        this.fir = fir; this.pannWindow = pannWindow; this.efficientWindow = efficientWindow;
        this.pannMel = pannMel; this.efficientMel = efficientMel;
    }

    float[] resample(short[] pcm, int offset, int count, BooleanSupplier cancelled) {
        if (offset < 0 || count < 0 || offset > pcm.length - count)
            throw new IllegalArgumentException("Invalid source interval");
        float[] out = new float[count * 2];
        for (int j = 0; j < out.length; j++) {
            if ((j & 1023) == 0) check(cancelled);
            // scipy resample_poly(up=2,down=1): centered 41-tap Kaiser(5) filter.
            // Zero extension is only the resampling boundary convention, never invented duration.
            double sum = 0;
            int first = Math.max(0, (j - 20 + 1) / 2), last = Math.min(count - 1, (j + 20) / 2);
            for (int k = first; k <= last; k++) {
                int coefficient = j - 2 * k + 20;
                if (coefficient >= 0 && coefficient < fir.length)
                    sum += (pcm[offset + k] / 32768.0f) * fir[coefficient];
            }
            out[j] = (float) sum;
        }
        return out;
    }

    float[] pann(float[] samples, BooleanSupplier cancelled) {
        return mel(samples, pannWindow, pannMel, 64, false, cancelled);
    }
    float[] efficient(float[] samples, BooleanSupplier cancelled) {
        float[] emphasized = new float[samples.length - 1];
        for (int i = 0; i < emphasized.length; i++) {
            if ((i & 1023) == 0) check(cancelled);
            emphasized[i] = samples[i + 1] - .97f * samples[i];
        }
        return mel(emphasized, efficientWindow, efficientMel, 128, true, cancelled);
    }

    private static float[] mel(float[] samples, float[] window, float[] weights, int bands,
                               boolean efficient, BooleanSupplier cancelled) {
        if (samples.length <= 512) throw new IllegalArgumentException("Sound interval too short");
        int frames = 1 + samples.length / 320;
        float[] out = new float[frames * bands];
        double[] real = new double[1024], imaginary = new double[1024], power = new double[513];
        for (int frame = 0; frame < frames; frame++) {
            check(cancelled);
            int center = frame * 320;
            for (int i = 0; i < 1024; i++) {
                int source = center + i - 512;
                // torch center=True, pad_mode=reflect (endpoints are not repeated).
                if (source < 0) source = -source;
                else if (source >= samples.length) source = 2 * samples.length - source - 2;
                real[i] = (float) (samples[source] * window[i]); imaginary[i] = 0;
            }
            fft(real, imaginary);
            for (int bin = 0; bin < 513; bin++)
                power[bin] = real[bin] * real[bin] + imaginary[bin] * imaginary[bin];
            for (int band = 0; band < bands; band++) {
                double sum = 0;
                for (int bin = 0; bin < 513; bin++) sum += power[bin] * weights[bin * bands + band];
                float value = efficient ? (float) ((Math.log(sum + .00001) + 4.5) / 5)
                        : (float) (10 * Math.log10(Math.max(1e-10, sum)));
                out[efficient ? band * frames + frame : frame * bands + band] = value;
            }
        }
        return out;
    }

    private static void fft(double[] real, double[] imaginary) {
        for (int i = 1, j = 0; i < 1024; i++) {
            int bit = 512;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;
            if (i < j) {
                double swap = real[i]; real[i] = real[j]; real[j] = swap;
                swap = imaginary[i]; imaginary[i] = imaginary[j]; imaginary[j] = swap;
            }
        }
        for (int length = 2; length <= 1024; length <<= 1) {
            double angle = -2 * Math.PI / length, stepR = Math.cos(angle), stepI = Math.sin(angle);
            for (int start = 0; start < 1024; start += length) {
                double wr = 1, wi = 0;
                for (int j = 0; j < length / 2; j++) {
                    int a = start + j, b = a + length / 2;
                    double br = real[b] * wr - imaginary[b] * wi;
                    double bi = real[b] * wi + imaginary[b] * wr;
                    real[b] = real[a] - br; imaginary[b] = imaginary[a] - bi;
                    real[a] += br; imaginary[a] += bi;
                    double next = wr * stepR - wi * stepI;
                    wi = wr * stepI + wi * stepR; wr = next;
                }
            }
        }
    }

    static void check(BooleanSupplier cancelled) {
        if (Thread.currentThread().isInterrupted() || cancelled.getAsBoolean())
            throw new CancellationException("Sound analysis cancelled");
    }
}

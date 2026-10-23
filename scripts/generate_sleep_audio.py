"""Original seamless procedural ambience; does not overwrite legacy or alarm tones.

Circularly filtered noise with periodic envelopes closes the loop without an edit seam.
These are sound textures, not field recordings. Listening/phone quality review is separate.
"""
from pathlib import Path
import math
import random
import struct
import wave


def generate():
    folder = Path(__file__).resolve().parents[1] / "app/src/main/assets"
    rate, seconds = 22050, 24
    count = rate * seconds
    for source, seed in [("ocean", 31), ("fireplace", 47), ("wind", 59)]:
        rng = random.Random(seed)
        noise = [rng.uniform(-1, 1) for _ in range(count)]
        low = []
        # Warm up a periodic filter using the end of the source.
        state = 0.0
        alpha = .04 if source == "wind" else .12
        for i in range(-2000, count):
            state += alpha * (noise[i] - state)
            if i >= 0:
                low.append(state)
        cracks = [0.0] * count
        if source == "fireplace":
            for _ in range(150):
                start, amplitude = rng.randrange(count), rng.uniform(.03, .18)
                for j in range(500):
                    cracks[(start + j) % count] += amplitude * math.exp(-j / 65) * rng.uniform(-1, 1)
        values = []
        for i in range(count):
            phase = 2 * math.pi * i / count
            if source == "ocean":
                swell = .12 + .35 * ((1 + math.sin(phase * 3 - 1.1)) / 2) ** 2
                value = swell * (low[i] * 2 + noise[i] * .16)
            elif source == "wind":
                gust = .2 + .25 * (1 + math.sin(phase * 2)) / 2 + .08 * math.sin(phase * 5)
                value = low[i] * gust * 3
            else:
                value = low[i] * .32 + noise[i] * .018 + cracks[i]
            values.append(value)
        # DC removal is important for a continuously looped sample.
        mean = sum(values) / count
        peak = max(abs(v - mean) for v in values)
        scale = min(1, .65 / peak)
        frames = b"".join(struct.pack("<h", round((v - mean) * scale * 32767)) for v in values)
        with wave.open(str(folder / (source + ".wav")), "wb") as output:
            output.setnchannels(1)
            output.setsampwidth(2)
            output.setframerate(rate)
            output.writeframes(frames)


if __name__ == "__main__":
    generate()

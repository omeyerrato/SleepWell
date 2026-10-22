"""Reproduce SleepWell's original synthesized WAV assets (Python standard library)."""
from pathlib import Path
import math
import random
import struct
import wave


def generate():
    folder = Path(__file__).resolve().parents[1] / "app/src/main/assets"
    folder.mkdir(parents=True, exist_ok=True)
    rng = random.Random(17)
    for name in ["daybreak", "pulse", "rain", "brown"]:
        rate, brown = 22050, 0.0
        frames = bytearray()
        for i in range(rate * 8):
            t = i / rate
            if name == "daybreak":
                frequency = [523.25, 659.25, 783.99, 1046.5][int(t * 2) % 4]
                envelope = min(1, (t % 0.5) * 40) * math.exp(-(t % 0.5) * 5)
                value = 0.65 * envelope * (math.sin(2 * math.pi * frequency * t) + 0.2 * math.sin(2 * math.pi * frequency * 2 * t))
            elif name == "pulse":
                value = 0.60 * math.sin(2 * math.pi * (660 if int(t * 4) % 2 == 0 else 880) * t) * (0.3 + 0.7 * (int(t * 4) % 4 < 3))
            else:
                brown = max(-1, min(1, brown + rng.uniform(-0.04, 0.04)))
                value = 0.35 * brown if name == "brown" else 0.18 * rng.uniform(-1, 1) + 0.1 * brown
            frames.extend(struct.pack("<h", int(max(-0.9, min(0.9, value)) * 32767)))
        with wave.open(str(folder / (name + ".wav")), "wb") as output:
            output.setnchannels(1)
            output.setsampwidth(2)
            output.setframerate(rate)
            output.writeframes(frames)


if __name__ == "__main__":
    generate()

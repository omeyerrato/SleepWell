"""Original SleepWell chime; deterministic additive synthesis, no sampled/vendor audio."""
import math
from pathlib import Path
import struct
import wave

rate = 44100
seconds = 4
notes = [(0.08, 659.255), (0.62, 830.609), (1.18, 987.767), (2.10, 1318.510)]
target = Path(__file__).resolve().parents[1] / "app/src/main/assets/chime.wav"
with wave.open(str(target), "wb") as output:
    output.setnchannels(1)
    output.setsampwidth(2)
    output.setframerate(rate)
    pcm = bytearray()
    for index in range(rate * seconds):
        time = index / rate
        value = 0.0
        for start, frequency in notes:
            age = time - start
            if age >= 0:
                envelope = (1 - math.exp(-age * 100)) * math.exp(-age * 2.8)
                value += envelope * (math.sin(2 * math.pi * frequency * age) + 0.20 * math.sin(2 * math.pi * frequency * 2.01 * age))
        value *= 0.18 * min(1.0, max(0, (seconds - time) * 8))
        pcm.extend(struct.pack("<h", round(max(-1, min(1, value)) * 32767)))
    output.writeframes(pcm)

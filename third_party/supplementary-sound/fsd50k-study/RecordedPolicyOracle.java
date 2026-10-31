package org.sleepwell.app;

import java.nio.*;
import java.nio.file.*;

/** Host adapter over the shipping policy. Input is pairs of existing PANN/EfficientAT score vectors. */
final class RecordedPolicyOracle {
    private static String estimate(RecordedSoundPolicy.Estimate value) {
        return value.score+"\t"+value.competitor+"\t"+value.qualifies+"\t"+value.index;
    }
    public static void main(String[] args) throws Exception {
        ByteBuffer bytes=ByteBuffer.wrap(Files.readAllBytes(Path.of(args[0]))).order(ByteOrder.LITTLE_ENDIAN);
        if(bytes.remaining()%(527*8)!=0)throw new IllegalArgumentException("Incomplete score vectors");
        while(bytes.hasRemaining()) {
            float[] pann=new float[527],efficient=new float[527];
            for(int n=0;n<527;n++)pann[n]=bytes.getFloat();
            for(int n=0;n<527;n++)efficient[n]=bytes.getFloat();
            RecordedSoundPolicy.Decision d=RecordedSoundPolicy.decide(pann,efficient);
            System.out.println(d.candidate+"\t"+d.instrumentModel+"\t"+estimate(d.laughter)+"\t"
                    +estimate(d.instrument)+"\t"+estimate(d.efficientInstrument)+"\t"+estimate(d.pannInstrument));
        }
    }
}

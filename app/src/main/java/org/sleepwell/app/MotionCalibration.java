package org.sleepwell.app;

import org.json.*;
import java.util.*;

/** Surface response check using real gravity-filtered RMS bins; not a sleep classifier. */
final class MotionCalibration {
    static final long STILL_MS=10000,MOVE_MS=5000,BIN_MS=500;
    static final String ALGORITHM="surface_rms_separation_v1";
    static final class Capture {
        final long start,duration;final MotionFilter filter=new MotionFilter();
        final double[] sums;final int[] counts;long last=-1;int samples;String error="";
        Capture(long start,long duration){this.start=start;this.duration=duration;sums=new double[(int)(duration/BIN_MS)];counts=new int[sums.length];}
        void sample(long time,long now,float x,float y,float z,int accuracy){
            if(!error.isEmpty())return;
            if(accuracy<android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW||accuracy>android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH){error="Sensor accuracy is unreliable. Try the calibration again.";return;}
            if(time<start||time>now+50||now-time>750||time<=last){error="Motion timestamps were missing or stale. Keep this screen open and retry.";return;}
            if(last>=0&&time-last>1000){error="Motion readings had a gap. Keep this screen open and retry.";return;}
            last=time;if(time>=start+duration)return;
            if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z)||Math.abs(x)>100||Math.abs(y)>100||Math.abs(z)>100){error="Motion readings were outside the supported range. Retry with gentle movement.";return;}
            Double raw=filter.sample(x,y,z);if(raw==null||time-start<1000)return;
            if(!Double.isFinite(raw)||raw>40){error="Motion readings were outside the supported range.";return;}
            int bin=(int)((time-start)/BIN_MS);sums[bin]+=raw*raw;counts[bin]++;samples++;if(samples>10000)error="Too many readings for this bounded calibration. Try again.";
        }
        List<Double> bins(){List<Double> out=new ArrayList<>();for(int n=0;n<sums.length;n++)out.add(counts[n]==0?null:Math.sqrt(sums[n]/counts[n]));return out;}
        Summary finish(long now){if(!error.isEmpty())throw new IllegalArgumentException(error);if(now<start+duration||now>start+duration+1000||last<0||now-last>1000)throw new IllegalArgumentException("The capture did not finish with fresh readings. Try again.");List<Double> values=new ArrayList<>();for(int n=2;n<sums.length;n++)if(counts[n]>0)values.add(Math.sqrt(sums[n]/counts[n]));int needed=(int)Math.ceil((sums.length-2)*.85);if(values.size()<needed||samples<(duration==STILL_MS?30:12))throw new IllegalArgumentException("Too few reliable readings. Leave the screen open and try again.");Collections.sort(values);return new Summary(percentile(values,.90),percentile(values,.50),values.size(),samples);}
    }
    static final class Summary{final double p90,median;final int bins,samples;Summary(double p90,double median,int bins,int samples){this.p90=p90;this.median=median;this.bins=bins;this.samples=samples;}}
    final double gain,restP90,moveMedian;final int restBins,moveBins,restSamples,moveSamples;final long created;
    private MotionCalibration(double gain,double restP90,double moveMedian,int restBins,int moveBins,int restSamples,int moveSamples,long created){this.gain=gain;this.restP90=restP90;this.moveMedian=moveMedian;this.restBins=restBins;this.moveBins=moveBins;this.restSamples=restSamples;this.moveSamples=moveSamples;this.created=created;validateValues();}
    static MotionCalibration derive(Summary rest,Summary move,long created){double restHigh=rest.p90,moveTypical=move.median;if(moveTypical<=restHigh*2||moveTypical-restHigh<.04)throw new IllegalArgumentException("Still and movement readings overlap too much. Use a steadier surface, then make a small slow movement.");double gain=.12/((restHigh+moveTypical)/2);return new MotionCalibration(gain,restHigh,moveTypical,rest.bins,move.bins,rest.samples,move.samples,created);}
    private void validateValues(){if(!Double.isFinite(gain)||gain<.5||gain>2||!Double.isFinite(restP90)||!Double.isFinite(moveMedian)||restP90<0||moveMedian>40||moveMedian<=restP90*2||moveMedian-restP90<.04||restP90*gain>=.09||moveMedian*gain<=.15||!(restP90<.12/gain&&.12/gain<moveMedian)||Math.abs(gain-.12/((restP90+moveMedian)/2))>1e-9||restBins<16||restBins>18||moveBins<7||moveBins>8||restSamples<30||restSamples>10000||moveSamples<12||moveSamples>10000||created<=0||created>253402300799999L)throw new IllegalArgumentException("These readings cannot support a conservative gain within Low–High sensitivity. Retry with a still phone and smaller, gentle movements, or use manual sensitivity.");}
    String config(){try{return new JSONObject().put("version",1).put("algorithm",ALGORITHM).put("gain",gain).put("rest_p90",restP90).put("move_median",moveMedian).put("rest_bins",restBins).put("move_bins",moveBins).put("rest_samples",restSamples).put("move_samples",moveSamples).put("still_ms",STILL_MS).put("move_ms",MOVE_MS).put("created",created).toString();}catch(JSONException impossible){throw new IllegalStateException(impossible);}}
    static MotionCalibration read(String raw){if(raw==null||raw.isEmpty()||raw.length()>1024)throw new IllegalArgumentException("Invalid surface calibration.");try{JSONObject j=new JSONObject(raw);Set<String> fields=Set.of("version","algorithm","gain","rest_p90","move_median","rest_bins","move_bins","rest_samples","move_samples","still_ms","move_ms","created");if(j.length()!=fields.size())throw new JSONException("Unexpected calibration fields");for(Iterator<String> keys=j.keys();keys.hasNext();)if(!fields.contains(keys.next()))throw new JSONException("Unknown calibration field");if(SensorProvenance.whole(j,"version")!=1||!ALGORITHM.equals(SensorProvenance.text(j,"algorithm"))||SensorProvenance.whole(j,"still_ms")!=STILL_MS||SensorProvenance.whole(j,"move_ms")!=MOVE_MS)throw new JSONException("Unsupported calibration");return new MotionCalibration(SensorProvenance.number(j,"gain"),SensorProvenance.number(j,"rest_p90"),SensorProvenance.number(j,"move_median"),integer(j,"rest_bins"),integer(j,"move_bins"),integer(j,"rest_samples"),integer(j,"move_samples"),SensorProvenance.whole(j,"created"));}catch(JSONException invalid){throw new IllegalArgumentException("Saved surface calibration could not be read. Reset calibration or use manual sensitivity.",invalid);}}
    private static int integer(JSONObject j,String key)throws JSONException{long v=SensorProvenance.whole(j,key);if(v<0||v>10000)throw new JSONException("Invalid calibration count");return(int)v;}
    private static double percentile(List<Double> values,double fraction){return values.get(Math.max(0,(int)Math.ceil(values.size()*fraction)-1));}
}

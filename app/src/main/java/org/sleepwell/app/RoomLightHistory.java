package org.sleepwell.app;

import android.os.Bundle;
import java.util.*;

/** At most 901 actual per-second observations from the last 15 minutes; absent intervals stay absent. */
final class RoomLightHistory {
    static final long WINDOW=15*60*1000L;
    static final class Sample {final long time;final float lux;Sample(long time,float lux){this.time=time;this.lux=lux;}}
    final List<Sample> samples=new ArrayList<>();
    private long opened;
    RoomLightHistory(long now,Bundle saved){opened=now;if(saved!=null){long start=saved.getLong("roomLightOpened",now);long[] times=saved.getLongArray("roomLightTimes");float[] values=saved.getFloatArray("roomLightLux");if(start>=0&&start<=now)opened=start;if(times!=null&&values!=null&&times.length==values.length&&times.length<=901)for(int n=0;n<times.length;n++)if(times[n]>=0&&times[n]<=now&&(n==0||times[n]>=times[n-1])&&(Float.isNaN(values[n])||Float.isFinite(values[n])&&values[n]>=0))samples.add(new Sample(times[n],values[n]));}prune(now);}
    void sample(long at,float lux,boolean reliable,long now){if(at<0||at>now+1000||!samples.isEmpty()&&at<samples.get(samples.size()-1).time)return;float value=reliable&&Float.isFinite(lux)&&lux>=0?lux:Float.NaN;if(!samples.isEmpty()&&at/1000==samples.get(samples.size()-1).time/1000)samples.remove(samples.size()-1);samples.add(new Sample(at,value));prune(now);}
    void prune(long now){samples.removeIf(value->value.time<now-WINDOW);while(samples.size()>901)samples.remove(0);}
    long start(long now){return Math.max(opened,now-WINDOW);}
    void save(Bundle out){long[] times=new long[samples.size()];float[] values=new float[samples.size()];for(int n=0;n<samples.size();n++){times[n]=samples.get(n).time;values[n]=samples.get(n).lux;}out.putLong("roomLightOpened",opened);out.putLongArray("roomLightTimes",times);out.putFloatArray("roomLightLux",values);}
}

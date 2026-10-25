package org.sleepwell.app;
import java.util.*;

/** One deterministic per-occurrence permutation; position denotes the last durably started track. */
final class AlarmSoundSequence {
    final String key;final List<String> order;private int position;
    AlarmSoundSequence(AlarmSoundMode mode,String run,int saved){if(!mode.playlist()||run==null||run.isEmpty())throw new IllegalArgumentException("Missing playlist occurrence");List<String> ordered=new ArrayList<>(mode.tracks);if(mode.shuffle)Collections.shuffle(ordered,new Random(run.hashCode()));order=List.copyOf(ordered);key=mode.kind.name()+":"+mode.shuffle+":"+String.join("\n",mode.tracks);position=saved>=0&&saved<order.size()?saved:0;}
    int position(){return position;}String current(){return order.get(position);}void next(){position=(position+1)%order.size();}
}

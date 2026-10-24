package org.sleepwell.app;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
/** Pure occurrence/task ownership, failed persistence and release-order boundaries. */
public final class WalkFallbackTest {
    static final class Effects implements WalkFallback.Effects {
        String run="night";int unit=2,stops,saves;boolean release=true,persist=true,changeOnStop,throwStop,throwSave;List<String> calls=new ArrayList<>();
        public boolean current(String run,int unit){calls.add("current");return this.run.equals(run)&&this.unit==unit;}
        public boolean stop(){stops++;calls.add("stop");if(throwStop)throw new IllegalStateException("unregister");if(changeOnStop)run="next";return release;}
        public boolean save(String run,int unit){saves++;calls.add("save");if(throwSave)throw new IllegalStateException("storage");return persist;}
    }
    @Test public void methodChangeConfirmsReleaseThenRechecksOwnerBeforeSave(){Effects e=new Effects();assertEquals(WalkFallback.Result.CHOSEN,WalkFallback.choose("night",2,e));assertEquals(List.of("current","stop","current","save"),e.calls);assertEquals(1,e.stops);assertEquals(1,e.saves);}
    @Test public void staleTaskAndOccurrenceCannotStopCurrentSensor(){for(boolean otherTask:new boolean[]{false,true}){Effects e=new Effects();if(otherTask)e.unit++;else e.run="later";assertEquals(WalkFallback.Result.STALE,WalkFallback.choose("night",2,e));assertEquals(0,e.stops);assertEquals(0,e.saves);}}
    @Test public void takeoverDuringReleaseCannotSaveAnotherTask(){Effects e=new Effects();e.changeOnStop=true;assertEquals(WalkFallback.Result.STALE,WalkFallback.choose("night",2,e));assertEquals(1,e.stops);assertEquals(0,e.saves);}
    @Test public void uncertainUnregisterCannotSaveAndRetryRemainsExplicit(){Effects e=new Effects();e.release=false;assertEquals(WalkFallback.Result.STOP_FAILED,WalkFallback.choose("night",2,e));assertEquals(0,e.saves);e.release=true;assertEquals(WalkFallback.Result.CHOSEN,WalkFallback.choose("night",2,e));assertEquals(2,e.stops);assertEquals(1,e.saves);}
    @Test public void unregisterAndSaveExceptionsAreDistinctAndContained(){Effects e=new Effects();e.throwStop=true;assertEquals(WalkFallback.Result.STOP_FAILED,WalkFallback.choose("night",2,e));assertEquals(0,e.saves);e.throwStop=false;e.throwSave=true;assertEquals(WalkFallback.Result.SAVE_FAILED,WalkFallback.choose("night",2,e));}
    @Test public void failedSaveNeverClaimsChosenAndCanBeRetried(){Effects e=new Effects();e.persist=false;assertEquals(WalkFallback.Result.SAVE_FAILED,WalkFallback.choose("night",2,e));e.persist=true;assertEquals(WalkFallback.Result.CHOSEN,WalkFallback.choose("night",2,e));assertEquals(2,e.saves);}
    @Test public void savedChoiceIsSpecificToExactOccurrenceAndRoutineUnit(){assertTrue(WalkFallback.chosen("night",2,"night",2));assertFalse(WalkFallback.chosen("night",2,"next",2));assertFalse(WalkFallback.chosen("night",2,"night",3));assertFalse(WalkFallback.chosen("",-1,"",-1));}
    @Test public void malformedOwnersCannotCauseAnyEffects(){for(String run:new String[]{null,"","x".repeat(201)}){Effects e=new Effects();assertEquals(WalkFallback.Result.STALE,WalkFallback.choose(run,2,e));assertTrue(e.calls.isEmpty());}for(int unit:new int[]{-1,48}){Effects e=new Effects();assertEquals(WalkFallback.Result.STALE,WalkFallback.choose("night",unit,e));assertTrue(e.calls.isEmpty());}}
}

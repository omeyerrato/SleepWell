package org.sleepwell.app;

/** A fallback changes only the current task's method; it never completes a routine unit. */
final class WalkFallback {
    enum Result { CHOSEN, STALE, STOP_FAILED, SAVE_FAILED }
    interface Effects {boolean current(String run,int unit);boolean stop();boolean save(String run,int unit);}
    static boolean valid(String run,int unit){return run!=null&&!run.isEmpty()&&run.length()<=200&&unit>=0&&unit<ChallengeRoutine.LIMIT*6;}
    static boolean chosen(String savedRun,int savedUnit,String run,int unit){return valid(run,unit)&&run.equals(savedRun)&&unit==savedUnit;}
    static Result choose(String run,int unit,Effects effects){
        if(!valid(run,unit)||!effects.current(run,unit))return Result.STALE;
        // Revoke callbacks and confirm unregister before changing the saved method.
        try{if(!effects.stop())return Result.STOP_FAILED;}catch(RuntimeException error){return Result.STOP_FAILED;}
        if(!effects.current(run,unit))return Result.STALE;
        try{return effects.save(run,unit)?Result.CHOSEN:Result.SAVE_FAILED;}catch(RuntimeException error){return Result.SAVE_FAILED;}
    }
}

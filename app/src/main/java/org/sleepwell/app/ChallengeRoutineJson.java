package org.sleepwell.app;

import org.json.*;
import java.util.*;

final class ChallengeRoutineJson {
    static JSONArray encode(ChallengeRoutine routine){JSONArray array=new JSONArray();try{for(ChallengeRoutine.Task task:routine.tasks)array.put(new JSONObject().put("type",task.type).put("rounds",task.rounds));}catch(JSONException impossible){throw new IllegalStateException(impossible);}return array;}
    static ChallengeRoutine decode(JSONArray array)throws JSONException{if(array==null)throw new JSONException("Missing challenge routine");List<ChallengeRoutine.Task> tasks=new ArrayList<>();if(array.length()>ChallengeRoutine.LIMIT)throw new JSONException("Too many challenge tasks");try{for(int n=0;n<array.length();n++){JSONObject task=array.getJSONObject(n);Object type=task.get("type"),rounds=task.get("rounds");if(!(type instanceof String)||!(rounds instanceof Number))throw new JSONException("Challenge type must be text and rounds must be an integer");double count=((Number)rounds).doubleValue();if(!Double.isFinite(count)||count!=Math.rint(count)||count<1||count>6)throw new JSONException("Challenge rounds must be an integer from 1 to 6");tasks.add(new ChallengeRoutine.Task((String)type,(int)count));}return new ChallengeRoutine(tasks);}catch(IllegalArgumentException error){throw new JSONException(error.getMessage());}}
}

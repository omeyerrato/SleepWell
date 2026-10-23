package org.sleepwell.app;

import org.json.*;
import java.util.*;

final class ChallengeRoutineJson {
    static JSONArray encode(ChallengeRoutine routine){JSONArray array=new JSONArray();try{for(ChallengeRoutine.Task task:routine.tasks)array.put(new JSONObject().put("type",task.type).put("rounds",task.rounds));}catch(JSONException impossible){throw new IllegalStateException(impossible);}return array;}
    static ChallengeRoutine decode(JSONArray array)throws JSONException{if(array==null)throw new JSONException("Missing challenge routine");List<ChallengeRoutine.Task> tasks=new ArrayList<>();if(array.length()>ChallengeRoutine.LIMIT)throw new JSONException("Too many challenge tasks");try{for(int n=0;n<array.length();n++){JSONObject task=array.getJSONObject(n);tasks.add(new ChallengeRoutine.Task(task.getString("type"),task.getInt("rounds")));}return new ChallengeRoutine(tasks);}catch(IllegalArgumentException error){throw new JSONException(error.getMessage());}}
}

package org.sleepwell.app;

import android.content.Context;
import org.json.JSONObject;
import java.util.List;

/** Shared native bar rendering for existing record-detail signal plots. */
class GraphView extends RecordChart {
    public GraphView(Context c){this(c,List.of(),"movement");}
    GraphView(Context c,List<JSONObject> data,String metric){
        super(c,data,metric,data.isEmpty()?0:data.get(0).optLong("time"),
            data.isEmpty()?1:data.get(data.size()-1).optLong("time"));
    }
}

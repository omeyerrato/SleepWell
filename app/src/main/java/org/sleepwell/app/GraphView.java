package org.sleepwell.app;

import android.content.Context;
import org.json.JSONObject;
import java.util.List;

/** Shared native bar rendering for existing record-detail signal plots. */
class GraphView extends RecordChart {
    GraphView(Context c,List<JSONObject> data,String metric,long start,long end){
        super(c,data,metric,start,end);
    }
}

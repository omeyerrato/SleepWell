package org.sleepwell.app;
import java.util.*;
/** Explicit targets are actual indices in the pinned model's embedded1000-label order. */
final class ObjectTarget {
 static final String MODEL_ID="efficientnet-lite0-aa03e401-p1";
 static final List<Integer> INDICES=List.of(504,898,761,487,508,673,414,837,879,770,852,954,951,950,892,709);
 static final List<String> LABELS=List.of("coffee mug","water bottle","remote control","cellular telephone","computer keyboard","mouse","backpack","sunglasses","umbrella","running shoe","tennis ball","banana","lemon","orange","wall clock","pencil box");
 static final ObjectTarget DEFAULT=new ObjectTarget(504);final int index;
 ObjectTarget(int index){if(!INDICES.contains(index))throw new IllegalArgumentException("Choose one supported model object");this.index=index;}
 String label(){return LABELS.get(INDICES.indexOf(index));}String key(){return MODEL_ID+":"+index;}
}

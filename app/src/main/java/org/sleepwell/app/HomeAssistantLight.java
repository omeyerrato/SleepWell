package org.sleepwell.app;

import org.json.*;

/** A specific Home Assistant light entity, not an invented all-vendor connection. */
final class HomeAssistantLight implements SmartLightRun.Port {
    private final SmartLightConfig config;private final HomeAssistantHttp.Connections connections;
    HomeAssistantLight(SmartLightConfig config){this(config,HomeAssistantHttp.DEFAULT);}
    HomeAssistantLight(SmartLightConfig config,HomeAssistantHttp.Connections connections){this.config=config;this.connections=connections;}
    public SmartLightRun.State read()throws Exception{return parse(config.entity,HomeAssistantHttp.request(config,"/api/states/"+config.entity,null,connections));}
    static SmartLightRun.State parse(String entity,String json)throws Exception{JSONObject state=new JSONObject(json);if(!entity.equals(state.getString("entity_id")))throw new IllegalArgumentException("Unexpected light entity");String value=state.getString("state");if(!value.equals("on")&&!value.equals("off"))throw new IllegalArgumentException("Light is unavailable");JSONObject attributes=state.getJSONObject("attributes");JSONArray modes=attributes.optJSONArray("supported_color_modes");boolean brightness=false;if(modes!=null)for(int n=0;n<modes.length();n++)if(java.util.Set.of("brightness","color_temp","hs","xy","rgb","rgbw","rgbww","white").contains(modes.getString(n)))brightness=true;int level=attributes.optInt("brightness",0);if(value.equals("on")&&(level<1||level>255))throw new IllegalArgumentException("Light brightness unavailable");return new SmartLightRun.State(value.equals("on"),level,brightness);}
    public void set(boolean on,int brightness)throws Exception{if(on&&(brightness<1||brightness>255))throw new IllegalArgumentException("Invalid brightness");String body=new JSONObject().put("entity_id",config.entity).put("transition",0).toString();if(on)body=new JSONObject(body).put("brightness",brightness).toString();HomeAssistantHttp.request(config,"/api/services/light/"+(on?"turn_on":"turn_off"),body,connections);}
}

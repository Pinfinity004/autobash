package com.pratik.imagetap;
import org.json.*;import java.util.UUID;
/** Display-relative coordinates keep markers aligned across screen resolutions. */
final class TimedAction {
 final String id,name;final ActionType action;final float x,y,endX,endY;final long delay;final int duration;final boolean enabled;
 TimedAction(String id,String name,ActionType action,float x,float y,float ex,float ey,long delay,int duration,boolean enabled){
  if(action==null||!valid(x)||!valid(y)||!valid(ex)||!valid(ey)||delay<100||delay>3600000||duration<40||duration>1000)throw new IllegalArgumentException("Use 0.1–3600 seconds and valid screen positions.");
  this.id=id==null?UUID.randomUUID().toString():id;this.name=name==null?"":name.trim();this.action=action;this.x=x;this.y=y;endX=ex;endY=ey;this.delay=delay;this.duration=duration;this.enabled=enabled;
  if(action.swipe()&&((action==ActionType.LEFT&&ex>=x)||(action==ActionType.RIGHT&&ex<=x)||(action==ActionType.UP&&ey>=y)||(action==ActionType.DOWN&&ey<=y)))throw new IllegalArgumentException("Swipe end must follow the selected direction.");
 }
 static boolean valid(float v){return Float.isFinite(v)&&v>=0&&v<=1;}
 static TimedAction create(ActionType a){return new TimedAction(null,"",a,.5f,.5f,a==ActionType.LEFT?.2f:a==ActionType.RIGHT?.8f:.5f,a==ActionType.UP?.2f:a==ActionType.DOWN?.8f:.5f,1000,a.swipe()?350:100,true);}
 String title(){return name.isEmpty()?action.label:name;}
 TimedAction enabled(boolean value){return new TimedAction(id,name,action,x,y,endX,endY,delay,duration,value);}
 JSONObject json()throws JSONException{JSONObject o=new JSONObject();o.put("id",id);o.put("name",name);o.put("action",action.id);o.put("x",x);o.put("y",y);o.put("endX",endX);o.put("endY",endY);o.put("delay",delay);o.put("duration",duration);o.put("enabled",enabled);return o;}
 static TimedAction parse(JSONObject o)throws JSONException{String kind=o.getString("action");ActionType a=ActionType.parse(kind);if(!a.id.equals(kind))throw new IllegalArgumentException("Unknown action");return new TimedAction(o.getString("id"),o.optString("name",""),a,(float)o.getDouble("x"),(float)o.getDouble("y"),(float)o.getDouble("endX"),(float)o.getDouble("endY"),o.getLong("delay"),o.getInt("duration"),o.getBoolean("enabled"));}
}

package com.pratik.imagetap;
import java.util.*;import org.json.*;
/** Persist progress, never screenshots or stale recognition candidates. */
final class SessionCheckpoint {
 final String mode;final long actions,cooldown;final boolean paused,uncertain;final Set<String> latched;final AutomationClock clock;
 SessionCheckpoint(String mode,long actions,long cooldown,boolean paused,boolean uncertain,Set<String> latched,AutomationClock clock){this.mode=mode;this.actions=actions;this.cooldown=cooldown;this.paused=paused;this.uncertain=uncertain;this.latched=latched;this.clock=clock;}
 String encode(long now)throws JSONException{
  JSONObject o=new JSONObject();o.put("version",1);o.put("mode",mode);o.put("actions",actions);o.put("cooldown",cooldown);o.put("paused",paused);o.put("uncertain",uncertain);o.put("latched",new JSONArray(latched));
  if(clock!=null){JSONObject c=new JSONObject();c.put("timeline",clock.timeline);c.put("loop",clock.loop);c.put("index",clock.index);c.put("done",clock.done);c.put("pending",clock.pending);JSONArray a=new JSONArray(),remaining=new JSONArray();long base=clock.pausedAt>=0?clock.pausedAt:now;for(int i=0;i<clock.actions.size();i++){a.put(clock.actions.get(i).json());remaining.put(Math.max(0,clock.due[i]-base));}c.put("actions",a);c.put("remaining",remaining);o.put("clock",c);}return o.toString();
 }
 static SessionCheckpoint decode(String text,long now)throws JSONException{
  JSONObject o=new JSONObject(text);if(o.getInt("version")!=1)throw new JSONException("Unknown checkpoint");String mode=o.getString("mode");if(!Arrays.asList("recognition","timeline","general").contains(mode))throw new JSONException("Unknown mode");long count=o.getLong("actions"),cooldown=o.getLong("cooldown");if(count<0||cooldown<0||cooldown>5000)throw new JSONException("Invalid progress");Set<String> keys=new HashSet<>();JSONArray l=o.getJSONArray("latched");for(int i=0;i<l.length();i++)keys.add(l.getString(i));AutomationClock clock=null;boolean uncertain=o.getBoolean("uncertain");
  if(!mode.equals("recognition")){JSONObject c=o.getJSONObject("clock");JSONArray a=c.getJSONArray("actions"),r=c.getJSONArray("remaining");if(a.length()>100||r.length()!=a.length())throw new JSONException("Invalid timer count");List<TimedAction> actions=new ArrayList<>();for(int i=0;i<a.length();i++)actions.add(TimedAction.parse(a.getJSONObject(i)));clock=new AutomationClock(actions,c.getBoolean("timeline"),c.getBoolean("loop"),now);if(clock.timeline!=mode.equals("timeline"))throw new JSONException("Invalid timer mode");clock.index=c.getInt("index");clock.done=c.getBoolean("done");if(clock.index<0||clock.index>actions.size()||(!clock.done&&clock.index==actions.size()))throw new JSONException("Invalid step");int pending=c.getInt("pending");if(pending < -1||pending>=actions.size())throw new JSONException("Invalid pending step");uncertain|=pending>=0;for(int i=0;i<r.length();i++){long wait=r.getLong(i);if(wait<0||wait>3600000)throw new JSONException("Invalid countdown");clock.due[i]=now+wait;}if(o.getBoolean("paused")||uncertain)clock.pause(now);}
  return new SessionCheckpoint(mode,count,cooldown,o.getBoolean("paused"),uncertain,keys,clock);
 }
 static String key(java.io.File f){return f.getParentFile().getName()+"/"+f.getName()+"#"+f.length()+"#"+f.lastModified();}
}

package com.pratik.imagetap;
import android.content.*;import java.util.*;import org.json.*;
final class AutomationStore {
 static List<TimedAction> load(Context c,boolean timeline){try{JSONArray a=new JSONArray(c.getSharedPreferences("automation",0).getString(timeline?"timeline":"general","[]"));List<TimedAction> out=new ArrayList<>();for(int i=0;i<a.length();i++)out.add(TimedAction.parse(a.getJSONObject(i)));return out;}catch(Exception e){throw new IllegalStateException("Timed actions could not be read. Your saved data was not changed.",e);}}
 static void save(Context c,boolean timeline,List<TimedAction> entries){if(entries.size()>100)throw new IllegalArgumentException("Maximum 100 timed actions per mode");try{JSONArray a=new JSONArray();Set<String> ids=new HashSet<>();for(TimedAction item:entries){if(!ids.add(item.id))throw new IllegalArgumentException("Duplicate action ID");a.put(item.json());}if(!c.getSharedPreferences("automation",0).edit().putString(timeline?"timeline":"general",a.toString()).commit())throw new IllegalStateException("Could not save actions");}catch(JSONException e){throw new IllegalStateException(e);}}
 static List<TimedAction> active(Context c,boolean timeline){List<TimedAction> out=load(c,timeline);out.removeIf(a->!a.enabled);return out;}
 static boolean loop(Context c){return c.getSharedPreferences("automation",0).getBoolean("loop",false);}
}

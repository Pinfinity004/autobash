package com.pratik.imagetap;
import android.content.Context;
import org.json.*;
import java.util.*;
final class RegionStore {
    static List<RegionRules.Region> decode(String json){
        List<RegionRules.Region> regions=new ArrayList<>();
        try{JSONArray a=new JSONArray(json);for(int i=0;i<Math.min(20,a.length());i++){
            try{JSONObject r=a.getJSONObject(i);regions.add(new RegionRules.Region((float)r.getDouble("l"),(float)r.getDouble("t"),(float)r.getDouble("r"),(float)r.getDouble("b"),r.getInt("a"),r.optBoolean("x",false)));}catch(JSONException|IllegalArgumentException ignored){}
        }}catch(JSONException ignored){}return regions;
    }
    static List<RegionRules.Region> load(Context c){return decode(c.getSharedPreferences("config",0).getString("regions","[]"));}
    static void save(Context c,List<RegionRules.Region> regions){
        JSONArray a=new JSONArray();for(RegionRules.Region r:regions){try{JSONObject o=new JSONObject();o.put("l",r.left);o.put("t",r.top);o.put("r",r.right);o.put("b",r.bottom);o.put("a",r.accuracy);o.put("x",r.excluded);a.put(o);}catch(JSONException e){throw new IllegalArgumentException("Could not save area",e);}}
        c.getSharedPreferences("config",0).edit().putString("regions",a.toString()).apply();
    }
}

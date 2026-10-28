package com.pratik.imagetap;
import android.content.Context;import android.content.SharedPreferences;import org.json.JSONObject;import java.io.File;
/** Per-file settings survive merge/undo because the stable filename is retained. */
final class TargetOptions {
 final boolean enabled;final int sourceWidth;final RegionRules.Region area;final String name;
 TargetOptions(boolean enabled,int width,RegionRules.Region area,String name){if(width!=0&&(width<200||width>10000))throw new IllegalArgumentException("Source width must be 0 (unknown) or 200–10000 pixels.");this.enabled=enabled;sourceWidth=width;this.area=area;this.name=name==null?"":name.trim();}
 static String key(File f){String parent=f.getParentFile()==null?"targets":f.getParentFile().getName();return parent.equals("targets")?f.getName():parent+"/"+f.getName();}
 static TargetOptions defaults(){return new TargetOptions(true,0,null,"");}
 static TargetOptions load(Context c,File f){try{JSONObject j=new JSONObject(c.getSharedPreferences("target-options",0).getString(key(f),"{}"));RegionRules.Region area=j.has("left")?new RegionRules.Region((float)j.getDouble("left"),(float)j.getDouble("top"),(float)j.getDouble("right"),(float)j.getDouble("bottom"),75):null;return new TargetOptions(j.optBoolean("enabled",true),j.optInt("sourceWidth",0),area,j.optString("name",""));}catch(Exception e){return new TargetOptions(false,0,null,"Options need repair");}}
 void save(Context c,File f){try{JSONObject j=new JSONObject();j.put("enabled",enabled);j.put("sourceWidth",sourceWidth);j.put("name",name);if(area!=null){j.put("left",area.left);j.put("top",area.top);j.put("right",area.right);j.put("bottom",area.bottom);}if(!c.getSharedPreferences("target-options",0).edit().putString(key(f),j.toString()).commit())throw new IllegalStateException("Could not save image options");}catch(org.json.JSONException e){throw new IllegalArgumentException(e);}}
 double scale(int screenWidth){return sourceWidth==0?1:screenWidth/(double)sourceWidth;}
 String summary(){return (enabled?"Active":"Disabled")+" · "+(area==null?"Full screen":"Limited area")+(sourceWidth==0?"":" · source "+sourceWidth+" px");}
}

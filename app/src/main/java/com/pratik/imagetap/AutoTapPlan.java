package com.pratik.imagetap;
import org.json.*;import java.util.*;
/** Auto Tap timings are independent of recognition and general-timer minimums. */
final class AutoTapPlan {
 static final int MAX_TARGETS=20;
 static final class Point {float x,y;int hold=0,gap=-1;Point(float x,float y){this.x=x;this.y=y;}}
 final List<Point> points=new ArrayList<>();int hold=40,gap=60,startDelay=0,cycles=0;
 AutoTapPlan(){points.add(new Point(.5f,.5f));}
 void count(int n){if(n<1||n>MAX_TARGETS)throw new IllegalArgumentException("Choose 1–20 targets");while(points.size()>n)points.remove(points.size()-1);while(points.size()<n){int i=points.size();points.add(new Point(.25f+(i%3)*.25f,.35f+(i/3)*.075f));}}
 int hold(int i){return points.get(i).hold==0?hold:points.get(i).hold;}int gap(int i){return points.get(i).gap<0?gap:points.get(i).gap;}
 void validate(){if(points.isEmpty()||points.size()>MAX_TARGETS||hold<1||hold>1000||gap<0||gap>60000||startDelay<0||startDelay>60000||cycles<0||cycles>1000000)throw new IllegalArgumentException("Hold: 1–1000 ms; gap/start: 0–60000 ms; cycles: 0–1000000");for(Point p:points)if(!Float.isFinite(p.x)||!Float.isFinite(p.y)||p.x<0||p.x>1||p.y<0||p.y>1||p.hold<0||p.hold>1000||p.gap< -1||p.gap>60000)throw new IllegalArgumentException("Invalid target coordinates or timing");}
 String encode(){validate();try{JSONObject o=new JSONObject();o.put("version",1);o.put("hold",hold);o.put("gap",gap);o.put("start",startDelay);o.put("cycles",cycles);JSONArray a=new JSONArray();for(Point p:points){JSONObject item=new JSONObject();item.put("x",p.x);item.put("y",p.y);item.put("hold",p.hold);item.put("gap",p.gap);a.put(item);}o.put("points",a);return o.toString();}catch(JSONException e){throw new IllegalStateException(e);}}
 static AutoTapPlan decode(String value){try{JSONObject o=new JSONObject(value);if(o.getInt("version")!=1)throw new IllegalArgumentException("Unsupported Auto Tap layout");AutoTapPlan p=new AutoTapPlan();p.hold=o.getInt("hold");p.gap=o.getInt("gap");p.startDelay=o.getInt("start");p.cycles=o.getInt("cycles");p.points.clear();JSONArray a=o.getJSONArray("points");for(int i=0;i<a.length();i++){JSONObject q=a.getJSONObject(i);Point point=new Point((float)q.getDouble("x"),(float)q.getDouble("y"));point.hold=q.getInt("hold");point.gap=q.getInt("gap");p.points.add(point);}p.validate();return p;}catch(JSONException e){throw new IllegalArgumentException("Auto Tap layout could not be read",e);}}
}

package com.pratik.imagetap;
import android.content.*;
/** Stable storage IDs: never rename. Tap retains all legacy paths/preferences. */
enum ActionType {
 TAP("tap","Tap"), LEFT("left","Swipe left"), RIGHT("right","Swipe right"), UP("up","Swipe up"), DOWN("down","Swipe down"), BACK("back","Back"), HOME("home","Home");
 final String id,label;
 ActionType(String id,String label){this.id=id;this.label=label;}
 boolean swipe(){return this==LEFT||this==RIGHT||this==UP||this==DOWN;}
 boolean navigation(){return this==BACK||this==HOME;}
 static ActionType parse(String id){for(ActionType a:values())if(a.id.equals(id))return a;return TAP;}
 static ActionType selected(Context c){if(c instanceof Scoped)return ((Scoped)c).action;return parse(c.getSharedPreferences("action-ui",0).getString("selected","tap"));}
 static void select(Context c,ActionType a){c.getSharedPreferences("action-ui",0).edit().putString("selected",a.id).commit();}
 static Context scope(Context c,ActionType a){return new Scoped(c,a);}
 private static final class Scoped extends ContextWrapper {final ActionType action;Scoped(Context c,ActionType a){super(c);action=a;}}
}

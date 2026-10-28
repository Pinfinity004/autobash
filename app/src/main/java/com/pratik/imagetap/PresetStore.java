package com.pratik.imagetap;
import android.content.*;import java.io.*;import java.nio.file.Files;import java.util.*;
/** Three persistent working collections. Copies commit by one preferences pointer swap. */
final class PresetStore {
 static final int COUNT=3;
 static SharedPreferences prefs(Context c){return c.getSharedPreferences(ActionType.selected(c)==ActionType.TAP?"presets":"presets-"+ActionType.selected(c).id,0);}
 static void check(int slot){if(slot<0||slot>=COUNT)throw new IllegalArgumentException("Invalid preset slot");}
 static int active(Context c){return Math.max(0,Math.min(2,prefs(c).getInt("active",0)));}
 static String name(Context c,int slot){check(slot);return prefs(c).getString("name"+slot,"Preset "+(slot+1));}
 static File folder(Context c,int slot){check(slot);String path=prefs(c).getString("dir"+slot,ActionType.selected(c)==ActionType.TAP?(slot==0?"targets":"preset-"+slot):"action-"+ActionType.selected(c).id+"-preset-"+slot);File f=new File(c.getFilesDir(),path);if(!f.isDirectory()&&!f.mkdirs())throw new IllegalStateException("Cannot open preset");return f;}
 static void open(Context c,int slot){check(slot);if(!prefs(c).edit().putInt("active",slot).commit())throw new IllegalStateException("Could not switch preset");}
 static void rename(Context c,int slot,String name){check(slot);name=name.trim();if(name.isEmpty())throw new IllegalArgumentException("Enter a name");if(!prefs(c).edit().putString("name"+slot,name).commit())throw new IllegalStateException("Could not save name");}
 static List<File> files(File folder){return TargetOrder.files(folder);}
 static synchronized void copyCurrent(Context c,int destination,String name)throws IOException{
  check(destination);if(name.trim().isEmpty())throw new IOException("Enter a name");int source=active(c);if(source==destination){rename(c,destination,name);return;}
  File previous=folder(c,destination);File stage=new File(c.getFilesDir(),"preset-copy-"+UUID.randomUUID());if(!stage.mkdirs())throw new IOException("Could not create preset");boolean committed=false;
  try{List<File> files=files(folder(c,source));if(files.size()>500)throw new IOException("Preset exceeds 500 images");for(File f:files){File out=new File(stage,f.getName());Files.copy(f.toPath(),out.toPath());try(FileOutputStream stream=new FileOutputStream(out,true)){stream.getFD().sync();}TargetOptions.load(c,f).save(c,out);}
   List<File> ordered=new ArrayList<>();for(File f:files)ordered.add(new File(stage,f.getName()));TargetOrder.save(stage,ordered);
   if(!prefs(c).edit().putString("dir"+destination,stage.getName()).putString("name"+destination,name.trim()).commit())throw new IOException("Could not commit preset");committed=true;
  }catch(RuntimeException e){throw new IOException("Could not save preset",e);}finally{if(!committed)delete(stage);}
  // Pointer is committed before old data is removed; failures never destroy the destination.
  if(committed&&!previous.equals(folder(c,source)))delete(previous);
 }
 static void delete(File f){File[] children=f.listFiles();if(children!=null)for(File child:children)delete(child);f.delete();}
}

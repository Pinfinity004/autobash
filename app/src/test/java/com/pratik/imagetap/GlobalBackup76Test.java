package com.pratik.imagetap;
import android.content.*;import android.graphics.*;import java.io.*;import java.util.*;import java.util.zip.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class GlobalBackup76Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();}
 byte[] export()throws Exception{ByteArrayOutputStream b=new ByteArrayOutputStream();GlobalArchive.exportAll(c,b);return b.toByteArray();}
 @Test public void everyActionAndSlotRoundTripsWithSettingsAndLayouts()throws Exception{
 for(ActionType a:ActionType.values())for(int i=0;i<3;i++){Context s=ActionType.scope(c,a);PresetStore.rename(s,i,a.id+i);Bitmap bitmap=Bitmap.createBitmap(3,3,Bitmap.Config.ARGB_8888);bitmap.eraseColor(Color.RED);try(OutputStream f=new FileOutputStream(new File(PresetStore.folder(s,i),"sample.png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,f);}bitmap.recycle();}
 c.getSharedPreferences("config",0).edit().putInt("similarity",91).putBoolean("tap",true).putString("app","private.local.app").commit();AutoTapPlan plan=new AutoTapPlan();plan.count(2);AutoTapStore.slot(c,1,"Two points",plan);
 byte[] bytes=export();PresetStore.rename(ActionType.scope(c,ActionType.HOME),2,"Changed");
 try(GlobalArchive.Staged staged=GlobalArchive.stage(c,new ByteArrayInputStream(bytes))){assertEquals(21,staged.count());GlobalArchive.install(c,staged);}
 for(ActionType a:ActionType.values())for(int i=0;i<3;i++)assertEquals(a.id+i,PresetStore.name(ActionType.scope(c,a),i));
 assertEquals(91,c.getSharedPreferences("config",0).getInt("similarity",0));assertFalse(c.getSharedPreferences("config",0).getBoolean("tap",true));assertEquals(2,AutoTapStore.slot(c,1).points.size());assertFalse(GlobalArchive.journal(c).exists());
 }
 @Test public void exportExcludesLiveSessionAndDeviceApp()throws Exception{c.getSharedPreferences("config",0).edit().putBoolean("tap",true).putString("sessionCheckpoint","secret-runtime").putString("app","local-app").commit();String metadata=GlobalArchive.portable(c).toString();assertFalse(metadata.contains("secret-runtime"));assertFalse(metadata.contains("local-app"));assertFalse(metadata.contains("\"tap\""));}
 @Test public void incompleteBackupDoesNotChangeExistingCollections()throws Exception{byte[] b;try(ByteArrayOutputStream out=new ByteArrayOutputStream();ZipOutputStream z=new ZipOutputStream(out)){z.putNextEntry(new ZipEntry("../bad"));z.write(1);z.closeEntry();z.finish();b=out.toByteArray();}try{GlobalArchive.stage(c,new ByteArrayInputStream(b));fail();}catch(IOException expected){}assertEquals("Preset 1",PresetStore.name(c,0));}
 @Test public void interruptedRestoreRollsBackAndDisablesActions()throws Exception{c.getSharedPreferences("config",0).edit().putInt("similarity",93).putBoolean("tap",true).commit();GlobalArchive.checkpoint(c);c.getSharedPreferences("config",0).edit().putInt("similarity",75).commit();GlobalArchive.recover(c);assertEquals(93,c.getSharedPreferences("config",0).getInt("similarity",0));assertFalse(c.getSharedPreferences("config",0).getBoolean("tap",true));assertFalse(GlobalArchive.journal(c).exists());}
}

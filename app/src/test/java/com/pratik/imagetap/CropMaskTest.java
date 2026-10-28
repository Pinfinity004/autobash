package com.pratik.imagetap;
import android.graphics.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import java.io.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CropMaskTest {
 @Test public void rectangleEraseRestoreUndoAndSavePreserveMask() throws Exception {
  Bitmap b=Bitmap.createBitmap(60,40,Bitmap.Config.ARGB_8888);b.eraseColor(Color.WHITE);
  CropView crop=new CropView(RuntimeEnvironment.getApplication(),b);crop.selection.set(10,5,50,35);
  Bitmap rectangular=crop.crop();assertEquals(40,rectangular.getWidth());assertEquals(30,rectangular.getHeight());rectangular.recycle();
  Path stroke=new Path();stroke.moveTo(20,10);stroke.lineTo(20,30);crop.edits.add(new CropView.MaskEdit(stroke,"Erase",6));
  Bitmap erased=crop.crop();assertEquals(0,Color.alpha(erased.getPixel(10,15)));assertEquals(255,Color.alpha(erased.getPixel(30,15)));
  TargetStore.save(RuntimeEnvironment.getApplication(),erased);Bitmap saved=TargetStore.load(TargetStore.files(RuntimeEnvironment.getApplication()).get(0));assertEquals(0,Color.alpha(saved.getPixel(10,15)));saved.recycle();erased.recycle();
  crop.edits.add(new CropView.MaskEdit(stroke,"Restore",3));Bitmap restored=crop.crop();assertEquals(255,Color.alpha(restored.getPixel(10,15)));restored.recycle();
  crop.undoMask();Bitmap undone=crop.crop();assertEquals(0,Color.alpha(undone.getPixel(10,15)));undone.recycle();b.recycle();
 }
 @Test public void freehandLassoIgnoresOutsideShape(){
  Bitmap b=Bitmap.createBitmap(40,40,Bitmap.Config.ARGB_8888);b.eraseColor(Color.WHITE);CropView crop=new CropView(RuntimeEnvironment.getApplication(),b);crop.selection.set(0,0,40,40);
  Path triangle=new Path();triangle.moveTo(5,5);triangle.lineTo(35,5);triangle.lineTo(20,35);triangle.close();crop.edits.add(new CropView.MaskEdit(triangle,"Lasso",1));Bitmap out=crop.crop();assertEquals(0,Color.alpha(out.getPixel(2,25)));assertEquals(255,Color.alpha(out.getPixel(20,15)));out.recycle();b.recycle();
 }
 @Test public void fiveHundredLimit()throws Exception {
  android.content.Context c=RuntimeEnvironment.getApplication();File folder=TargetStore.folder(c);for(File f:folder.listFiles())f.delete();Bitmap b=Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888);b.eraseColor(Color.WHITE);
  try{for(int i=0;i<499;i++)new File(folder,"test_"+i+".png").createNewFile();TargetStore.save(c,b);assertEquals(500,TargetStore.files(c).size());try{TargetStore.save(c,b);fail("501st image must be rejected");}catch(IOException expected){assertTrue(expected.getMessage().contains("500"));}}finally{for(File f:folder.listFiles())f.delete();b.recycle();}
 }
 @Test public void quickCaptureHandoffSavesThenReturns()throws Exception {
  android.content.Context c=RuntimeEnvironment.getApplication();File frame=new File(c.getCacheDir(),"quick-save.png");Bitmap b=Bitmap.createBitmap(24,24,Bitmap.Config.ARGB_8888);b.eraseColor(Color.WHITE);try(FileOutputStream out=new FileOutputStream(frame)){b.compress(Bitmap.CompressFormat.PNG,100,out);}b.recycle();
  int before=TargetStore.files(c).size();android.content.Intent intent=new android.content.Intent(c,MainActivity.class).putExtra("quickSave",true);
  org.robolectric.android.controller.ActivityController<MainActivity> ctl=Robolectric.buildActivity(MainActivity.class,intent).setup();MainActivity a=ctl.get();assertFalse(frame.exists());android.app.AlertDialog destination=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();destination.getListView().performItemClick(destination.getListView().getChildAt(0),0,0);android.view.View save=find(a.findViewById(android.R.id.content),"Save target");assertNotNull(save);save.performClick();assertEquals(before+1,TargetStore.files(c).size());assertTrue(a.isFinishing());ctl.pause().stop().destroy();
 }
 private android.view.View find(android.view.View v,String text){if(v instanceof android.widget.TextView&&((android.widget.TextView)v).getText().toString().equals(text))return v;if(v instanceof android.view.ViewGroup)for(int i=0;i<((android.view.ViewGroup)v).getChildCount();i++){android.view.View found=find(((android.view.ViewGroup)v).getChildAt(i),text);if(found!=null)return found;}return null;}

}

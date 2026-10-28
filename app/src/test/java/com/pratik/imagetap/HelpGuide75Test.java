package com.pratik.imagetap;
import android.app.*;import android.view.*;import android.widget.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,qualifiers="w412dp-h892dp-xhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class HelpGuide75Test {
 MainActivity a;
 @Before public void setup(){a=Robolectric.buildActivity(MainActivity.class).setup().get();}
 @After public void finish(){a.finish();}
 @Test public void homeShowsTaskChoicesAndAccessNextStep()throws Exception{View root=Release67Test.root(a);assertNotNull(AutoBashUiTest.find(root,"Tap one place repeatedly"));assertNotNull(AutoBashUiTest.find(root,"Act when a picture appears"));assertNotNull(AutoBashUiTest.find(root,"Follow a list of steps"));assertNotNull(AutoBashUiTest.find(root,"First: enable Android access"));AutoBashUiTest.save(root,"home75",824,1656);}
 @Test public void eachMainDestinationHasHelp()throws Exception{for(int tab=0;tab<6;tab++){Release67Test.tab(a,tab);assertNotNull(AutoBashUiTest.find(Release67Test.root(a),"How to use this · ?"));}}
 @Test public void everyGuideCanBeOpenedWithoutEnablingActions(){for(int i=0;i<HelpGuide.TITLES.length;i++){assertTrue(HelpGuide.STEPS[i].length>=3);HelpGuide.show(a,i);AlertDialog d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertTrue(d.isShowing());d.dismiss();}assertFalse(a.getSharedPreferences("config",0).getBoolean("tap",false));assertFalse(a.getSharedPreferences("config",0).getBoolean("sessionActive",false));}
 @Test public void nextAndBackPreserveGuideOrder(){HelpGuide.show(a,5);AlertDialog d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();d.getButton(-1).performClick();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertNotNull(AutoBashUiTest.find(d.findViewById(android.R.id.content),"STEP 2 OF 5"));d.getButton(-3).performClick();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertNotNull(AutoBashUiTest.find(d.findViewById(android.R.id.content),"STEP 1 OF 5"));d.dismiss();}
 @Test public void guideRendersWithNavigation()throws Exception{HelpGuide.show(a,0);AlertDialog d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();AutoBashUiTest.save(d.getWindow().getDecorView(),"guide75",824,1000);assertNotNull(d.getButton(-1));d.dismiss();}
}

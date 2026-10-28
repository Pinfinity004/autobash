package com.pratik.imagetap;
import android.content.*;import android.view.*;import android.widget.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,qualifiers="w412dp-h892dp-xhdpi")
public class PopupSize74Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();}
 @Test public void independentPreferencesAndDefault(){assertEquals(1,PopupSize.scale(c,"simplePopupSize",64,64),0);c.getSharedPreferences("config",0).edit().putInt("simplePopupSize",75).putInt("advancedPopupSize",150).commit();assertEquals(.75,PopupSize.scale(c,"simplePopupSize",64,64),.001);assertEquals(1.5,PopupSize.scale(c,"advancedPopupSize",264,360),.001);}
 @Test public void scaleChangesMeasuredGeometryAndText(){LinearLayout box=new LinearLayout(c);TextView label=new TextView(c);label.setTextSize(20);box.addView(label,new LinearLayout.LayoutParams(200,80));float before=label.getTextSize();PopupSize.tree(box,1.5f);assertEquals(300,label.getLayoutParams().width);assertEquals(120,label.getLayoutParams().height);assertEquals(before*1.5,label.getTextSize(),.01);}
 @Test public void clampKeepsWindowInsideScreen(){LinearLayout box=new LinearLayout(c);box.addView(new View(c),new LinearLayout.LayoutParams(100,100));WindowManager.LayoutParams p=new WindowManager.LayoutParams();p.width=200;p.x=10000;p.y=10000;PopupSize.fit(box,p,c);assertTrue(p.x+p.width<=c.getResources().getDisplayMetrics().widthPixels);assertTrue(p.y+box.getMeasuredHeight()<=c.getResources().getDisplayMetrics().heightPixels);}
 @Test @Config(qualifiers="w320dp-h360dp-xhdpi") public void requestedLargeSizeFitsShortDisplay(){c.getSharedPreferences("config",0).edit().putInt("advancedPopupSize",150).commit();float s=PopupSize.scale(c,"advancedPopupSize",264,360);assertTrue(s<=1);assertTrue(s>0);}
}

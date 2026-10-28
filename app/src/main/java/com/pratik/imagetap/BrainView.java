package com.pratik.imagetap;
import android.content.Context;import android.graphics.*;import android.os.SystemClock;import android.view.View;
final class BrainView extends View {
 private final Paint paint=new Paint(3);private final boolean halo;
 BrainView(Context c,boolean halo){super(c);this.halo=halo;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 @Override protected void onDraw(Canvas c){float size=Math.min(getWidth(),getHeight()),x=(getWidth()-size)/2,y=(getHeight()-size)/2;float phase=Ui.motion(getContext())?(float)(.5+.5*Math.sin(SystemClock.uptimeMillis()/700.0)):0;
  if(halo){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Ui.dp(getContext(),1));paint.setColor(Ui.LINE);c.drawCircle(getWidth()/2f,getHeight()/2f,size*.49f,paint);paint.setColor(Ui.GREEN);paint.setAlpha((int)(15+20*phase));paint.setStrokeWidth(Ui.dp(getContext(),5));c.drawCircle(getWidth()/2f,getHeight()/2f,size*.45f,paint);paint.setAlpha(255);}
  BrainArt.draw(c,paint,x+size*.05f,y+size*.02f,size*.9f,Ui.CREAM,phase);if(halo&&Ui.motion(getContext())&&isShown()&&getWindowVisibility()==VISIBLE)postInvalidateDelayed(50);
 }
}

package com.pratik.imagetap;
import android.content.Context;import android.graphics.*;import android.view.View;
final class BrainStopView extends View {
 private final Paint p=new Paint(3);BrainStopView(Context c){super(c);setFocusable(true);setClickable(true);setContentDescription("Stop AutoBash session. Drag to move.");setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
 @Override protected void onDraw(Canvas c){float size=Math.min(getWidth(),getHeight());p.setStyle(Paint.Style.FILL);p.setColor(Ui.SURFACE);c.drawCircle(size/2,size/2,size*.49f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Ui.dp(getContext(),1));p.setColor(Ui.LINE);c.drawCircle(size/2,size/2,size*.48f,p);BrainArt.draw(c,p,size*.1f,size*.06f,size*.8f,Ui.CREAM,0);p.setStyle(Paint.Style.FILL);p.setColor(Ui.BG);c.drawCircle(size*.5f,size*.49f,size*.16f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(Ui.dp(getContext(),2.3f));p.setColor(Ui.RED);float d=size*.065f,x=size*.5f,y=size*.49f;c.drawLine(x-d,y-d,x+d,y+d,p);c.drawLine(x+d,y-d,x-d,y+d,p);}
 @Override public boolean performClick(){super.performClick();return true;}
}

package com.pratik.imagetap;
import android.content.Context;import android.graphics.*;import android.os.SystemClock;import android.view.View;import android.widget.ProgressBar;
/** The travelling signal uses the real pulse bar, or an indeterminate wave during recognition. */
final class NerveStrip extends View {
 private final Paint p=new Paint(3);private final Path route=new Path();private PathMeasure measure;private final float[] point=new float[2];private final ProgressBar progress;private boolean active=true;
 NerveStrip(Context c,ProgressBar progress){super(c);this.progress=progress;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 void setActive(boolean value){active=value;invalidate();}
 @Override protected void onSizeChanged(int w,int h,int ow,int oh){route.reset();route.moveTo(0,h*.5f);for(int i=1;i<=12;i++)route.lineTo(w*i/12f,h*(i%3==0?.5f:i%2==0?.25f:.75f));measure=new PathMeasure(route,false);}
 @Override protected void onDraw(Canvas c){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Ui.dp(getContext(),1));p.setColor(Ui.LINE);c.drawPath(route,p);for(int i=2;i<12;i+=3){float x=getWidth()*i/12f;c.drawLine(x,getHeight()*.5f,x+Ui.dp(getContext(),5),getHeight()*.05f,p);}if(active&&measure!=null){float phase=progress.isIndeterminate()?(SystemClock.uptimeMillis()%1600)/1600f:progress.getProgress()/1000f;if(!Ui.motion(getContext()))phase=.5f;measure.getPosTan(measure.getLength()*phase,point,null);p.setStyle(Paint.Style.FILL);p.setColor(Ui.GREEN);p.setAlpha(35);c.drawCircle(point[0],point[1],Ui.dp(getContext(),7),p);p.setAlpha(255);c.drawCircle(point[0],point[1],Ui.dp(getContext(),2.3f),p);if(Ui.motion(getContext())&&isShown()&&getWindowVisibility()==VISIBLE)postInvalidateDelayed(50);}}
}

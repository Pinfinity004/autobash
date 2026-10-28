package com.pratik.imagetap;
import android.content.Context;import android.graphics.*;import android.os.SystemClock;import android.view.View;
/** Decorative focus illustration, not a live recognition result. */
final class FocusPreview extends View {
    private final Paint p=new Paint(3);private final long start=SystemClock.uptimeMillis();
    FocusPreview(Context c){super(c);setContentDescription("Illustration of a focused target");}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=getResources().getDisplayMetrics().density,cx=getWidth()/2f,cy=getHeight()/2f;p.setColor(Ui.LINE);p.setStrokeWidth(d);for(int i=-3;i<=3;i++)for(int j=-1;j<=1;j++)c.drawCircle(cx+i*36*d,cy+j*36*d,d,p);boolean motion=Ui.motion(getContext());float t=motion?(SystemClock.uptimeMillis()-start)/900f:0;
        p.setStyle(Paint.Style.FILL);p.setColor(0x1283efb3);c.drawCircle(cx,cy,57*d,p);c.drawCircle(cx,cy,43*d,p);p.setColor(Ui.GREEN);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*d);float s=32*d,a=11*d;Path path=new Path();path.moveTo(cx-s,cy-s+a);path.lineTo(cx-s,cy-s);path.lineTo(cx-s+a,cy-s);path.moveTo(cx+s-a,cy-s);path.lineTo(cx+s,cy-s);path.lineTo(cx+s,cy-s+a);path.moveTo(cx+s,cy+s-a);path.lineTo(cx+s,cy+s);path.lineTo(cx+s-a,cy+s);path.moveTo(cx-s+a,cy+s);path.lineTo(cx-s,cy+s);path.lineTo(cx-s,cy+s-a);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);p.setColor(Ui.GREEN);c.drawCircle(cx,cy,5*d,p);p.setStyle(Paint.Style.STROKE);p.setColor(0x5583efb3);c.drawCircle(cx,cy,(16+(float)Math.sin(t)*3)*d,p);p.setStyle(Paint.Style.FILL);if(motion&&getWindowVisibility()==VISIBLE)postInvalidateDelayed(33);
    }
}

package com.pratik.imagetap;
import android.graphics.*;
/** Original, resolution-independent circuit brain. No bitmap textures or external assets. */
final class BrainArt {
 private static final Path OUTLINE=new Path();
 private static final float[][] TRACES={{44,22,36,22,30,28,22,28},{44,30,36,30,27,39,17,39},{43,38,37,38,31,44,31,53,24,60,16,60},{22,47,16,47,12,51},{43,47,38,52,38,64,31,71,24,71},{43,58,43,72,37,78,31,78},{18,66,18,70,24,76},{30,33,25,33,21,37},{26,48,21,53,15,53},{43,80,39,84}};
 static {OUTLINE.moveTo(47,18);OUTLINE.cubicTo(43,9,32,10,28,17);OUTLINE.cubicTo(18,16,12,24,14,32);OUTLINE.cubicTo(5,36,4,47,9,53);OUTLINE.cubicTo(5,63,10,72,18,74);OUTLINE.cubicTo(19,84,29,89,37,84);OUTLINE.cubicTo(42,85,47,80,47,74);OUTLINE.lineTo(47,18);}
 static void draw(Canvas c,Paint p,float left,float top,float size,int color,float signal){c.save();c.translate(left,top);c.scale(size/100,size/100);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
  for(int side=0;side<2;side++){c.save();if(side==1){c.translate(100,0);c.scale(-1,1);}p.setColor(color);p.setStrokeWidth(1.7f);c.drawPath(OUTLINE,p);p.setStrokeWidth(1.15f);for(float[] line:TRACES){for(int i=2;i<line.length;i+=2)c.drawLine(line[i-2],line[i-1],line[i],line[i+1],p);}p.setStyle(Paint.Style.FILL);p.setColor(Ui.GREEN);for(int i=0;i<TRACES.length;i+=3){float[] l=TRACES[i];c.drawCircle(l[0],l[1],1.5f,p);if(signal>0){p.setAlpha((int)(60*signal));c.drawCircle(l[0],l[1],4,p);p.setAlpha(255);}}p.setStyle(Paint.Style.STROKE);c.restore();}
  p.setColor(color);p.setStrokeWidth(1.4f);c.drawLine(48,87,48,93,p);c.drawLine(52,87,52,93,p);p.setAlpha(255);c.restore();
 }
}

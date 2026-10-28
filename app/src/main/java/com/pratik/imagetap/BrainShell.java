package com.pratik.imagetap;
import android.graphics.*;import android.graphics.drawable.Drawable;
/** Soft cortex-like scallops and a small stem; content stays inside a regular readable panel. */
final class BrainShell extends Drawable {
 private final Paint p=new Paint(3);private final Path path=new Path();
 @Override public void draw(Canvas c){Rect b=getBounds();float w=b.width(),h=b.height(),r=20;path.reset();path.moveTo(r,8);path.cubicTo(w*.2f,-2,w*.32f,0,w*.4f,5);path.cubicTo(w*.55f,-2,w*.68f,0,w*.77f,5);path.cubicTo(w*.92f,0,w,10,w,h*.16f);path.lineTo(w,h-r-12);path.quadTo(w,h-12,w-r,h-12);path.lineTo(w*.82f,h-12);path.quadTo(w*.81f,h,w*.76f,h);path.lineTo(w*.7f,h-12);path.lineTo(r,h-12);path.quadTo(0,h-12,0,h-r-12);path.lineTo(0,r+8);path.quadTo(0,8,r,8);path.close();p.setStyle(Paint.Style.FILL);p.setColor(0xfa101e32);c.drawPath(path,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(Ui.LINE);c.drawPath(path,p);}
 @Override public void setAlpha(int a){p.setAlpha(a);invalidateSelf();}@Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}

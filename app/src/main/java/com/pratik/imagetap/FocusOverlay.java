package com.pratik.imagetap;
import android.content.Context;import android.graphics.*;import android.os.SystemClock;import android.view.View;import java.util.*;
/** Visual-only feedback. Hidden before screenshots; never sends gestures. */
final class FocusOverlay extends View {
    private final Paint p=new Paint(3);private final Path path=new Path();private final RectF target=new RectF();
    private List<RegionRules.Region> areas=Collections.emptyList();private boolean scanning,guides=true,reduced;private String label="";private long changedAt,pulseAt=-10000;private float px,py;
    FocusOverlay(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    void preferences(boolean guides,boolean reduced){this.guides=guides;this.reduced=reduced||!android.animation.ValueAnimator.areAnimatorsEnabled();invalidate();}
    void scanning(List<RegionRules.Region> regions){areas=new ArrayList<>(regions);scanning=true;target.setEmpty();invalidate();}
    void candidate(Rect rect,String label){scanning=false;this.label=label;if(rect==null)target.setEmpty();else target.set(rect);changedAt=SystemClock.uptimeMillis();invalidate();}
    private ActionType effect=ActionType.TAP;private ActionGesture gesture;
    void action(ActionType action,ActionGesture geometry){effect=action;gesture=geometry;px=geometry.x;py=geometry.y;pulseAt=SystemClock.uptimeMillis();invalidate();}
    void pulse(float x,float y){effect=ActionType.TAP;px=x;py=y;pulseAt=SystemClock.uptimeMillis();invalidate();}
    private float dp(float v){return v*getResources().getDisplayMetrics().density;}
    private void corners(Canvas c,RectF r,int alpha,float width){float arm=Math.min(dp(18),Math.min(r.width(),r.height())*.32f);path.reset();path.moveTo(r.left,r.top+arm);path.lineTo(r.left,r.top);path.lineTo(r.left+arm,r.top);path.moveTo(r.right-arm,r.top);path.lineTo(r.right,r.top);path.lineTo(r.right,r.top+arm);path.moveTo(r.right,r.bottom-arm);path.lineTo(r.right,r.bottom);path.lineTo(r.right-arm,r.bottom);path.moveTo(r.left+arm,r.bottom);path.lineTo(r.left,r.bottom);path.lineTo(r.left,r.bottom-arm);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setColor(Ui.GREEN);p.setAlpha(alpha);p.setStrokeWidth(width);c.drawPath(path,p);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);long now=SystemClock.uptimeMillis();boolean animate=false;
        if(guides&&scanning){c.save();for(RegionRules.Region a:areas)if(a.excluded)c.clipOutRect(a.left*getWidth(),a.top*getHeight(),a.right*getWidth(),a.bottom*getHeight());corners(c,new RectF(dp(8),dp(8),getWidth()-dp(8),getHeight()-dp(8)),65,dp(1));for(RegionRules.Region a:areas)if(!a.excluded)corners(c,new RectF(a.left*getWidth(),a.top*getHeight(),a.right*getWidth(),a.bottom*getHeight()),85,dp(1));c.restore();}
        if(guides&&!target.isEmpty()){float breath=reduced?1f:(float)(.85+.15*Math.sin((now-changedAt)/220.0));corners(c,target,(int)(18*breath),dp(12));corners(c,target,(int)(45*breath),dp(6));corners(c,target,240,dp(2));p.setStyle(Paint.Style.FILL);p.setColor(Ui.GREEN);p.setAlpha(12);c.drawRoundRect(target,dp(7),dp(7),p);p.setAlpha(190);c.drawCircle(target.centerX(),target.centerY(),dp(2.5f),p);p.setTextSize(dp(11));p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));float tw=p.measureText(label),x=Math.max(dp(8),Math.min(target.left,getWidth()-tw-dp(24))),y=target.top>dp(36)?target.top-dp(10):target.bottom+dp(22);y=Math.min(getHeight()-dp(10),y);p.setColor(0xf0152c20);c.drawRoundRect(x-dp(6),y-dp(16),x+tw+dp(8),y+dp(5),dp(7),dp(7),p);p.setColor(Ui.GREEN);c.drawText(label,x,y,p);animate=!reduced;}
        long elapsed=now-pulseAt,duration=reduced?280:720;if(elapsed>=0&&elapsed<duration&&effect!=ActionType.TAP){drawAction(c,elapsed/(float)duration);animate=true;}
        if(elapsed>=0&&elapsed<duration&&effect==ActionType.TAP){float t=elapsed/(float)duration,ease=1-(1-t)*(1-t),r=dp(reduced?18:10+42*ease);int alpha=(int)(230*(1-t));p.setColor(Ui.GREEN);p.setAlpha(alpha/6);p.setStyle(Paint.Style.FILL);c.drawCircle(px,py,r+dp(12),p);p.setAlpha(alpha);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));c.drawCircle(px,py,r,p);p.setAlpha(alpha/2);c.drawCircle(px,py,r*.65f,p);p.setStyle(Paint.Style.FILL);p.setAlpha(alpha);c.drawCircle(px,py,dp(4)*(1-t*.7f),p);animate=true;}
        if(animate&&getVisibility()==VISIBLE)postInvalidateOnAnimation();
    }
    private void drawAction(Canvas c,float t){
        p.setColor(effect==ActionType.BACK?0xffe8cda8:effect==ActionType.HOME?0xffb3c9f2:Ui.GREEN);p.setAlpha((int)(235*(1-t)));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));p.setStrokeCap(Paint.Cap.ROUND);
        if(effect.swipe()&&gesture!=null){
            float progress=reduced?1:Math.min(1,t*1.8f),ex=gesture.x+(gesture.endX-gesture.x)*progress,ey=gesture.y+(gesture.endY-gesture.y)*progress;
            c.drawLine(gesture.x,gesture.y,ex,ey,p);float angle=(float)Math.atan2(gesture.endY-gesture.y,gesture.endX-gesture.x);
            for(int side:new int[]{-1,1})c.drawLine(ex,ey,ex-dp(12)*(float)Math.cos(angle+side*.55),ey-dp(12)*(float)Math.sin(angle+side*.55),p);
            if(!reduced)for(int i=0;i<8;i++){float f=i/8f*progress,sx=gesture.x+(gesture.endX-gesture.x)*f,sy=gesture.y+(gesture.endY-gesture.y)*f,offset=dp(5)*(float)Math.sin(i*2+t*9);sx+=(float)Math.cos(angle+Math.PI/2)*offset;sy+=(float)Math.sin(angle+Math.PI/2)*offset;float r=dp(2+(i%3));c.drawLine(sx-r,sy,sx+r,sy,p);c.drawLine(sx,sy-r,sx,sy+r,p);}
        }else{
            float cx=getWidth()/2f,cy=getHeight()/2f,r=dp(24)*(reduced?1:1+t*.3f);path.reset();
            if(effect==ActionType.BACK){for(int i=0;i<2;i++){float x=cx+(i-.5f)*dp(18)-(reduced?0:t*dp(22));path.moveTo(x+r*.4f,cy-r);path.lineTo(x-r*.4f,cy);path.lineTo(x+r*.4f,cy+r);}}
            else{path.moveTo(cx-r,cy);path.lineTo(cx,cy-r);path.lineTo(cx+r,cy);path.moveTo(cx-r*.7f,cy-r*.2f);path.lineTo(cx-r*.7f,cy+r);path.lineTo(cx+r*.7f,cy+r);path.lineTo(cx+r*.7f,cy-r*.2f);c.drawCircle(cx,cy,r*(1.5f+t),p);}
            c.drawPath(path,p);
        }
    }

}

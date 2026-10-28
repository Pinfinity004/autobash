package com.pratik.imagetap;
/** Screen-pixel gesture geometry, shared by dispatch, preview and tests. */
final class ActionGesture {
 final float x,y,endX,endY;final int duration;
 ActionGesture(ActionType action,int x,int y,int width,int height,int percent,int ms){
  this.x=x;this.y=y;float dx=0,dy=0;float fraction=Math.max(10,Math.min(70,percent))/100f;
  if(action==ActionType.LEFT)dx=-width*fraction;if(action==ActionType.RIGHT)dx=width*fraction;
  if(action==ActionType.UP)dy=-height*fraction;if(action==ActionType.DOWN)dy=height*fraction;
  endX=Math.max(1,Math.min(width-2,x+dx));endY=Math.max(1,Math.min(height-2,y+dy));duration=Math.max(150,Math.min(1000,ms));
 }
 ActionGesture(float x,float y,float ex,float ey,int duration){this.x=x;this.y=y;endX=ex;endY=ey;this.duration=duration;}
 boolean tooShort(){return Math.hypot(endX-x,endY-y)<12;}
 boolean crosses(float l,float t,float r,float b){return Math.max(x,endX)>=l&&Math.min(x,endX)<=r&&Math.max(y,endY)>=t&&Math.min(y,endY)<=b;}
}

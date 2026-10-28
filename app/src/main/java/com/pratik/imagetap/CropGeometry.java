package com.pratik.imagetap;

/** Original-image coordinates remain independent of zoom and pan. */
final class CropGeometry {
    final float imageWidth,imageHeight;
    float viewWidth,viewHeight,scale=1,minScale=1,tx,ty;
    CropGeometry(int w,int h){imageWidth=w;imageHeight=h;}
    void resize(int w,int h){viewWidth=w;viewHeight=h;reset();}
    void reset(){if(viewWidth<=0||viewHeight<=0)return;minScale=Math.min(viewWidth/imageWidth,viewHeight/imageHeight);scale=minScale;tx=(viewWidth-imageWidth*scale)/2;ty=(viewHeight-imageHeight*scale)/2;}
    void zoom(float multiplier,float focusX,float focusY){
        float next=Math.max(minScale,Math.min(minScale*48,scale*multiplier));
        float ratio=next/scale;tx=focusX-(focusX-tx)*ratio;ty=focusY-(focusY-ty)*ratio;scale=next;bound();
    }
    void pan(float dx,float dy){tx+=dx;ty+=dy;bound();}
    private void bound(){
        float w=imageWidth*scale,h=imageHeight*scale;
        tx=w<=viewWidth?(viewWidth-w)/2:Math.max(viewWidth-w,Math.min(0,tx));
        ty=h<=viewHeight?(viewHeight-h)/2:Math.max(viewHeight-h,Math.min(0,ty));
    }
    float imageX(float x){return Math.max(0,Math.min(imageWidth,(x-tx)/scale));}
    float imageY(float y){return Math.max(0,Math.min(imageHeight,(y-ty)/scale));}
    float screenX(float x){return tx+x*scale;}
    float screenY(float y){return ty+y*scale;}
}

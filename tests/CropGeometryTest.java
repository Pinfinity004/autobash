package com.pratik.imagetap;
public class CropGeometryTest {
    static void close(float expected,float actual,String label){if(Math.abs(expected-actual)>.01)throw new AssertionError(label+": "+actual+" expected "+expected);}
    public static void main(String[] args){
        CropGeometry g=new CropGeometry(1000,2000);g.resize(500,600);
        close(.3f,g.scale,"fit scale");close(100,g.tx,"centered horizontally");
        float x=g.imageX(250),y=g.imageY(300);g.zoom(3,250,300);
        close(x,g.imageX(250),"zoom focus X");close(y,g.imageY(300),"zoom focus Y");
        // A crop drawn in original pixels must map to the same source area after panning/zooming.
        g.pan(40,-70);float sx=g.screenX(400),sy=g.screenY(700);
        close(400,g.imageX(sx),"source X after pan");close(700,g.imageY(sy),"source Y after pan");
        g.zoom(100,250,300);close(g.minScale*48,g.scale,"max zoom");
        g.pan(999999,999999);close(0,g.tx,"right pan boundary");close(0,g.ty,"bottom pan boundary");
        g.pan(-999999,-999999);close(g.viewWidth-g.imageWidth*g.scale,g.tx,"left pan boundary");close(g.viewHeight-g.imageHeight*g.scale,g.ty,"top pan boundary");
        g.reset();close(.3f,g.scale,"reset scale");close(100,g.tx,"reset center");
        close(0,g.imageX(-300),"clamp left");close(1000,g.imageX(2000),"clamp right");
        g.zoom(.01f,250,300);close(g.minScale,g.scale,"min zoom");
        System.out.println("PASS: crop coordinate mapping, focus zoom, pan limits, 48x limit, fit reset");
    }
}

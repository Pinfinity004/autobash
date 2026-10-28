package com.pratik.imagetap;
import java.util.List;
/** Regions are relative to the full screen, not the position of an imported target. */
final class RegionRules {
    static final class Region {
        final float left,top,right,bottom;final int accuracy;final boolean excluded;
        Region(float l,float t,float r,float b,int a){this(l,t,r,b,a,false);}
        Region(float l,float t,float r,float b,int a,boolean exclude){
            excluded=exclude;
            if(!Float.isFinite(l)||!Float.isFinite(t)||!Float.isFinite(r)||!Float.isFinite(b))throw new IllegalArgumentException("Invalid area");
            left=Math.max(0,Math.min(1,l));top=Math.max(0,Math.min(1,t));right=Math.max(0,Math.min(1,r));bottom=Math.max(0,Math.min(1,b));accuracy=TapTiming.similarity(a);
            if(right<=left||bottom<=top)throw new IllegalArgumentException("Draw an area first");
        }
        Region withAccuracy(int value){return new Region(left,top,right,bottom,value,excluded);}
    }
    static byte[] accuracyMap(int width,int height,int fallback,List<Region> regions){
        if(regions.isEmpty())return null;
        byte[] map=new byte[width*height];
        for(Region r:regions){
            int l=Math.max(0,(int)Math.ceil(r.left*width-.5)),t=Math.max(0,(int)Math.ceil(r.top*height-.5));
            int right=Math.min(width,(int)Math.ceil(r.right*width-.5)),bottom=Math.min(height,(int)Math.ceil(r.bottom*height-.5));
            for(int y=t;y<bottom;y++)for(int x=l;x<right;x++){int index=y*width+x;map[index]=r.excluded||map[index]<0?(byte)-1:(byte)Math.max(map[index],r.accuracy);}
        }
        byte base=(byte)TapTiming.similarity(fallback);for(int i=0;i<map.length;i++)if(map[i]==0)map[i]=base;
        return map;
    }
}

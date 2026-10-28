package com.pratik.imagetap;
import java.util.*;
import java.util.concurrent.CancellationException;

/** Conservative duplicate suggestions, independent from the live detection score. */
final class DuplicateSimilarity {
    static final class Signature {
        final int width,height;final int[] pixels;final String pixelHash;
        Signature(int w,int h,int[] normalized,String hash){width=w;height=h;pixels=normalized;pixelHash=hash;}
    }
    static double score(Signature a,Signature b){
        if(a.width==b.width&&a.height==b.height&&a.pixelHash.equals(b.pixelHash))return 100;
        if(Math.abs((a.width/(double)a.height)/(b.width/(double)b.height)-1)>.01||a.pixels.length!=b.pixels.length)return 0;
        double error=0,alphaError=0,sa=0,sb=0,saa=0,sbb=0,sab=0;int n=a.pixels.length;
        for(int i=0;i<n;i++){
            int p=a.pixels[i],q=b.pixels[i],ap=p>>>24,aq=q>>>24;alphaError+=Math.abs(ap-aq);
            if(ap<16&&aq<16)continue;
            double x=0,y=0;for(int c=0;c<3;c++){int ca=(p>>(c*8))&255,cb=(q>>(c*8))&255;error+=Math.abs(ca-cb);x+=ca/3.0;y+=cb/3.0;}
            sa+=x;sb+=y;saa+=x*x;sbb+=y*y;sab+=x*y;
        }
        double ma=sa/n,mb=sb/n,va=Math.max(0,saa/n-ma*ma),vb=Math.max(0,sbb/n-mb*mb),cov=sab/n-ma*mb;
        double ssim=((2*ma*mb+6.5025)*(2*cov+58.5225))/((ma*ma+mb*mb+6.5025)*(va+vb+58.5225));
        double pixel=1-error/(n*3*255.0),alpha=1-alphaError/(n*255.0);
        return Math.min(99.999,Math.max(0,100*Math.min(pixel,Math.min(ssim,alpha))));
    }
    /** Complete-link groups prevent A≈B≈C from silently merging A and C if A is not close to C. */
    static List<List<Integer>> groups(List<Signature> images,int threshold){
        List<List<Integer>> groups=new ArrayList<>();
        for(int i=0;i<images.size();i++){
            boolean placed=false;
            for(List<Integer> group:groups){
                boolean all=true;for(int j:group){if(Thread.currentThread().isInterrupted())throw new CancellationException();if(score(images.get(i),images.get(j))+1e-9<threshold){all=false;break;}}
                if(all){group.add(i);placed=true;break;}
            }
            if(!placed){List<Integer> singleton=new ArrayList<>();singleton.add(i);groups.add(singleton);}
        }
        groups.removeIf(g->g.size()<2);return groups;
    }
}

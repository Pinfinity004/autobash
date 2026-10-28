package com.pratik.imagetap;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.*;

/** Native normalized-correlation search followed by independent structural/color verification. */
public final class VisionMatcher {
    public static final class Hit {
        public final int x,y,width,height,accuracy;public final double score;
        Hit(int x,int y,int w,int h,double score,int accuracy){this.x=x;this.y=y;width=w;height=h;this.score=score;this.accuracy=accuracy;}
    }
    public static Mat fromArgb(int[] pixels,int width,int height){
        byte[] bytes=new byte[width*height*4];for(int i=0;i<pixels.length;i++){int c=pixels[i];bytes[4*i]=(byte)(c>>16);bytes[4*i+1]=(byte)(c>>8);bytes[4*i+2]=(byte)c;bytes[4*i+3]=(byte)(c>>>24);}
        Mat rgba=new Mat(height,width,CvType.CV_8UC4);rgba.put(0,0,bytes);return rgba;
    }
    public static final class Frame implements AutoCloseable {
        final Mat original,rgba,gray,edges;final byte[] pixels,map;final int width,height,originalW,originalH;final double factor;
        final int[] blocked;byte[] originalPixels;final List<RegionRules.Region> areas;
        RegionRules.Region searchArea;java.util.function.BooleanSupplier cancelled=()->false;
        double closest;int closestRequired;boolean strictMismatch;
        /** Takes ownership of originalRGBA. Regions apply to all templates. */
        public Frame(Mat originalRGBA,int workingWidth,int fallback,List<RegionRules.Region> regions){
            areas=new ArrayList<>(regions);original=originalRGBA;originalW=original.cols();originalH=original.rows();factor=Math.min(1.0,workingWidth/(double)originalW);
            width=(int)Math.round(originalW*factor);height=(int)Math.round(originalH*factor);rgba=new Mat();
            Imgproc.resize(original,rgba,new Size(width,height),0,0,Imgproc.INTER_AREA);gray=new Mat();Imgproc.cvtColor(rgba,gray,Imgproc.COLOR_RGBA2GRAY);
            pixels=new byte[width*height*4];rgba.get(0,0,pixels);map=RegionRules.accuracyMap(width,height,fallback,regions);
            blocked=new int[(width+1)*(height+1)];byte[] mask=new byte[width*height];
            for(int y=0;y<height;y++){int row=0;for(int x=0;x<width;x++){boolean skip=map!=null&&map[y*width+x]<0;row+=skip?1:0;blocked[(y+1)*(width+1)+x+1]=blocked[y*(width+1)+x+1]+row;if(skip)mask[y*width+x]=(byte)255;}}
            Mat ignored=new Mat(height,width,CvType.CV_8UC1);try{ignored.put(0,0,mask);gray.setTo(new Scalar(0),ignored);}finally{ignored.release();}
            edges=new Mat();Imgproc.Canny(gray,edges,50,120);
            baseAccuracy=TapTiming.similarity(fallback);
        }
        final int baseAccuracy;
        int accuracy(int x,int y,int w,int h){return map==null?baseAccuracy:map[Math.min(height-1,y+h/2)*width+Math.min(width-1,x+w/2)];}
        boolean allowed(int x,int y,int w,int h){
            if(x<0||y<0||w<1||h<1||x+w>width||y+h>height)return false;
            int stride=width+1;return blocked[(y+h)*stride+x+w]-blocked[y*stride+x+w]-blocked[(y+h)*stride+x]+blocked[y*stride+x]==0;
        }
        boolean originalAllowed(int x,int y,int w,int h){if(searchArea!=null&&(x<Math.ceil(searchArea.left*originalW)||y<Math.ceil(searchArea.top*originalH)||x+w>Math.floor(searchArea.right*originalW)||y+h>Math.floor(searchArea.bottom*originalH)))return false;for(RegionRules.Region r:areas)if(r.excluded&&x+w>r.left*originalW&&x<r.right*originalW&&y+h>r.top*originalH&&y<r.bottom*originalH)return false;return true;}
        void maskCandidates(Mat result,int left,int top,int w,int h){
            for(RegionRules.Region r:areas)if(r.excluded){
                int l=Math.max(0,(int)Math.floor(r.left*width)-w+1-left),t=Math.max(0,(int)Math.floor(r.top*height)-h+1-top),right=Math.min(result.cols(),(int)Math.ceil(r.right*width)-left),bottom=Math.min(result.rows(),(int)Math.ceil(r.bottom*height)-top);
                if(right>l&&bottom>t){Mat cut=result.submat(t,bottom,l,right);try{cut.setTo(new Scalar(-2));}finally{cut.release();}}
            }
        }
        void observe(double score,int required,boolean mismatch){if(score>closest){closest=score;closestRequired=required;strictMismatch=mismatch;}}
        String diagnostic(){if(closest<=0)return "No close match";return strictMismatch?String.format(java.util.Locale.US,"Closest shape %.1f%% · exact pixels differ",closest):String.format(java.util.Locale.US,"Closest %.1f%% / need %d%%",closest,closestRequired);}
        byte[] originalBytes(){if(originalPixels==null){originalPixels=new byte[originalW*originalH*4];original.get(0,0,originalPixels);}return originalPixels;}
        @Override public void close(){original.release();rgba.release();gray.release();edges.release();}
    }
    private static final double[] SCALES={1,.95,1.05,.9,1.1,.8,1.25,.65,1.5};
    public static Hit find(Frame frame,Mat template,Hit previous,long deadlineNanos){
        return find(frame,template,previous,deadlineNanos,1,null);
    }
    public static Hit find(Frame frame,Mat template,Hit previous,long deadlineNanos,double baseScale,RegionRules.Region area){
        if(!Double.isFinite(baseScale)||baseScale<=0)return null;
        frame.searchArea=area;
        try{return findScaled(frame,template,previous,deadlineNanos,baseScale);}finally{frame.searchArea=null;}
    }
    public static Hit findExpected(Frame frame,Mat template,Hit previous,double scale,RegionRules.Region area){
        if(frame.cancelled.getAsBoolean()||!Double.isFinite(scale)||scale<=0)return null;frame.searchArea=area;
        try{if(previous!=null){Hit tracked=search(frame,template,previous.width/(double)template.cols(),previous,Long.MAX_VALUE);if(tracked!=null)return tracked;}return search(frame,template,scale,null,Long.MAX_VALUE);}finally{frame.searchArea=null;}
    }
    private static Hit findScaled(Frame frame,Mat template,Hit previous,long deadlineNanos,double baseScale){
        if(frame.cancelled.getAsBoolean()||template.empty()||template.channels()!=4)return null;
        Hit best=null;
        if(previous!=null){double scale=previous.width/(double)template.cols();best=search(frame,template,scale,previous,deadlineNanos);if(best!=null)return best;}
        double bestScale=baseScale;
        for(double multiplier:SCALES){double scale=baseScale*multiplier;
            if(frame.cancelled.getAsBoolean()||expired(deadlineNanos))break;Hit h=search(frame,template,scale,null,deadlineNanos);
            if(h!=null&&(best==null||h.score>best.score)){best=h;bestScale=scale;}if(best!=null&&best.score==100)return best;
        }
        // Refine the size around a viable match instead of accepting only a handful of fixed sizes.
        if(best!=null)for(double offset:new double[]{-.025,.025,-.05,.05}){
            if(frame.cancelled.getAsBoolean()||expired(deadlineNanos))break;Hit h=search(frame,template,bestScale+baseScale*offset,best,deadlineNanos);if(h!=null&&h.score>best.score)best=h;
        }
        return best;
    }
    private static boolean expired(long deadline){return Thread.currentThread().isInterrupted()||System.nanoTime()>deadline;}
    private static Hit search(Frame f,Mat original,double scale,Hit around,long deadline){
        int w=(int)Math.round(original.cols()*f.factor*scale),h=(int)Math.round(original.rows()*f.factor*scale);
        if(f.cancelled.getAsBoolean()||w<8||h<8||w>f.width||h>f.height)return null;
        int left=f.searchArea==null?0:(int)Math.ceil(f.searchArea.left*f.width),top=f.searchArea==null?0:(int)Math.ceil(f.searchArea.top*f.height),right=f.searchArea==null?f.width:(int)Math.floor(f.searchArea.right*f.width),bottom=f.searchArea==null?f.height:(int)Math.floor(f.searchArea.bottom*f.height);
        if(around!=null){int ax=(int)Math.round(around.x*f.factor),ay=(int)Math.round(around.y*f.factor);int margin=Math.max(12,(int)Math.round(36*f.factor));left=Math.max(left,ax-margin);top=Math.max(top,ay-margin);right=Math.min(right,ax+w+margin);bottom=Math.min(bottom,ay+h+margin);}
        if(right-left<w||bottom-top<h)return null;
        Mat color=new Mat(),gray=new Mat(),alpha=new Mat(),result=new Mat(),view=f.gray.submat(top,bottom,left,right);
        MatOfDouble mean=new MatOfDouble(),std=new MatOfDouble();
        try{
            Imgproc.resize(original,color,new Size(w,h),0,0,scale*f.factor<1?Imgproc.INTER_AREA:Imgproc.INTER_LINEAR);Imgproc.cvtColor(color,gray,Imgproc.COLOR_RGBA2GRAY);Core.extractChannel(color,alpha,3);Imgproc.threshold(alpha,alpha,127,255,Imgproc.THRESH_BINARY);
            Core.meanStdDev(gray,mean,std,alpha);if(std.toArray().length==0||Core.countNonZero(alpha)<16)return null;
            boolean transparent=Core.countNonZero(alpha)<w*h;
            if(transparent){
                // Candidate generation follows the retained silhouette, not the erased scenery.
                Mat intensity=gray.clone(),colorResult=new Mat(),intensityView=f.gray.submat(top,bottom,left,right),shape=new Mat(),inside=new Mat(),internal=new Mat(),kernel=Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));
                try{Imgproc.Canny(alpha,shape,50,120);Imgproc.erode(alpha,inside,kernel);Imgproc.Canny(gray,internal,50,120);Core.bitwise_and(internal,inside,internal);Core.bitwise_or(shape,internal,gray);
                    if(Core.countNonZero(gray)<4)return null;
                    view.release();view=f.edges.submat(top,bottom,left,right);
                    Imgproc.matchTemplate(view,gray,result,Imgproc.TM_CCORR);Core.multiply(result,new Scalar(1.0/(Core.countNonZero(gray)*255.0*255.0)),result);
                    Core.patchNaNs(result,0);
                    Imgproc.matchTemplate(intensityView,intensity,colorResult,Imgproc.TM_SQDIFF,alpha);
                    Core.max(colorResult,new Scalar(0),colorResult);Core.multiply(colorResult,new Scalar(1.0/(Core.countNonZero(alpha)*255.0*255.0)),colorResult);Core.sqrt(colorResult,colorResult);Core.multiply(colorResult,new Scalar(-1),colorResult);Core.add(colorResult,new Scalar(1),colorResult);
                    Core.addWeighted(result,.35,colorResult,.65,0,result);
                }finally{intensity.release();colorResult.release();intensityView.release();shape.release();inside.release();internal.release();kernel.release();}
            }else {if(std.toArray()[0]<3)return null;Imgproc.matchTemplate(view,gray,result,Imgproc.TM_CCOEFF_NORMED);}
            f.maskCandidates(result,left,top,w,h);Core.patchNaNs(result,0);byte[] target=new byte[w*h*4];color.get(0,0,target);Hit best=null;
            for(int peak=0;peak<20&&!f.cancelled.getAsBoolean()&&!expired(deadline);peak++){
                Core.MinMaxLocResult mm=Core.minMaxLoc(result);if(Double.isFinite(mm.maxVal)&&mm.maxVal<.55)break;
                int px=(int)mm.maxLoc.x,py=(int)mm.maxLoc.y,x=left+px,y=top+py;
                Imgproc.rectangle(result,new Point(Math.max(0,px-w/4),Math.max(0,py-h/4)),new Point(Math.min(result.cols()-1,px+w/4),Math.min(result.rows()-1,py+h/4)),new Scalar(-2),-1);
                if(!Double.isFinite(mm.maxVal)||!f.allowed(x,y,w,h))continue;
                int required=f.accuracy(x,y,w,h);if(required<0)continue;
                if(required==100){Hit exact=exact(f,original,x,y,w,h);if(exact!=null)return exact;f.observe(verify(f.pixels,f.width,target,w,h,x,y),100,true);continue;}
                double score=transparent?verifyMasked(f,target,w,h,x,y,gray):verify(f.pixels,f.width,target,w,h,x,y);f.observe(score,required,false);
                if(score+1e-6<required)continue;
                Hit hit=new Hit((int)Math.round(x/f.factor),(int)Math.round(y/f.factor),(int)Math.round(w/f.factor),(int)Math.round(h/f.factor),score,required);
                if(f.originalAllowed(hit.x,hit.y,hit.width,hit.height)&&(best==null||hit.score>best.score))best=hit;
            }
            return best;
        }finally{color.release();gray.release();alpha.release();result.release();view.release();mean.release();std.release();}
    }
    private static int u(byte b){return b&255;}
    private static double lum(byte[] p,int i){return .299*u(p[i])+.587*u(p[i+1])+.114*u(p[i+2]);}
    private static double verify(byte[] screen,int sw,byte[] target,int w,int h,int x,int y){
        double st=0,ss=0,tt=0,ssq=0,cross=0;int n=0;int step=Math.max(1,Math.min(w,h)/48);
        for(int yy=0;yy<h;yy+=step)for(int xx=0;xx<w;xx+=step){int ti=(yy*w+xx)*4;if(u(target[ti+3])<128)continue;int si=((y+yy)*sw+x+xx)*4;double a=lum(target,ti),b=lum(screen,si);st+=a;ss+=b;tt+=a*a;ssq+=b*b;cross+=a*b;n++;}
        if(n<16)return 0;double vt=tt-st*st/n,vs=ssq-ss*ss/n;if(vt<9*n||vs<9*n)return 0;
        double correlation=(cross-st*ss/n)/Math.sqrt(vt*vs);if(correlation<.65)return 0;
        double brightness=(ss-st)/n,error=0,dot=0,gt=0,gs=0;int gradients=0;
        for(int yy=0;yy<h;yy+=step)for(int xx=0;xx<w;xx+=step){int ti=(yy*w+xx)*4;if(u(target[ti+3])<128)continue;int si=((y+yy)*sw+x+xx)*4;
            for(int c=0;c<3;c++)error+=Math.min(255,Math.abs(u(screen[si+c])-u(target[ti+c])-brightness));
            if(xx+1<w&&yy+1<h&&u(target[ti+7])>=128&&u(target[ti+w*4+3])>=128){double a=lum(target,ti),b=lum(screen,si),tx=lum(target,ti+4)-a,ty=lum(target,ti+w*4)-a,sx=lum(screen,si+4)-b,sy=lum(screen,si+sw*4)-b;dot+=tx*sx+ty*sy;gt+=tx*tx+ty*ty;gs+=sx*sx+sy*sy;gradients++;}
        }
        if(gradients<8||gt<1||gs<1)return 0;double edge=Math.max(0,Math.min(1,dot/Math.sqrt(gt*gs)));if(edge<.5)return 0;
        double pixel=1-error/(n*3*255.0);if(pixel<.88)return 0;return Math.min(100,100*(.7*Math.max(0,Math.min(1,correlation))+.2*edge+.1*pixel));
    }
    private static double verifyMasked(Frame f,byte[] target,int w,int h,int x,int y,Mat outline){
        double error=0;int kept=0;byte[] edge=new byte[w*h];outline.get(0,0,edge);
        Mat patch=f.edges.submat(y,y+h,x,x+w);byte[] actual=new byte[w*h];try{patch.get(0,0,actual);}finally{patch.release();}
        int boundary=0,found=0;
        for(int yy=0;yy<h;yy++)for(int xx=0;xx<w;xx++){
            int i=yy*w+xx,ti=i*4;
            if(u(target[ti+3])>=128){int si=((y+yy)*f.width+x+xx)*4;for(int c=0;c<3;c++)error+=Math.abs(u(target[ti+c])-u(f.pixels[si+c]));kept++;}
            if(u(edge[i])>0){boundary++;boolean nearby=false;for(int dy=-1;dy<=1&&!nearby;dy++)for(int dx=-1;dx<=1;dx++){int ex=xx+dx,ey=yy+dy;if(ex>=0&&ey>=0&&ex<w&&ey<h&&u(actual[ey*w+ex])>0){nearby=true;break;}}if(nearby)found++;}
        }
        if(kept<16||boundary<4)return 0;
        double color=1-error/(kept*3*255.0),shape=found/(double)boundary;
        if(color<.88||shape<.8)return 0;
        return 100*(.65*color+.35*shape);
    }
    private static Hit exact(Frame f,Mat template,int wx,int wy,int ww,int wh){
        // Preserve the actual imported size when the working-size rounding maps back within a pixel.
        int w=(int)Math.round(ww/f.factor),h=(int)Math.round(wh/f.factor);if(Math.abs(w-template.cols())<=1&&Math.abs(h-template.rows())<=1){w=template.cols();h=template.rows();}
        Mat resized=new Mat();try{
            if(w==template.cols()&&h==template.rows())template.copyTo(resized);else Imgproc.resize(template,resized,new Size(w,h),0,0,Imgproc.INTER_LINEAR);
            byte[] t=new byte[w*h*4];resized.get(0,0,t);byte[] s=f.originalBytes();int ox=(int)Math.round(wx/f.factor),oy=(int)Math.round(wy/f.factor),range=Math.max(2,(int)Math.ceil(1/f.factor));
            for(int y=oy-range;y<=oy+range;y++)for(int x=ox-range;x<=ox+range;x++){
                if(x<0||y<0||x+w>f.originalW||y+h>f.originalH||!f.originalAllowed(x,y,w,h))continue;
                int fx=(int)Math.round(x*f.factor),fy=(int)Math.round(y*f.factor);if(!f.allowed(fx,fy,ww,wh)||f.accuracy(fx,fy,ww,wh)!=100)continue;
                boolean same=true;for(int yy=0;yy<h&&same;yy++)for(int xx=0;xx<w;xx++){int a=(yy*w+xx)*4,b=((y+yy)*f.originalW+x+xx)*4;if(u(t[a+3])<128)continue;if(t[a]!=s[b]||t[a+1]!=s[b+1]||t[a+2]!=s[b+2]){same=false;break;}}
                if(same)return new Hit(x,y,w,h,100,100);
            }return null;
        }finally{resized.release();}
    }
}

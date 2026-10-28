package com.pratik.imagetap;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.*;

final class CropView extends View {
    final Bitmap bitmap; final CropGeometry geometry;
    // Store the rectangle in original pixels; zoom never resizes the saved crop.
    final RectF selection=new RectF(),beforeGesture=new RectF();
    final Paint paint=new Paint(3); final ScaleGestureDetector pinch;
    float startX,startY,lastX,lastY,focusX,focusY;
    boolean panMode,multiTouch,drawing;
    Runnable selectionFinished;
    String tool="Rectangle";float brushSize=8;Path activePath;
    static final class MaskEdit {final Path path;final String mode;final float brush;MaskEdit(Path p,String m,float b){path=new Path(p);mode=m;brush=b;}}
    final List<MaskEdit> edits=new ArrayList<>();
    void setTool(String value){tool=value;panMode=false;activePath=null;}
    void undoMask(){if(!edits.isEmpty())edits.remove(edits.size()-1);invalidate();}
    private void applyEdit(Canvas c,MaskEdit edit){
        Paint ink=new Paint(Paint.ANTI_ALIAS_FLAG);ink.setStrokeCap(Paint.Cap.ROUND);ink.setStrokeJoin(Paint.Join.ROUND);ink.setStrokeWidth(edit.brush);ink.setStyle(Paint.Style.STROKE);
        Path shape=new Path();
        if(edit.mode.equals("Lasso")){shape.addRect(0,0,bitmap.getWidth(),bitmap.getHeight(),Path.Direction.CW);shape.addPath(edit.path);shape.setFillType(Path.FillType.EVEN_ODD);}
        else ink.getFillPath(edit.path,shape);
        if(edit.mode.equals("Restore")){int save=c.save();c.clipPath(shape);c.drawBitmap(bitmap,0,0,null);c.restoreToCount(save);}
        else {ink.setStyle(Paint.Style.FILL);ink.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));c.drawPath(shape,ink);}
    }
    private void drawEdited(Canvas c){c.drawBitmap(bitmap,0,0,null);for(MaskEdit e:edits)applyEdit(c,e);if(activePath!=null&&!tool.equals("Lasso"))applyEdit(c,new MaskEdit(activePath,tool,brushSize));}

    List<RegionRules.Region> regions=Collections.emptyList();
    void setRegions(List<RegionRules.Region> value){regions=new ArrayList<>(value);invalidate();}
    CropView(Context c,Bitmap b){
        super(c);bitmap=b;geometry=new CropGeometry(b.getWidth(),b.getHeight());setBackgroundColor(Ui.BG);
        pinch=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override public boolean onScale(ScaleGestureDetector d){geometry.zoom(d.getScaleFactor(),d.getFocusX(),d.getFocusY());invalidate();return true;}
        });
        pinch.setQuickScaleEnabled(false);
    }
    void setPanMode(boolean on){panMode=on;drawing=false;}
    void zoom(float factor){geometry.zoom(factor,getWidth()/2f,getHeight()/2f);invalidate();}
    void resetView(){geometry.reset();invalidate();}
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){geometry.resize(w,h);}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        RectF display=new RectF(geometry.tx,geometry.ty,geometry.screenX(bitmap.getWidth()),geometry.screenY(bitmap.getHeight()));
        paint.setColor(0xff23372e);c.drawRect(display,paint);
        int layer=c.saveLayer(display,null);c.translate(geometry.tx,geometry.ty);c.scale(geometry.scale,geometry.scale);drawEdited(c);c.restoreToCount(layer);
        if(activePath!=null&&tool.equals("Lasso")){int save=c.save();c.translate(geometry.tx,geometry.ty);c.scale(geometry.scale,geometry.scale);paint.setColor(Ui.GREEN);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2/geometry.scale);c.drawPath(activePath,paint);paint.setStyle(Paint.Style.FILL);c.restoreToCount(save);}

        int number=0;
        for(RegionRules.Region region:regions){
            number++;RectF r=new RectF(geometry.screenX(region.left*bitmap.getWidth()),geometry.screenY(region.top*bitmap.getHeight()),geometry.screenX(region.right*bitmap.getWidth()),geometry.screenY(region.bottom*bitmap.getHeight()));
            paint.setColor(region.excluded?0x55ff3333:0x2283efb3);c.drawRect(r,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2*getResources().getDisplayMetrics().density);paint.setColor(region.excluded?0xffff6666:Ui.GREEN);c.drawRect(r,paint);paint.setStyle(Paint.Style.FILL);
            float size=14*getResources().getDisplayMetrics().scaledDensity;paint.setTextSize(size);String label="Area "+number+" · "+(region.excluded?"DO NOT SCAN":region.accuracy+"%");
            float x=Math.max(4,r.left+5),y=Math.max(size+4,r.top+size+4);paint.setColor(0xee14281e);c.drawRect(x-3,y-size,x+paint.measureText(label)+5,y+5,paint);paint.setColor(Color.WHITE);c.drawText(label,x,y,paint);
        }
        if(!selection.isEmpty()){
            RectF r=new RectF(geometry.screenX(selection.left),geometry.screenY(selection.top),geometry.screenX(selection.right),geometry.screenY(selection.bottom));
            paint.setColor(0x3340ffbd);c.drawRect(r,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3*getResources().getDisplayMetrics().density);paint.setColor(Ui.GREEN);c.drawRect(r,paint);paint.setStyle(Paint.Style.FILL);
        }
    }
    @Override public boolean onTouchEvent(MotionEvent e){
        int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){
            getParent().requestDisallowInterceptTouchEvent(true);beforeGesture.set(selection);multiTouch=false;drawing=!panMode;
            startX=geometry.imageX(e.getX());startY=geometry.imageY(e.getY());lastX=e.getX();lastY=e.getY();
            if(drawing&&!tool.equals("Rectangle")){activePath=new Path();activePath.moveTo(startX,startY);activePath.lineTo(startX+.01f,startY+.01f);}

        }
        if(action==MotionEvent.ACTION_POINTER_DOWN){
            if(!multiTouch){selection.set(beforeGesture);drawing=false;activePath=null;}
            multiTouch=true;focusX=(e.getX(0)+e.getX(1))/2;focusY=(e.getY(0)+e.getY(1))/2;
        }
        pinch.onTouchEvent(e);
        if(action==MotionEvent.ACTION_MOVE){
            if(e.getPointerCount()>=2){
                float x=(e.getX(0)+e.getX(1))/2,y=(e.getY(0)+e.getY(1))/2;
                geometry.pan(x-focusX,y-focusY);focusX=x;focusY=y;
            }else if(!multiTouch){
                if(panMode)geometry.pan(e.getX()-lastX,e.getY()-lastY);
                else if(drawing){float x=geometry.imageX(e.getX()),y=geometry.imageY(e.getY());if(activePath!=null)activePath.lineTo(x,y);else selection.set(Math.min(x,startX),Math.min(y,startY),Math.max(x,startX),Math.max(y,startY));}
                lastX=e.getX();lastY=e.getY();
            }
        }
        if(action==MotionEvent.ACTION_UP){if(activePath!=null&&!multiTouch){if(tool.equals("Lasso")){activePath.close();RectF bounds=new RectF();activePath.computeBounds(bounds,true);if(bounds.width()>=8&&bounds.height()>=8)selection.set(bounds);}edits.add(new MaskEdit(activePath,tool,brushSize));activePath=null;}boolean selected=drawing&&!multiTouch&&!selection.isEmpty();drawing=false;getParent().requestDisallowInterceptTouchEvent(false);performClick();if(selected&&selectionFinished!=null)selectionFinished.run();}
        if(action==MotionEvent.ACTION_CANCEL){selection.set(beforeGesture);drawing=false;activePath=null;multiTouch=false;getParent().requestDisallowInterceptTouchEvent(false);}
        invalidate();return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    Bitmap crop(){
        RectF r=selection.isEmpty()?new RectF(0,0,bitmap.getWidth(),bitmap.getHeight()):selection;
        int x=Math.max(0,(int)Math.floor(r.left)),y=Math.max(0,(int)Math.floor(r.top));
        int w=Math.min(bitmap.getWidth()-x,(int)Math.ceil(r.right)-x),h=Math.min(bitmap.getHeight()-y,(int)Math.ceil(r.bottom)-y);
        if(w<8||h<8)throw new IllegalArgumentException("Select an area at least 8 × 8 original pixels. 16 × 16 or larger is more reliable.");
        if(w>1200||h>1200)throw new IllegalArgumentException("Crop a smaller icon or button, under 1200 × 1200 pixels.");
        Bitmap result=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas output=new Canvas(result);output.translate(-x,-y);drawEdited(output);
        int retained=0;int[] pixels=new int[w*h];result.getPixels(pixels,0,w,0,0,w,h);for(int pixel:pixels)if(Color.alpha(pixel)>=128)retained++;
        if(retained<16){result.recycle();throw new IllegalArgumentException("Keep at least 16 visible pixels in the symbol.");}return result;
    }
}

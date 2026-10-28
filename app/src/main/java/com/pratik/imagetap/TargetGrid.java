package com.pratik.imagetap;
import android.app.*;import android.content.ClipData;import android.view.*;import android.widget.*;import java.io.File;import java.util.*;
final class TargetGrid {
 static void add(Activity a,LinearLayout body,List<File> files,int start,int end,Runnable changed){
  int columns=Math.max(2,Math.min(6,(a.getResources().getConfiguration().screenWidthDp-48)/110));float font=Math.max(1,a.getResources().getConfiguration().fontScale);
  for(int rowStart=start;rowStart<end;rowStart+=columns){LinearLayout row=new LinearLayout(a);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=Ui.dp(a,12);body.addView(row,rp);
   for(int col=0;col<columns;col++){LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);if(col>0)cp.leftMargin=Ui.dp(a,12);int id=rowStart+col;if(id>=end){row.addView(new View(a),cp);continue;}
    File f=files.get(id);TargetOptions opt=TargetOptions.load(a,f);String label=opt.name.isEmpty()?String.format(Locale.US,"Target %02d",id+1):opt.name;LinearLayout tile=Ui.column(a);tile.setTag("target-tile");tile.setPadding(Ui.dp(a,8),Ui.dp(a,8),Ui.dp(a,8),Ui.dp(a,8));tile.setBackground(Ui.shape(a,Ui.SURFACE,opt.enabled?0xff527768:Ui.LINE,18));row.addView(tile,cp);
    ImageView img=new ImageView(a);img.setImageBitmap(TargetStore.thumbnail(f));img.setScaleType(ImageView.ScaleType.FIT_CENTER);img.setBackground(Ui.shape(a,Ui.BG,0,10));img.setClipToOutline(true);tile.addView(img,new LinearLayout.LayoutParams(-1,Ui.dp(a,60)));TextView title=Ui.label(a,label,14,Ui.TEXT);title.setMaxLines(1);title.setEllipsize(android.text.TextUtils.TruncateAt.END);title.setPadding(0,Ui.dp(a,8),0,0);tile.addView(title,new LinearLayout.LayoutParams(-1,Ui.dp(a,30*font)));TextView meta=Ui.label(a,(opt.area==null?"Full screen":"Limited area")+(opt.sourceWidth==0?"":" · "+opt.sourceWidth+" px"),10,Ui.MUTED);meta.setMaxLines(1);meta.setEllipsize(android.text.TextUtils.TruncateAt.END);tile.addView(meta,new LinearLayout.LayoutParams(-1,Ui.dp(a,18*font)));
    View.OnLongClickListener drag=v->tile.startDragAndDrop(ClipData.newPlainText("AutoBash target",f.getName()),new View.DragShadowBuilder(tile),f,0);
    tile.setOnLongClickListener(drag);img.setOnLongClickListener(drag);title.setOnLongClickListener(drag);
    tile.setOnDragListener((v,event)->{
     Object local=event.getLocalState();if(!(local instanceof File)||!f.getParentFile().equals(((File)local).getParentFile()))return false;
     switch(event.getAction()){
      case DragEvent.ACTION_DRAG_STARTED:return true;
      case DragEvent.ACTION_DRAG_ENTERED:case DragEvent.ACTION_DRAG_LOCATION:tile.setAlpha(.65f);return true;
      case DragEvent.ACTION_DRAG_EXITED:case DragEvent.ACTION_DRAG_ENDED:tile.setAlpha(1f);return true;
      case DragEvent.ACTION_DROP:tile.setAlpha(1f);File from=(File)local;if(!from.equals(f)){List<File> current=TargetOrder.files(f.getParentFile());move(a,from,current.indexOf(f),changed);}return true;
      default:return true;
     }
    });
    tile.setFocusable(true);tile.setContentDescription(label+". "+opt.summary()+". Edit target");tile.setOnClickListener(v->TargetEditor.open(a,f,changed));img.setOnClickListener(v->TargetEditor.open(a,f,changed));img.setContentDescription("Edit "+label);LinearLayout tools=new LinearLayout(a);tools.setGravity(Gravity.CENTER_VERTICAL);tile.addView(tools);TextView state=Ui.label(a,opt.enabled?"● On":"○ Off",10,Ui.MUTED);Ui.activeGlow(state,opt.enabled);state.setGravity(Gravity.CENTER_VERTICAL);state.setText((opt.enabled?"● On":"○ Off")+"  ↕");state.setContentDescription("Move "+label+" to another position");state.setFocusable(true);state.setOnClickListener(v->position(a,f,changed));tools.addView(state,new LinearLayout.LayoutParams(0,Ui.dp(a,48),1));Button remove=Ui.button(a,"×",false);remove.setPadding(0,0,0,0);remove.setTextSize(22);remove.setTextColor(Ui.RED);remove.setContentDescription("Delete target "+(id+1));tools.addView(remove,new LinearLayout.LayoutParams(Ui.dp(a,48),Ui.dp(a,48)));remove.setOnClickListener(v->new AlertDialog.Builder(a).setTitle("Delete this target?").setNegativeButton("Keep",null).setPositiveButton("Delete",(d,w)->{if(!f.delete())Toast.makeText(a,"Could not remove target",Toast.LENGTH_LONG).show();else if(TapService.instance!=null)TapService.instance.refreshTargets();changed.run();}).show());
   }
  }
 }
 static void move(Activity a,File f,int position,Runnable changed){try{TargetOrder.move(f.getParentFile(),f,position);if(TapService.instance!=null)TapService.instance.refreshTargets();changed.run();}catch(java.io.IOException e){Toast.makeText(a,e.getMessage(),Toast.LENGTH_LONG).show();}}
 static void position(Activity a,File f,Runnable changed){List<File> files=TargetOrder.files(f.getParentFile());EditText input=new EditText(a);input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);input.setText(String.valueOf(files.indexOf(f)+1));input.selectAll();input.setContentDescription("New target position");AlertDialog dialog=new AlertDialog.Builder(a).setTitle("Move to position · 1–"+files.size()).setView(input).setNegativeButton("Cancel",null).setPositiveButton("Move",null).create();dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{int position=Integer.parseInt(input.getText().toString().trim());if(position<1||position>files.size())throw new NumberFormatException();move(a,f,position-1,changed);dialog.dismiss();}catch(NumberFormatException e){input.setError("Enter a position from 1 to "+files.size());}}));dialog.show();}
}

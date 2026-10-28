package com.pratik.imagetap;
import android.app.*;import android.graphics.*;import android.text.InputType;import android.view.*;import android.widget.*;import java.io.File;
/** Options describe where/at what scale to search, not what a symbol means. */
final class TargetEditor {
 static void open(Activity a,File file,Runnable changed){
  TargetOptions old=TargetOptions.load(a,file);LinearLayout form=Ui.column(a);int pad=Ui.dp(a,18);form.setPadding(pad,pad,pad,pad);ScrollView scroll=new ScrollView(a);scroll.addView(form);
  Switch active=new ActiveSwitch(a);active.setText("Include in scans");active.setChecked(old.enabled);form.addView(active);
  EditText name=new EditText(a);name.setSingleLine(true);name.setHint("Optional target name");name.setText(old.name);form.addView(name);
  form.addView(Ui.label(a,"Source screenshot width (pixels)",14,Ui.TEXT));EditText width=new EditText(a);width.setInputType(InputType.TYPE_CLASS_NUMBER);width.setSingleLine(true);width.setHint("0 = keep original pixel size");width.setText(Integer.toString(old.sourceWidth));form.addView(width);
  form.addView(Ui.label(a,"For a crop from a 709 px-wide screenshot, enter 709—not the cropped icon width. Leave 0 if unknown. This adjusts size, not image detail.",12,Ui.MUTED));
  RegionRules.Region[] area={old.area};TextView areaLabel=Ui.label(a,"",13,Ui.GREEN);form.addView(areaLabel);Runnable describe=()->areaLabel.setText(area[0]==null?"Search: full screen":String.format(java.util.Locale.US,"Search: %.0f–%.0f%% across · %.0f–%.0f%% down",area[0].left*100,area[0].right*100,area[0].top*100,area[0].bottom*100));describe.run();
  LinearLayout row=new LinearLayout(a);form.addView(row);String[] labels={"Full","Top left","Top right"};for(int i=0;i<3;i++){final int n=i;Button b=Ui.button(a,labels[i],false);b.setTextSize(12);row.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(a,48),1));b.setOnClickListener(v->{area[0]=n==0?null:n==1?new RegionRules.Region(0,0,.35f,.35f,75):new RegionRules.Region(.65f,0,1,.35f,75);describe.run();});}
  Button draw=Ui.button(a,"Draw a custom search area",false);form.addView(draw);draw.setOnClickListener(v->draw(a,area,describe));
  form.addView(Ui.label(a,"The complete target box must fit inside this area. Global exclusions still apply. Similar-looking store buttons can still match inside the allowed area.",12,Ui.MUTED));
  Ui.polish(form);AlertDialog dialog=new AlertDialog.Builder(a).setTitle("Image options").setView(scroll).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b->{try{int w=width.getText().toString().trim().isEmpty()?0:Integer.parseInt(width.getText().toString());new TargetOptions(active.isChecked(),w,area[0],name.getText().toString()).save(a,file);if(TapService.instance!=null)TapService.instance.refreshTargets();dialog.dismiss();changed.run();}catch(Exception e){width.setError(e.getMessage());}}));dialog.show();
 }
 private static void draw(Activity a,RegionRules.Region[] area,Runnable changed){
  android.util.DisplayMetrics dm=new android.util.DisplayMetrics();a.getWindowManager().getDefaultDisplay().getRealMetrics(dm);int w=400,h=Math.max(200,Math.round(w*dm.heightPixels/(float)dm.widthPixels));Bitmap black=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);black.eraseColor(Color.BLACK);CropView crop=new CropView(a,black);
  if(area[0]!=null)crop.selection.set(area[0].left*w,area[0].top*h,area[0].right*w,area[0].bottom*h);
  LinearLayout layout=Ui.column(a);layout.setPadding(16,8,16,8);layout.addView(Ui.label(a,"Drag a rectangle on this representation of your screen.",13,Ui.MUTED));layout.addView(crop,new LinearLayout.LayoutParams(-1,Ui.dp(a,320)));
  AlertDialog picker=new AlertDialog.Builder(a).setTitle("Search area for this image").setView(layout).setNegativeButton("Cancel",null).setPositiveButton("Use area",null).create();picker.setOnShowListener(v->picker.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b->{if(crop.selection.isEmpty()){Toast.makeText(a,"Draw an area first",Toast.LENGTH_SHORT).show();return;}RectF r=crop.selection;area[0]=new RegionRules.Region(r.left/w,r.top/h,r.right/w,r.bottom/h,75);changed.run();picker.dismiss();}));picker.setOnDismissListener(v->black.recycle());picker.show();
 }
}

package com.pratik.imagetap;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.widget.*;
import java.util.*;

/** Global screen map: no screenshot or imported target is needed. */
final class RegionEditor extends LinearLayout {
    interface Result {void finish(List<RegionRules.Region> saved);}
    private final List<RegionRules.Region> areas;private final CropView preview;private final Bitmap screenMap;
    private final LinearLayout rows;private final TextView count;private final Result result;
    private boolean dialogOpen;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private Button button(String label,LinearLayout row,Runnable action){Button b=new Button(getContext());b.setText(label);row.addView(b,new LinearLayout.LayoutParams(0,-2,1));b.setOnClickListener(v->action.run());return b;}
    private TextView label(String value,int size){TextView t=new TextView(getContext());t.setText(value);t.setTextSize(size);t.setPadding(0,dp(4),0,dp(4));addView(t);return t;}
    RegionEditor(Context context,Bitmap blankScreen,List<RegionRules.Region> existing,Result onDone){
        super(context);screenMap=blankScreen;result=onDone;areas=new ArrayList<>(existing);setOrientation(VERTICAL);setPadding(dp(12),dp(25),dp(12),dp(20));
        label("Screen regions",22);label("This black canvas represents your entire screen. Draw a rectangle to choose its accuracy or exclude it from scanning.",13);
        preview=new CropView(context,blankScreen);preview.selectionFinished=()->areaOptions(-1);addView(preview,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout tools=new LinearLayout(context);addView(tools);button("−",tools,()->preview.zoom(1/1.5f));button("+",tools,()->preview.zoom(1.5f));button("Fit",tools,preview::resetView);
        ToggleButton move=new ActiveToggle(context);move.setTextOff("Select");move.setTextOn("Move");move.setChecked(false);tools.addView(move,new LinearLayout.LayoutParams(0,-2,1));move.setOnCheckedChangeListener((b,on)->preview.setPanMode(on));
        LinearLayout actions=new LinearLayout(context);addView(actions);button("Set selected area",actions,()->areaOptions(-1));button("Clear areas",actions,()->{areas.clear();refresh();});
        count=label("",14);ScrollView scroll=new ScrollView(context);rows=new LinearLayout(context);rows.setOrientation(VERTICAL);scroll.addView(rows);addView(scroll,new LinearLayout.LayoutParams(-1,dp(140)));
        label("Red = do not scan. Green = custom accuracy. Exclusions win overlaps; any target touching an excluded area is ignored. Unmarked space uses main accuracy.",12);
        LinearLayout done=new LinearLayout(context);addView(done);button("Save regions",done,()->result.finish(new ArrayList<>(areas)));button("Cancel",done,this::cancel);refresh();setBackgroundColor(Ui.BG);Ui.polish(this);
    }
    void cancel(){result.finish(null);}
    private void refresh(){
        count.setText("Areas: "+areas.size()+" / 20 · tap a row to edit");preview.setRegions(areas);rows.removeAllViews();
        for(int i=0;i<areas.size();i++){final int index=i;RegionRules.Region area=areas.get(i);Button b=new Button(getContext());b.setText("Area "+(i+1)+" · "+(area.excluded?"DO NOT SCAN":area.accuracy+"%")+" · Edit");rows.addView(b,new LinearLayout.LayoutParams(-1,-2));b.setOnClickListener(v->areaOptions(index));Ui.polish(b);}
    }
    private void areaOptions(int index){
        if(dialogOpen)return;
        final RegionRules.Region region;
        if(index<0){
            if(areas.size()>=20){Toast.makeText(getContext(),"Up to 20 areas. Edit or delete an existing area.",Toast.LENGTH_LONG).show();return;}
            RectF r=new RectF(preview.selection);if(r.width()<8||r.height()<8){Toast.makeText(getContext(),"Draw a rectangle first.",Toast.LENGTH_LONG).show();return;}
            region=new RegionRules.Region(r.left/screenMap.getWidth(),r.top/screenMap.getHeight(),r.right/screenMap.getWidth(),r.bottom/screenMap.getHeight(),96);
        }else region=areas.get(index);
        LinearLayout form=new LinearLayout(getContext());form.setOrientation(VERTICAL);form.setPadding(dp(16),dp(8),dp(16),dp(8));
        Switch exclude=new ActiveSwitch(getContext());exclude.setText("Do not scan this area");exclude.setChecked(region.excluded);form.addView(exclude);
        TextView value=new TextView(getContext());value.setText(region.excluded?"Excluded from recognition":"Accuracy: "+region.accuracy+"%");form.addView(value);SeekBar slider=new SeekBar(getContext());slider.setMax(25);slider.setProgress(region.accuracy-75);slider.setEnabled(!region.excluded);form.addView(slider);
        exclude.setOnCheckedChangeListener((b,on)->{slider.setEnabled(!on);value.setText(on?"Excluded from recognition":"Accuracy: "+(75+slider.getProgress())+"%");});
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int p,boolean user){value.setText("Accuracy: "+(75+p)+"%");}});
        AlertDialog.Builder dialog=new AlertDialog.Builder(getContext()).setTitle(index<0?"New screen area":"Area "+(index+1)).setView(form).setPositiveButton(index<0?"Add area":"Apply",(d,w)->{
            RegionRules.Region updated=new RegionRules.Region(region.left,region.top,region.right,region.bottom,75+slider.getProgress(),exclude.isChecked());if(index<0)areas.add(updated);else areas.set(index,updated);preview.selection.setEmpty();refresh();
        }).setNegativeButton("Cancel",null);
        if(index>=0)dialog.setNeutralButton("Delete",(d,w)->{areas.remove(index);refresh();});
        dialogOpen=true;AlertDialog shown=dialog.create();shown.setOnDismissListener(d->dialogOpen=false);Ui.polish(form);shown.show();
    }
}

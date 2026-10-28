package com.pratik.imagetap;
import android.app.*;
import android.graphics.Bitmap;
import android.widget.*;
import java.io.File;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

final class DuplicateTool {
    private final Activity activity;private final Runnable changed;private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private Future<?> task;private ProgressDialog progress;private AlertDialog dialog;private boolean closed;private int scanVersion;
    DuplicateTool(Activity activity,Runnable changed){this.activity=activity;this.changed=changed;}
    private void toast(String s){Toast.makeText(activity,s,Toast.LENGTH_LONG).show();}
    void close(){closed=true;scanVersion++;if(task!=null)task.cancel(true);worker.shutdownNow();if(progress!=null)progress.dismiss();if(dialog!=null)dialog.dismiss();}
    void open(){
        LinearLayout form=new LinearLayout(activity);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(24,16,24,16);
        TextView help=new TextView(activity);help.setText("Checks every saved image. Review each group and choose what to keep. Merged copies are backed up and can be restored.");form.addView(help);
        int saved=activity.getSharedPreferences("config",0).getInt("duplicateThreshold",99);int initial=Math.max(80,Math.min(100,saved));TextView value=Ui.label(activity,"",16,Ui.GREEN);form.addView(value);SeekBar threshold=new SeekBar(activity);threshold.setMax(20);threshold.setProgress(initial-80);threshold.setContentDescription("Duplicate similarity, 80 to 100 percent");form.addView(threshold,new LinearLayout.LayoutParams(-1,Ui.dp(activity,48)));
        Runnable label=()->value.setText((80+threshold.getProgress())+"%"+(threshold.getProgress()==20?" · identical original pixels":" · similarity threshold"));label.run();threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int n,boolean u){label.run();}});form.addView(Ui.label(activity,"Lower values suggest more groups. Every group needs your review; nothing merges automatically.",12,Ui.MUTED));
        Ui.polish(form);dialog=new AlertDialog.Builder(activity).setTitle("Find similar entries").setView(form).setPositiveButton("Scan all entries",(d,w)->{int percent=80+threshold.getProgress();activity.getSharedPreferences("config",0).edit().putInt("duplicateThreshold",percent).apply();scan(percent);}).setNegativeButton("Cancel",null).show();
    }
    private static DuplicateSimilarity.Signature signature(File f)throws Exception{
        Bitmap b=TargetStore.load(f);if(b==null)throw new IllegalArgumentException("Could not read "+f.getName());
        try{
            int w=b.getWidth(),h=b.getHeight();MessageDigest digest=MessageDigest.getInstance("SHA-256");int[] row=new int[w];byte[] bytes=new byte[w*4];
            for(int y=0;y<h;y++){if(Thread.currentThread().isInterrupted())throw new CancellationException();b.getPixels(row,0,w,0,y,w,1);for(int x=0;x<w;x++){int c=row[x];bytes[x*4]=(byte)(c>>>24);bytes[x*4+1]=(byte)(c>>16);bytes[x*4+2]=(byte)(c>>8);bytes[x*4+3]=(byte)c;}digest.update(bytes);}
            Bitmap small=Bitmap.createScaledBitmap(b,64,64,true);int[] pixels=new int[64*64];small.getPixels(pixels,0,64,0,0,64,64);if(small!=b)small.recycle();
            return new DuplicateSimilarity.Signature(w,h,pixels,Base64.getEncoder().encodeToString(digest.digest()));
        }finally{b.recycle();}
    }
    private void scan(int threshold){
        List<File> files=TargetStore.files(activity);if(files.size()<2){toast("Add at least two images first.");return;}
        int version=++scanVersion;
        progress=new ProgressDialog(activity);progress.setTitle("Checking all "+files.size()+" images");progress.setMessage("Comparing pixels and structure…");progress.setCancelable(false);progress.setButton(ProgressDialog.BUTTON_NEGATIVE,"Cancel",(d,w)->{scanVersion++;if(task!=null)task.cancel(true);});progress.show();
        task=worker.submit(()->{
            try{
                List<DuplicateSimilarity.Signature> signatures=new ArrayList<>();for(int i=0;i<files.size();i++){if(Thread.currentThread().isInterrupted())return;signatures.add(signature(files.get(i)));final int done=i+1;activity.runOnUiThread(()->{if(!closed&&scanVersion==version&&progress!=null)progress.setMessage("Reading image "+done+" / "+files.size());});}
                List<List<Integer>> groups=DuplicateSimilarity.groups(signatures,threshold);
                activity.runOnUiThread(()->{if(closed||scanVersion!=version)return;progress.dismiss();progress=null;if(groups.isEmpty())toast("No groups meet "+threshold+"% similarity.");else review(files,signatures,groups,0);});
            }catch(CancellationException ignored){activity.runOnUiThread(()->{if(scanVersion==version&&progress!=null){progress.dismiss();progress=null;}});}
            catch(Exception e){activity.runOnUiThread(()->{if(closed||scanVersion!=version)return;if(progress!=null){progress.dismiss();progress=null;}toast("Scan failed: "+e.getMessage());});}
        });
    }
    private void review(List<File> files,List<DuplicateSimilarity.Signature> signatures,List<List<Integer>> groups,int position){
        if(closed)return;if(position>=groups.size()){changed.run();toast("Duplicate review complete.");return;}
        List<Integer> group=groups.get(position);int preferred=group.get(0);double min=100;
        for(int i:group){if((long)signatures.get(i).width*signatures.get(i).height>(long)signatures.get(preferred).width*signatures.get(preferred).height)preferred=i;for(int j:group)if(i!=j)min=Math.min(min,DuplicateSimilarity.score(signatures.get(i),signatures.get(j)));}
        LinearLayout panel=new LinearLayout(activity);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(24,12,24,12);TextView explanation=new TextView(activity);explanation.setText(String.format(Locale.US,"Group %d / %d · minimum similarity %.2f%%\nChoose the image to keep. Other copies move to a recoverable backup.",position+1,groups.size(),min));panel.addView(explanation);
        Map<Integer,Integer> ids=new HashMap<>();RadioGroup choices=new RadioGroup(activity);panel.addView(choices);
        for(int i:group){RadioButton choice=new RadioButton(activity);choice.setId(android.view.View.generateViewId());ids.put(choice.getId(),i);DuplicateSimilarity.Signature sig=signatures.get(i);choice.setText("Entry "+(i+1)+" · "+sig.width+" × "+sig.height+(i==preferred?" · largest":""));
            Bitmap thumbnail=TargetStore.thumbnail(files.get(i));if(thumbnail!=null){android.graphics.drawable.BitmapDrawable drawable=new android.graphics.drawable.BitmapDrawable(activity.getResources(),thumbnail);int size=Math.round(90*activity.getResources().getDisplayMetrics().density);drawable.setBounds(0,0,size,size);choice.setCompoundDrawables(drawable,null,null,null);choice.setCompoundDrawablePadding(16);}choices.addView(choice);if(i==preferred)choices.check(choice.getId());}
        ScrollView scroll=new ScrollView(activity);scroll.addView(panel);
        Ui.polish(panel);dialog=new AlertDialog.Builder(activity).setTitle("Merge similar entries?").setView(scroll).setPositiveButton("Merge group",(d,w)->{
            try{File keep=files.get(ids.get(choices.getCheckedRadioButtonId()));List<File> toMerge=new ArrayList<>();for(int i:group)toMerge.add(files.get(i));TargetStore.merge(activity,toMerge,keep);if(TapService.instance!=null)TapService.instance.refreshTargets();changed.run();}
            catch(Exception e){toast("Merge failed: "+e.getMessage());}
            review(files,signatures,groups,position+1);
        }).setNeutralButton("Keep all / next",(d,w)->review(files,signatures,groups,position+1)).setNegativeButton("Finish",(d,w)->changed.run()).show();
    }
}

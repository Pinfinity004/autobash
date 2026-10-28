package com.pratik.imagetap;

import android.accessibilityservice.*;
import android.app.KeyguardManager;
import android.content.*;
import android.graphics.*;
import android.hardware.HardwareBuffer;
import android.os.*;
import android.view.*;
import android.view.accessibility.*;
import android.widget.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import org.opencv.core.Mat;
import org.opencv.core.Core;

public class TapService extends AccessibilityService {
    public static TapService instance;
    private AutoTapOverlay autoTap;long autoTapBusyUntil;private long autoTapLease;
    long beginAutoTapStroke(int hold){autoTapBusyUntil=SystemClock.uptimeMillis()+hold+2500;return ++autoTapLease;}
    void endAutoTapStroke(long lease){if(lease==autoTapLease)autoTapBusyUntil=0;}
    public boolean openAutoTap(boolean recovering){if(autoTap!=null)return true;boolean finishing=gesturePending;stopSession("Opening Auto Tap");if(finishing)autoTapBusyUntil=SystemClock.uptimeMillis()+1100;try{autoTap=new AutoTapOverlay(this,recovering);return true;}catch(RuntimeException e){Toast.makeText(this,"Auto Tap could not open: "+e.getMessage(),Toast.LENGTH_LONG).show();return false;}}
    public void closeAutoTap(boolean preserve){if(autoTap!=null){AutoTapOverlay old=autoTap;autoTap=null;old.close(preserve);}}
    public void pauseAutoTap(String reason){if(autoTap!=null)autoTap.runner.pause(reason);}
    public void reloadAutoTap(){if(autoTap!=null)autoTap.reload();}
    void autoTapDialog(android.app.Dialog dialog){if(autoTap!=null)autoTap.dialogs.add(dialog);}

    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private WindowManager wm; private LinearLayout panel; private TextView status,tapFeedback,tapBadge,scanStats;private NerveStrip nerve;
    private volatile boolean running; private volatile int generation;
    private SharedPreferences prefs;private boolean visionReady;
    // Native objects are used/released only by the recognition worker.
    private final LinkedHashMap<String,Mat> templateCache=new LinkedHashMap<>(16,.75f,true);
    private final Map<String,VisionMatcher.Hit> tracking=new HashMap<>();
    private long cacheBytes;
    private final VisionMatcher.Cache preparedCache=new VisionMatcher.Cache();

    private String runMode="recognition";private AutomationClock automation;private MarkerPicker markerPicker;private android.app.Activity markerOwner;private Runnable cancelMarker;private long timedTicket;
    private boolean quickRequested;
    private final SessionPause pause=new SessionPause();private Button pauseButton;
    public boolean editorOpen;
    private FocusOverlay marker;
    private ProgressBar pulseProgress;
    private Runnable pulseTick;private TextView pulseLabel;
    private int pulseMs=1000,captureFloorMs=100;private boolean panelDetailed=true;
    private final TapConfirmation confirmations=new TapConfirmation();
    private boolean actual,repeat,requireConfirmation,gesturePending; private double maxError;
    private String regionJson="[]";private boolean useRegions;
    private int pressMs=100,delayMs,failures,confirmId=-1,confirmX,confirmY,confirmCount,recheckId=-1;
    private long lastCheckpoint=-10000,recognitionTicket;private boolean actionInFlight,recoveryNeedsReview;private String recoveryNotice="";
    private final List<String> targetKeys=new ArrayList<>();
    private long lastCapture=-1000,lastTap=-1,taps,gestureStarted,lastDispatchedFrame=-1;
    private int attempt; private Runnable pendingCapture,watchdog;
    private Future<?> work;private final CaptureGate captureGate=new CaptureGate();
    private String coverage="";
    private ScanEngine scanEngine;private long captureMillis;
    private final Map<String,String> fingerprints=new HashMap<>();
    private String fingerprint(File file){
        String identity=file.getAbsolutePath()+"#"+file.lastModified()+"#"+file.length();String known=fingerprints.get(identity);if(known!=null)return known;
        try{java.security.MessageDigest digest=java.security.MessageDigest.getInstance("SHA-256");try(java.io.InputStream in=new java.io.FileInputStream(file)){byte[] buffer=new byte[16384];for(int n;(n=in.read(buffer))!=-1;)digest.update(buffer,0,n);}StringBuilder value=new StringBuilder();for(byte b:digest.digest())value.append(String.format(java.util.Locale.ROOT,"%02x",b&255));known=value.toString();}catch(Exception e){known=identity;}
        if(fingerprints.size()>4000)fingerprints.clear();fingerprints.put(identity,known);return known;
    }
    private int scanWorkers(){
        android.app.ActivityManager am=(android.app.ActivityManager)getSystemService(ACTIVITY_SERVICE);
        int memoryLimit=am==null||am.isLowRamDevice()||am.getMemoryClass()<384?2:4;
        return Math.max(1,Math.min(Math.min(Runtime.getRuntime().availableProcessors(),memoryLimit),prefs.getBoolean("fastPerformance",false)?4:2));
    }

    private FreshCheck freshCheck;
    private static final class FreshCheck {
        final VisionMatcher.Hit[] hits;final int width,height;final double limit;final String regions;final boolean useRegions;final long stamp;
        FreshCheck(VisionMatcher.Hit[] hits,int w,int h,double limit,String regions,boolean useRegions,long stamp){this.hits=hits.clone();width=w;height=h;this.limit=limit;this.regions=regions;this.useRegions=useRegions;this.stamp=stamp;}
    }

    private final List<File> targets=new ArrayList<>(); private TargetSchedule schedule;
    private boolean[] latched; private int[] missing;
    @Override protected void onServiceConnected(){
        if(running||autoTap!=null){instance=this;return;} // A repeated connection is not a new user session.
        instance=this;wm=(WindowManager)getSystemService(WINDOW_SERVICE);prefs=getSharedPreferences("config",0);
        try{System.loadLibrary("opencv_java4");Core.setNumThreads(1);visionReady=true;}catch(UnsatisfiedLinkError|RuntimeException e){visionReady=false;}
        // Resume only a session previously started by the user and not explicitly stopped.
        if(AutoTapStore.prefs(this).getBoolean("active",false))openAutoTap(true);else if(prefs.getBoolean("sessionActive",false))startSession(true);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event){}
    @Override public void onInterrupt(){if(running){setStatus("Interrupted • retrying");next(generation,1000);}}
    @Override public void onDestroy(){closeAutoTap(true);checkpoint(true);savePanelPosition();endLoop();try{worker.execute(()->{if(scanEngine!=null){scanEngine.close();scanEngine=null;}preparedCache.clear();for(Mat m:templateCache.values())m.release();templateCache.clear();tracking.clear();});}catch(RejectedExecutionException ignored){}worker.shutdown();if(instance==this)instance=null;super.onDestroy();}
    private Set<String> latchedKeys(){Set<String> keys=new HashSet<>();if(latched!=null)for(int i=0;i<Math.min(latched.length,targetKeys.size());i++)if(latched[i])keys.add(targetKeys.get(i));return keys;}
    private boolean checkpoint(boolean durable){
        if(!running||prefs==null||(instance!=null&&instance!=this))return true;long now=SystemClock.elapsedRealtime();if(!durable&&now-lastCheckpoint<1000)return true;
        try{SessionCheckpoint state=new SessionCheckpoint(runMode,taps,lastTap<0?0:Math.max(0,delayMs-(now-lastTap)),pause.paused()||editorOpen,actionInFlight||recoveryNeedsReview,latchedKeys(),automation);String value=state.encode(now);SharedPreferences.Editor edit=prefs.edit().putString("sessionCheckpoint",value);if(durable){if(!edit.commit())throw new IllegalStateException("Cannot save session");}else edit.apply();lastCheckpoint=now;return true;}catch(Exception e){pause.manual=true;recoveryNotice="Session could not be saved · paused. Check device storage.";return false;}
    }
    private void savePanelPosition(){if(instance!=null&&instance!=this)return;if(prefs!=null&&panel!=null&&panel.getLayoutParams() instanceof WindowManager.LayoutParams){WindowManager.LayoutParams p=(WindowManager.LayoutParams)panel.getLayoutParams();float density=getResources().getDisplayMetrics().density;prefs.edit().putFloat("launcherX",p.x/density).putFloat("launcherY",p.y/density).commit();}}
    private void restorePanelPosition(WindowManager.LayoutParams p){android.util.DisplayMetrics m=getResources().getDisplayMetrics();p.width=Math.min(p.width,Math.max(1,m.widthPixels));panel.measure(View.MeasureSpec.makeMeasureSpec(p.width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(m.heightPixels,View.MeasureSpec.AT_MOST));p.x=Math.max(0,Math.min(Math.round(prefs.getFloat("launcherX",12)*m.density),Math.max(0,m.widthPixels-p.width)));p.y=Math.max(0,Math.min(Math.round(prefs.getFloat("launcherY",110)*m.density),Math.max(0,m.heightPixels-panel.getMeasuredHeight())));}
    private void setStatus(String value){if(status!=null)status.setText(value);}
    private void tapState(String message){if(tapFeedback!=null)tapFeedback.setText(message);if(tapBadge!=null)tapBadge.setText(taps==1?"1 action":taps+" actions");}
    private void resetConfirmation(){freshCheck=null;confirmCount=0;confirmations.clear();}
    private boolean current(int token){return running&&generation==token;}
    private String screenWaitReason(){
        if(SystemClock.uptimeMillis()<autoTapBusyUntil)return "Waiting · previous Auto Tap gesture is finishing";
        if(!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive())return "Waiting • screen is off";
        if(((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked())return "Waiting • screen is locked";
        return null;
    }
    private void settings(){
        actual=prefs.getBoolean("tap",false);repeat=prefs.getBoolean("repeat",true);requireConfirmation=prefs.getBoolean("confirm",true);
        maxError=255*(100-TapTiming.similarity(prefs.getInt("similarity",96)))/100.0;
        regionJson=prefs.getString("regions","[]");useRegions=prefs.getBoolean("useRegions",true);
        pressMs=TapTiming.press(prefs.getInt("pressMs",100));delayMs=TapTiming.delay(prefs.getInt("delayMs",0));pulseMs=TapTiming.pulse(prefs.getInt("pulseMs",1000));
    }
    private void loadTargets(){
        targets.clear();targets.addAll(TargetStore.allActiveFiles(this));targetKeys.clear();for(File f:targets)targetKeys.add(SessionCheckpoint.key(f));schedule=new TargetSchedule();
        coverage="";latched=new boolean[targets.size()];missing=new int[targets.size()];resetConfirmation();confirmId=-1;recheckId=-1;
    }
    public boolean startSession(){return startSession(false);}
    private boolean startSession(boolean recovering){
        closeAutoTap(false);
        SessionCheckpoint saved=null;recoveryNotice="";recoveryNeedsReview=false;
        if(recovering)try{saved=SessionCheckpoint.decode(prefs.getString("sessionCheckpoint",""),SystemClock.elapsedRealtime());}catch(Exception e){recoveryNotice="Recovery unavailable · paused. Restart session to begin again.";}
        runMode=prefs.getString("runMode","recognition");if(!runMode.equals("general")&&!runMode.equals("timeline"))runMode="recognition";
        if(saved!=null)runMode=saved.mode;
        if(runMode.equals("recognition")&&!visionReady){Toast.makeText(this,"Native recognition could not load. Install the correct AutoBash build for your device.",Toast.LENGTH_LONG).show();return false;}
        savePanelPosition();endLoop();settings();loadTargets();captureFloorMs=100;taps=0;failures=0;pause.reset();quickRequested=false;lastTap=-1;lastDispatchedFrame=-1;lastCapture=-1000;gesturePending=false;actionInFlight=false;lastCheckpoint=-10000;
        if(saved!=null){recoveryNeedsReview=saved.uncertain;taps=saved.actions;automation=saved.clock;pause.manual=saved.paused||saved.uncertain;for(int i=0;i<latched.length;i++)latched[i]=saved.latched.contains(targetKeys.get(i));if(saved.cooldown>0)lastTap=Math.max(0,SystemClock.elapsedRealtime()-Math.max(0,delayMs-saved.cooldown));if(saved.uncertain)recoveryNotice="Action interrupted · outcome unknown. Check screen before Resume.";}
        else if(!runMode.equals("recognition")){try{automation=new AutomationClock(AutomationStore.active(this,runMode.equals("timeline")),runMode.equals("timeline"),AutomationStore.loop(this),SystemClock.elapsedRealtime());if(automation.done){Toast.makeText(this,"Add or enable a timed action first.",Toast.LENGTH_LONG).show();return false;}}catch(RuntimeException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();return false;}}
        if(recovering&&saved==null)pause.manual=true;
        try{showPanel();}catch(RuntimeException e){endLoop();Toast.makeText(this,"Could not show Stop control. Session not started.",Toast.LENGTH_LONG).show();return false;}
        prefs.edit().putBoolean("sessionActive",true).putBoolean("sessionPaused",pause.manual).commit();running=true;checkpoint(true);if(recovering)tapState("Session recovered · "+taps+" actions retained");next(generation,500);return true;
    }
    public void refreshTargets(){
        if(!running)return;
        // Cancel the old snapshot so deleting/replacing a target cannot leave a stale tap queued.
        generation++;attempt++;main.removeCallbacksAndMessages(null);captureGate.reset();
        Set<String> retained=latchedKeys();loadTargets();for(int i=0;i<latched.length;i++)latched[i]=retained.contains(targetKeys.get(i));gesturePending=false;checkpoint(true);next(generation,350);
    }
    public void stopSession(String reason){
        closeAutoTap(false);
        if(prefs==null)prefs=getSharedPreferences("config",0);
        savePanelPosition();prefs.edit().putBoolean("sessionActive",false).putBoolean("sessionPaused",false).remove("sessionCheckpoint").commit();pause.reset();quickRequested=false;endLoop();
    }
    private void invalidateScan(){if(automation!=null&&(pause.paused()||editorOpen))automation.pause(SystemClock.elapsedRealtime());generation++;attempt++;main.removeCallbacksAndMessages(null);captureGate.reset();gesturePending=false;resetConfirmation();confirmId=-1;if(marker!=null)marker.setVisibility(View.INVISIBLE);checkpoint(true);}
    public void togglePause(){pause.toggle();if(!pause.manual)recoveryNeedsReview=false;recoveryNotice="";prefs.edit().putBoolean("sessionPaused",pause.manual).commit();invalidateScan();next(generation,0);}
    private void requestQuickSave(){pause.beginQuick();quickRequested=true;invalidateScan();setStatus("Quick save · capturing");next(generation,0);}
    public void quickSaveClosed(){pause.endQuick();quickRequested=false;if(running){invalidateScan();next(generation,0);}}
    private void showPaused(){if(nerve!=null)nerve.setActive(false);tapState("Paused · no new actions");if(pauseButton!=null)pauseButton.setText(pause.manual?"Resume":"Pause");setStatus(!recoveryNotice.isEmpty()?recoveryNotice:pause.marker?"Paused · marker editor":pause.quick?"Paused · Quick save editor":pause.transfer?"Paused · preset transfer":"Paused · tap Resume");if(pulseLabel!=null)pulseLabel.setText(runMode.equals("recognition")?"Scanning paused":"Countdown paused");if(pulseProgress!=null){pulseProgress.setIndeterminate(false);pulseProgress.setProgress(0);}}
    private void endLoop(){
        if(pulseTick!=null)main.removeCallbacks(pulseTick);
        running=false;automation=null;timedTicket++;recognitionTicket++;if(cancelMarker!=null)cancelMarker.run();generation++;attempt++;main.removeCallbacksAndMessages(null);pendingCapture=null;watchdog=null;
        captureGate.reset();gesturePending=false;freshCheck=null;
        if(marker!=null){try{wm.removeView(marker);}catch(RuntimeException ignored){}marker=null;}
        if(panel!=null){try{wm.removeView(panel);}catch(RuntimeException ignored){}panel=null;status=null;}
    }
    public void beginPresetTransfer(){if(!pause.transfer){pause.transfer=true;invalidateScan();next(generation,0);}}
    public void endPresetTransfer(){if(pause.transfer){pause.transfer=false;invalidateScan();next(generation,0);}}
    private int appliedSimple=100,appliedAdvanced=100;private float popupScale=1;
    public void applyLauncherPreference(){
        if(autoTap!=null)autoTap.resize();
        if(!recoveryNotice.isEmpty()||!running||panel==null||(panelDetailed==prefs.getBoolean("detailedLauncher",true)&&appliedSimple==prefs.getInt("simplePopupSize",100)&&appliedAdvanced==prefs.getInt("advancedPopupSize",100)))return;
        WindowManager.LayoutParams old=(WindowManager.LayoutParams)panel.getLayoutParams();int x=old.x,y=old.y;savePanelPosition();invalidateScan();wm.removeView(panel);panel=null;if(marker!=null){wm.removeView(marker);marker=null;}showPanel();WindowManager.LayoutParams p=(WindowManager.LayoutParams)panel.getLayoutParams();p.x=Math.min(x,Math.max(0,getResources().getDisplayMetrics().widthPixels-p.width));p.y=Math.min(y,Math.max(0,getResources().getDisplayMetrics().heightPixels-Ui.dp(this,panelDetailed?320:64)));PopupSize.fit(panel,p,this);wm.updateViewLayout(panel,p);next(generation,0);
    }
    private void attachOverlays(WindowManager.LayoutParams p){
        marker=new FocusOverlay(this);android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(metrics);WindowManager.LayoutParams mp=new WindowManager.LayoutParams(metrics.widthPixels,metrics.heightPixels,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);mp.gravity=Gravity.TOP|Gravity.START;mp.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;wm.addView(marker,mp);appliedSimple=prefs.getInt("simplePopupSize",100);appliedAdvanced=prefs.getInt("advancedPopupSize",100);popupScale=PopupSize.scale(this,panelDetailed?"advancedPopupSize":"simplePopupSize",panelDetailed?264:64,panelDetailed?360:64);PopupSize.tree(panel,popupScale);p.width=Math.round(p.width*popupScale);if(p.height>0)p.height=Math.round(p.height*popupScale);restorePanelPosition(p);PopupSize.fit(panel,p,this);wm.addView(panel,p);
    }
    private void showCompactPanel(){
        panel=Ui.column(this);panel.setTag("compact-launcher");BrainStopView stop=new BrainStopView(this);panel.addView(stop,new LinearLayout.LayoutParams(Ui.dp(this,64),Ui.dp(this,64)));stop.setOnClickListener(v->stopSession("Stopped"));WindowManager.LayoutParams p=new WindowManager.LayoutParams(Ui.dp(this,64),Ui.dp(this,64),WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.END;p.x=Ui.dp(this,12);p.y=Ui.dp(this,110);attachOverlays(p);
        stop.setOnTouchListener(new View.OnTouchListener(){float x,y;int ox,oy;boolean moved;public boolean onTouch(View v,MotionEvent e){switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:x=e.getRawX();y=e.getRawY();ox=p.x;oy=p.y;moved=false;return true;
            case MotionEvent.ACTION_MOVE:if(Math.abs(e.getRawX()-x)+Math.abs(e.getRawY()-y)>Ui.dp(TapService.this,6))moved=true;if(moved){android.util.DisplayMetrics m=getResources().getDisplayMetrics();p.x=Math.max(0,Math.min(m.widthPixels-p.width,ox-(int)(e.getRawX()-x)));p.y=Math.max(0,Math.min(m.heightPixels-p.height,oy+(int)(e.getRawY()-y)));wm.updateViewLayout(panel,p);}return true;
            case MotionEvent.ACTION_UP:savePanelPosition();if(!moved)v.performClick();return true;case MotionEvent.ACTION_CANCEL:savePanelPosition();return true;default:return true;
        }}});
    }
    private void showPanel(){
        status=null;tapFeedback=null;tapBadge=null;scanStats=null;nerve=null;pauseButton=null;pulseProgress=null;pulseLabel=null;panelDetailed=prefs.getBoolean("detailedLauncher",true)||!recoveryNotice.isEmpty();if(!panelDetailed){showCompactPanel();return;}
        panel=Ui.column(this);panel.setPadding(Ui.dp(this,12),Ui.dp(this,14),Ui.dp(this,12),Ui.dp(this,20));panel.setBackground(new BrainShell());panel.setElevation(Ui.dp(this,8));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);panel.addView(top);BrainView emblem=new BrainView(this,false);emblem.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);emblem.setContentDescription("Collapse AutoBash controls. Drag to move.");emblem.setFocusable(true);top.addView(emblem,new LinearLayout.LayoutParams(Ui.dp(this,36),Ui.dp(this,36)));TextView brand=Ui.label(this,"AutoBash",14,Ui.TEXT);brand.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-2,1);bp.leftMargin=Ui.dp(this,8);top.addView(brand,bp);tapBadge=Ui.label(this,taps+" actions",10,Ui.CREAM);tapBadge.setSingleLine(true);tapBadge.setPadding(Ui.dp(this,7),Ui.dp(this,5),Ui.dp(this,7),Ui.dp(this,5));tapBadge.setBackground(Ui.shape(this,Ui.RAISED,Ui.LINE,10));top.addView(tapBadge);
        status=Ui.label(this,actual?(runMode.equals("recognition")?"Ready to recognize":"Timer ready"):"Preview · actions off",11,Ui.MUTED);status.setPadding(Ui.dp(this,4),Ui.dp(this,8),Ui.dp(this,4),Ui.dp(this,10));status.setMaxLines(3);status.setEllipsize(android.text.TextUtils.TruncateAt.END);panel.addView(status,new LinearLayout.LayoutParams(-1,Ui.dp(this,64)));LinearLayout actions=new LinearLayout(this);panel.addView(actions);Button teach=Ui.button(this,"Quick save",false),stop=Ui.button(this,"Stop",false);stop.setTextColor(Ui.RED);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,Ui.dp(this,48),1);ap.rightMargin=Ui.dp(this,6);actions.addView(teach,ap);actions.addView(stop,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));teach.setTextSize(11);teach.setOnClickListener(v->requestQuickSave());stop.setOnClickListener(v->stopSession("Stopped"));pauseButton=Ui.button(this,"Pause",false);pauseButton.setTextSize(11);actions.addView(pauseButton,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));pauseButton.setOnClickListener(v->togglePause());
        actions.removeView(stop);actions.addView(stop,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));for(Button b:new Button[]{teach,pauseButton,stop}){b.setMaxLines(1);b.setTextSize(11);b.setGravity(Gravity.CENTER);b.setPadding(Ui.dp(this,4),Ui.dp(this,8),Ui.dp(this,4),Ui.dp(this,8));}
        pulseLabel=Ui.label(this,runMode.equals("recognition")?"Next scan":"Next action",10,Ui.MUTED);pulseLabel.setPadding(0,Ui.dp(this,8),0,0);pulseLabel.setSingleLine(true);pulseLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);panel.addView(pulseLabel,new LinearLayout.LayoutParams(-1,Ui.dp(this,28)));
        pulseProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);pulseProgress.setMax(1000);pulseProgress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.LINE));pulseProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(Ui.GREEN));panel.addView(pulseProgress,new LinearLayout.LayoutParams(-1,Ui.dp(this,3)));nerve=new NerveStrip(this,pulseProgress);panel.addView(nerve,new LinearLayout.LayoutParams(-1,Ui.dp(this,22)));
        scanStats=Ui.label(this,runMode.equals("recognition")?"Scan time appears after first scan":"Timer mode · no image detection",10,Ui.MUTED);scanStats.setMaxLines(2);scanStats.setEllipsize(android.text.TextUtils.TruncateAt.END);panel.addView(scanStats,new LinearLayout.LayoutParams(-1,Ui.dp(this,34)));
        tapFeedback=Ui.label(this,actual?(runMode.equals("recognition")?"Actions enabled · waiting for match":"Actions enabled · timer ready"):"Preview · actions off",10,Ui.GREEN);tapFeedback.setMaxLines(2);tapFeedback.setEllipsize(android.text.TextUtils.TruncateAt.END);tapFeedback.setPadding(0,Ui.dp(this,6),0,0);panel.addView(tapFeedback,new LinearLayout.LayoutParams(-1,Ui.dp(this,38)));
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(Ui.dp(this,264),-2,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.END;p.x=Ui.dp(this,12);p.y=Ui.dp(this,110);
        attachOverlays(p);
        final boolean[] collapsed={false},restoring={prefs.getBoolean("launcherCollapsed",false)&&recoveryNotice.isEmpty()};emblem.setOnClickListener(v->{collapsed[0]=!collapsed[0];brand.setVisibility(collapsed[0]?View.GONE:View.VISIBLE);status.setVisibility(collapsed[0]?View.GONE:View.VISIBLE);actions.setVisibility(collapsed[0]?View.GONE:View.VISIBLE);pulseProgress.setVisibility(collapsed[0]?View.GONE:View.VISIBLE);pulseLabel.setVisibility(collapsed[0]?View.GONE:View.VISIBLE);tapFeedback.setVisibility(collapsed[0]?View.GONE:View.VISIBLE);tapBadge.setVisibility(collapsed[0]?View.GONE:View.VISIBLE);scanStats.setVisibility(collapsed[0]?View.GONE:View.VISIBLE);nerve.setVisibility(collapsed[0]?View.GONE:View.VISIBLE);panel.setBackground(collapsed[0]?Ui.shape(this,Ui.SURFACE,Ui.LINE,28):new BrainShell());panel.setPadding(Ui.dp(this,collapsed[0]?10:12),Ui.dp(this,collapsed[0]?10:14),Ui.dp(this,collapsed[0]?10:12),Ui.dp(this,collapsed[0]?10:20));p.width=Ui.dp(this,(collapsed[0]?56:264)*popupScale);p.x=Math.min(p.x,Math.max(0,getResources().getDisplayMetrics().widthPixels-p.width));p.y=Math.min(p.y,Math.max(0,getResources().getDisplayMetrics().heightPixels-Ui.dp(this,collapsed[0]?56:290)));PopupSize.fit(panel,p,this);wm.updateViewLayout(panel,p);prefs.edit().putBoolean("launcherCollapsed",collapsed[0]).apply();if(!restoring[0])savePanelPosition();emblem.setContentDescription(collapsed[0]?"Expand AutoBash controls. Drag to move.":"Collapse AutoBash controls. Drag to move.");});if(restoring[0]){emblem.performClick();restorePanelPosition(p);wm.updateViewLayout(panel,p);}restoring[0]=false;
        View.OnTouchListener drag=new View.OnTouchListener(){float x,y;int ox,oy;boolean moved;public boolean onTouch(View v,MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){x=e.getRawX();y=e.getRawY();ox=p.x;oy=p.y;moved=false;return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){if(Math.abs(e.getRawX()-x)+Math.abs(e.getRawY()-y)>Ui.dp(TapService.this,5))moved=true;if(moved){android.util.DisplayMetrics m=getResources().getDisplayMetrics();p.x=Math.max(0,Math.min(m.widthPixels-panel.getWidth(),ox-(int)(e.getRawX()-x)));p.y=Math.max(0,Math.min(m.heightPixels-panel.getHeight(),oy+(int)(e.getRawY()-y)));wm.updateViewLayout(panel,p);}return true;}if(e.getAction()==MotionEvent.ACTION_UP){savePanelPosition();if(!moved)v.performClick();return true;}if(e.getAction()==MotionEvent.ACTION_CANCEL){savePanelPosition();return true;}return true;}};emblem.setOnTouchListener(drag);status.setOnTouchListener(drag);
    }
    private void focus(Rect rect,String title){if(marker!=null){marker.preferences(prefs.getBoolean("focusGuides",true),prefs.getBoolean("reduceMotion",false));marker.candidate(rect,title);marker.setVisibility(View.VISIBLE);}}
    private void touchPulse(int x,int y){if(marker!=null&&prefs.getBoolean("touchPulse",true)){marker.preferences(prefs.getBoolean("focusGuides",true),prefs.getBoolean("reduceMotion",false));marker.pulse(x,y);marker.setVisibility(View.VISIBLE);}}
    private void next(int token){next(token,0);}
    private void next(int token,long minimumWait){
        if(!current(token))return;checkpoint(false);
        if(!runMode.equals("recognition")&&!quickRequested){nextAutomation(token);return;}
        if(pauseButton!=null)pauseButton.setText(pause.manual?"Resume":"Pause");
        if(pause.paused()&&!quickRequested){showPaused();return;}
        if(nerve!=null)nerve.setActive(true);
        if(pendingCapture!=null)main.removeCallbacks(pendingCapture);
        settings();long wait=Math.max(minimumWait,Math.max(0,lastCapture+Math.max(captureFloorMs,(quickRequested||freshCheck!=null)?350:pulseMs)-SystemClock.elapsedRealtime()));
        if(pulseTick!=null)main.removeCallbacks(pulseTick);
        if(pulseProgress!=null){pulseProgress.setIndeterminate(false);pulseProgress.setProgress(0);final long starts=SystemClock.elapsedRealtime(),duration=wait;
            pulseTick=new Runnable(){public void run(){if(!current(token)||pulseProgress==null)return;long elapsed=SystemClock.elapsedRealtime()-starts,remaining=Math.max(0,duration-elapsed);pulseProgress.setProgress(duration==0?1000:(int)Math.min(1000,elapsed*1000/duration));if(pulseLabel!=null)pulseLabel.setText(String.format(java.util.Locale.US,"Next scan · %.1f s",remaining/1000.0));if(remaining>0)main.postDelayed(this,100);}};pulseTick.run();}


        pendingCapture=()->{if(marker!=null)marker.setVisibility(View.INVISIBLE);main.postDelayed(()->capture(token),80);};main.postDelayed(pendingCapture,Math.max(0,wait-80));
    }
    private void retry(int token,int captureAttempt,String reason){
        if(!current(token)||attempt!=captureAttempt)return;
        if(panel!=null)panel.setVisibility(View.VISIBLE);
        if(watchdog!=null)main.removeCallbacks(watchdog);watchdog=null;attempt++;
        captureGate.reset();resetConfirmation();setStatus(reason+" • retrying");next(token,TapTiming.retryWait(++failures));
    }
    private void capture(int token){
        if(!current(token))return;if(pause.paused()&&!quickRequested){showPaused();return;}pendingCapture=null;settings();if(captureGate.busy())return;
        if(editorOpen&&!quickRequested){setStatus("Image editor / app open");next(token,500);return;}
        if(work!=null&&!work.isDone()){next(token,100);return;}
        try{
            if(!quickRequested&&targets.isEmpty()){setStatus("Waiting • add a target");next(token,1000);return;}
            String wait=screenWaitReason();if(wait!=null){resetConfirmation();setStatus(wait);next(token,1000);return;}
        }catch(RuntimeException e){setStatus("Waiting for screen access • retrying");next(token,1000);return;}
        if(gesturePending){if(SystemClock.elapsedRealtime()-gestureStarted>2000){gesturePending=false;setStatus("Action timeout • retrying");tapState("No Android callback · retrying");}else{next(token,50);return;}}
        boolean quickCapture=quickRequested;int ticket=captureGate.begin();if(ticket==0)return;
        if(quickCapture&&panel!=null)panel.setVisibility(View.INVISIBLE);
        main.postDelayed(()->requestScreenshot(token,quickCapture,ticket),quickCapture?80:0);
    }
    private void requestScreenshot(int token,boolean quickCapture,int ticket){
        if(!current(token))return;
        int captureAttempt=++attempt;lastCapture=SystemClock.elapsedRealtime();if(pulseTick!=null)main.removeCallbacks(pulseTick);if(pulseProgress!=null)pulseProgress.setIndeterminate(true);if(pulseLabel!=null)pulseLabel.setText("Scanning this pulse…");setStatus("Scanning…");
        watchdog=()->retry(token,captureAttempt,"Capture timeout");main.postDelayed(watchdog,6000);
        try{takeScreenshot(Display.DEFAULT_DISPLAY,getMainExecutor(),new TakeScreenshotCallback(){
            @Override public void onSuccess(ScreenshotResult result){
                HardwareBuffer buffer=result.getHardwareBuffer();Bitmap bitmap=null;
                try{
                    if(!current(token)||attempt!=captureAttempt)return;
                    Bitmap hw=Bitmap.wrapHardwareBuffer(buffer,result.getColorSpace());
                    if(hw!=null){bitmap=hw.copy(Bitmap.Config.ARGB_8888,false);hw.recycle();}
                }catch(RuntimeException ignored){}finally{buffer.close();}
                if(!captureGate.finish(ticket)){if(bitmap!=null)bitmap.recycle();return;}
                if(panel!=null)panel.setVisibility(View.VISIBLE);
                if(bitmap==null){retry(token,captureAttempt,"Capture unavailable");return;}captureMillis=Math.max(0,SystemClock.elapsedRealtime()-lastCapture);
                if(marker!=null){WindowManager.LayoutParams ml=(WindowManager.LayoutParams)marker.getLayoutParams();if(ml.width!=bitmap.getWidth()||ml.height!=bitmap.getHeight()){ml.width=bitmap.getWidth();ml.height=bitmap.getHeight();wm.updateViewLayout(marker,ml);}marker.preferences(prefs.getBoolean("focusGuides",true),prefs.getBoolean("reduceMotion",false));marker.scanning(scanAreas(bitmap.getWidth(),bitmap.getHeight()));marker.setVisibility(View.VISIBLE);}
                if(watchdog!=null)main.removeCallbacks(watchdog);watchdog=null;failures=0;
                try{
                    if(quickCapture){quickRequested=false;Bitmap captured=bitmap;work=worker.submit(()->{try{File file=new File(getCacheDir(),"quick-save.png");try(java.io.FileOutputStream out=new java.io.FileOutputStream(file)){if(!captured.compress(Bitmap.CompressFormat.PNG,100,out))throw new java.io.IOException("Capture failed");}main.post(()->{if(current(token)){editorOpen=true;startActivity(new Intent(TapService.this,MainActivity.class).putExtra("quickSave",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));showPaused();}});}catch(Exception e){main.post(()->{if(current(token)){pause.endQuick();setStatus("Quick save failed • try again");next(token);}});}finally{captured.recycle();}});}
                    else analyze(bitmap,token,result.getTimestamp());
                }catch(RuntimeException e){if(!bitmap.isRecycled())bitmap.recycle();retry(token,captureAttempt,"Processing unavailable");}
            }
            @Override public void onFailure(int error){if(!current(token)||attempt!=captureAttempt)return;if(error==ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT){if(watchdog!=null)main.removeCallbacks(watchdog);watchdog=null;captureGate.finish(ticket);if(panel!=null)panel.setVisibility(View.VISIBLE);captureFloorMs=TapTiming.throttledCaptureFloor(captureFloorMs);setStatus("Adapting to Android capture rate");next(token);return;}retry(token,captureAttempt,"Capture unavailable ("+error+")");}
        });}catch(RuntimeException e){retry(token,captureAttempt,"Capture unavailable");}
    }
    public void cancelMarkerFor(android.app.Activity owner){if(markerOwner==owner&&cancelMarker!=null)cancelMarker.run();}
    public void pickMarker(android.app.Activity activity,TimedAction item,java.util.function.Consumer<float[]> result){
        if(markerPicker!=null)return;pause.marker=true;invalidateScan();if(panel!=null)panel.setVisibility(View.INVISIBLE);if(marker!=null)marker.setVisibility(View.INVISIBLE);markerOwner=activity;
        java.util.function.Consumer<float[]> close=points->{if(markerPicker==null)return;try{wm.removeView(markerPicker);}catch(RuntimeException ignored){}markerPicker=null;markerOwner=null;cancelMarker=null;pause.marker=false;if(panel!=null)panel.setVisibility(View.VISIBLE);if(!activity.isDestroyed()&&!activity.isFinishing()&&!activity.isChangingConfigurations()){editorOpen=true;result.accept(points);activity.startActivity(new Intent(activity,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));}next(generation,0);};
        markerPicker=new MarkerPicker(this,item,close);cancelMarker=()->close.accept(null);android.util.DisplayMetrics dm=new android.util.DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(dm);WindowManager.LayoutParams lp=new WindowManager.LayoutParams(dm.widthPixels,dm.heightPixels,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);lp.gravity=Gravity.TOP|Gravity.LEFT;lp.setFitInsetsTypes(0);lp.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        try{wm.addView(markerPicker,lp);String pkg=prefs.getString("app","");Intent launch=pkg.isEmpty()?null:getPackageManager().getLaunchIntentForPackage(pkg);if(launch!=null)activity.startActivity(launch);else activity.moveTaskToBack(true);}catch(RuntimeException e){close.accept(null);Toast.makeText(activity,"Could not open screen marker editor.",Toast.LENGTH_LONG).show();}
    }
    private void scheduleAutomation(int token,long wait){if(pendingCapture!=null)main.removeCallbacks(pendingCapture);pendingCapture=()->nextAutomation(token);main.postDelayed(pendingCapture,Math.max(16,wait));}
    private void nextAutomation(int token){
        if(!current(token)||automation==null)return;checkpoint(false);AutomationClock clock=automation;long now=SystemClock.elapsedRealtime();settings();
        if(pause.paused()){clock.pause(now);showPaused();return;}
        String reason=screenWaitReason();if(editorOpen||reason!=null||!actual){clock.pause(now);setStatus(editorOpen?"Timed actions paused · editor open":reason!=null?reason:"Preview · Allow actions is off");if(nerve!=null)nerve.setActive(false);scheduleAutomation(token,250);return;}
        clock.resume(now);if(pauseButton!=null)pauseButton.setText("Pause");
        if(clock.done){setStatus("Timeline complete");tapState(taps+" actions completed");if(pulseLabel!=null)pulseLabel.setText("Finished · restart to run again");if(pulseProgress!=null){pulseProgress.setIndeterminate(false);pulseProgress.setProgress(1000);}if(nerve!=null)nerve.setActive(false);prefs.edit().putBoolean("sessionActive",false).remove("sessionCheckpoint").commit();return;}
        if(clock.pending>=0){if(now-clock.startedAt>2500){timedTicket++;clock.failed(now);gesturePending=false;tapState("Action callback timeout · retrying");}scheduleAutomation(token,50);return;}
        int id=clock.next();if(id<0)return;TimedAction item=clock.actions.get(id);if(item.action.navigation())focus(null,"");else{android.util.DisplayMetrics m=new android.util.DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(m);int px=Math.round(item.x*m.widthPixels),py=Math.round(item.y*m.heightPixels);focus(new Rect(px-18,py-18,px+18,py+18),item.action.label+" · timed");}long remaining=clock.remaining(now);setStatus((clock.timeline?"Timeline · step "+(id+1)+"/"+clock.actions.size():"General")+"\n"+item.title());if(pulseLabel!=null)pulseLabel.setText("Next action · "+AutomationUi.seconds(remaining));if(pulseProgress!=null){pulseProgress.setIndeterminate(false);pulseProgress.setProgress((int)Math.max(0,Math.min(1000,1000*(item.delay-remaining)/item.delay)));}if(nerve!=null)nerve.setActive(true);
        if(remaining>0){scheduleAutomation(token,Math.min(100,remaining));return;}if(clock.take(now)<0)return;sendTimed(clock,id,token);
    }
    private void sendTimed(AutomationClock clock,int id,int token){
        TimedAction item=clock.actions.get(id);android.util.DisplayMetrics dm=new android.util.DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(dm);float x=Math.max(1,Math.min(dm.widthPixels-2,item.x*dm.widthPixels)),y=Math.max(1,Math.min(dm.heightPixels-2,item.y*dm.heightPixels));ActionGesture geometry=new ActionGesture(x,y,Math.max(1,Math.min(dm.widthPixels-2,item.endX*dm.widthPixels)),Math.max(1,Math.min(dm.heightPixels-2,item.endY*dm.heightPixels)),item.duration);
        int[] pos=new int[2];panel.getLocationOnScreen(pos);Rect overlay=new Rect(pos[0]-8,pos[1]-8,pos[0]+panel.getWidth()+8,pos[1]+panel.getHeight()+8);
        if(!item.action.navigation()&&(overlay.contains((int)x,(int)y)||(item.action.swipe()&&(geometry.tooShort()||geometry.crosses(overlay.left,overlay.top,overlay.right,overlay.bottom))))){clock.failed(SystemClock.elapsedRealtime());tapState("Move controls away from markers; check swipe length");scheduleAutomation(token,500);return;}
        final long ticket=++timedTicket;java.util.function.Consumer<Boolean> completed=ok->{if(automation!=clock||!running||ticket!=timedTicket)return;gesturePending=false;actionInFlight=false;if(ok){clock.complete(SystemClock.elapsedRealtime());taps++;tapState((item.action.navigation()?"Android accepted ":"Android completed ")+item.action.label);actionEffect(item.action,geometry);}else{clock.failed(SystemClock.elapsedRealtime());tapState("Android rejected/cancelled action · retrying");}checkpoint(true);next(generation);};
        actionInFlight=true;if(!checkpoint(true)){actionInFlight=false;clock.failed(SystemClock.elapsedRealtime());showPaused();return;}
        try{tapState("Sending "+item.action.label);if(item.action.navigation())completed.accept(sendNavigationAction(item.action==ActionType.BACK?GLOBAL_ACTION_BACK:GLOBAL_ACTION_HOME));else{Path path=new Path();path.moveTo(x,y);if(item.action.swipe())path.lineTo(geometry.endX,geometry.endY);gesturePending=true;gestureStarted=SystemClock.elapsedRealtime();boolean accepted=sendTapGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,item.duration)).build(),new GestureResultCallback(){@Override public void onCompleted(GestureDescription g){completed.accept(true);}@Override public void onCancelled(GestureDescription g){completed.accept(false);}});if(!accepted)completed.accept(false);else scheduleAutomation(token,50);}}catch(RuntimeException e){completed.accept(false);}
    }
    private static Mat bitmapMat(Bitmap bitmap){
        int[] p=new int[bitmap.getWidth()*bitmap.getHeight()];bitmap.getPixels(p,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());return VisionMatcher.fromArgb(p,bitmap.getWidth(),bitmap.getHeight());
    }
    private Mat template(File file){
        String key=file.getAbsolutePath()+"#"+file.lastModified()+"#"+file.length();Mat cached=templateCache.get(key);if(cached!=null)return cached;
        Bitmap bitmap=TargetStore.load(file);if(bitmap==null)return null;Mat m;try{m=bitmapMat(bitmap);}finally{bitmap.recycle();}
        templateCache.put(key,m);cacheBytes+=m.total()*m.elemSize();
        while(templateCache.size()>500||cacheBytes>48*1024*1024){String oldest=templateCache.keySet().iterator().next();if(oldest.equals(key))break;Mat old=templateCache.remove(oldest);cacheBytes-=old.total()*old.elemSize();preparedCache.clear();old.release();}
        return m;
    }
    private void analyze(Bitmap screen,int token,long capturedAt){
        if(!CaptureGate.fresh(SystemClock.uptimeMillis(),capturedAt)){screen.recycle();setStatus("Old screenshot • retrying");next(token);return;}int originalW=screen.getWidth(),originalH=screen.getHeight();
        FreshCheck pending=freshCheck;freshCheck=null;
        if(pending!=null&&pending.width==originalW&&pending.height==originalH&&pending.hits.length==targets.size()&&Double.compare(pending.limit,maxError)==0&&pending.regions.equals(regionJson)&&pending.useRegions==useRegions){
            if(capturedAt<=pending.stamp){freshCheck=pending;screen.recycle();tapState("Waiting for a newer screenshot · retrying");next(token,350);return;}
            revalidate(screen,token,capturedAt,pending);return;
        }
        File[] files=targets.toArray(new File[0]);TargetOptions[] options=new TargetOptions[files.length];for(int i=0;i<files.length;i++)options[i]=TargetOptions.load(this,files[i]);
        final double limit=maxError;final String scannedRegions=regionJson;final boolean scannedUseRegions=useRegions;
        final boolean hybrid=prefs.getBoolean("hybridSearch",true);
        final boolean alternate=prefs.getBoolean("alternateSizes",false);final int workers=scanWorkers();
        final int fallbackAccuracy=TapTiming.similarity(prefs.getInt("similarity",96));
        final List<RegionRules.Region> areas=scanAreas(originalW,originalH);final long captureTime=captureMillis;final int[] displayed={0};
        work=worker.submit(()->{
            Mat original=null;
            try{
                long preparation=System.nanoTime();try{original=bitmapMat(screen);}finally{screen.recycle();}
                if(scanEngine==null||scanEngine.workers()!=workers){if(scanEngine!=null)scanEngine.close();scanEngine=new ScanEngine(workers);}
                // Native parallelism is bounded by our lanes; avoid four lanes spawning four more pools.
                List<ScanEngine.Job> jobs=new ArrayList<>();
                for(int i=0;i<files.length;i++){final File file=files[i];jobs.add(new ScanEngine.Job(fingerprint(file),options[i].scale(originalW),options[i].area,()->{Bitmap b=TargetStore.load(file);if(b==null)return null;try{return bitmapMat(b);}finally{b.recycle();}},tracking.get(file.getAbsolutePath())));}
                long preparedMs=(System.nanoTime()-preparation)/1000000;
                ScanEngine.Result result=scanEngine.scan(original,jobs,fallbackAccuracy,areas,hybrid,alternate,()->!current(token),count->{if(count%4==0||count==files.length)main.post(()->{if(current(token)&&pulseLabel!=null&&count>displayed[0]){displayed[0]=count;pulseLabel.setText("Checking targets · "+count+"/"+files.length);if(pulseProgress!=null){pulseProgress.setIndeterminate(false);pulseProgress.setProgress(count*1000/Math.max(1,files.length));}}});});
                if(!current(token))return;
                for(int i=0;i<files.length;i++){if(result.hits[i]!=null)tracking.put(files[i].getAbsolutePath(),result.hits[i]);else tracking.remove(files[i].getAbsolutePath());}
                main.post(()->{if(!current(token))return;coverage="Capture "+captureTime+" · Prep "+preparedMs+" · Search "+result.millis+" ms\n"+files.length+" targets / "+result.unique+" searches · "+result.workers+" workers";if(scanStats!=null)scanStats.setText(coverage);
                    boolean matched=false;for(VisionMatcher.Hit h:result.hits)if(h!=null){matched=true;break;}if(!matched){focus(null,"");setStatus(result.diagnostic);}
                    finish(result.hits,result.checked,originalW,originalH,token,limit,scannedRegions,scannedUseRegions,result.diagnostic,capturedAt,true);
                });
            }catch(RuntimeException e){if(!screen.isRecycled())screen.recycle();main.post(()->{if(current(token)){setStatus("Recognition error • retrying");next(token,1000);}});}finally{if(original!=null)original.release();}
        });
    }
    private void queueFreshCheck(VisionMatcher.Hit[] hits,int width,int height,double limit,String regions,boolean use,long stamp){
        freshCheck=new FreshCheck(hits,width,height,limit,regions,use,stamp);
        // The slow discovery frame remains evidence, never permission to dispatch.
        for(int i=0;i<hits.length;i++)if(hits[i]!=null){VisionMatcher.Hit h=hits[i];confirmations.observe(i,Math.round(h.x+h.width/2f),Math.round(h.y+h.height/2f),stamp);}
    }
    private void revalidate(Bitmap screen,int token,long capturedAt,FreshCheck pending){
        final File[] files=targets.toArray(new File[0]);final List<RegionRules.Region> areas=scanAreas(pending.width,pending.height);
        final int accuracy=TapTiming.similarity(prefs.getInt("similarity",96));
        setStatus("Verifying candidates on a fresh screenshot…");
        work=worker.submit(()->{
            try{
                Mat pixels;try{pixels=bitmapMat(screen);}finally{screen.recycle();}
                VisionMatcher.Hit[] verified=new VisionMatcher.Hit[files.length];boolean[] checked=new boolean[files.length];boolean complete=true;
                try(VisionMatcher.Frame frame=new VisionMatcher.Frame(pixels,720,accuracy,areas,preparedCache)){
                    frame.cancelled=()->!current(token);VisionMatcher.Frame fine=null;
                    try{
                        long deadline=System.nanoTime()+1200000000L;
                        for(int i=0;i<files.length;i++)if(pending.hits[i]!=null){
                            if(!current(token))return;
                            if(System.nanoTime()>deadline){complete=false;break;}
                            Mat target=template(files[i]);checked[i]=true;if(target==null)continue;
                            VisionMatcher.Hit old=pending.hits[i];VisionMatcher.Frame chosen=frame;
                            if(Math.min(old.width,old.height)*frame.factor<24){if(fine==null){fine=new VisionMatcher.Frame(frame.original.clone(),pending.width,accuracy,areas,preparedCache);fine.cancelled=frame.cancelled;}chosen=fine;}
                            verified[i]=VisionMatcher.recheck(chosen,target,old,TargetOptions.load(this,files[i]).area,deadline);
                        }
                    }finally{if(fine!=null)fine.close();}
                }
                final boolean finished=complete;
                main.post(()->{
                    if(!current(token))return;
                    if(!finished||!CaptureGate.fresh(SystemClock.uptimeMillis(),capturedAt)){
                        resetConfirmation();tapState("Verification timed out · restarting scan");next(token,350);return;
                    }
                    coverage="Fresh verification · "+(SystemClock.uptimeMillis()-capturedAt)+" ms";if(scanStats!=null)scanStats.setText(coverage);
                    finish(verified,checked,pending.width,pending.height,token,pending.limit,pending.regions,pending.useRegions,"",capturedAt,true);
                });
            }catch(RuntimeException e){if(!screen.isRecycled())screen.recycle();main.post(()->{if(current(token)){resetConfirmation();tapState("Verification unavailable · retrying");next(token,500);}});}
        });
    }
    private void finish(VisionMatcher.Hit[] hits,boolean[] checked,int width,int height,int token,double scannedLimit,String scannedRegions,boolean scannedUseRegions,String diagnostic,long capturedAt,boolean reschedule){
        if(!current(token))return;settings();
        if(pause.paused()){tapState("Paused · action blocked");if(reschedule)next(token);return;}if(editorOpen){tapState("Editor open · action blocked");if(reschedule)next(token,500);return;}
        if(Double.compare(maxError,scannedLimit)!=0||!regionJson.equals(scannedRegions)||useRegions!=scannedUseRegions){resetConfirmation();confirmId=-1;if(reschedule)next(token);return;}
        String wait=screenWaitReason();if(wait!=null){resetConfirmation();setStatus(wait);next(token,1000);return;}
        android.util.DisplayMetrics dm=new android.util.DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(dm);
        if(dm.widthPixels!=width||dm.heightPixels!=height){tapState("Display changed · recapturing");resetConfirmation();if(reschedule)next(token);return;}
        int chosen=-1;VisionMatcher.Hit best=null;
        for(int i=0;i<hits.length;i++){
            if(!checked[i])continue;
            if(hits[i]==null){confirmations.reset(i);if(++missing[i]>=2)latched[i]=false;}
            else{missing[i]=0;if((repeat||!latched[i])&&(best==null||hits[i].score>best.score)){chosen=i;best=hits[i];}}
        }
        if(best==null){tapState(repeat?"No eligible target":"Already acted · Repeat visible targets off");if(reschedule)next(token);return;}
        if(!CaptureGate.fresh(SystemClock.uptimeMillis(),capturedAt)){
            queueFreshCheck(hits,width,height,scannedLimit,scannedRegions,scannedUseRegions,capturedAt);
            tapState("Old frame · rechecking candidates now");if(reschedule)next(token,0);return;
        }
        final ActionType action=chosen<targets.size()?TargetStore.actionOf(this,targets.get(chosen)):ActionType.TAP;
        int x=Math.round(best.x+best.width/2f),y=Math.round(best.y+best.height/2f);
        focus(new Rect(best.x,best.y,best.x+best.width,best.y+best.height),actual?action.label+" · "+Math.round(best.score)+"%":"Preview · "+Math.round(best.score)+"%");
        confirmCount=confirmations.observe(chosen,x,y,capturedAt);
        confirmId=chosen;confirmX=x;confirmY=y;
        setStatus(action.label+" · Target "+(chosen+1)+" • "+x+", "+y+" • "+String.format(java.util.Locale.US,"%.1f",best.score)+"/"+best.accuracy+"%"+(actual?" • "+delayMs+"ms":" • test"));
        int[] pos=new int[2];panel.getLocationOnScreen(pos);Rect overlay=new Rect(pos[0]-8,pos[1]-8,pos[0]+panel.getWidth()+8,pos[1]+panel.getHeight()+8);
        String blocked=null;
        if(!actual)blocked="Preview · actions off";
        else if(lastDispatchedFrame==capturedAt)blocked="Already sent an action for this frame";
        else if(!CaptureGate.fresh(SystemClock.uptimeMillis(),capturedAt))blocked="Old frame · fresh check required";
        else if(requireConfirmation&&confirmCount<2)blocked="Target "+(chosen+1)+" · confirming 1/2 frames";
        else if(gesturePending)blocked="Waiting for Android gesture callback";
        else if(!TapTiming.canTap(SystemClock.elapsedRealtime(),lastTap,delayMs,false))blocked="Waiting for configured action delay";
        else if(overlay.contains(x,y))blocked="Move floating controls away from target";
        if(blocked!=null){tapState(blocked);if(reschedule)next(token);return;}
        final boolean centred=action.swipe()&&prefs.getBoolean("swipeFromCentre",false);
        final ActionGesture geometry=new ActionGesture(action,centred?width/2:x,centred?height/2:y,width,height,prefs.getInt("swipePercent",35),prefs.getInt("swipeMs",350));
        if(action.swipe()&&(geometry.tooShort()||geometry.crosses(overlay.left,overlay.top,overlay.right,overlay.bottom))){tapState(geometry.tooShort()?"Swipe too close to screen edge":"Move floating controls away from swipe path");if(reschedule)next(token);return;}
        final int id=chosen;final String dispatchKey=id<targetKeys.size()?targetKeys.get(id):"";final long dispatchTicket=++recognitionTicket;actionInFlight=true;if(!checkpoint(true)){actionInFlight=false;showPaused();return;}
        try{
            if(action.navigation()){
                boolean accepted=sendNavigationAction(action==ActionType.BACK?GLOBAL_ACTION_BACK:GLOBAL_ACTION_HOME);
                if(accepted){recordDispatch(id,capturedAt);latched[id]=true;taps++;tapState("Android accepted "+action.label+" · "+taps+" actions");actionEffect(action,geometry);}
                else tapState("Android rejected "+action.label+" · retrying");
                actionInFlight=false;checkpoint(true);
            }else{
                Path path=new Path();path.moveTo(geometry.x,geometry.y);if(action.swipe())path.lineTo(geometry.endX,geometry.endY);
                gesturePending=true;gestureStarted=SystemClock.elapsedRealtime();tapState("Sending "+action.label);
                boolean accepted=sendTapGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,action.swipe()?geometry.duration:pressMs)).build(),new GestureResultCallback(){
                    @Override public void onCompleted(GestureDescription g){if(running&&dispatchTicket==recognitionTicket){gesturePending=false;actionInFlight=false;int restoredId=targetKeys.indexOf(dispatchKey);if(restoredId>=0)latched[restoredId]=true;else if(current(token)&&id<latched.length)latched[id]=true;taps++;checkpoint(true);tapState("Android completed "+action.label+" · "+taps+" actions");actionEffect(action,geometry);if(pause.paused())showPaused();}}
                    @Override public void onCancelled(GestureDescription g){if(running&&dispatchTicket==recognitionTicket){gesturePending=false;actionInFlight=false;checkpoint(true);tapState("Android cancelled "+action.label+" · retrying");if(pause.paused())showPaused();}}
                });
                if(accepted)recordDispatch(id,capturedAt);else{gesturePending=false;actionInFlight=false;checkpoint(true);tapState("Android rejected "+action.label+" · check Accessibility");}
            }
        }catch(RuntimeException e){gesturePending=false;actionInFlight=false;checkpoint(true);tapState(action.label+" unavailable · check Accessibility");}
        if(reschedule)next(token);
    }
    private void recordDispatch(int id,long frame){lastDispatchedFrame=frame;lastTap=SystemClock.elapsedRealtime();confirmations.reset(id);confirmCount=0;}
    protected boolean sendNavigationAction(int action){return performGlobalAction(action);}
    private void actionEffect(ActionType action,ActionGesture geometry){if(marker!=null&&prefs.getBoolean("touchPulse",true)){marker.preferences(prefs.getBoolean("focusGuides",true),prefs.getBoolean("reduceMotion",false));marker.action(action,geometry);marker.setVisibility(View.VISIBLE);}}
    /** Adapter for exercising gesture gates and callbacks without a physical device. */
    protected boolean sendTapGesture(GestureDescription gesture,GestureResultCallback callback){return dispatchGesture(gesture,callback,main);}
    private List<RegionRules.Region> scanAreas(int w,int h){
        List<RegionRules.Region> out=useRegions?RegionStore.decode(regionJson):new ArrayList<>();
        if(panel!=null){int[] p=new int[2];panel.getLocationOnScreen(p);float l=Math.max(0,(p[0]-8f)/w),t=Math.max(0,(p[1]-8f)/h),r=Math.min(1,(p[0]+panel.getWidth()+8f)/w),b=Math.min(1,(p[1]+panel.getHeight()+8f)/h);if(r>l&&b>t)out.add(new RegionRules.Region(l,t,r,b,100,true));}
        return out;
    }
}

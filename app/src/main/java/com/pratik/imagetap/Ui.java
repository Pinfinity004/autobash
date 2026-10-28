package com.pratik.imagetap;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

final class Ui {
    static final int BG=0xff0c1424,SURFACE=0xff121f33,RAISED=0xff1c2b42,LINE=0xff34425a,GREEN=0xff85d9a8,CREAM=0xfff1e5ca,TEXT=CREAM,MUTED=0xffa5b0bf,RED=0xffe5a49b;
    private static Typeface displayFont, bodyFont;
    static Typeface display(Context c){if(displayFont==null)displayFont=c.getResources().getFont(R.font.space_grotesk);return Typeface.create(displayFont,600,false);}
    static Typeface body(Context c){if(bodyFont==null)bodyFont=c.getResources().getFont(R.font.manrope);return Typeface.create(bodyFont,400,false);}
    static int accent(int section){return section==1?0xffe4c895:section==2?0xff85d9a8:section==4?0xffaebeff:0xffa9cce5;}
    static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
    static GradientDrawable shape(Context c,int color,int border,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));if(border!=0)d.setStroke(dp(c,1),border);return d;}
    static ColorStateList tint(int on,int off){return new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{on,off});}
    static TextView label(Context c,String value,int sp,int color){TextView t=new TextView(c);t.setText(value);t.setTextSize(sp);t.setTextColor(color);t.setTypeface(sp>=20?display(c):body(c));t.setFontFeatureSettings("kern");t.setIncludeFontPadding(false);t.setLineSpacing(dp(c,3),1);return t;}
    static void window(Activity a){a.getWindow().setStatusBarColor(BG);a.getWindow().setNavigationBarColor(BG);a.getWindow().getDecorView().setSystemUiVisibility(0);}
    static Button button(Context c,String text,boolean primary){Button b=new Button(c);b.setText(text);styleButton(b,primary);return b;}
    static void styleButton(Button b,boolean primary){Context c=b.getContext();b.setAllCaps(false);b.setTextSize(14);b.setTypeface(Typeface.create(body(c),600,false));b.setTextColor(primary?BG:TEXT);b.setMinHeight(dp(c,48));b.setMinimumHeight(dp(c,48));b.setPadding(dp(c,14),dp(c,10),dp(c,14),dp(c,10));b.setStateListAnimator(null);b.setElevation(0);b.setBackgroundTintList(null);b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x3083efb3),shape(c,primary?CREAM:RAISED,primary?0:LINE,16),null));}
    static void activeGlow(TextView view,boolean on){view.setTextColor(on?GREEN:TEXT);view.setShadowLayer(on?dp(view.getContext(),3):0,0,0,on?0x9985d9a8:Color.TRANSPARENT);}
    static boolean motion(Context c){return ValueAnimator.areAnimatorsEnabled()&&!c.getSharedPreferences("config",0).getBoolean("reduceMotion",false);}
    static void enter(View v){if(!motion(v.getContext()))return;v.setAlpha(0);v.setTranslationY(dp(v.getContext(),8));v.animate().alpha(1).translationY(0).setDuration(220).setInterpolator(new DecelerateInterpolator()).start();}
    static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
    static void polish(View view){
        if(view instanceof ToggleButton){ToggleButton b=(ToggleButton)view;styleButton(b,false);b.setBackgroundTintList(tint(0xff3f5268,RAISED));activeGlow(b,b.isChecked());}
        else if(view instanceof Switch){Switch s=(Switch)view;s.setTextColor(TEXT);s.setTypeface(body(view.getContext()));s.setTextSize(14);s.setSwitchPadding(dp(view.getContext(),12));s.setThumbTintList(tint(CREAM,MUTED));s.setTrackTintList(tint(0xff6d756e,LINE));s.setMinHeight(dp(view.getContext(),48));activeGlow(s,s.isChecked());}
        else if(view instanceof CheckBox){CheckBox b=(CheckBox)view;b.setTextColor(TEXT);b.setButtonTintList(tint(CREAM,MUTED));b.setMinHeight(dp(view.getContext(),48));}
        else if(view instanceof RadioButton){RadioButton b=(RadioButton)view;b.setTextColor(TEXT);b.setButtonTintList(tint(CREAM,MUTED));b.setPadding(dp(view.getContext(),6),dp(view.getContext(),10),dp(view.getContext(),6),dp(view.getContext(),10));}
        else if(view instanceof Button){Button b=(Button)view;String s=b.getText().toString().toLowerCase(java.util.Locale.ROOT);styleButton(b,(s.startsWith("save")&&!s.contains("do not click")&&!s.contains("unsure"))||s.startsWith("start")||s.startsWith("add area"));if(s.contains("delete")||s.equals("stop")||s.contains("do not click"))b.setTextColor(RED);}
        else if(view instanceof TextView){TextView t=(TextView)view;t.setTextColor(TEXT);t.setTypeface(body(view.getContext()));}
        if(view instanceof SeekBar){SeekBar s=(SeekBar)view;s.setProgressTintList(ColorStateList.valueOf(CREAM));s.setThumbTintList(ColorStateList.valueOf(CREAM));s.setProgressBackgroundTintList(ColorStateList.valueOf(LINE));s.setMinimumHeight(dp(view.getContext(),48));}
        if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++)polish(g.getChildAt(i));}
    }
    static final class Glyph extends View {
        private final Paint p=new Paint(3);private final String kind;private final int color;
        Glyph(Context c,String kind,int color){super(c);this.kind=kind;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float size=Math.min(getWidth(),getHeight()),u=size/24f;canvas.save();canvas.translate((getWidth()-size)/2,(getHeight()-size)/2);canvas.scale(u,u);p.setColor(color);p.setStrokeWidth(1.65f);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
            if(kind.equals("run")){BrainArt.draw(canvas,p,0,0,24,color,0);}
            else if(kind.equals("presets")){canvas.drawRoundRect(5,3,21,17,2,2,p);canvas.drawRoundRect(2,7,18,21,2,2,p);}
            else if(kind.equals("images")){canvas.drawRoundRect(3,4,21,20,3,3,p);canvas.drawCircle(8,9,1.5f,p);Path path=new Path();path.moveTo(4,17);path.lineTo(10,12);path.lineTo(14,16);path.lineTo(17,13);path.lineTo(21,17);canvas.drawPath(path,p);}
            else if(kind.equals("studio")){canvas.drawRoundRect(4,4,20,20,4,4,p);canvas.drawLine(8,12,16,12,p);canvas.drawLine(12,8,12,16,p);}
            else if(kind.equals("settings")){for(int y:new int[]{6,12,18})canvas.drawLine(3,y,21,y,p);p.setStyle(Paint.Style.FILL);canvas.drawCircle(9,6,2.4f,p);canvas.drawCircle(16,12,2.4f,p);canvas.drawCircle(7,18,2.4f,p);}
            else{Path path=new Path();path.moveTo(8,3);path.lineTo(4,3);path.quadTo(3,3,3,4);path.lineTo(3,8);path.moveTo(16,3);path.lineTo(20,3);path.quadTo(21,3,21,4);path.lineTo(21,8);path.moveTo(21,16);path.lineTo(21,20);path.quadTo(21,21,20,21);path.lineTo(16,21);path.moveTo(8,21);path.lineTo(4,21);path.quadTo(3,21,3,20);path.lineTo(3,16);canvas.drawPath(path,p);p.setStyle(Paint.Style.FILL);canvas.drawCircle(12,12,3,p);}
            canvas.restore();
        }
    }
}

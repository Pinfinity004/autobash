package com.pratik.imagetap;
import android.content.Context;import android.widget.Switch;
final class ActiveSwitch extends Switch {ActiveSwitch(Context c){super(c);}@Override protected void drawableStateChanged(){super.drawableStateChanged();Ui.activeGlow(this,isChecked());}}

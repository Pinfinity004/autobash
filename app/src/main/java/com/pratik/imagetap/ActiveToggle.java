package com.pratik.imagetap;
import android.content.Context;import android.widget.ToggleButton;
final class ActiveToggle extends ToggleButton {ActiveToggle(Context c){super(c);}@Override protected void drawableStateChanged(){super.drawableStateChanged();Ui.activeGlow(this,isChecked());}}

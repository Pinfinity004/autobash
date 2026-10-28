package com.pratik.imagetap;
/** Manual pause and editor pause are independent; closing the editor never clears manual pause. */
final class SessionPause {
 boolean manual,quick,transfer,marker;
 boolean paused(){return manual||quick||transfer||marker;}
 void toggle(){manual=!manual;}
 void beginQuick(){quick=true;}
 void endQuick(){quick=false;}
 void reset(){manual=false;quick=false;transfer=false;marker=false;}
}

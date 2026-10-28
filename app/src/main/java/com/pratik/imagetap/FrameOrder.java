package com.pratik.imagetap;
/** Full collection on every frame; a previous candidate may move to the front, never replace another target. */
final class FrameOrder {
 static int[] all(int count,int priority){int[] order=new int[count];int at=0;if(priority>=0&&priority<count)order[at++]=priority;for(int i=0;i<count;i++)if(i!=priority)order[at++]=i;return order;}
}

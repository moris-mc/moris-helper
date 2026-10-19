package org.NJ.hwamaihelper.client;
public final class PreviewTiming {
 public static boolean mayRequest(long now,long selected,long last,boolean awaiting,boolean held,long next){if(!held&&now-selected<150)return false;if(now<next)return false;if(awaiting&&now-last<2000)return false;return last==0||now-last>=500;}
 private PreviewTiming(){}
}

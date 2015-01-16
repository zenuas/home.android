package org.zenu.home;

import android.content.Context;
import android.graphics.drawable.Drawable;


public interface Entry
{
	public String getEntryName(Context context);
	public String getTitle(Context context);
	public Drawable getIcon(Context context);
	public void launch(Context context);
	public void info(Context context);
	public Entry join(Context context, Entry entry);
	
	public boolean moveParentEntry(Context context, Entry parent);
	public Entry getParentEntry();
	
//	public boolean getDragging();
//	public void setDragging(boolean isdrag);
}

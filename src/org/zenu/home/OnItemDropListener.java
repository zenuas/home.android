package org.zenu.home;

import android.widget.AdapterView;


public interface OnItemDropListener
{
	boolean onItemDrop(AdapterView<?> parent, int from, int to, float x, float y);
}

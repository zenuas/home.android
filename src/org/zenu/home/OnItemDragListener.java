package org.zenu.home;

import android.widget.AdapterView;


public interface OnItemDragListener
{
	void onItemDrag(AdapterView<?> parent, int from, int to, float x, float y, boolean isdrag_start);
}

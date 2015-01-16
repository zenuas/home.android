package org.zenu.home;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;


public class SortableArrayAdapter<T>
	extends ArrayAdapter<T>
	implements SortableAdapter
{
	public SortableArrayAdapter(Context context, int resource)
	{
		super(context, resource);
	}
	public SortableArrayAdapter(Context context, int resource, int textViewResourceId)
	{
		super(context, resource, textViewResourceId);
	}
	public SortableArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects)
	{
		super(context, resource, textViewResourceId, objects);
	}
	public SortableArrayAdapter(Context context, int resource, int textViewResourceId, T[] objects)
	{
		super(context, resource, textViewResourceId, objects);
	}
	public SortableArrayAdapter(Context context, int textViewResourceId, List<T> objects)
	{
		super(context, textViewResourceId, objects);
	}
	public SortableArrayAdapter(Context context, int resource, T[] objects)
	{
		super(context, resource, objects);
	}


	@Override
	public void switchingPosition(int from, int to)
	{
		T from_item = getItem(from);
		T to_item = getItem(to);
		remove(from_item);
		remove(to_item);
		
		if(from < to)
		{
			insert(to_item, from);
			insert(from_item, to);
		}
		else
		{
			insert(from_item, to);
			insert(to_item, from);
		}
	}

	@Override
	public void shiftPosition(int from, int to)
	{
		T temp = getItem(from);
		remove(temp);
		insert(temp, to);
	}

	@Override
	public void appendLast(int from)
	{
		T temp = getItem(from);
		remove(temp);
		add(temp);
	}
}

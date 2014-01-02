package org.zenu.home;

import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class DirectoryActivity
	extends Activity
{
	private DirectoryEntry entry_;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory);

		entry_ = (DirectoryEntry) ((ApplicationContext) getApplicationContext()).getObjectStore(getIntent().getIntExtra(DirectoryEntry.class.getName(), 0));
		setTitle(entry_.getTitle(this));
		createHome(entry_.getEntries());
	}
	
	public void createHome(List<Entry> entries)
	{
		final SortableGridView grid = (SortableGridView) findViewById(R.id.directory);

		final LayoutInflater inflater_ = getLayoutInflater();

		grid.setAdapter(new SortableArrayAdapter<Entry>(this, 0, entries)
			{
				@Override
				public View getView(int position, View convertView, ViewGroup parent)
				{
					class ViewHolder
					{
						public ImageView icon;
						public TextView text;
					}
					ViewHolder item;
					
					if(convertView == null)
					{
						convertView = inflater_.inflate(R.layout.item, parent, false);

						item = new ViewHolder();
						item.icon = (ImageView) convertView.findViewById(R.id.icon);
						item.text = (TextView) convertView.findViewById(R.id.edit);
						
						convertView.setTag(item);
					}
					else
					{
						item = (ViewHolder) convertView.getTag();
					}

					if(position < getCount())
					{
						Entry app = getItem(position);
						item.icon.setImageDrawable(app.getIcon(DirectoryActivity.this));
						item.text.setText(app.getTitle(DirectoryActivity.this));
					}
					
					return(convertView);
				}
				
				@Override
				public void switchingPosition(int from, int to)
				{
					super.switchingPosition(from, to);
					((Main) ((ApplicationContext) DirectoryActivity.this.getApplicationContext()).getMainActivity()).saveApplications();
				}

				@Override
				public void shiftPosition(int from, int to)
				{
					super.shiftPosition(from, to);
					((Main) ((ApplicationContext) DirectoryActivity.this.getApplicationContext()).getMainActivity()).saveApplications();
				}

				@Override
				public void appendLast(int from)
				{
					super.appendLast(from);
					((Main) ((ApplicationContext) DirectoryActivity.this.getApplicationContext()).getMainActivity()).saveApplications();
				}
			});
		
		grid.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					Entry app = (Entry) parent.getItemAtPosition(position);
					app.launch(DirectoryActivity.this);
					DirectoryActivity.this.finish();
				}
			});
		
		class DragDrop
			implements OnItemDropListener, OnItemDragListener
		{
			@Override
			public boolean onItemDrop(AdapterView<?> parent, int from, int to, float x, float y)
			{
				if(from == to)
				{
					Entry app = (Entry) parent.getItemAtPosition(from);
					app.info(DirectoryActivity.this);
					DirectoryActivity.this.finish();
					return(true);
				}
				else
				{
					View dector = DirectoryActivity.this.getWindow().getDecorView();
					Main main = ((Main) ((ApplicationContext) DirectoryActivity.this.getApplicationContext()).getMainActivity());
					
					//Log.v("drop", "x=" + x + ", y=" + y);
					//Log.v("dector", "width=" + dector.getWidth() + ", height=" + dector.getHeight());
					
					if(0 <= x &&
						dector.getWidth() >= x &&
						0 <= y &&
						dector.getHeight() >= y)
					{
						entry_.destroyIconCache();
						((BaseAdapter) main.getGrid().getAdapter()).notifyDataSetChanged();
					}
					else
					{
						entry_.destroyIconCache();
						Entry app = (Entry) parent.getItemAtPosition(from);
						entry_.getParentEntry().join(DirectoryActivity.this, app);
						if(entry_.getEntries().size() <= 0)
						{
							entry_.delete();
							finish();
						}
						main.saveApplications();
						((BaseAdapter) main.getGrid().getAdapter()).notifyDataSetChanged();
						return(true);
					}
				}
				return(false);
			}

			@SuppressWarnings("deprecation")
			@Override
			public void onItemDrag(AdapterView<?> parent, int from, int to, float x, float y, boolean isdrag_start)
			{
				if(isdrag_start)
				{
					final int DRAG_IMAGE_ALPHA = (int)(0xFF * 0.75);
					
					//grid.getDragItem().startAnimation(AnimationUtils.loadAnimation(grid.getContext(), R.anim.drag_start));
					grid.getDragItem().setAlpha(DRAG_IMAGE_ALPHA);
				}
			}
		}
		DragDrop dd = new DragDrop();
		grid.setOnItemDropListener(dd);
		grid.setOnItemDragListener(dd);
	}
	
	TextView title_;
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch(event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			if(title_ == null)
			{
				title_ = (TextView) findViewById(Resources.getSystem().getIdentifier("title", "id", "android"));
				if(title_ == null) {break;}
			}

			float x = event.getX();
			float y = event.getY();
			if(title_.getLeft() <= x &&
				title_.getRight() >= x &&
				title_.getTop() <= y &&
				title_.getBottom() >= y)
			{
				EditWithOkCancel.show(
					this,
					getString(R.string.change_directory_name),
					title_.getText().toString(),
					new EditWithOkCancelListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which, String text)
							{
								if(entry_.setTitle(DirectoryActivity.this, text))
								{
									setTitle(text);
									((Main) ((ApplicationContext) DirectoryActivity.this.getApplicationContext()).getMainActivity()).saveApplications();
								}
								else
								{
									Toast.makeText(DirectoryActivity.this, R.string.directory_name_already_exists, Toast.LENGTH_LONG);
								}
							}
						});
			}
			break;
		}
		
		return(super.onTouchEvent(event));
	}
}

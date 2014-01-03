package org.zenu.home;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;


public class Main
	extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		((ApplicationContext) getApplicationContext()).setMainActivity(this);
		
		createHome();
	}
	
	private SortableGridView grid_;
	public SortableGridView getGrid()
	{
		return(grid_);
	}
	
	private DirectoryEntry root_;
	public void createHome()
	{
		final SortableGridView grid = (SortableGridView) findViewById(R.id.home);
		grid_ = grid;
		
		final LayoutInflater inflater_ = getLayoutInflater();
		
		root_ = getApplications();
		grid.setAdapter(new SortableArrayAdapter<Entry>(this, 0, root_.getEntries())
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
						item.icon.setImageDrawable(app.getIcon(Main.this));
						item.text.setText(app.getTitle(Main.this));
					}
					
					return(convertView);
				}
				
				@Override
				public void switchingPosition(int from, int to)
				{
					super.switchingPosition(from, to);
					Main.this.saveApplications();
				}
				
				@Override
				public void shiftPosition(int from, int to)
				{
					super.shiftPosition(from, to);
					Main.this.saveApplications();
				}
				
				@Override
				public void appendLast(int from)
				{
					super.appendLast(from);
					Main.this.saveApplications();
				}
			});
		
		grid.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					Entry app = (Entry) parent.getItemAtPosition(position);
					app.launch(Main.this);
				}
			});
		
		class DragDrop
			implements OnItemDropListener, OnItemDragListener
		{
			private int drag_to_create_directory_ = -1;
			
			@Override
			public boolean onItemDrop(AdapterView<?> parent, int from, int to, float x, float y)
			{
				boolean isdragging = isDirectoryCreating();
				cancelCreateDirectory(parent);
				
				if(from == to)
				{
					Entry app = (Entry) parent.getItemAtPosition(from);
					app.info(Main.this);
					return(true);
				}
				else if(isdragging)
				{
					Log.v("createDirectory", "from = " + from + ", to = " + to);
					
					Entry from_app = (Entry) parent.getItemAtPosition(from);
					Entry to_app = (Entry) parent.getItemAtPosition(to);
					Entry new_entry = to_app.join(Main.this, from_app);
					
					@SuppressWarnings("unchecked")
					SortableArrayAdapter<Entry> apps = (SortableArrayAdapter<Entry>) parent.getAdapter();
					
					apps.remove(new_entry);
					apps.insert(new_entry, to - (from < to ? 1 : 0));
					Main.this.saveApplications();
					return(true);
				}
				return(false);
			}
			
			@SuppressWarnings("deprecation")
			@Override
			public void onItemDrag(AdapterView<?> parent, int from, int to, float x, float y, boolean isdrag_start)
			{
				final double ENTRY_CENTER_X = 0.0;
				final double ENTRY_CENTER_Y = 0.3;
				final double DISTANCE_FROM_CENTER = 0.25;
				
				//Log.v("onItemDrag", "x=" + x + ", y=" + y + ", r=" + Math.sqrt(Math.pow(x - ENTRY_CENTER_X, 2) + Math.pow(y - ENTRY_CENTER_Y, 2)));
				
				if(isdrag_start)
				{
					final int DRAG_IMAGE_ALPHA = (int) (0xFF * 0.75);
					
					//grid.getDragItem().startAnimation(AnimationUtils.loadAnimation(grid.getContext(), R.anim.drag_start));
					grid.getDragItem().setAlpha(DRAG_IMAGE_ALPHA);
				}
				else if(from != to && to >= 0 &&
					Math.sqrt(Math.pow(x - ENTRY_CENTER_X, 2) + Math.pow(y - ENTRY_CENTER_Y, 2)) < DISTANCE_FROM_CENTER)
				{
					if(drag_to_create_directory_ != to && isDirectoryCreating())
					{
						cancelCreateDirectory(parent);
					}
					creatingDirectory(parent, to);
				}
				else
				{
					cancelCreateDirectory(parent);
				}
			}
			
			public void creatingDirectory(AdapterView<?> parent, int position)
			{
				if(!isDirectoryCreating() || drag_to_create_directory_ != position)
				{
					drag_to_create_directory_ = position;
					
					View view = parent.getChildAt(position - grid.getFirstVisiblePosition());
					view.startAnimation(AnimationUtils.loadAnimation(Main.this, R.anim.creating_directory));
				}
			}
			
			public void cancelCreateDirectory(AdapterView<?> parent)
			{
				if(isDirectoryCreating())
				{
					View view = parent.getChildAt(drag_to_create_directory_ - grid.getFirstVisiblePosition());
					if(view != null) {view.getAnimation().cancel();}
				}
				drag_to_create_directory_ = -1;
			}
			
			public boolean isDirectoryCreating()
			{
				return(drag_to_create_directory_ >= 0);
			}
		}
		DragDrop dd = new DragDrop();
		grid.setOnItemDropListener(dd);
		grid.setOnItemDragListener(dd);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		getMenuInflater().inflate(R.menu.main, menu);
		return(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.hidden_applications:
			
			ApplicationContext appcontext = (ApplicationContext) getApplicationContext();
			Intent intent = new Intent();
			intent.setClass(this, HiddenApplications.class);
			intent.putExtra(HiddenApplications.class.getName() + ".apps", appcontext.setObjectStore(getLauncherApplications()));
			intent.putExtra(HiddenApplications.class.getName() + ".hidden", appcontext.setObjectStore(getHiddenApplications()));
			
			this.startActivity(intent);
			break;
		
		default:
			return(false);
		}
		return(true);
	}
	
	public List<ResolveInfo> getLauncherApplications()
	{
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		
		return(getPackageManager().queryIntentActivities(mainIntent, 0));
	}
	
	public DirectoryEntry getApplications()
	{
		class Order
		{
			public int by;
			public String dir;
			public String pkg;
		}
		final HashMap<String, Order> order = new HashMap<String, Order>();
		int orderby = 0;
		for(String path : getPreferences(MODE_PRIVATE).getString("applications-order", "").split("\n"))
		{
			int index = path.lastIndexOf("/");
			if(index < 0) {continue;}
			
			Order x = new Order();
			x.by = orderby++;
			x.dir = path.substring(0, index + 1);
			x.pkg = path.substring(index + 1);
			order.put(x.pkg, x);
		}
		
		List<ResolveInfo> apps = getLauncherApplications();
		Collections.sort(apps, new Comparator<ResolveInfo>()
			{
				@Override
				public int compare(ResolveInfo lhs, ResolveInfo rhs)
				{
					String left = lhs.activityInfo.applicationInfo.packageName;
					String right = rhs.activityInfo.applicationInfo.packageName;
					boolean left_contains = order.containsKey(left);
					boolean right_contains = order.containsKey(right);
					
					if(!left_contains && !right_contains) {return(0);}
					if(!left_contains && right_contains) {return(-1);}
					if(left_contains && !right_contains) {return(1);}
					
					return(order.get(left).by - order.get(right).by);
				}
				
			});
		
		DirectoryEntry root = new DirectoryEntry(this, null, "");
		HashMap<String, Boolean> hidden = getHiddenApplications();
		for(ResolveInfo app : apps)
		{
			String pkg = app.activityInfo.applicationInfo.packageName;
			if(hidden.containsKey(pkg) && hidden.get(pkg)) {continue;}
			
			if(order.containsKey(pkg))
			{
				Order x = order.get(pkg);
				DirectoryEntry current = root;
				int prev_separator = 1;
				int dir_separator;
				
				while((dir_separator = x.dir.indexOf("/", prev_separator)) >= 0)
				{
					current = current.getOrCreateDirectory(this, x.dir.substring(prev_separator, dir_separator));
					prev_separator = dir_separator + 1;
				}
				new ApplicationEntry(this, current, app);
			}
			else
			{
				new ApplicationEntry(this, root, app);
			}
		}
		
		return(root);
	}
	
	public void saveApplications()
	{
		saveApplications(root_);
	}
	
	public void saveApplications(DirectoryEntry root)
	{
		class Saver
		{
			public void searchApplication(Context context, DirectoryEntry dir, String path, StringBuffer s)
			{
				for(Entry x : dir.getEntries())
				{
					String title = path + "/" + x.getEntryName(context);
					if(x instanceof DirectoryEntry)
					{
						searchApplication(context, (DirectoryEntry) x, title, s);
					}
					else
					{
						s.append(title + "\n");
					}
				}
			}
		}
		
		StringBuffer s = new StringBuffer();
		new Saver().searchApplication(this, root, "", s);
		Editor edit = getPreferences(MODE_PRIVATE).edit();
		edit.putString("applications-order", s.toString());
		edit.commit();
	}

	public HashMap<String, Boolean> getHiddenApplications()
	{
		HashMap<String, Boolean> hidden = new HashMap<String, Boolean>();
		
		for(String pkg : getPreferences(MODE_PRIVATE).getString("applications-hidden", "").split("\n"))
		{
			if(pkg.length() == 0) {continue;}
			
			hidden.put(pkg, true);
		}
		return(hidden);
	}

	public void saveHiddenApplications(HashMap<String, Boolean> hidden)
	{
		StringBuffer s = new StringBuffer(
			);
		for(String pkg : hidden.keySet())
		{
			if(hidden.get(pkg))
			{
				s.append(pkg + "\n");
			}
		}
		Editor edit = getPreferences(MODE_PRIVATE).edit();
		edit.putString("applications-hidden", s.toString());
		edit.commit();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
		case KeyEvent.KEYCODE_BACK:
			return(true);
		}
		return(super.dispatchKeyEvent(e));
	}
}

package org.zenu.home;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


public class DirectoryEntry
	implements Entry
{
	public DirectoryEntry parent_;
	public String title_;
	
	public DirectoryEntry(Context context, DirectoryEntry parent, String title)
	{
		if(!checkInvalidName(title)) {throw new RuntimeException("invalid directory name");}
		if(parent != null && parent.containsDirectoryEntry(context, title)) {throw new RuntimeException("directory name already exists");}
		
		title_ = title;
		moveParentEntry(context, parent);
	}

	@Override
	public String getEntryName(Context context)
	{
		return(title_);
	}
	
	@Override
	public String getTitle(Context context)
	{
		return(title_);
	}
	
	public boolean setTitle(Context context, String title)
	{
		if(!checkInvalidName(title)) {return(false);}
		
		DirectoryEntry parent = (DirectoryEntry) getParentEntry();
		
		if(parent.containsDirectoryEntry(context, title)) {return(false);}
		title_ = title;
		destroyIconCache();
		return(true);
	}
	
	public static boolean checkInvalidName(String name)
	{
		if(name.contains("/")) {return(false);}
		return(true);
	}

	private Drawable icon_;
	@Override
	public Drawable getIcon(Context context)
	{
		if(icon_ == null)
		{
			Bitmap directory = BitmapFactory.decodeResource(context.getResources(), R.drawable.directory);
			int width = directory.getWidth();
			int height = directory.getHeight();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			
			Canvas canvas = new Canvas(bmp);
			canvas.drawBitmap(directory, 0, 0, null);
			int size = getEntries().size();
			if(size > 0)
			{
				final int MAX_DRAWABLE_ICON = 3;
				int draw_icon_count = Math.min(size, MAX_DRAWABLE_ICON);
				
				for(int i = 0; i < draw_icon_count; i++)
				{
					canvas.drawBitmap(
						((BitmapDrawable) getEntries().get(draw_icon_count - i - 1).getIcon(context)).getBitmap(),
						(i * height / 4.0f) / draw_icon_count,
						(i * width / 3.0f) / draw_icon_count,
						null);
				}
			}
			icon_ = new BitmapDrawable(context.getResources(), bmp);
		}
		return(icon_);
	}
	
	public void destroyIconCache()
	{
		icon_ = null;
		
		Entry parent = getParentEntry();
		if(parent instanceof DirectoryEntry) {((DirectoryEntry) parent).destroyIconCache();}
	}

	@Override
	public void launch(Context context)
	{
		Intent intent = new Intent();
		intent.setClass(context, DirectoryActivity.class);
		intent.putExtra(DirectoryEntry.class.getName(), ((ApplicationContext) context.getApplicationContext()).setObjectStore(this));
		
		context.startActivity(intent);
	}

	@Override
	public void info(Context context)
	{
	}

	private List<Entry> entries_ = new ArrayList<Entry>();
	public List<Entry> getEntries()
	{
		return(entries_);
	}
	
	public void delete()
	{
		DirectoryEntry parent = (DirectoryEntry) getParentEntry();
		parent.getEntries().remove(this);
	}

	@Override
	public Entry join(Context context, Entry entry)
	{
		appendChild(context, entry);
		return(this);
	}

	@Override
	public boolean moveParentEntry(Context context, Entry parent)
	{
		DirectoryEntry dir = (DirectoryEntry) parent;
		if(dir != null && !dir.appendChild(context, this)) {return(false);}
		parent_ = dir;
		return(true);
	}

	@Override
	public Entry getParentEntry()
	{
		return(parent_);
	}

	public DirectoryEntry createDirectory(Context context)
	{
		String title = "directory";
		if(containsDirectoryEntry(context, title))
		{
			for(int i = 2; ; i++)
			{
				String x = title + i;
				if(!containsDirectoryEntry(context, x))
				{
					title = x;
					break;
				}
			}
		}
		return(new DirectoryEntry(context, this, title));
	}
	
	public DirectoryEntry getOrCreateDirectory(Context context, String title)
	{
		DirectoryEntry dir = getDirectoryEntry(context, title);
		if(dir != null) {return(dir);}
		return(new DirectoryEntry(context, this, title));
	}
	
	public boolean appendChild(Context context, Entry entry)
	{
		if(containsDirectoryEntry(context, entry)) {return(false);}
		
		DirectoryEntry dir = (DirectoryEntry) entry.getParentEntry();
		if(dir != null)
		{
			dir.getEntries().remove(entry);
			dir.destroyIconCache();
		}
		this.getEntries().add(entry);
		this.destroyIconCache();
		return(true);
	}
	
	public boolean containsDirectoryEntry(Context context, Entry entry)
	{
		if(entry instanceof ApplicationEntry) {return(false);}
		
		return(containsDirectoryEntry(context, getTitle(context)));
	}
	
	public boolean containsDirectoryEntry(Context context, String title)
	{
		return(getDirectoryEntry(context, title) != null);
	}
	
	public DirectoryEntry getDirectoryEntry(Context context, String title)
	{
		for(Entry x : getEntries())
		{
			if(x instanceof ApplicationEntry) {continue;}
			if(title.equals(x.getTitle(context))) {return((DirectoryEntry) (x));}
		}
		return(null);
	}

//	private boolean isdragging_ = false;
//	@Override
//	public boolean getDragging()
//	{
//		return(isdragging_);
//	}
//
//	@Override
//	public void setDragging(boolean isdrag)
//	{
//		isdragging_ = isdrag;
//	}
}
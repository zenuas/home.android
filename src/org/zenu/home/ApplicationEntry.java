package org.zenu.home;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;


public class ApplicationEntry
	implements Entry
{
	public DirectoryEntry parent_;
	private ResolveInfo resolve_;
	
	public ApplicationEntry(Context context, DirectoryEntry parent, ResolveInfo resolve)
	{
		resolve_ = resolve;
		moveParentEntry(context, parent);
	}

	@Override
	public String getEntryName(Context context)
	{
		return(resolve_.activityInfo.applicationInfo.packageName);
	}
	
	@Override
	public String getTitle(Context context)
	{
		return(resolve_.loadLabel(context.getPackageManager()).toString());
	}

	@Override
	public Drawable getIcon(Context context)
	{
		return(resolve_.activityInfo.loadIcon(context.getPackageManager()));
	}

	@Override
	public void launch(Context context)
	{
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClassName(resolve_.activityInfo.packageName, resolve_.activityInfo.name);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		try
		{
			context.startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
		}
	}
	
	/*
	 * reference by:
	 *   http://stackoverflow.com/questions/4421527/start-android-application-info-screen
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void info(Context context)
	{
		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if(apiLevel >= Build.VERSION_CODES.GINGERBREAD)
		{
			// above 2.3
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", resolve_.activityInfo.packageName, null);
			intent.setData(uri);
		}
		else
		{
			// below 2.3
			final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName"; 
			final String APP_PKG_NAME_22 = "pkg";
			
			final String appPkgName = (apiLevel == Build.VERSION_CODES.FROYO ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra(appPkgName, resolve_.activityInfo.packageName);
		}
		context.startActivity(intent);
	}

	@Override
	public Entry join(Context context, Entry entry)
	{
		if(entry instanceof DirectoryEntry) {return(((DirectoryEntry) entry).join(context, this));}
		
		DirectoryEntry parent = (DirectoryEntry) getParentEntry(); 
		DirectoryEntry dir = parent.createDirectory(context);
		dir.join(context, entry);
		dir.join(context, this);
		return(dir);
	}

	@Override
	public boolean moveParentEntry(Context context, Entry parent)
	{
		DirectoryEntry dir = (DirectoryEntry) parent;
		if(!dir.appendChild(context, this)) {return(false);}
		parent_ = dir;
		return(true);
	}

	@Override
	public Entry getParentEntry()
	{
		return(parent_);
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

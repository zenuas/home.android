package org.zenu.home;

import java.util.Random;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.util.SparseArray;


public class ApplicationContext
	extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	private Activity main_activity_ = null;
	public void setMainActivity(Activity main)
	{
		main_activity_ = main;
	}
	
	public Activity getMainActivity()
	{
		return(main_activity_);
	}
	
	public void removePackageFromDB(String packageName)
	{
		Log.v("removePackageFromDB", packageName);
	}
	
	public class ObjectStore
	{
		public Object value = null;
		
		public ObjectStore(Object x)
		{
			value = x;
		}
	}
	
	private SparseArray<ObjectStore> store_ = new SparseArray<ObjectStore>();
	public int setObjectStore(Object x)
	{
		Random random = new Random();
		ObjectStore store = new ObjectStore(x);
		while(true)
		{
			int key = random.nextInt();
			
			if(store_.get(key) != null) {continue;}
			
			store_.put(key, store);
			return(key);
		}
	}
	
	public int setObjectStore(int key, Object x)
	{
		store_.get(key).value = x;
		return(key);
	}
	
	public Object getObjectStore(int key)
	{
		ObjectStore x = store_.get(key);
		store_.remove(key);
		return(x.value);
	}
}

package org.zenu.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class PackageChangedReceiver
	extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String packageName = intent.getData().getSchemeSpecificPart();

		if(packageName == null || packageName.length() == 0)
		{
			return;
		}
		Log.v("PackageChangedReceiver", "act=" + intent.getAction() + ", pkg=" + packageName);
		
		ApplicationContext app = (ApplicationContext) context.getApplicationContext();
		app.removePackageFromDB(packageName);
	}
}

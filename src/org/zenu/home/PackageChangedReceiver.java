package org.zenu.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


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
		
		Context x = context.getApplicationContext();
		ApplicationContext app = (ApplicationContext) x;
		app.removePackageFromDB(packageName);
	}
}

package org.zenu.home;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.util.SparseArray;


public class ApplicationContext
	extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
        self_ = this;
        registerBugReport();
	}

    private static ApplicationContext self_ = null;
    public static ApplicationContext getContext()
    {
        return(self_);
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
		Main main = (Main) getMainActivity();
		if(main == null) {return;}
		
		main.createHome();
	}

    public void addBugReport(Throwable ex)
    {
        SharedPreferences pref = getSharedPreferences(getClass().getName(), MODE_PRIVATE);

        String report = pref.getString("BugReport", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Editor edit = pref.edit();
        String pkg = getPackageName();

        PrintWriter p = new PrintWriter(out);
        try
        {
            if(report.length() > 0)
            {
                p.println(report);
                p.println();
            }

            p.println("PackageInfo.PackageName : " + pkg);
            try
            {
                PackageInfo info = getPackageManager().getPackageInfo(pkg, 0);
                p.println("PackageInfo.VersionName : " + info.versionName);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                //ApplicationContext.getContext().addBugReport(e);
            }
            p.println("Date : " + (new Date()).toString());
            p.println("Build.DEVICE : " + Build.DEVICE);
            p.println("Build.MODEL : " + Build.MODEL);
            p.println("Build.VERSION.SDK_INT : " + Build.VERSION.SDK_INT);
            ex.printStackTrace(p);
            p.println();
        }
        finally
        {
            p.close();
        }

        edit.putString("BugReport", out.toString());
        edit.commit();
    }

    public void registerBugReport()
    {
        final UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread thread, Throwable ex)
            {
                addBugReport(ex);
                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
    }

    public boolean existsBugReport()
    {
        final SharedPreferences pref = getSharedPreferences(getClass().getName(), MODE_PRIVATE);
        String report = pref.getString("BugReport", "");
        return(report.length() > 0);
    }

    public boolean sendBugReport()
    {
        final String BUG_REPORT_SENDTO = "mailto:zenuas@gmail.com";

        final SharedPreferences pref = getSharedPreferences(getClass().getName(), MODE_PRIVATE);
        String report = pref.getString("BugReport", "");
        if(report.length() <= 0) {return(false);}

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(BUG_REPORT_SENDTO));
        intent.putExtra(Intent.EXTRA_SUBJECT, "BugReport");
        intent.putExtra(Intent.EXTRA_TEXT, report);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        Editor edit = pref.edit();
        edit.putString("BugReport", "");
        edit.commit();
        return(true);
    }
}

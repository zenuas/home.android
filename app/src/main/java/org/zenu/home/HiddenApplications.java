package org.zenu.home;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class HiddenApplications
	extends Activity
{
	private HashMap<String, Boolean> hidden_;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hidden_applications);
		
		setTitle(R.string.hidden_applications);

		//List<ResolveInfo>        resolves_ = ((ObjectParcelable<List<ResolveInfo>>)        (getIntent().getExtras().get(HiddenApplications.class.getName() + ".apps"))).Data;
		//HashMap<String, Boolean> hidden    = ((ObjectParcelable<HashMap<String, Boolean>>) (getIntent().getExtras().get(HiddenApplications.class.getName() + ".hidden"))).Data;
		Main parent = (Main) ApplicationContext.getContext().getMainActivity();
		List<ResolveInfo>        resolves_ = parent.getLauncherApplications();
		HashMap<String, Boolean> hidden    = parent.getHiddenApplications();
		hidden_ = hidden;
		
		ListView list = (ListView) findViewById(R.id.hidden_applications);
		
		final LayoutInflater inflater_ = getLayoutInflater();
		
		list.setAdapter(new ArrayAdapter<ResolveInfo>(this, 0, resolves_)
			{
				@Override
				public View getView(int position, View convertView, ViewGroup parent)
				{
					class ViewHolder
					{
						public ImageView icon;
						public TextView text;
						public TextView package_name;
						public CheckBox hidden;
					}
					ViewHolder item;
					
					if(convertView == null)
					{
						convertView = inflater_.inflate(R.layout.hidden_item, parent, false);
						
						item = new ViewHolder();
						item.icon = (ImageView) convertView.findViewById(R.id.icon);
						item.text = (TextView) convertView.findViewById(R.id.edit);
						item.package_name = (TextView) convertView.findViewById(R.id.package_name);
						item.hidden = (CheckBox) convertView.findViewById(R.id.hidden);
						
						convertView.setTag(item);
					}
					else
					{
						item = (ViewHolder) convertView.getTag();
					}
					
					if(position < getCount())
					{
						ResolveInfo resolve = (ResolveInfo) getItem(position);
						PackageManager manager = HiddenApplications.this.getPackageManager();
						String pkg = resolve.activityInfo.packageName;
						
						item.icon.setImageDrawable(resolve.activityInfo.loadIcon(manager));
						item.text.setText(resolve.loadLabel(manager));
						item.package_name.setText(pkg);
						item.hidden.setChecked(hidden_.containsKey(pkg) && hidden_.get(pkg));
					}
					
					return(convertView);
				}
			});
		
		list.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					if(position < parent.getCount())
					{
						ResolveInfo resolve = (ResolveInfo) parent.getItemAtPosition(position);
						String pkg = resolve.activityInfo.packageName;
						CheckBox hidden = (CheckBox) view.findViewById(R.id.hidden);
						boolean checked = !hidden.isChecked();
						
						hidden.setChecked(checked);
						hidden_.put(pkg, checked);
					}
				}
			});
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		ApplicationContext appcontext = (ApplicationContext) getApplicationContext();
		Main main = (Main) appcontext.getMainActivity();
		
		main.saveHiddenApplications(hidden_);
		main.createHome();
	}
}

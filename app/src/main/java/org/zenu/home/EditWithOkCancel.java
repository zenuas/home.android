package org.zenu.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;


public class EditWithOkCancel
{
	static void show(Activity activity, String title, EditWithOkCancelListener listener)
	{
		show(activity, title, "", listener);
	}
	
	static void show(Activity activity, String title, String text, final EditWithOkCancelListener listener)
	{
		final EditText edit = new EditText(activity);
		edit.setText(text);
		edit.setSelectAllOnFocus(true);
		
		new AlertDialog.Builder(activity)
			.setTitle(title)
			.setView(edit)
			.setCancelable(false)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if(listener != null)
						{
							listener.onClick(dialog, which, edit.getText().toString());
						}
					}
				})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
					}
				})
			.show();
	}
}

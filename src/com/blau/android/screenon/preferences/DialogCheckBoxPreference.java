/*
    This file is part of dgAlert Classic.

    dgAlert Classic is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    dgAlert Classic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with dgAlert Classic.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.blau.android.screenon.preferences;

import com.blau.android.screenon.ManageNotification;
import com.blau.android.screenon.R;
import com.blau.android.screenon.ReminderReceiver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class DialogCheckBoxPreference extends CheckBoxPreference {
	Context context;
	
	public DialogCheckBoxPreference(Context c) {
	   super(c);
	   context = c;
   }
	
	public DialogCheckBoxPreference(Context c, AttributeSet attrs) {
	   super(c, attrs);
	   context = c;
   }
	
	public DialogCheckBoxPreference(Context c, AttributeSet attrs,
         int defStyle) {
	   super(c, attrs, defStyle);
	   context = c;
   }

	@Override
   protected void onClick() {
	   super.onClick();   
	   if (isChecked()) {
	   	new AlertDialog.Builder(context)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(context.getString(R.string.pref_notif_title))
			.setMessage(context.getString(R.string.pref_notif_enabled_warning))
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					//Nothing to do here
				}
			}).show();
	   } else {
		   ManageNotification.clearAll(context);
		   ReminderReceiver.cancelReminder(context);
	   }
   }
}
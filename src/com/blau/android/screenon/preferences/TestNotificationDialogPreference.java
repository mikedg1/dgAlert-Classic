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
import com.blau.android.screenon.SmsMmsMessage;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

public class TestNotificationDialogPreference extends DialogPreference {
	Context c;
	String version;
	
	public TestNotificationDialogPreference(Context context, AttributeSet attrs) {
	   super(context, attrs);
	   c = context;
   }
	
	public TestNotificationDialogPreference(Context context, AttributeSet attrs,
         int defStyle) {
	   super(context, attrs, defStyle);
	   c = context;
   }
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		ManageNotification.clear(c, ManageNotification.NOTIFICATION_TEST);
   }

	@Override
	protected View onCreateDialogView() {

		// Create a fake SmsMmsMessage
		String testPhone = "123-456-7890";
		SmsMmsMessage message = new SmsMmsMessage(c, testPhone, c
		      .getString(R.string.pref_notif_test_title), 0, null, testPhone, null, 1, 0,
		      SmsMmsMessage.MESSAGE_TYPE_SMS);
		
		// Show notification
		ManageNotification.show(c, message, ManageNotification.NOTIFICATION_TEST);
		
		return super.onCreateDialogView();
	}
}
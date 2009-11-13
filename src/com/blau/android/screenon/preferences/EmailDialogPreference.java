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

import com.blau.android.screenon.R;
import com.blau.android.screenon.DgAlertClassicUtils;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class EmailDialogPreference extends DialogPreference {
	Context c;
	String version;
	
	public EmailDialogPreference(Context context, AttributeSet attrs) {
	   super(context, attrs);
	   c = context;
   }
	
	public EmailDialogPreference(Context context, AttributeSet attrs,
         int defStyle) {
	   super(context, attrs, defStyle);
	   c = context;
   }
	
	public void setVersion(String v) {
		version = v;
	}

	@Override
   public void onClick(DialogInterface dialog, int which) {
	   super.onClick(dialog, which);
	   
	   if (which == DialogInterface.BUTTON1) {
	   	DgAlertClassicUtils.launchEmailToIntent(c, c.getString(R.string.app_name) + version, true);
	   } else if (which == DialogInterface.BUTTON2) {
	   	DgAlertClassicUtils.launchEmailToIntent(c, c.getString(R.string.app_name) + version, false);
	   }
   }
}
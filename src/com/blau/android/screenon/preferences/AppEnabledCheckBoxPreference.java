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

import com.blau.android.screenon.Log;
import com.blau.android.screenon.SMSReceiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;


public class AppEnabledCheckBoxPreference extends CheckBoxPreference {
	private Context context;
	
	public AppEnabledCheckBoxPreference(Context c, AttributeSet attrs,
			int defStyle) {
		super(c, attrs, defStyle);
		context = c;
	}

	public AppEnabledCheckBoxPreference(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;
	}

	public AppEnabledCheckBoxPreference(Context c) {
		super(c);
		context = c;
	}

	@Override
	protected void onClick() {
		super.onClick();
		
		PackageManager pm = (PackageManager) context.getPackageManager();
		ComponentName cn = new ComponentName(context, SMSReceiver.class);

		if (isChecked()) {
			Log.v("SMSPopup receiver is enabled");
			pm.setComponentEnabledSetting(cn, 
					PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 
					PackageManager.DONT_KILL_APP);
		} else {
			Log.v("SMSPopup receiver is disabled");
			pm.setComponentEnabledSetting(cn, 
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
					PackageManager.DONT_KILL_APP);
		}
	}
}
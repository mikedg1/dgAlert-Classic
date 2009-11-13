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

public class WorkingDialogPreference extends DialogPreference {
	
	public WorkingDialogPreference(Context context, AttributeSet attrs) {
	   super(context, attrs);
	 
   }
	
	public WorkingDialogPreference(Context context, AttributeSet attrs,
         int defStyle) {
	   super(context, attrs, defStyle);
   }
	
	
}
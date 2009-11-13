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
package com.blau.android.screenon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blau.android.screenon.mikedg.AlertService;

public class SMSReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("SMSReceiver: onReceive()");
		ManageWakeLock.acquirePartial(context);
		
		intent.setClass(context, SMSReceiverService.class);
		intent.putExtra("result", getResultCode());
		SMSReceiverService.beginStartingService(context, intent);
		AlertService.StartMeUp(context);
	}
}
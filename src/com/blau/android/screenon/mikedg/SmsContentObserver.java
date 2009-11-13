package com.blau.android.screenon.mikedg;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

import com.blau.android.screenon.DgAlertClassicConfigActivity;
import com.blau.android.screenon.DgAlertClassicUtils;
import com.blau.android.screenon.Log;

public class SmsContentObserver extends ContentObserver
{
	Context context;
	public static final String TAG = DgAlertClassicConfigActivity.TAG;
	
	public SmsContentObserver(Handler h, Context aContext)
	{
		super(h);
		context = aContext;
	}
	
	@Override
	public void onChange(boolean selfChange)
	{
		super.onChange(selfChange);
		//Log.v(this.getClass().toString() + ".onChange");
		
		//TODO: get latest unreads!
		//TODO: temp to see when this gets fupdated
		//Log.v("SmsContentObserver.onChange unread count:" + DgAlertClassicUtils.getUnreadMessagesCount(context));
		
		AlertService.handleNotificationDetails(context, false, false);
	}

}

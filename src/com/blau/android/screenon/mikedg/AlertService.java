package com.blau.android.screenon.mikedg;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.widget.Toast;

import com.blau.android.screenon.DgAlertClassicUtils;
import com.blau.android.screenon.Log;

public class AlertService extends Service
{
	public static ContentObserver				smsObserver		= null;

	public static void StartMeUp(Context context)
	{
		// Make sure the service is started. It will continue running
		// until someone calls stopService(). The Intent we use to find
		// the service explicitly specifies our service component, because
		// we want it running in our own process and don't want other
		// applications to replace it.
		Log.v("AlertService.StartMeUp");
		Intent intent = new Intent(context, AlertService.class);
		
		context.startService(intent);
	}

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder
	{
		AlertService getService()
		{
			return AlertService.this;
		}
	}

	@Override
	public void onCreate()
	{
		Log.v("AlertService.onCreate");
		//TODO: this is where we should check for null and start, since this gets auto created at some point
		if (smsObserver == null)
		{
			Log.v("AlertService.StartMeUp smsObserver == null");
			ContentResolver cr = this.getContentResolver();
			smsObserver = new SmsContentObserver(new Handler(), this);
			cr.registerContentObserver(DgAlertClassicUtils.CONVERSATION_CONTENT_URI, true, smsObserver);
		}
	}

	@Override
	public void onDestroy()
	{
		// Cancel the persistent notification.
		// mNM.cancel(R.string.local_service_started);

		// Tell the user we stopped.
		Toast.makeText(this, "Stopped dgAway Service", Toast.LENGTH_SHORT).show();
		Log.v("AlertService.onDestroy");
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder	mBinder	= new LocalBinder();

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		Log.v("AlertService.start()");
	

	}

	public static void handleNotificationDetails(Context context, boolean sound, boolean newSmsTrigger)
	{
		Log.v("AlertService.handleNotificationDetails newSmsTrigger: " + newSmsTrigger);

		
		// On detect change check for unreads and cancel if none launch cancel
		Log.v("AlertService.handleNotifcationDetails");

		// do this twice, selecting once for mms and once for sms, get the
		// earliest and see which one is first!
		long lastTimeUnread = 0;
		long lastTimeRead = 0;

		//type 1 makes sure we dont count items i sent
		Cursor c = context.getContentResolver().query(DgAlertClassicUtils.SMS_CONTENT_URI, new String[]
		{ "date", "read" }, "type = 1", null, "date desc");
		//I think type = 1 means sent by someone else
		if (c.moveToNext())
		{
			// if first is unread then record it
			if (c.getInt(1) == 0) // unread
			{
				lastTimeUnread = c.getLong(0);
				Log.v("AlertService.using sms for unread: " + lastTimeUnread );

			}
			else
			// read
			{

				lastTimeRead = c.getLong(0);
				Log.v("AlertService.using sms for read: " + lastTimeRead);
			}
		}
		c.close();

		//TODO: no type in MMS so now what?
		//TODO: handle figuring out if i sent or someone else sent
		c = context.getContentResolver().query(DgAlertClassicUtils.MMS_CONTENT_URI, new String[]
		{ "date", "read" }, null, null, "date desc");
		if (c.moveToNext())
		{
			// if first is unread then record it
			if (c.getInt(1) == 0) // unread
			{
				long mmstime = c.getLong(0) * 1000; // TODO: double check this
				// factor

				if (mmstime > lastTimeUnread) // mms message was closer than
				// sms
				{
					lastTimeUnread = mmstime;
					Log.v("AlertService.using mms for unread: "+lastTimeUnread);
				}
			}
			else
			// read
			{
				int mmstime = c.getInt(0) * 1000; // TODO: double check this
				// factor

				if (mmstime > lastTimeRead) // mms message was closer than sms
				{
					lastTimeRead = mmstime;
					Log.v("AlertService.using mms for read: " +lastTimeRead);
				}
			}
		}

		c.close();

		//If most recent is MMS and time is right then set sound == true!
		Log.v(lastTimeUnread + "      " + lastTimeRead);
		if (lastTimeUnread < lastTimeRead)
		{
			// cancel notif then
			NotificationManager myNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			myNM.cancelAll();
			Log.v("AlertService.cancelling notifications");
		}
	}
}

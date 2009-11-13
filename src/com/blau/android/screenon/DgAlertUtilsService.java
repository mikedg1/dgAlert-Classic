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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;

public class DgAlertUtilsService extends Service {
	public static final String ACTION_MARK_THREAD_READ =
		"com.blau.android.screenon.ACTION_MARK_THREAD_READ";
	
	public static final String ACTION_MARK_MESSAGE_READ =
		"com.blau.android.screenon.ACTION_MARK_MESSAGE_READ";
	
	public static final String ACTION_DELETE_MESSAGE =
		"com.blau.android.screenon.ACTION_DELETE_MESSAGE";
	
	public static final String ACTION_UPDATE_NOTIFICATION =
		"com.blau.android.screenon.ACTION_UPDATE_NOTIFICATION";
		
	private Context context;
   private ServiceHandler mServiceHandler;
	private Looper mServiceLooper;
	
	static final Object mStartingServiceSync = new Object();
	static PowerManager.WakeLock mStartingService;
	
	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread(Log.TAG, Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		context = getApplicationContext();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);	
	}
	
   @Override
	public void onStart(Intent intent, int startId) {
		//mResultCode = intent.getIntExtra("result", 0);
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = intent;
		mServiceHandler.sendMessage(msg);
	}

	@Override
	public void onDestroy() {
		mServiceLooper.quit();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
   private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.v("DgAlertUtilsService: handleMessage()");
			int serviceId = msg.arg1;
			Intent intent = (Intent) msg.obj;

			String action = intent.getAction();
			
			if (ACTION_MARK_THREAD_READ.equals(action)) {
				Log.v("DgAlertUtilsService: Marking thread read");
				SmsMmsMessage message = new SmsMmsMessage(context, intent.getExtras());
				message.setThreadRead();
			} else if (ACTION_MARK_MESSAGE_READ.equals(action)) {
				Log.v("DgAlertUtilsService: Marking message read");
				SmsMmsMessage message = new SmsMmsMessage(context, intent.getExtras());
				message.setMessageRead();
			} else if (ACTION_DELETE_MESSAGE.equals(action)) {
				Log.v("DgAlertUtilsService: Deleting message");
				SmsMmsMessage message = new SmsMmsMessage(context, intent.getExtras());
				message.delete();
			} else if (ACTION_UPDATE_NOTIFICATION.equals(action)) {
				Log.v("DgAlertUtilsService: Updating notification");

				// In the case the user is "replying" to the message (ie. starting an
				// external intent) we need to ignore all messages in the thread when
				// calculating the unread messages to show in the status notification
				boolean ignoreThread = 
					intent.getBooleanExtra(SmsMmsMessage.EXTRAS_REPLYING, false);				

				SmsMmsMessage message;
				if (ignoreThread) {
					// If ignoring messages from the tread, pass the full message over
					message = new SmsMmsMessage(context, intent.getExtras());
				} else {
					// Otherwise we can just calculate unread messages by checking the
					// database as normal
					message = null;
				}
				
				// Get the most recent message + total message counts
				SmsMmsMessage recentMessage = 
					DgAlertClassicUtils.getRecentMessage(context, message);
				
				// Update the notification in the status bar
				ManageNotification.update(context, recentMessage);
			}
			
			// NOTE: We MUST not call stopSelf() directly, since we need to
			// make sure the wake lock acquired by AlertReceiver is released.
			finishStartingService(DgAlertUtilsService.this, serviceId);
		}
	}

   /**
	 * Start the service to process the current event notifications, acquiring
	 * the wake lock before returning to ensure that the service will run.
	 */
	public static void beginStartingService(Context context, Intent intent) {
		synchronized (mStartingServiceSync) {
			Log.v("DgAlertUtilsService: beginStartingService()");
			if (mStartingService == null) {
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				      "StartingPopupUtilsService");
				mStartingService.setReferenceCounted(false);
			}
			mStartingService.acquire();
			context.startService(intent);
		}
	}
	
	/**
	 * Called back by the service when it has finished processing notifications,
	 * releasing the wake lock if the service is now stopping.
	 */
	public static void finishStartingService(Service service, int startId) {
		synchronized (mStartingServiceSync) {
			Log.v("DgAlertUtilsService: finishStartingService()");
			if (mStartingService != null) {
				if (service.stopSelfResult(startId)) {
					mStartingService.release();
				}
			}
		}
	}	
}
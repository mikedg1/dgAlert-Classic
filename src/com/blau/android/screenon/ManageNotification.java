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

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;

/*
 * This class handles the Notifications (sounds/vibrate/LED)
 */
public class ManageNotification {
	public static final int NOTIFICATION_ALERT = 1337;
	public static final int NOTIFICATION_TEST = 888;
	private static NotificationManager myNM = null;
	private static SharedPreferences myPrefs = null;
	private static final String PRIVACY_OFF = "off";
	private static final String PRIVACY_NAME_ONLY = "name_only";
	private static final String PRIVACY_NO_INFO = "no_info";
	
	/*
	 * Create the NotificationManager
	 */
	private static synchronized void createNM(Context context) {
		if (myNM == null) {
			myNM = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	}

	/*
	 * Create the PreferenceManager
	 */
	private static synchronized void createPM(Context context) {
		if (myPrefs == null) {
			myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		}
	}
	
	/*
	 * Show/play the notification given a SmsMmsMessage and a notification ID
	 * (really just NOTIFICATION_ALERT for the main alert and NOTIFICATION_TEST
	 * for the test notification from the preferences screen)
	 */
	public static void show(Context context, SmsMmsMessage message, int notif) {
		notify(context, message, false, notif);
	}

	/*
	 * Default to NOTIFICATION_ALERT if notif is left out
	 */
	public static void show(Context context, SmsMmsMessage message) {
		notify(context, message, false, NOTIFICATION_ALERT);
	}

	/*
	 * Only update the notification given the SmsMmsMessage (ie. do not play
	 * re-play the vibrate/sound, just update the text).
	 */
	public static void update(Context context, SmsMmsMessage message) {
		if (message != null) {
			if (message.getUnreadCount() > 0) {
				notify(context, message, true, NOTIFICATION_ALERT);
				return;
			}
		}
		// TODO: Should reply flag be set to true?
		ManageNotification.clearAll(context, true);
	}

	/*
	 * The main notify method, this thing is WAY too long. Needs to be broken up.
	 */
	private static synchronized void notify(Context context, SmsMmsMessage message,
	      boolean onlyUpdate, int notif) {
		Log.v("ManageNotification.notify ***IN***");

		// Make sure the PreferenceManager is created
		createPM(context);
		
		// Fetch info from the message object
		int unreadCount = message.getUnreadCount();
		String messageBody = message.getMessageBody();
		String contactName = message.getContactName();
		long timestamp = message.getTimestamp();

		Log.v("ManageNotification.notify unread=" + unreadCount + " body:"+messageBody +"timestamp:"+timestamp);

		// Check if there are unread messages and if notifications are enabled -
		// if not, we're done :)
		//TODO: temp mike dg if we have a message this is fine, how else does this fire?
		if (/*unreadCount > 0 &&*/
				myPrefs.getBoolean(context.getString(R.string.pref_notif_enabled_key),
					Boolean.parseBoolean(
						context.getString(R.string.pref_notif_enabled_default)))) {
		
			Log.v("ManageNotification.notify Notify enabled and unreads");

			// Make sure the NotificationManager is created
			createNM(context);

			// Get some preferences: vibrate and vibrate_pattern prefs
			boolean vibrate = myPrefs.getBoolean(context.getString(R.string.pref_vibrate_key), Boolean
			      .valueOf(context.getString(R.string.pref_vibrate_default)));
			String vibrate_pattern_raw = myPrefs.getString(context
			      .getString(R.string.pref_vibrate_pattern_key), context
			      .getString(R.string.pref_vibrate_pattern_default));
			String vibrate_pattern_custom_raw = myPrefs.getString(context
			      .getString(R.string.pref_vibrate_pattern_custom_key), context
			      .getString(R.string.pref_vibrate_pattern_default));

			// Get LED preferences
			boolean flashLed = myPrefs.getBoolean(
					context.getString(R.string.pref_flashled_key),
			      Boolean.valueOf(context.getString(R.string.pref_flashled_default)));
			String flashLedCol = myPrefs.getString(
			      context.getString(R.string.pref_flashled_color_key), 
			      context.getString(R.string.pref_flashled_color_default));
			String flashLedColCustom = myPrefs.getString(
			      context.getString(R.string.pref_flashled_color_custom_key), 
			      context.getString(R.string.pref_flashled_color_default));
			String flashLedPattern = myPrefs.getString(
			      context.getString(R.string.pref_flashled_pattern_key), 
			      context.getString(R.string.pref_flashled_pattern_default));
			String flashLedPatternCustom = myPrefs.getString(
			      context.getString(R.string.pref_flashled_pattern_custom_key), 
			      context.getString(R.string.pref_flashled_pattern_default));

			boolean ringInCall = myPrefs.getBoolean(
					context.getString(R.string.pref_ring_in_call_key), 
				    Boolean.valueOf(context.getString(R.string.pref_ring_in_call_default)));
			boolean vibrateInCall = myPrefs.getBoolean(
					context.getString(R.string.pref_vibrate_in_call_key), 
				    Boolean.valueOf(context.getString(R.string.pref_vibrate_in_call_default)));
			
			// The default system ringtone
			// ("content://settings/system/notification_sound")
			String defaultRingtone = Settings.System.DEFAULT_NOTIFICATION_URI.toString();

			// Try and parse the user ringtone, use the default if it fails
			Uri alarmSoundURI = Uri.parse(myPrefs.getString(
					context.getString(R.string.pref_notif_sound_key), defaultRingtone));

			// The notification title, sub-text and text that will scroll
			String contentTitle;
			String contentText;
			String scrollText;
			
			// The default intent when the notification is clicked (Inbox)
			Intent smsIntent = DgAlertClassicUtils.getSmsIntent();

			// See if user wants some privacy
			//Using booleans here
			//false is off
			//true is just name showing
			//all is nothing important showing
			String privacyMode = myPrefs.getString(context.getString(R.string.pref_privacy_key),
			      context.getString(R.string.pref_privacy_default));

			// If we're updating the notification, do not set the ticker text
			if (onlyUpdate) {
				scrollText = null;
			} else {
				// If we're in privacy mode and the keyguard is on then just display
				// the name of the person, otherwise scroll the name and message
				if (privacyMode.equals(PRIVACY_NAME_ONLY)/* && ManageKeyguard.inKeyguardRestrictedInputMode()*/) {
					//TODO: handle old style and new style privacy mode
					scrollText = 
						String.format(
							context.getString(R.string.notification_scroll_privacy),
					      contactName);
				} else if (privacyMode.equals(PRIVACY_NO_INFO)/* && ManageKeyguard.inKeyguardRestrictedInputMode()*/) {
					//TODO: handle old style and new style privacy mode
					scrollText = 
						String.format(
							context.getString(R.string.new_private_text),
					      contactName);
				} else {
					scrollText = String.format(context.getString(R.string.notification_scroll),
							contactName,
							messageBody);
				}
			}
			// If more than one message waiting ...
			if (unreadCount > 1) {
				contentTitle = context.getString(R.string.notification_multiple_title);
				contentText = context.getString(R.string.notification_multiple_text);
				boolean showLatest = myPrefs.getBoolean(context.getString(R.string.pref_show_latest_key),
					      Boolean.valueOf(context.getString(R.string.pref_show_latest_default)));
				if (showLatest)
				{
					//TODO: work something elegant out for the repy intent
					//Possibly do the normal reply intent if it's only one person, otherwise show the max
					contentTitle = contactName + " (" + unreadCount +")";
					contentText = messageBody;
				}
				else
				{
					contentTitle = context.getString(R.string.notification_multiple_title);
					contentText = context.getString(R.string.notification_multiple_text);
				}
				// smsIntent = DgAlertClassicUtils.getSmsIntent();
			} else { // Else 1 message, set text and intent accordingly
				contentTitle = contactName;
				contentText = messageBody;
				smsIntent = message.getReplyIntent();
			}

			/*
			 * Ok, let's create our Notification object and set up all its
			 * parameters.
			 */

			// Set the icon, scrolling text and timestamp
			Notification notification =
					new Notification(R.drawable.stat_notify_sms, scrollText, timestamp);
			
			// Set auto-cancel flag
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			
			// Set audio stream to ring
			notification.audioStreamType = AudioManager.STREAM_RING; //TODO: I think this can be improved!! -Mike dg 6/18/09

			// If this is a new notification (not updating a notification)
			// then set LED, vibrate and ringtone to fire
			if (!onlyUpdate) {
			
				// Set up LED pattern and color
				if (flashLed) {
					notification.flags |= Notification.FLAG_SHOW_LIGHTS;

					/*					 
					 * Set up LED blinking pattern
					 */
					int[] led_pattern = null;
					
					if (context.getString(R.string.pref_custom_val).equals(flashLedPattern)) {
						led_pattern = parseLEDPattern(flashLedPatternCustom);
					} else {
						led_pattern = parseLEDPattern(flashLedPattern);
					}
					
					if (led_pattern == null) {
						led_pattern = parseLEDPattern(myPrefs.getString(
						      context.getString(R.string.pref_flashled_pattern_key), 
						      context.getString(R.string.pref_flashled_pattern_default)));
					}
					
					notification.ledOnMS = led_pattern[0];
					notification.ledOffMS = led_pattern[1];
					
					/*					 
					 * Set up LED color
					 */

					// Check if a custom color is set
					if (context.getString(R.string.pref_custom_val).equals(flashLedCol)) {
						flashLedCol = flashLedColCustom;
					}
					
					// Default in case the parse fails
					int col = Color.parseColor(context.getString(R.string.pref_flashled_color_default));
					
					// Try and parse the color
					try {
						col = Color.parseColor(flashLedCol);
					} catch (IllegalArgumentException e) {
						// No need to do anything here
					}
					notification.ledARGB = col;
				}

				/*					 
				 * Set up vibrate pattern
				 */
				AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

				if (vibrate) {
					long[] vibrate_pattern = null;
					if (context.getString(R.string.pref_custom_val).equals(
					      vibrate_pattern_raw)) {
						vibrate_pattern = parseVibratePattern(vibrate_pattern_custom_raw);
					} else {
						vibrate_pattern = parseVibratePattern(vibrate_pattern_raw);
					}
					if (vibrate_pattern != null) {
						if (vibrateInCall)
						{
							//TODO: think I need vibration manager, should already have permission here
							if (audioMgr.getRingerMode() != AudioManager.RINGER_MODE_SILENT)
							{
								//Log.d(TAG, "In call vibe");
								
								Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

								vibrator.vibrate(vibrate_pattern, -1); //-1 = don't repeat
					
							}
							
						}
						else
						{
							notification.vibrate = vibrate_pattern;
						}
					} else {
						if (vibrateInCall)
						{
							//TODO: think I need vibration manager, should already have permission here
							if (audioMgr.getRingerMode() != AudioManager.RINGER_MODE_SILENT)
							{
								//Log.d(TAG, "In call vibe");
								
								Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

								vibrator.vibrate(vibrate_pattern, -1);
					
							}
							
						}
						else
						{
							notification.defaults = Notification.DEFAULT_VIBRATE;
						}
					}
				}
	
				// Notification sound
				Log.v(alarmSoundURI.toString());
				if (ringInCall)
				{
					//TODO:
					if (audioMgr.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
					{
						//TODO: verify that this works on calls...
						MediaPlayer mp = MediaPlayer.create(context, alarmSoundURI);
						//TODO: Mikedg 9.22.09 fails!!!
						//TODO: why?
						//TODO: maybe context or uri?
						//mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
						//mp.setAudioStreamType(AudioManager.stre); //is this one right?
					    mp.start();
					    //TODO: hrmm do i need to manually vibrate?
					}

				}
				else
				{
					notification.sound = alarmSoundURI;
				}
			}
			
			// Set the PendingIntent if the status message is clicked
			PendingIntent notifIntent = PendingIntent.getActivity(context, 0, smsIntent, 0);

			// Set the messages that show when the status bar is pulled down
			notification.setLatestEventInfo(context, contentTitle, 
					String.format(contentText, unreadCount), notifIntent);

			// Set number of events that this notification signifies (unread
			// messages)
			if (unreadCount > 1) {
				notification.number = unreadCount;
			}

			// Set intent to execute if the "clear all" notifications button is
			// pressed -
			// sically stop any future reminders.
			Intent deleteIntent = new Intent(new Intent(context, ReminderReceiver.class));
			deleteIntent.setAction(Intent.ACTION_DELETE);
			PendingIntent pendingDeleteIntent = PendingIntent
			      .getBroadcast(context, 0, deleteIntent, 0);

			notification.deleteIntent = pendingDeleteIntent;

			// Seems this is needed for the .number value to take effect
			// on the Notification
			//TODO: find notif num
			myNM.cancelAll();
			
			// Finally: run the notification!
			Log.v("ManageNotification.*** Notify running ***");
			myNM.notify(notif, notification); //TODO: is this the only location with notif
		} 
	}
	
	public static void clear(Context context) {
		clear(context, NOTIFICATION_ALERT);
	}

	public static synchronized void clear(Context context, int notif) {
		createNM(context);
		if (myNM != null) {
			Log.v("ManageNotification.Notification cleared");
			myNM.cancel(notif);
		}		
	}

	public static synchronized void clearAll(Context context, boolean reply) {
		createPM(context);

		if (reply || myPrefs.getBoolean(
		      context.getString(R.string.pref_markread_key),
		      Boolean.parseBoolean(
		      		context.getString(R.string.pref_markread_default)))) {
			createNM(context);
			if (myNM != null) {
				myNM.cancelAll();
				Log.v("ManageNotification.All notifications cleared");
			}
		}	
	}
	
	public static void clearAll(Context context) {
		clearAll(context, false);
	}

	/*
	 * Parse the user provided custom vibrate pattern into a long[]
	 */
	//TODO: tidy this up
	public static long[] parseVibratePattern(String stringPattern) {
		ArrayList<Long> arrayListPattern = new ArrayList<Long>();
		Long l;
		String[] splitPattern = stringPattern.split(",");
		int VIBRATE_PATTERN_MAX_SECONDS = 60000;
		int VIBRATE_PATTERN_MAX_PATTERN = 30;

		for (int i = 0; i < splitPattern.length; i++) {
			try {
				l = Long.parseLong(splitPattern[i].trim());
			} catch (NumberFormatException e) {
				return null;
			}
			if (l > VIBRATE_PATTERN_MAX_SECONDS) {
				return null;
			}
			arrayListPattern.add(l);
		}
		
		// TODO: can i just cast the whole ArrayList into long[]?
		int size = arrayListPattern.size();
		if (size > 0 && size < VIBRATE_PATTERN_MAX_PATTERN) {
			long[] pattern = new long[size];
			for (int i = 0; i < pattern.length; i++) {
				pattern[i] = arrayListPattern.get(i);
			}
			return pattern;
		}
		
		return null;
	}
	
	public static int[] parseLEDPattern(String stringPattern) {
		int[] arrayPattern = new int[2];
		int on, off;
		String[] splitPattern = stringPattern.split(",");
		
		if (splitPattern.length != 2) {
			return null;
		}
		
		int LED_PATTERN_MIN_SECONDS = 0;
		int LED_PATTERN_MAX_SECONDS = 60000;

		try {
			on = Integer.parseInt(splitPattern[0]);
		} catch (NumberFormatException e) {
			return null;
		}
		
		try {
			off = Integer.parseInt(splitPattern[1]);
		} catch (NumberFormatException e) {
			return null;	
		}
		
		if (on >= LED_PATTERN_MIN_SECONDS && on <= LED_PATTERN_MAX_SECONDS &&
			 off >= LED_PATTERN_MIN_SECONDS && off <= LED_PATTERN_MAX_SECONDS) {
			arrayPattern[0] = on;
			arrayPattern[1] = off;
			return arrayPattern;
		}
		 
		return null;
	}	

}
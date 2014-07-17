package com.appenjoyment.ticompanion;

import org.joda.time.DateTime;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class RateUsManager
{
	public static final String URI_RATE_US = "ticountdown://rateus";

	public static void sendNotification(Context context)
	{
		// only show the notification once
		SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (!prefs.getBoolean(PREFERENCE_KEY_NOTIFIED_FOR_RATE_PROMPT, false))
		{
			String message = createRateUsMessage(context);
			Notification notification =
					new NotificationCompat.Builder(context)
							.setContentTitle(context.getString(R.string.notification_title))
							.setTicker(message)
							.setContentText(message)
							.setStyle(new NotificationCompat.BigTextStyle().bigText(message))
							.setOnlyAlertOnce(true)
							.setSmallIcon(R.drawable.ic_launcher)
							.setContentIntent(
									PendingIntent.getActivity(
											context,
											0,
											IntentUtility.getBringToFrontIntent(context, MainActivity.class)
													.setData(Uri.parse(URI_RATE_US)),
											PendingIntent.FLAG_UPDATE_CURRENT))
							.build();

			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notification);

			prefs.edit().putBoolean(PREFERENCE_KEY_NOTIFIED_FOR_RATE_PROMPT, true).commit();
		}
	}

	public static void scheduleAlarm(Context context)
	{
		// schedule a notification 1hr before TI4 (or now, if within 4 days of the event) asking them to rate the app
		Intent rateApplicationPrompt = new Intent(ACTION_RATE_US);
		rateApplicationPrompt.setPackage(context.getPackageName());

		PendingIntent rateApplicationPromptPendingIntent = PendingIntent.getBroadcast(context, 0, rateApplicationPrompt,
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		DateTime alarmTime = TIInfo.Date2014LocalTime.minusHours(1);
		if (alarmTime.isAfterNow())
		{
			Log.i(TAG, "Set alarm for 1hr before TI");
			alarmService.set(AlarmManager.RTC, alarmTime.getMillis(), rateApplicationPromptPendingIntent);
		}
		else if (TIInfo.Date2014LocalTime.plusDays(4).isAfterNow())
		{
			Log.i(TAG, "Set alarm for now");
			alarmService.set(AlarmManager.RTC, System.currentTimeMillis(), rateApplicationPromptPendingIntent);
		}
		else
		{
			Log.i(TAG, "Not setting an alarm");
		}
	}

	public static void showRateUsDialog(final Context context)
	{
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);

		new AlertDialog.Builder(context)
				.setPositiveButton(context.getString(R.string.rate_prompt_yes), new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id=com.appenjoyment.ticompanion"));
						try
						{
							context.startActivity(intent);
						}
						catch (ActivityNotFoundException e)
						{
							Log.e(TAG, "No market installed");
						}
					}
				})
				.setNegativeButton(R.string.rate_prompt_no, null)
				.setMessage(createRateUsMessage(context))
				.setTitle(context.getString(R.string.rate_prompt_title))
				.show();
	}

	private static String createRateUsMessage(Context context)
	{
		DateTime alarmTime = TIInfo.Date2014LocalTime;

		// use the "almost here" text if the alarm is fired before the event
		boolean use1Hr = alarmTime.isAfterNow();
		return use1Hr ? context.getString(R.string.rate_prompt_1_hour) : context.getString(R.string.rate_prompt_past);
	}

	private static final String TAG = RateUsManager.class.getName();

	private static final String PREFERENCES_NAME = "RateUs";
	private static final String PREFERENCE_KEY_NOTIFIED_FOR_RATE_PROMPT = "NotifiedForRatePrompt";
	private static final String ACTION_RATE_US = "com.appenjoyment.ticountdown.action.RATE_US";
	private static final int NOTIFICATION_ID = 100;
}

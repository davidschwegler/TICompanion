package com.appenjoyment.ticompanion;

import android.app.Application;
import android.util.Log;

public class TICompanionApp extends Application
{
	public void onCreate()
	{
		Log.i("TICompanionApp", "onCreate()");
		CountdownDisplayManager.getInstance().init(getApplicationContext());
		CountdownWidgetManager.getInstance().init(getApplicationContext());
		CountdownNotificationManager.getInstance().init(getApplicationContext());
	};
}

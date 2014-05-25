
package com.appenjoyment.ticompanion;

import android.app.Application;
import android.util.Log;

public class TICompanionApp extends Application
{
	public void onCreate()
	{
		Log.i("TICompanionApp", "onCreate()");
		CountdownWidgetManager.getInstance().init(getApplicationContext());
	};
}

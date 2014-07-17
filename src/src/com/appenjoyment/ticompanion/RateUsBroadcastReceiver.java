package com.appenjoyment.ticompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RateUsBroadcastReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i("RateUsBroadcastReceiver", "onReceive()");

		RateUsManager.sendNotification(context);
	}
}

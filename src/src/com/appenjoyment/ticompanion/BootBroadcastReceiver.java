
package com.appenjoyment.ticompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i("BootBroadcastReceiver", "onReceive()");
		
		Intent startServiceIntent = new Intent(context, CountdownService.class);
		context.startService(startServiceIntent);
	}
}

package com.appenjoyment.ticompanion;

import android.content.Context;
import android.content.Intent;

public final class IntentUtility
{
	public static Intent getBringToFrontIntent(Context applicationContext, Class<?> mainLauncherActivity)
	{
		return new Intent(applicationContext, mainLauncherActivity)
				.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	}

	private IntentUtility()
	{
	}
}

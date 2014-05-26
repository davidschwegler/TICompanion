package com.appenjoyment.ticompanion;

import android.content.Context;
import android.content.Intent;

public final class IntentUtility
{
	public static Intent getBringToFrontIntent(Context applicationContext, Class<?> mainLauncherActivity)
	{
		return new Intent(applicationContext, mainLauncherActivity)
				.addCategory(Intent.CATEGORY_LAUNCHER)
				.setAction(Intent.ACTION_MAIN);
	}

	private IntentUtility()
	{
	}
}

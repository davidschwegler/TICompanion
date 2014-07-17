package com.appenjoyment.ticompanion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class MainActivity extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		if (savedInstanceState == null)
		{
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new MainFragment())
					.commit();
		}

		if (getIntent() != null && RateUsManager.URI_RATE_US.equals(getIntent().getDataString()))
			RateUsManager.showRateUsDialog(this);
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		if (intent != null && RateUsManager.URI_RATE_US.equals(intent.getDataString()))
			RateUsManager.showRateUsDialog(this);
	}
}

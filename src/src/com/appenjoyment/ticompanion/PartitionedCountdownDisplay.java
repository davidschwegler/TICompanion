package com.appenjoyment.ticompanion;

import android.content.res.Resources;

public final class PartitionedCountdownDisplay extends CountdownDisplay
{
	public PartitionedCountdownDisplay(Resources resources, int titleResourceId)
	{
		super(CountdownDisplayKind.Partitioned, resources, titleResourceId);
	}

	@Override
	protected String render()
	{
		return TIInfo.createDisplayString(TimeUntil.getPartitionedTimeUntilDate(getCurrentTime(), TIInfo.Date2014LocalTime), getResources());
	}
}
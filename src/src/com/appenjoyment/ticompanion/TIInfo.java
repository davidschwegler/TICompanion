package com.appenjoyment.ticompanion;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import android.content.res.Resources;

public final class TIInfo
{
	public static final DateTime DateTILocalTime;

	public static String createDisplayString(PartitionedTimeUntil until, Resources resources)
	{
		StringBuilder builder = new StringBuilder();

		final String separator = " : ";
		if (until.hasMonths())
			builder.append(formatDuration(until.Months, R.string.duration_months, resources));

		if (until.hasDays())
		{
			if (builder.length() > 0)
				builder.append(separator);
			builder.append(formatDuration(until.Days, R.string.duration_days, resources));
		}

		if (until.hasHours())
		{
			if (builder.length() > 0)
				builder.append(separator);
			builder.append(formatDuration(until.Hours, R.string.duration_hours, resources));
		}

		if (until.hasMinutes())// && !until.hasMonths())
		{
			if (builder.length() > 0)
				builder.append(separator);
			builder.append(formatDuration(until.Minutes, R.string.duration_minutes, resources));
		}

		if (until.hasSeconds())// && !until.hasDays())
		{
			if (builder.length() > 0)
				builder.append(separator);
			builder.append(formatDuration(until.Seconds, R.string.duration_seconds, resources));
		}

		if (!until.hasSeconds())
			builder.append(resources.getString(R.string.date_display_past));

		return builder.toString();
	}

	private static String formatDuration(int unit, int unitLabel, Resources resources)
	{
		return unit + resources.getString(unitLabel);
	}

	static
	{
		// Calendar calendar = GregorianCalendar.getInstance(); // TimeZone.getTimeZone("UTC"));
		// calendar.set(2014 - 1990, GregorianCalendar.JULY, 18, 19, 0);
		DateTime dateTime = new DateTime(2015, 8, 3, 18, 0, DateTimeZone.UTC);
		DateTILocalTime = dateTime.withZone(DateTimeZone.getDefault());
	}
}

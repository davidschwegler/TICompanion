package com.appenjoyment.ticompanion;

import org.joda.time.DateTime;

public class TimeUntil
{
	public static PartitionedTimeUntil getPartitionedTimeUntilDate(DateTime start, DateTime target)
	{
		PartitionedTimeUntil until = new PartitionedTimeUntil();
		until.Months = org.joda.time.Months.monthsBetween(start, TIInfo.Date2014LocalTime).getMonths();
		if (until.Months > 0)
			start = start.plusMonths(until.Months);

		until.Days = org.joda.time.Days.daysBetween(start, TIInfo.Date2014LocalTime).getDays();
		if (until.Days > 0)
			start = start.plusDays(until.Days);

		until.Hours = org.joda.time.Hours.hoursBetween(start, TIInfo.Date2014LocalTime).getHours();
		if (until.Hours > 0)
			start = start.plusHours(until.Hours);

		until.Minutes = org.joda.time.Minutes.minutesBetween(start, TIInfo.Date2014LocalTime).getMinutes();
		if (until.Minutes > 0)
			start = start.plusMinutes(until.Minutes);

		until.Seconds = org.joda.time.Seconds.secondsBetween(start, TIInfo.Date2014LocalTime).getSeconds();

		return until;
	}

	public static long getSecondsUntilDate(DateTime start, DateTime target)
	{
		return org.joda.time.Seconds.secondsBetween(start, target).getSeconds();
	}

	private TimeUntil()
	{
	}
}

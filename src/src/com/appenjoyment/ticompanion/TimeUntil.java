package com.appenjoyment.ticompanion;

import org.joda.time.DateTime;

public class TimeUntil
{
	public int Months;

	public int Days;

	public int Hours;

	public int Minutes;

	public int Seconds;

	public boolean hasMonths()
	{
		return Months > 0;
	}

	public boolean hasDays()
	{
		return Days > 0 || hasMonths();
	}

	public boolean hasHours()
	{
		return Hours > 0 || hasDays();
	}

	public boolean hasMinutes()
	{
		return Minutes > 0 || hasHours();
	}

	public boolean hasSeconds()
	{
		return Seconds > 0 || hasMinutes();
	}

	public static TimeUntil TimeUntilDate(DateTime target)
	{
		DateTime distance = DateTime.now();

		TimeUntil until = new TimeUntil();
		until.Months = org.joda.time.Months.monthsBetween(distance, TIInfo.Date2014LocalTime).getMonths();
		if (until.Months > 0)
			distance = distance.plusMonths(until.Months);

		until.Days = org.joda.time.Days.daysBetween(distance, TIInfo.Date2014LocalTime).getDays();
		if (until.Days > 0)
			distance = distance.plusDays(until.Days);

		until.Hours = org.joda.time.Hours.hoursBetween(distance, TIInfo.Date2014LocalTime).getHours();
		if (until.Hours > 0)
			distance = distance.plusHours(until.Hours);

		until.Minutes = org.joda.time.Minutes.minutesBetween(distance, TIInfo.Date2014LocalTime).getMinutes();
		if (until.Minutes > 0)
			distance = distance.plusMinutes(until.Minutes);

		until.Seconds = org.joda.time.Seconds.secondsBetween(distance, TIInfo.Date2014LocalTime).getSeconds();

		return until;
	}
}

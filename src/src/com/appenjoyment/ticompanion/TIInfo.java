package com.appenjoyment.ticompanion;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class TIInfo
{
	public static final DateTime Date2014LocalTime;

	static
	{
		// Calendar calendar = GregorianCalendar.getInstance(); // TimeZone.getTimeZone("UTC"));
		// calendar.set(2014 - 1990, GregorianCalendar.JULY, 18, 19, 0);
		DateTime dateTime = new DateTime(2014, 7, 18, 19, 0, DateTimeZone.UTC);
		Date2014LocalTime = dateTime.withZone(DateTimeZone.getDefault());
	}
}

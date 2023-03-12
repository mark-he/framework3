package com.eagletsoft.boot.framework.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DateUtils {
	public final static String DATE_FORMAT = "yyyy-MM-dd";
	public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static boolean isSameDate(Date date, Date date2) {
		return DateUtils.format(date, DATE_FORMAT).equals(DateUtils.format(date2, DATE_FORMAT));
	}
	
	public static Date start(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date end(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0); // MYSQL does not support.
		return cal.getTime();
	}

	public static Date goPast(Date today, int seconds) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.SECOND, -1 * seconds);
		return cal.getTime();
	}

	public static Date goFuture(Date today, int seconds) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.SECOND, seconds);
		return cal.getTime();
	}
	
	public static Date next(Date today) {
		return next(today, 1);
	}

	public static Date next(Date today, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}


	public static String format(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	
	public static Date parse(String date, String pattern) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			return sdf.parse(date);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Date getYearStart(int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);// MYSQL does not support.
		return cal.getTime();
	}

	public static Date getYearEnd(int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 31);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);// MYSQL does not support.
		return cal.getTime();
	}

	public static Date getMonthStart(Date start) {
		Calendar c = Calendar.getInstance();
		c.setTime(start(start));
		c.set(Calendar.DATE, 1);
		return c.getTime();
	}

	public static Date getMonthEnd(Date start, int months) {
		Calendar c = Calendar.getInstance();
		c.setTime(end(start));
		c.set(Calendar.DATE, 1);
		c.add(Calendar.MONTH, months - 1);
		c.set(Calendar.DATE, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		return c.getTime();
	}

	public static Date getDynMonthEnd(Date start, int months) {
		Calendar c = Calendar.getInstance();
		c.setTime(start);
		c.add(Calendar.MONTH, months);
		c.add(Calendar.DATE, -1);
		return c.getTime();
	}
	
	public static int dayDiff(Date big, Date small) {
		big = start(big);
		small = start(small);
		
		return (int)((long)(big.getTime() - small.getTime()) / (60 * 60 * 24 * 1000));
	}

	public static int dayCount(Date start, Date end) {
		return dayDiff(end, start) + 1;
	}

	public static int monthCount(Date start, Date end) {
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(start);
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(end);

		int months = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH);
		int offset = (endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)) * 12 + months;
		return offset + 1;
	}

	public static Date nextWorkingDays(Date date, int days) {//for weekend
		return nextWorkingDays(date, days, null);
	}

	public static boolean isWorkingDay(Date date, Set<String> holidays) {
		String key = DateUtils.format(date, DATE_FORMAT);
		String year = key.substring(0, 4);
		if (null != holidays && ifContainsYear(year, holidays)) {
			return !holidays.contains(key);
		}
		return !isWeekend(date);
	}

	private static boolean ifContainsYear(String year, Set<String> holidays) {
		for (String h : holidays) {
			if (h.startsWith(year)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isWeekend(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int weekday = cal.get(Calendar.DAY_OF_WEEK);
		return (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY);
	}

	public static Date nextWorkingDays(Date date, int days, Set<String> holidays) {
		if (days == 0) {
			days = 1;
		}

		int positive = days > 0 ? 1 : -1;
		int abs = Math.abs(days);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		while(abs > 0) {
			cal.add(Calendar.DATE, positive);
			if (isWorkingDay(cal.getTime(), holidays)) {
				abs--;
			}
		}
		return cal.getTime();
	}

	public static int getWorkingDays(Date start, Date end, Set<String> holidays) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);

		int ret = 0;
		while (cal.getTime().compareTo(end) <= 0) {
			if (isWorkingDay(cal.getTime(), holidays)) {
				ret ++;
			}
			cal.add(Calendar.DATE, 1);
		}
		return ret;
	}

	public static void main(String[] args) {
		Date date = DateUtils.parse("2021-01-23", DATE_FORMAT);

		Set<String> holidays = new HashSet<>();
		holidays.add("2021-01-24");

		System.out.println(isWorkingDay(date, holidays));
	}
}

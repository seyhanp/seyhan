/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mdpinar
*/
public class DateUtils {

	private final static Logger log = LoggerFactory.getLogger(DateUtils.class);

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private static final SimpleDateFormat sdfForDB = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat sdfLongForDB = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private static final SimpleDateFormat sdfForTrans = new SimpleDateFormat("yyyy-MM");
	private static final SimpleDateFormat sdfStandart = new SimpleDateFormat("dd/MM/yyyy");

	public static String formatDateForDB(Date date) {
		if (date != null)
			return "'" + sdfForDB.format(date) + "'";
		else
			return "";
	}

	public static String formatLongDateForDB(Date date) {
		if (date != null)
			return "'" + sdfLongForDB.format(date) + "'";
		else
			return "";
	}

	public static String formatDateStandart(Date date) {
		if (date != null)
			return sdfStandart.format(date);
		else
			return "";
	}

	public static String formatDate(Date date, String format) {
		if (date != null) {
			SimpleDateFormat formatPattern = new SimpleDateFormat(format);
			return formatPattern.format(date);
		} else {
			return "";
		}
	}

	public static String formatReverseDate(Date date) {
		if (date != null)
			return sdfForDB.format(date);
		else
			return "";
	}

	public static String today(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	public static Date today() {
		return getZeroTime(new Date()); 
	}

	public static Date getZeroTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime(); 
	}

	public static Date getFirstDayOfMonth() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.DAY_OF_MONTH, 1);

		return now.getTime();
	}

	public static Date getFirstDayOfYear() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.DAY_OF_MONTH, 1);
		now.set(Calendar.MONTH, 0);

		return now.getTime();
	}

	public static Date findFirstDayOfPastMonth() {
		DateTime dt = new DateTime();

		return dt.minusMonths(1).dayOfMonth().withMinimumValue().toDate();
	}

	public static Date findLastDayOfPastMonth() {
		DateTime dt = new DateTime();

		return dt.minusMonths(1).dayOfMonth().withMaximumValue().toDate();
	}

	public static Date findLastMonth() {
		DateTime dt = new DateTime();

		return dt.minusMonths(1).toDate();
	}

	public static Date findFirstDay(String yyyymm) {
		try {
			return sdf.parse(yyyymm + "/01");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Date findLastDay(String yyyymm) {
		for (int i = 31; i > 27; i--) {
			try {
				return sdf.parse(yyyymm + "/" + i);
			} catch (ParseException e) {
				;
			}
		}

		return null;
	}

	public static Date parse(String date, String format) {
		try {
			return new SimpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}

		return null;
	}

	public static boolean isGreateThan(Date lesser, Date bigger) {
		if (lesser == null || bigger == null) return false;

		Days days = Days.daysBetween(new DateTime(bigger), new DateTime(lesser));
		return (days.getDays() > 0);
	}
	
	public static boolean isLessThan(Date lesser, Date bigger) {
		if (lesser == null || bigger == null) return false;
		
		Days days = Days.daysBetween(new DateTime(bigger), new DateTime(lesser));
		return (days.getDays() <= 0);
	}

	public static int getYear(Date date) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);

	    return cal.get(Calendar.YEAR);
	}

	public static String getYearMonth(Date date) {
		return sdfForTrans.format(date);
	}

	public static String getYearForSQL(String field) {
		return "YEAR("+field+")";
	}

	public static String getYearMonthForSQL(String field) {
		if (GlobalCons.dbVendor.equals("mysql")) {
			return "DATE_FORMAT("+field+",'%Y-%m')";
		}
		if (GlobalCons.dbVendor.equals("h2")) {
			return "FORMATDATETIME("+field+",'yyyy-MM')";
		}
		if (GlobalCons.dbVendor.equals("sqlserver")) {
			return "CONVERT(VARCHAR(7), "+field+", 126)";
		}

		return "TO_CHAR("+field+",'YYYY-MM')";
	}

}

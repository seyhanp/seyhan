/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package utils;

import controllers.global.Profiles;

/**
 * @author mdpinar
*/
public class NumericUtils {

	public static double round(Double value) {
		if (value == null) return 0d;
		return round(value, Profiles.chosen().gnel_pennyDigitNumber);
	}

	public static double round(Double value, int places) {
		if (value == null || places < 0) return 0d;

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public static double roundingDiscount(Double value, Integer digits) {
		double discount = 0;
		if (value != null && digits != null) {
			if (digits > 0 && digits < 4) {
				discount = value - (Math.floor(value / Math.pow(10, digits)) * Math.pow(10, digits));
			} else {
				discount = value - ((long) value.doubleValue());
			}
		}
		return round(discount);
	}

	public static Long strToLong(Object val) {
		if (val != null)
			return strToLong(val.toString());
		else
			return null;
	}

	public static Integer strToInteger(Object val) {
		if (val != null)
			return strToInteger(val.toString());
		else
			return null;
	}

	public static Long strToLong(String val) {
		return strToLong(val, null);
	}

	public static Integer strToInteger(String val) {
		return strToInteger(val, null);
	}

	public static Long strToLong(String val, Long defauld) {
		Long result = null;
		try {
			result = Long.valueOf(val);
		} catch (Exception e) {
			result = defauld;
		}

		return result;
	}

	public static Integer strToInteger(String val, Integer defauld) {
		Integer result = null;
		try {
			result = Integer.parseInt(val);
		} catch (Exception e) {
			result = defauld;
		}

		return result;
	}

	/**
	 * Sayiyi yaziya cevirir
	 * ornek:
	 *  346.75 -> (ucyuzkirkalti-lira, 75-kurus)
	 *  101100 -> (yuzbirbinyuzbir-lira)
	 */
	public static String withWritingInTurkish(Double number) {
		if (number == null || number.doubleValue() <= 0) return "";

		String snum = number.toString();
		int pointAt = snum.indexOf(".");

		String iPart = "";
		String fPart = "";
		if (pointAt > 0) {
			iPart = snum.substring(0, pointAt);
			fPart = snum.substring(pointAt+1);
			if (fPart.length() == 1) fPart = fPart + "0";
		} else {
			iPart = snum;
		}
		
		if (iPart.equals("0") && fPart.equals("00")) return "";
		
		StringBuilder sb = new StringBuilder();
		
		if (number > 0) {
			sb.append("(");
		}
		
		if (! (iPart.isEmpty() || iPart.equals("0"))) {
			sb.append(withWritingInTurkish(iPart));
			sb.append("-lira");
			if (! (fPart.isEmpty() || fPart.equals("00"))) {
				sb.append(", ");
			}
		}

		if (! (fPart.isEmpty() || fPart.equals("00"))) {
			sb.append(withWritingInTurkish(fPart));
			sb.append("-kuruş");
		}
		
		if (number > 0) {
			sb.append(")");
		}

		return sb.toString();
	}

	private static String withWritingInTurkish(String number) {
		final String ZEROS = "000";

		String[] ones = { "", "bir", "iki", "üç", "dört", "beş", "altı", "yedi", "sekiz", "dokuz" };
		String[] tens = { "", "on", "yirmi", "otuz", "kırk", "elli", "altmış", "yetmiş", "seksen", "doksan" };
		String[] highers = { "", "bin", "milyon", "milyar", "trilyon" };

		int remain = number.length() % 3;

		if (remain > 0 && remain < 3) {
			number = ZEROS.substring(0, 3-remain) + number;
		}
		String[] parts = number.split("(?<=\\G.{3})");

		String result = "";
		for (int i = parts.length; i > 0; i--) {
			String writing = "";
			String part = parts[i - 1];
			for (int j = 0; j < part.length(); j++) {
				int digit = Integer.parseInt(part.substring(j, j+1));
				if (digit > 0) {
					switch (j) {
						case 0: {
							if (digit != 1) writing += ones[digit];
							writing += "yüz";
							break;
						}
						case 1: {
							writing += tens[digit];
							break;
						}
						case 2: {
							writing += ones[digit];
							break;
						}
					}
				}
			}
			if (writing.equals("bir") && highers[parts.length - i].equals("bin")) {
				result = highers[parts.length - i] + result;
			} else {
				result = writing + highers[parts.length - i] + result;
			}
		}

		return result;
	}
	
}

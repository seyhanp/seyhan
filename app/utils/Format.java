/**
* Copyright (c) 2015 Mustafa DUMLUPINAR, mdumlupinar@gmail.com
*
* This file is part of seyhan project.
*
* seyhan is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package utils;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * @author mdpinar
*/
public class Format {

	public static String asDate(Date date) {
		if (date == null) return null;
        return DateUtils.formatDateStandart(date);
	}

	public static String asMoney(double money) {
		DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(money);
	}

	public static String asDecimal(double decimal) {
		DecimalFormat df = new DecimalFormat("#,##0.##");
        return df.format(decimal);
	}

	public static String asMoneyB(double money) {
		DecimalFormat df = new DecimalFormat("#,##0.00 BB;#,##0.00 AB");
        return df.format(money);
	}

	public static String asQuantity(double quantity) {
		DecimalFormat df = new DecimalFormat("#,##0.##");
        return df.format(quantity);
	}

	public static String asInteger(Integer value, String format, int width) {
		return asInteger(null, value, format, width);
	}
	
	public static String asInteger(String prefix, Integer value, String format, int width) {
		if (value == null) return null;
		DecimalFormat df = new DecimalFormat(format);
		String result = df.format(value);
		if (! result.isEmpty()) {
			result = StringUtils.padLeft((prefix != null ? prefix : "") + result, width);
		}
		return result;
	}

	public static String asLong(Long value, String format, int width) {
		return asLong(null, value, format, width);
	}
	
	public static String asLong(String prefix, Long value, String format, int width) {
		if (value == null) return null;
		DecimalFormat df = new DecimalFormat(format);
		String result = df.format(value);
		if (! result.isEmpty()) {
			result = StringUtils.padLeft((prefix != null ? prefix : "") + result, width);
		}
		return result;
	}

	public static String asDouble(Double value, String format, int width) {
		return asDouble(null, value, format, width);
	}

	public static String asDouble(String prefix, Double value, String format, int width) {
		if (value == null) return null;
		DecimalFormat df = new DecimalFormat(format);
		String result = df.format(value);
		if (! result.isEmpty()) {
			result = StringUtils.padLeft((prefix != null ? prefix : "") + result, width);
		}
		return result;
	}

}

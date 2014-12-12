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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import meta.FieldExcluderForGson;
import models.ChqbllPayrollDetail;
import play.i18n.Messages;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import enums.Alignment;
import enums.Right;

/**
 * @author mdpinar
*/
public class StringUtils {
	
	private static final Gson gson;
	
	static {
		gson = new GsonBuilder()
				        .setExclusionStrategies(new FieldExcluderForGson())
				        .create();
	}

	public static String deAccent(String str) {
	    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	public static String buildLinkForApprovals(Right right, String target, String id) {
		String receiptName = "";
		String receiptLink = "";

		if (right == null || id == null || target == null) return "";

		List<String> acceptableTargetList = new ArrayList<String>();
		acceptableTargetList.add("waybills");
		acceptableTargetList.add("invoices");

		if (! acceptableTargetList.contains(target)) return "";

		if ("waybills".equals(target)) {
			if(right.equals(Right.SPRS_ALINAN_SIPARIS_FISI)) {
				receiptName = Right.IRSL_SATIS_IRSALIYESI.name();
				receiptLink = Messages.get("related", Messages.get(Right.IRSL_SATIS_IRSALIYESI.key));
			} else {
				receiptName = Right.IRSL_ALIS_IRSALIYESI.name();
				receiptLink = Messages.get("related", Messages.get(Right.IRSL_ALIS_IRSALIYESI.key));
			}
		} if ("invoices".equals(target)) {
			if(right.equals(Right.SPRS_ALINAN_SIPARIS_FISI) || right.equals(Right.IRSL_SATIS_IRSALIYESI)) {
				receiptName = Right.FATR_SATIS_FATURASI.name();
				receiptLink = Messages.get("related", Messages.get(Right.FATR_SATIS_FATURASI.key));
			} else if(right.equals(Right.SPRS_VERILEN_SIPARIS_FISI) || right.equals(Right.IRSL_ALIS_IRSALIYESI)) {
				receiptName = Right.FATR_ALIS_FATURASI.name();
				receiptLink = Messages.get("related", Messages.get(Right.FATR_ALIS_FATURASI.key));
			}
		}

		return String.format("<a class='btn btn-mini btn-primary' target='%s' href='/%s/trans/%s?rightBind=%s'>%s</a>", target, target, id, receiptName, receiptLink);
	}

	public static String join(Collection<?> col, String delim) {
		StringBuilder sb = new StringBuilder();
		Iterator<?> iter = col.iterator();
		if (iter.hasNext()) {
			sb.append(iter.next().toString());
		}
		while (iter.hasNext()) {
			sb.append(delim);
			sb.append(iter.next().toString());
		}
		return sb.toString();
	}

	public static String getHtmHeaderForReport() {
		StringBuilder htmlSB = new StringBuilder("<html><head><title></title><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>");
		htmlSB.append("<style type='text/css'>");
		htmlSB.append("a {text-decoration: none}");
		htmlSB.append("</style>");
		htmlSB.append("</head>");
		htmlSB.append("<body text='#000000'>");
		htmlSB.append("<table width='100%'>");
		htmlSB.append("<tr><td align='left'>");

		return htmlSB.toString();
	}

	public static String getHtmFooterForReport() {
		StringBuilder htmlSB = new StringBuilder("<input type='button' onclick='window.close()' value='Kapat'/>");
		htmlSB.append("</html>");

		return htmlSB.toString();
	}

	public static String buildOptionTag(Map<String, String> map, String selected) {
		return buildOptionTag(map, selected, true);
	}

	public static String buildOptionTag(Map<String, String> map, String selected, boolean hasBlankOption) {
		StringBuilder sb = new StringBuilder();

		if (hasBlankOption) {
			sb.append("<option class='_blank' value=''>");
			sb.append(Messages.get("choose"));
			sb.append("</option>");
		}

		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append("<option ");
			if (selected != null && entry.getValue().equals(selected)) sb.append("selected ");
			sb.append("value='");
			sb.append(entry.getKey());
			sb.append("'>");
			sb.append(entry.getValue());
			sb.append("</option>");
		}

		return sb.toString();
	}

	public static String getChqbllTitle(ChqbllPayrollDetail detail) {
		return Messages.get("chqbll.of", 
				Messages.get("enum.cqbl." + (detail.isCustomer ? "Customer" : "Firm")),  
				Messages.get(detail.sort.key));
	}

	public static String fill(String s, int n) {
		if (n > 0) {
			return padRight(s, n).replaceAll("\\s", s);
		} else {
			return "";
		}
	}

	public static String pad(String s, int n, Alignment align) {
		if (s != null && s.length() < n && align != null) {
			switch (align) {
				case Left: return padRight(s, n);
				case Center: return padCenter(s, n);
				case Right: return padLeft(s, n);
			}
		}
		return s;
	}
	
	public static String padRight(String s, int n) {
		if (n > 0) {
			return String.format("%1$-" + n + "s", s);
		} else {
			return "";
		}
	}

	public static String padLeft(String s, int n) {
		if (n > 0) {
			return String.format("%1$" + n + "s", s);
		} else {
			return "";
		}
	}
	
	public static String padCenter(String s, int w) {
		if (w > 0 && s.length() < w) {
			int spaceSize = (w - s.length()) / 2;
			if (spaceSize > -1) {
				String neu = fill(" ", spaceSize) + s;
				return neu + fill(" ", w - neu.length());
			}
		}
		return s;
	}

	public static String trimLeft(String s) {
		return s.replaceAll("^\\s+", "");
	}
	
	public static String trimRight(String s) {
		return s.replaceAll("\\s+$", "");
	}
	
	public static String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	public static String toJson(Object obj) {
		return gson.toJson(obj);
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		return gson.fromJson(json, clazz);
	}

	public static <T> T fromJson(String json, TypeToken<T> type) {
		return gson.fromJson(json, type.getType());
	}
	
	public static String trimLastSlash(String path) {
		if (path != null && path.endsWith("/") || path.endsWith("\\")) {
			return path.substring(0, path.length() - 1);
		}
		return path;
	}

}

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.Module;
import enums.Right;
import enums.TransType;

/**
 * @author mdpinar
*/
public class DocNoUtils {

	public static String findLastCode(Module module, String startWith) {
		String query = "select max(code) as lastNo from " + module.name() + " where workspace = " + CacheUtils.getWorkspaceId();
		if (startWith != null && ! startWith.isEmpty()) {
			query += " and code like '" + startWith + "%'";
		}
		SqlRow row = Ebean.createSqlQuery(query).findUnique();

		String lastNo = "";
		if (row != null && ! row.isEmpty()) {
			lastNo = DocNoUtils.docNoInc(row.getString("lastNo"));
		}

		if (lastNo == null || lastNo.trim().isEmpty()) {
			return startWith;
		} else {
			return lastNo;
		}
	}
	
	public static String findLastTransNo(Right right) {
		String tableName = right.module.name() + "_trans ";

		if (right.module.equals(Module.cheque) || right.module.equals(Module.bill)) {
			tableName = "chqbll_trans ";
		} else {
			if (right.transType == null || right.transType.equals(TransType.Input) || right.transType.equals(TransType.Credit)) return "";
		}

		if (right.equals(Right.CEK_GIRIS_BORDROSU) || right.equals(Right.SENET_GIRIS_BORDROSU)
		||  right.equals(Right.CEK_CIKIS_BORDROSU) || right.equals(Right.SENET_CIKIS_BORDROSU)) {
			tableName = "chqbll_payroll ";
		}

		String query = "select max(trans_no) as lastNo from " + tableName + " where workspace = " + CacheUtils.getWorkspaceId() + " and _right ='" + right.name() + "'";
		SqlRow row = Ebean.createSqlQuery(query).findUnique();

		String lastNo = "";
		if (row != null && ! row.isEmpty()) {
			lastNo = DocNoUtils.docNoInc(row.getString("lastNo"));
		}

		return lastNo;
	}

	public static Integer findLastReceiptNo(Right right) {
		String tableName = right.module.name() + "_trans ";

		if (right.module.equals(Module.cheque) || right.module.equals(Module.bill)) tableName = "chqbll_trans ";

		if (right.equals(Right.CEK_GIRIS_BORDROSU) || right.equals(Right.SENET_GIRIS_BORDROSU)
		||  right.equals(Right.CEK_CIKIS_BORDROSU) || right.equals(Right.SENET_CIKIS_BORDROSU)) tableName = "chqbll_payroll ";

		String query = "select max(receipt_no) as receiptNo from " + tableName + " where workspace = " + CacheUtils.getWorkspaceId() + " and _right ='" + right.name() + "' ";
		switch (Profiles.chosen().gnel_receiptNoRnwType) {
			case Daily: {
				query += " and trans_date = " + DateUtils.formatDateForDB(new Date());
				break;
			}
			case Monthly: {
				query += " and trans_month = '" + DateUtils.getYearMonth(new Date()) + "'";
				break;
			}
		}
		SqlRow row = Ebean.createSqlQuery(query).findUnique();

		Integer lastNo = 1;
		if (row != null && ! row.isEmpty()) {
			if (row.getInteger("receiptNo") != null) lastNo = row.getInteger("receiptNo") + 1;
		}

		return lastNo;
	}

	public static Integer findLastPortfolioNo(ChqbllSort sort) {
		String query = "select max(portfolio_no) as lastNo from chqbll_payroll_detail where workspace = " + CacheUtils.getWorkspaceId() + " and sort = '" + sort.name() + "'";
		SqlRow row = Ebean.createSqlQuery(query).findUnique();

		Integer lastNo = 1;
		if (row != null && ! row.isEmpty() && row.getInteger("lastNo") != null) {
			lastNo = row.getInteger("lastNo").intValue() + 1;
		}

		return lastNo;
	}

	private static String docNoInc(String no) {
		if (no == null) return "";

		StringBuilder result = new StringBuilder(no);
		char[] allChars = no.toCharArray();
		CharStat firstAlpha = null;

		List<CharStat> charStatList = new ArrayList<CharStat>();
		for (int i = allChars.length - 1; i > -1; i--) {
			char chr = allChars[i];
			if (chr > 47 && chr < 58) {
				charStatList.add(new CharStat(chr, i));
			} else if (firstAlpha == null && chr > 64 && chr < 91) {
				firstAlpha = new CharStat(chr, i);
			}
		}

		if (charStatList.size() > 0) {
			boolean isInc = false;
			for (int i = 0; i < charStatList.size(); i++) {
				charStatList.get(i).isChnaged = true;
				if (charStatList.get(i).chr < 57) {
					charStatList.get(i).chr++;
					isInc = true;
					break;
				} else {
					charStatList.get(i).chr = 48;
				}
			}

			if (! isInc && firstAlpha != null) {
				result.setCharAt(charStatList.get(0).index, ++charStatList.get(0).chr);
				result.setCharAt(firstAlpha.index, ++firstAlpha.chr);
			}

			for (int i = 0; i < charStatList.size(); i++) {
				if (charStatList.get(i).isChnaged) {
					result.setCharAt(charStatList.get(i).index, charStatList.get(i).chr);
				}
			}
		}

		return result.toString();
	}

	static class CharStat {
		char chr;
		int index;
		boolean isChnaged;

		CharStat(char chr, int index) {
			super();
			this.chr = chr;
			this.index = index;
		}

	}

}

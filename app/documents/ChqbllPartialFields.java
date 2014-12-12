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
package documents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import play.cache.Cache;
import utils.CacheUtils;
import utils.TemplateHelper;
import enums.ChqbllSort;
import enums.FieldType;
import enums.Module;

@SuppressWarnings("unchecked")
/**
 * @author mdpinar
*/
public class ChqbllPartialFields {

	private static final String TABLE_NAME = "chqbll_detail_partial";

	public static Map<String, String> getMasterOptions(ChqbllSort sort, boolean isReportFooter) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, TABLE_NAME, sort, "master_" + isReportFooter);

		Map<String, String> fieldMap = (Map<String, String>) Cache.get(cacheKey);
		if (fieldMap != null) return fieldMap;
		
		List<Field> fieldList = new ArrayList<Field>();
		fieldList.add(new Field("chqbll_payroll_detail.total_paid", "paid", 13, FieldType.CURRENCY));
		fieldList.add(new Field("chqbll_payroll_detail.amount-chqbll_payroll_detail.total_paid", "remain", "remaining", 13, FieldType.CURRENCY));
		fieldList.add(new Field(FieldType.NUMBER_TO_TEXT, "with_writing/paid", 80, "chqbll_payroll_detail.total_paid"));

		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(ChqbllPayrollFields.getDetailOptions(sort, fieldList));
		fieldMap.putAll(TemplateHelper.buildOptions("contact", ContactFields.getFields()));
		if (isReportFooter) {
			fieldMap.putAll(TemplateHelper.buildOptions("tables", TablesFields.getFields(Module.cheque))); //cek/senet olarak basvurmanin farki yok.
		}
		fieldMap.putAll(TemplateHelper.buildOptions("constant", ConstantFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("system", SystemFields.getFields()));

		Cache.set(cacheKey, fieldMap, CacheUtils.ONE_DAY);

		return fieldMap;
	}

	public static Map<String, String> getDetailOptions() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, TABLE_NAME, "detail");

		Map<String, String> fieldMap = (Map<String, String>) Cache.get(cacheKey);
		if (fieldMap != null) return fieldMap;

		List<Field> fieldList = new ArrayList<Field>();
		fieldList.add(new Field(TABLE_NAME + ".trans_date", "date", 10, FieldType.DATE));
		fieldList.add(new Field(TABLE_NAME + ".amount", "amount", 13, FieldType.CURRENCY));
		fieldList.add(new Field(TABLE_NAME + ".exc_code", "currency", 3));
		fieldList.add(new Field(TABLE_NAME + ".exc_rate", "exchange_rate", 6, FieldType.RATE));
		fieldList.add(new Field(TABLE_NAME + ".exc_equivalent", "exc_equivalent", 13, FieldType.CURRENCY));
		fieldList.add(new Field(TABLE_NAME + ".description", "description", 100));
		fieldList.add(new Field(TABLE_NAME + ".insert_by", "insert_by", 20));
		fieldList.add(new Field(TABLE_NAME + ".insert_at", "insert_at", 20, FieldType.LONGDATE));

		fieldList.add(new Field("safe.name", "safe", 30));

		fieldMap = new LinkedHashMap<String, String>();
		fieldMap.putAll(TemplateHelper.buildOptions("action", fieldList));

		Cache.set(cacheKey, fieldMap, CacheUtils.ONE_DAY);

		return fieldMap;
	}

}

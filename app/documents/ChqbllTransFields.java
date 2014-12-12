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
public class ChqbllTransFields {

	private static final String TABLE_NAME = "chqbll_trans";

	public static Map<String, String> getMasterOptions(boolean isReportFooter) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, isReportFooter, TABLE_NAME, "master");

		Map<String, String> fieldMap = (Map<String, String>) Cache.get(cacheKey);
		if (fieldMap != null) return fieldMap;

		List<Field> fieldList = new ArrayList<Field>();
		fieldList.addAll(BaseDocTransFields.getFields("chqbll_payroll", false));
		fieldList.add(new Field(TABLE_NAME + ".row_count", "row_count", 3, FieldType.INTEGER));
		fieldList.add(new Field(TABLE_NAME + ".adat", "Adat", 3, FieldType.INTEGER));
		fieldList.add(new Field(TABLE_NAME + ".avarage_date", "date.avarage", 10, FieldType.DATE));
		fieldList.add(new Field(TABLE_NAME + ".total", "total", 13, FieldType.CURRENCY));
		fieldList.add(new Field(FieldType.NUMBER_TO_TEXT, "with_writing/total", 80, TABLE_NAME + ".total"));
		fieldList.add(new Field("chqbll_payroll_source.name", "trans.source", 30));
		fieldList.add(new Field("global_trans_point.name", "trans.point", 30));
		fieldList.add(new Field("global_private_code.name", "private_code", 30));

		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(TemplateHelper.buildOptions("action", fieldList));
		fieldMap.putAll(TemplateHelper.buildOptions("contact", ContactFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("safe", SafeFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("bank", BankFields.getFields()));
		if (isReportFooter) {
			fieldMap.putAll(TemplateHelper.buildOptions("tables", TablesFields.getFields(Module.cheque)));
		}
		fieldMap.putAll(TemplateHelper.buildOptions("constant", ConstantFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("system", SystemFields.getFields()));

		Cache.set(cacheKey, fieldMap, CacheUtils.ONE_DAY);

		return fieldMap;
	}

	public static Map<String, String> getDetailOptions(ChqbllSort sort) {
		return ChqbllPayrollFields.getDetailOptions(sort);
	}
	
}

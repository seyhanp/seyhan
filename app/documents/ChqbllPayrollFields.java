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
public class ChqbllPayrollFields {

	private static final String MASTER_NAME = "chqbll_payroll";
	private static final String DETAIL_NAME = "chqbll_payroll_detail";

	public static Map<String, String> getMasterOptions(boolean isReportFooter) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, isReportFooter, MASTER_NAME);

		Map<String, String> fieldMap = (Map<String, String>) Cache.get(cacheKey);
		if (fieldMap != null) return fieldMap;

		List<Field> fieldList = new ArrayList<Field>();
		fieldList.addAll(BaseDocTransFields.getFields("chqbll_payroll", false));
		fieldList.add(new Field(MASTER_NAME + ".row_count", "row_count", 3, FieldType.INTEGER));
		fieldList.add(new Field(MASTER_NAME + ".adat", "Adat", 3, FieldType.INTEGER));
		fieldList.add(new Field(MASTER_NAME + ".avarage_date", "date.avarage", 10, FieldType.DATE));
		fieldList.add(new Field(MASTER_NAME + ".total", "total", 13, FieldType.CURRENCY));
		fieldList.add(new Field(FieldType.NUMBER_TO_TEXT, "with_writing/total", 80, MASTER_NAME + ".total"));
		fieldList.add(new Field(MASTER_NAME + "_source.name", "trans.source", 30));
		fieldList.add(new Field("global_trans_point.name", "trans.point", 30));
		fieldList.add(new Field("global_private_code.name", "private_code", 30));

		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(TemplateHelper.buildOptions("contact", ContactFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("action", fieldList));
		if (isReportFooter) {
			fieldMap.putAll(TemplateHelper.buildOptions("tables", TablesFields.getFields(Module.cheque)));
		}
		fieldMap.putAll(TemplateHelper.buildOptions("constant", ConstantFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("system", SystemFields.getFields()));

		Cache.set(cacheKey, fieldMap, CacheUtils.ONE_DAY);

		return fieldMap;
	}

	public static Map<String, String> getDetailOptions(ChqbllSort sort) {
		return getDetailOptions(sort, null);
	}

	public static Map<String, String> getDetailOptions(ChqbllSort sort, List<Field> extraList) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, DETAIL_NAME, sort);

		Map<String, String> fieldMap = (Map<String, String>) Cache.get(cacheKey);
		if (fieldMap != null) return fieldMap;

		List<Field> detailList = new ArrayList<Field>();
		detailList.add(new Field(DETAIL_NAME + ".row_no", "No", 3, FieldType.INTEGER));
		detailList.add(new Field(DETAIL_NAME + ".portfolio_no", "portfolio.no", 7, FieldType.INTEGER));
		detailList.add(new Field(DETAIL_NAME + ".serial_no", "serial.no", 25));
		detailList.add(new Field(DETAIL_NAME + ".due_date", "date.maturity", 10, FieldType.DATE));
		detailList.add(new Field(DETAIL_NAME + ".amount", "amount", 13, FieldType.CURRENCY));
		detailList.add(new Field(DETAIL_NAME + ".exc_code", "currency", 3));
		detailList.add(new Field(DETAIL_NAME + ".exc_rate", "exchange_rate", 6, FieldType.RATE));
		detailList.add(new Field(DETAIL_NAME + ".exc_equivalent", "exc_equivalent", 13, FieldType.CURRENCY));
		detailList.add(new Field(DETAIL_NAME + ".owner", "owner", 70));
		detailList.add(new Field(DETAIL_NAME + ".payment_place", "payment_place", 30));
		
		if (ChqbllSort.Cheque.equals(sort)) {
			detailList.add(new Field(DETAIL_NAME + ".bank_name", "bank.name", 50));
			detailList.add(new Field(DETAIL_NAME + ".bank_branch", "branch", 30));
			detailList.add(new Field(DETAIL_NAME + ".bank_account_no", "account.no", 26));
			detailList.add(new Field(DETAIL_NAME + ".correspondent_branch", "correspondent_branch", 15));
		} else {
			detailList.add(new Field(DETAIL_NAME + ".surety", "surety", 100));
			detailList.add(new Field(DETAIL_NAME + ".surety_address", "address", 100));
			detailList.add(new Field(DETAIL_NAME + ".surety_phone1", "phoneX/1", 15));
			detailList.add(new Field(DETAIL_NAME + ".surety_phone2", "phoneX/2", 15));
		}
		detailList.add(new Field(DETAIL_NAME + ".description", "description", 100));
		if (extraList != null) detailList.addAll(extraList);
		detailList.add(new Field("chqbll_type.name", "type", 30));
		
		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(TemplateHelper.buildOptions("action", detailList));
		fieldMap.putAll(TemplateHelper.buildOptions("constant", ConstantFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("system", SystemFields.getFields()));

		Cache.set(cacheKey, fieldMap, CacheUtils.ONE_DAY);

		return fieldMap;
	}
	
}

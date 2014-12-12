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
import enums.FieldType;
import enums.Module;

@SuppressWarnings("unchecked")
/**
 * @author mdpinar
*/
public class InvoiceTransFields {

	private static final String TABLE_NAME = "invoice_trans";

	public static Map<String, String> getMasterOptions(boolean isReportFooter) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, isReportFooter, TABLE_NAME, "master");

		Map<String, String> fieldMap = (Map<String, String>) Cache.get(cacheKey);
		if (fieldMap != null) return fieldMap;

		List<Field> masterList = new ArrayList<Field>();
		masterList.add(new Field(TABLE_NAME + ".is_cash", "is_cash", 30, FieldType.BOOLEAN));
		masterList.add(new Field(TABLE_NAME + ".withholding_rate", "withholding.rate", 3, FieldType.RATE));
		masterList.add(new Field(TABLE_NAME + ".withholding_amount", "withholding.amount", 13, FieldType.CURRENCY));

		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(TemplateHelper.buildOptions("contact", ContactFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("action", BaseStockTransFields.getMasterFields(TABLE_NAME, masterList)));
		fieldMap.putAll(TemplateHelper.buildOptions("ref.account", BaseDocTransFields.getRefFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("total", BaseStockTransFields.getSumFields(TABLE_NAME + "_detail")));
		if (isReportFooter) {
			fieldMap.putAll(TemplateHelper.buildOptions("tables", TablesFields.getFields(Module.invoice)));
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

		List<Field> detailList = new ArrayList<Field>();
		detailList.add(new Field(TABLE_NAME + "_detail.serial_no", "serial.no", 50));
		detailList.add(new Field(TABLE_NAME + "_detail.tax_rate2", "vof.taxRate2", 2, FieldType.RATE));
		detailList.add(new Field(TABLE_NAME + "_detail.tax_rate3", "vof.taxRate3", 2, FieldType.RATE));

		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(TemplateHelper.buildOptions("stock", StockFields.getFields(TABLE_NAME + "_detail")));
		fieldMap.putAll(TemplateHelper.buildOptions("action", BaseStockTransFields.getDetailFields(TABLE_NAME + "_detail", detailList)));
		fieldMap.putAll(TemplateHelper.buildOptions("constant", ConstantFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("system", SystemFields.getFields()));

		Cache.set(cacheKey, fieldMap, CacheUtils.ONE_DAY);

		return fieldMap;
	}

}

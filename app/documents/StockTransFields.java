/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
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
public class StockTransFields {

	private static final String TABLE_NAME = "stock_trans";

	public static Map<String, String> getMasterOptions(boolean isReportFooter) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, TABLE_NAME, isReportFooter, "master");

		Map<String, String> fieldMap = (Map<String, String>) Cache.get(cacheKey);
		if (fieldMap != null) return fieldMap;

		List<Field> fieldList = new ArrayList<Field>();
		fieldList.add(new Field("ref_stock_depot.name", "ref.depot", 30));

		fieldMap = new LinkedHashMap<String, String>();
		
		fieldMap.putAll(TemplateHelper.buildOptions("contact", ContactFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("action", BaseStockTransFields.getMasterFields(TABLE_NAME, fieldList)));
		fieldMap.putAll(TemplateHelper.buildOptions("ref.account", BaseDocTransFields.getRefFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("total", BaseStockTransFields.getSumFields(TABLE_NAME + "_detail")));
		if (isReportFooter) {
			fieldMap.putAll(TemplateHelper.buildOptions("tables", TablesFields.getFields(Module.stock)));
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

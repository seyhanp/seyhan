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
public class WaybillTransFields {

	private static final String TABLE_NAME = "waybill_trans";

	public static Map<String, String> getMasterOptions(boolean isReportFooter) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, isReportFooter, TABLE_NAME, "master");

		Map<String, String> fieldMap = (Map<String, String>) Cache.get(cacheKey);
		if (fieldMap != null) return fieldMap;
		
		fieldMap = new LinkedHashMap<String, String>();
		fieldMap.putAll(TemplateHelper.buildOptions("contact", ContactFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("action", BaseStockTransFields.getMasterFields(TABLE_NAME)));
		fieldMap.putAll(TemplateHelper.buildOptions("total", BaseStockTransFields.getSumFields(TABLE_NAME + "_detail")));
		if (isReportFooter) {
			fieldMap.putAll(TemplateHelper.buildOptions("tables", TablesFields.getFields(Module.waybill)));
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
		detailList.add(new Field(TABLE_NAME + "_detail.completed", "balance.completed", 3, FieldType.DOUBLE));
		detailList.add(new Field(TABLE_NAME + "_detail.completed", "balance.cancelled", 3, FieldType.DOUBLE));

		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(TemplateHelper.buildOptions("stock", StockFields.getFields(TABLE_NAME + "_detail")));
		fieldMap.putAll(TemplateHelper.buildOptions("action", BaseStockTransFields.getDetailFields(TABLE_NAME + "_detail", detailList)));
		fieldMap.putAll(TemplateHelper.buildOptions("constant", ConstantFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("system", SystemFields.getFields()));

		Cache.set(cacheKey, fieldMap, CacheUtils.ONE_DAY);

		return fieldMap;
	}

}

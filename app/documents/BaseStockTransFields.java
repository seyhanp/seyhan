/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package documents;

import java.util.ArrayList;
import java.util.List;

import play.cache.Cache;
import utils.CacheUtils;
import enums.FieldType;

@SuppressWarnings("unchecked")
/**
 * @author mdpinar
*/
public class BaseStockTransFields {

	public static List<Field> getMasterFields(String tableName) {
		return getMasterFields(tableName, null);
	}

	public static List<Field> getMasterFields(String tableName, List<Field> addRelsList) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, tableName, "master");

		List<Field> fieldList = (List<Field>) Cache.get(cacheKey);
		if (fieldList != null) {
			return fieldList;
		} else {
			fieldList = new ArrayList<Field>();
		}

		fieldList.addAll(BaseDocTransFields.getFields(tableName, false));

		fieldList.add(new Field(tableName + ".real_date", "date.real", 20, FieldType.LONGDATE));
		fieldList.add(new Field(tableName + ".delivery_date", "date.delivery", 20, FieldType.LONGDATE));
		fieldList.add(new Field(tableName + ".contact_name", "contact.name", 100));
		fieldList.add(new Field(tableName + ".contact_tax_office", "contact.tax.office", 20));
		fieldList.add(new Field(tableName + ".contact_tax_number", "contact.tax.no", 15));
		fieldList.add(new Field(tableName + ".contact_address1", "address/1", 100));
		fieldList.add(new Field(tableName + ".contact_address2", "address/2", 100));
		fieldList.add(new Field(tableName + ".consigner", "consigner", 50));
		fieldList.add(new Field(tableName + ".recepient", "recepient", 50));
		fieldList.add(new Field(tableName + ".rounding_digits", "rounding_digits", 1, FieldType.INTEGER));
		fieldList.add(new Field(tableName + ".total_discount_rate", "total_discount_rate", 5, FieldType.RATE));
		fieldList.add(new Field(tableName + ".total", "total", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".rounding_discount", "rounding_discount", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".discount_total", "discount_total", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".subtotal", "subtotal", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".plus_factor_total", "plus_factor_total", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".minus_factor_total", "minus_factor_total", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".tax_total", "tax_total", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".net_total", "net_total", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".is_tax_included", "tax_status", 5, FieldType.BOOLEAN));
		fieldList.add(new Field(FieldType.NUMBER_TO_TEXT, "with_writing/net_total", 80, tableName + ".net_total"));

		fieldList.add(new Field(tableName + "_source.name", "trans.source", 30));
		if (addRelsList != null) fieldList.addAll(addRelsList);
		fieldList.add(new Field("stock_depot.name", "depot", 30));
		fieldList.add(new Field("sale_seller.name", "seller", 30));
		fieldList.add(new Field("global_trans_point.name", "trans.point", 30));
		fieldList.add(new Field("global_private_code.name", "private_code", 30));

		Cache.set(cacheKey, fieldList, CacheUtils.ONE_DAY);

		return fieldList;
	}

	public static List<Field> getDetailFields(String tableName, List<Field> addRelsList) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, tableName, "detail");

		List<Field> fieldList = (List<Field>) Cache.get(cacheKey);
		if (fieldList != null) return fieldList;

		fieldList = new ArrayList<Field>();
		fieldList.add(new Field(tableName + ".row_no", "No", 3, FieldType.INTEGER));
		fieldList.add(new Field(tableName + ".name", "stock.name", 100));
		fieldList.add(new Field(tableName + ".quantity", "quantity", 5, FieldType.DOUBLE));
		fieldList.add(new Field(tableName + ".unit", "unit", 6));
		fieldList.add(new Field(tableName + ".price", "price", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".amount", "amount", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".discount_rate1", "stock.discount/1", 2, FieldType.RATE));
		fieldList.add(new Field(tableName + ".discount_rate2", "stock.discount/2", 2, FieldType.RATE));
		fieldList.add(new Field(tableName + ".discount_rate3", "stock.discount/3", 2, FieldType.RATE));
		fieldList.add(new Field(tableName + ".discount_amount", "discount_amount", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".tax_rate", "tax_rate", 2, FieldType.RATE));
		fieldList.add(new Field(tableName + ".tax_amount", "tax_amount", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".total", "total", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".description", "description", 100));
		if (addRelsList != null) fieldList.addAll(addRelsList);
		fieldList.add(new Field(tableName + ".unit_ration", "unit.ratio", 5, FieldType.DOUBLE));
		fieldList.add(new Field(tableName + ".exc_code", "currency", 3));
		fieldList.add(new Field(tableName + ".exc_rate", "exchange_rate", 6, FieldType.RATE));
		fieldList.add(new Field(tableName + ".exc_equivalent", "exc_equivalent", 13, FieldType.CURRENCY));
		fieldList.add(new Field("sale_seller.name", "seller", 30));

		Cache.set(cacheKey, fieldList, CacheUtils.ONE_DAY);

		return fieldList;
	}

	public static List<Field> getSumFields(String tableName) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, tableName, "sumFields");
		
		List<Field> fieldList = (List<Field>) Cache.get(cacheKey);
		if (fieldList != null) return fieldList;

		fieldList = new ArrayList<Field>();
		fieldList.add(new Field(tableName + ".quantity", "quantity", 5, FieldType.SUM_OF));
		fieldList.add(new Field(tableName + ".amount", "amount", 13, FieldType.SUM_OF));
		fieldList.add(new Field(tableName + ".discount_amount", "discount_amount", 13, FieldType.SUM_OF));
		fieldList.add(new Field(tableName + ".tax_amount", "tax_amount", 13, FieldType.SUM_OF));
		fieldList.add(new Field(tableName + ".total", "total", 13, FieldType.SUM_OF));
		fieldList.add(new Field(tableName + ".exc_equivalent", "exc_equivalent", 13, FieldType.SUM_OF));

		Cache.set(cacheKey, fieldList, CacheUtils.ONE_DAY);

		return fieldList;
	}

}

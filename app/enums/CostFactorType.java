/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

import java.util.LinkedHashMap;
import java.util.Map;

import play.cache.Cache;
import play.i18n.Messages;
import utils.CacheUtils;

import com.avaje.ebean.annotation.EnumValue;

public enum CostFactorType {

	@EnumValue("Tax")
	Tax,

	@EnumValue("Discount")
	Discount,

	@EnumValue("Expense")
	Expense
	;

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, CostFactorType.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Tax.name(), Messages.get("tax"));
		options.put(Discount.name(), Messages.get("discount"));
		options.put(Expense.name(), Messages.get("expense"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

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

public enum CostingType {

	@EnumValue("Simple")
	Simple,

	@EnumValue("Weighted")
	Weighted,

	@EnumValue("Moving")
	Moving,

	@EnumValue("FIFO")
	FIFO,

	@EnumValue("LIFO")
	LIFO;

	public String key = "enum.costing_type." + name();

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, CostingType.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		for(CostingType enm : values()) {
			options.put(enm.name(), Messages.get(enm.key));
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

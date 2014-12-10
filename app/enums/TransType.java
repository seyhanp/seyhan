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

@SuppressWarnings("unchecked")
public enum TransType {

	@EnumValue("Input")
	Input,

	@EnumValue("Output")
	Output,

	@EnumValue("Debt")
	Debt,

	@EnumValue("Credit")
	Credit;

	public String key = "enum." + name();

	public static TransType findType(String name) {
		if (name != null) {
			return valueOf(name);
		}

		return null;
	}

	public static Map<String, String> contactOptions() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "contact", TransType.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Debt.name(), Messages.get(Debt.key));
		options.put(Credit.name(), Messages.get(Credit.key));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> stockOptions() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "stock", TransType.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Input.name(), Messages.get(Input.key));
		options.put(Output.name(), Messages.get(Output.key));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

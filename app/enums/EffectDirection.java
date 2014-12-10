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

public enum EffectDirection {

	@EnumValue("Increase")
	Increase,

	@EnumValue("Decrease")
	Decrease;

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, EffectDirection.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Increase.name(), Messages.get("increase"));
		options.put(Decrease.name(), Messages.get("decrease"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

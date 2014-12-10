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

public enum UserEditingLimit {

	@EnumValue("Free")
	Free,

	@EnumValue("Restricted")
	Restricted;

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, UserEditingLimit.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Free.name(), Messages.get("enum.UserEditingLimit.Free"));
		options.put(Restricted.name(), Messages.get("enum.UserEditingLimit.Restricted"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

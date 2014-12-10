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

public enum PrintAttitude {

	no,
	Manual,
	Automatic;

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, PrintAttitude.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(no.name(), Messages.get("enum.no"));
		options.put(Manual.name(), Messages.get("enum.Manual"));
		options.put(Automatic.name(), Messages.get("enum.Automatic"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

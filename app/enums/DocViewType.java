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

public enum DocViewType {

	@EnumValue("PORTRAIT")
	PORTRAIT,

	@EnumValue("LANDSCAPE")
	LANDSCAPE;

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, DocViewType.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(PORTRAIT.name(), Messages.get("print_view." + PORTRAIT.name()));
		options.put(LANDSCAPE.name(), Messages.get("print_view." + LANDSCAPE.name()));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

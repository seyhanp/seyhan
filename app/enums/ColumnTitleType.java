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

public enum ColumnTitleType {

	@EnumValue("NOTHING")
	NOTHING,

	@EnumValue("PLAIN")
	PLAIN,
	
	@EnumValue("DASHED")
	DASHED,
	
	@EnumValue("UNLINED")
	UNLINED;

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, ColumnTitleType.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(NOTHING.name(), Messages.get("enum.NOTHING"));
		options.put(PLAIN.name(), Messages.get("enum.PLAIN"));
		options.put(DASHED.name(), Messages.get("enum.DASHED"));
		options.put(UNLINED.name(), Messages.get("enum.UNLINED"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

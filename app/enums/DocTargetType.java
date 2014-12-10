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

public enum DocTargetType {

	@EnumValue("FILE")
	FILE,

	@EnumValue("DOT_MATRIX")
	DOT_MATRIX,
	
	@EnumValue("LASER")
	LASER;

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, DocTargetType.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(FILE.name(), Messages.get("print_target." + FILE.name()));
		options.put(DOT_MATRIX.name(), Messages.get("print_target." + DOT_MATRIX.name()));
		options.put(LASER.name(), Messages.get("print_target." + LASER.name()));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

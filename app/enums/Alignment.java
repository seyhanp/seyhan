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

public enum Alignment {

	@EnumValue("Left")
	Left,
	
	@EnumValue("Center")
	Center,

	@EnumValue("Right")
	Right;

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, Alignment.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Left.name(), Messages.get("to.left"));
		options.put(Center.name(), Messages.get("to.center"));
		options.put(Right.name(), Messages.get("to.right"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

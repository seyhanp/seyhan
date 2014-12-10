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

public enum ContactStatus {

	@EnumValue("Waiting")
	Waiting,

	@EnumValue("Normal")
	Normal,

	@EnumValue("In_Blacklist")
	In_Blacklist;

	public String key = "enum." + name();

	@SuppressWarnings("unchecked")
	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, ContactStatus.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		for(ContactStatus enm : values()) {
			options.put(enm.name(), Messages.get(enm.key));
		}

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

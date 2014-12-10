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

public enum Module {

	@EnumValue("no")
	no,

	@EnumValue("contact")
	contact,

	@EnumValue("stock")
	stock,

	@EnumValue("order")
	order,

	@EnumValue("waybill")
	waybill,

	@EnumValue("invoice")
	invoice,

	@EnumValue("cheque")
	cheque,

	@EnumValue("bill")
	bill,

	@EnumValue("safe")
	safe,

	@EnumValue("bank")
	bank,

	@EnumValue("sale")
	sale,

	@EnumValue("global")
	global,

	@EnumValue("admin")
	admin;

	public String key = "enum.module." + name();

	@SuppressWarnings("unchecked")
	public static Map<String, String> reflectionOps(boolean hasContactOption) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, hasContactOption, Module.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();

		options.put(no.name(), Messages.get("nothing"));
		options.put(safe.name(), Messages.get(safe.name()));
		options.put(bank.name(), Messages.get(bank.name()));
		if (hasContactOption) options.put(contact.name(), Messages.get(contact.name()));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

}

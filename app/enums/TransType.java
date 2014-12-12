/**
* Copyright (c) 2015 Mustafa DUMLUPINAR, mdumlupinar@gmail.com
*
* This file is part of seyhan project.
*
* seyhan is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
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

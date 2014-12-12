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
public enum TransStatus {

	@EnumValue("Waiting")
	Waiting,

	@EnumValue("Completed")
	Completed,

	@EnumValue("Cancelled")
	Cancelled;

	public String key = "trans_status." + name();

	public static Map<String, String> options() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, TransStatus.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Waiting.name(), Messages.get("trans_status.Waiting"));
		options.put(Completed.name(), Messages.get("trans_status.Completed"));
		options.put(Cancelled.name(), Messages.get("trans_status.Cancelled"));

		Cache.set(cacheKey, options, CacheUtils.ONE_DAY);

		return options;
	}

	public static Map<String, String> optionsWithoutCancelled() {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.OPTIONS, "without", TransStatus.class.getSimpleName());

		Map<String, String> options = (LinkedHashMap<String, String>) Cache.get(cacheKey);
		if (options != null) return options;

		options = new LinkedHashMap<String, String>();
		options.put(Waiting.name(), Messages.get("trans_status.Waiting"));
		options.put(Completed.name(), Messages.get("trans_status.Completed"));

		return options;
	}

}

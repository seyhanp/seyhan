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
package utils;

import java.util.HashMap;
import java.util.Map;

import models.temporal.Pair;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Cookie;
import enums.CookieKeys;
import enums.Right;

/**
 * @author mdpinar
*/
public class CookieUtils {

	private static Map<String, Map<String, String>> cookieMap;

	public static String get(CookieKeys key) {
		return get(key.value);
	}

	public static String get(String key) {
		return getCookieMap().get(key);
	}

	public static void set(CookieKeys key, String value) {
		set(key.value, value);
	}

	public static void set(String key, String value) {
		getCookieMap().put(key, value);
	}

	public static void set(String userToken, CookieKeys key, String value) {
		getCookieMap(userToken).put(key.value, value);
	}

	public static void remove(CookieKeys key) {
		getCookieMap().remove(key.value);
	}

	public static void setUser(String token) {
		Controller.session().put(CookieKeys.USER_TOKEN.value, token);
	}

	public static String getUserToken() {
		if (Http.Context.current.get() != null) {
			return Controller.session().get(CookieKeys.USER_TOKEN.value);
		}
		return null;
	}

	public static Pair getSortInfo(Right right, String defaultField) {
		return getSortInfo(right, defaultField, "asc");
	}

	public static Pair getSortInfo(Right right, String defaultField, String defaultDirection) {
		Cookie field = Controller.request().cookie("sorting." + right + ".field");
		Cookie direction = Controller.request().cookie("sorting." + right + ".direction");

		if (field != null) {
			if (direction != null && (direction.value().equals("asc") || direction.value().equals("desc"))) {
				return new Pair(field.value(), " " + direction.value());
			}
			Controller.response().setCookie("sorting." + right + ".field", " asc");
			return new Pair(field.value(), " asc");
		} else {
			Controller.response().setCookie("sorting." + right + ".field", defaultField);
			Controller.response().setCookie("sorting." + right + ".direction", defaultDirection);
			return new Pair(defaultField, " " + defaultDirection);
		}
	}

	static {
		cookieMap = new HashMap<String, Map<String, String>>();
	}

	public static void cleanAll() {
		if (Context.current != null) {
			String token = CookieUtils.getUserToken();
			if (token != null) {
				Map<String, String> cacheMap = cookieMap.get(token);
				if (cacheMap != null) {
					for (String key : cacheMap.keySet()) {
						Controller.response().discardCookie(key);
					}
					cookieMap.remove(token);
				}
			}
			Controller.session().remove(CookieKeys.USER_TOKEN.value);
		}
	}

	public static void destroy() {
		cookieMap.clear();
		cookieMap = null;
	}

	private static Map<String, String> getCookieMap() {
		return getCookieMap(CookieUtils.getUserToken());
	}

	private static Map<String, String> getCookieMap(String userToken) {
		Map<String, String> map = null;
		if (Context.current != null) {
			if (userToken != null) {
				map = cookieMap.get(userToken);
			}
		}
		if (map == null) {
			map = new HashMap<String, String>();
			cookieMap.put(CookieUtils.getUserToken(), map);
		}
		return map;
	}
	
}

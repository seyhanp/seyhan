/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.AdminUser;
import models.AdminUserGroup;
import models.AdminWorkspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.Application;
import controllers.admin.Workspaces;
import play.db.ebean.Model;
import enums.CacheKeys;
import enums.Right;

/**
 * @author mdpinar
*/
public class CacheUtils {
	
	public static final String FIELDS = "fields";
	public static final String OPTIONS = "options";
	public static final int ONE_DAY = 60 * 60 * 60 * 24;

	private final static Logger log = LoggerFactory.getLogger(CacheUtils.class);

	private static final AdminUserGroup defaultUserGroup = new AdminUserGroup();

	private static Map<String, AdminUser> loginCache; //birinci anahtar alan kullanicinin auth. token bilgisidir
	private static Map<String, Map<String, Object>> userCache; //birinci anahtar alan kullanicinin auth. token bilgisidir
	private static Map<Integer, Map<String, Object>> globalCache; //birinci anahtar alan kullanicinin workspace id bilgisidir
	
	static {
		loginCache = new HashMap<String, AdminUser>();
		userCache = new HashMap<String, Map<String, Object>>();
		globalCache = new HashMap<Integer, Map<String, Object>>();
	}

	public static <T> T get(CacheKeys key) {
		return get(false, key.value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(boolean isGlobal, String key) {
		T result = null;
		if (isGlobal) {
			if (isLoggedIn()) {
				Integer token = getUser().workspace;
				if (token == null) token = -1;
				Map<String, Object> cache = globalCache.get(token);
				if (cache == null) cache = new HashMap<String, Object>();
				result = (T) cache.get(key);
			}
		} else {
			String token = CookieUtils.getUserToken();
			Map<String, Object> cache = userCache.get(token);
			if (cache == null) cache = new HashMap<String, Object>();
			result = (T) cache.get(key);
		}
		if (result != null) {
			log.debug(" -GET- " + (isGlobal ? "Global" : "User") + " Cache -> (" + key + ") icin deger donuluyor!");
		} else {
			log.debug(" -NON- " + (isGlobal ? "Global" : "User") + " Cache -> (" + key + ") icin deger bulunamadı!");
		}
		return result;
	}

	public static void set(CacheKeys key, Object value) {
		set(false, key.value, value);
	}

	public static void set(String authToken, CacheKeys key, Object value) {
		set(authToken, false, key.value, value);
	}

	public static void set(boolean isGlobal, String key, Object value) {
		set(null, isGlobal, key, value);
	}

	public static void set(String authToken, boolean isGlobal, String key, Object value) {
		if (isGlobal) {
			if (isLoggedIn()) {
				Integer token = getUser().workspace;
				if (token == null) token = -1;
				Map<String, Object> cache = globalCache.get(token);
				if (cache == null) cache = new HashMap<String, Object>();
				cache.put(key, value);
				globalCache.put(token, cache);
			}
		} else {
			String token = (authToken != null ? authToken : CookieUtils.getUserToken());
			Map<String, Object> cache = userCache.get(token);
			if (cache == null) cache = new HashMap<String, Object>();
			cache.put(key, value);
			userCache.put(token, cache);
		}
		log.debug(" -SET- " + (isGlobal ? "Global" : "User") + " Cache -> (" + key + ") icin deger setlendi!");
	}

	public static void remove(boolean isGlobal, String key) {
		if (isGlobal) {
			if (isLoggedIn()) {
				Integer token = getUser().workspace;
				if (token == null) token = -1;
				Map<String, Object> cache = globalCache.get(token);
				if (cache == null) cache = new HashMap<String, Object>();
				cache.remove(key);
			}
		} else {
			String token = CookieUtils.getUserToken();
			Map<String, Object> cache = userCache.get(token);
			if (cache == null) cache = new HashMap<String, Object>();
			cache.remove(key);
		}
		log.debug(" -DEL- " + (isGlobal ? "Global" : "User") + " Cache -> (" + key + ") icin deger silindi!");
	}

	public static void cleanAll() {
		if (loginCache != null) loginCache.remove(CookieUtils.getUserToken());
		userCache = new HashMap<String, Map<String, Object>>();
		Workspaces.destroyMaps();
	}

	public static void destroy() {
		if (loginCache != null) loginCache.clear();
		if (loginCache != null) userCache.clear();
		loginCache = null;
		userCache = null;
		Workspaces.destroyMaps();
	}
	
	/*********************************************************
	 * These methods are for User controls.
	 *********************************************************/

	public static boolean isSuperUser() {
		return (getUser() != null && getUser().id.intValue() == 1);
	}

	public static boolean isAdminUser() {
		return (getUser() != null && getUser().isAdmin);
	}
	
	public static boolean isSpecialUser() {
		return isAdminUser() || isSuperUser();
	}

	public static boolean isLoggedIn() {
		return (getUser() != null);
	}

	public static AdminUser getUser() {
		if (loginCache != null) {
			return loginCache.get(CookieUtils.getUserToken());
		} else {
			return null;
		}
	}

	public static void setUser(AdminUser user) {
		String token = user.createToken();
		loginCache.put(token, user);
		CookieUtils.setUser(token);
	}

	public static AdminUserGroup getUserGroup() {
		AdminUserGroup result = null;
		AdminUser user = getUser();
		if (user != null) {
			result = user.userGroup;
		}
		if (result != null) {
			return result;
		} else {
			return defaultUserGroup;
		}
	}

	public static String getProfile() {
		String profile = getUser().profile;

		if (profile != null) {
			return profile;
		} else {
			return "default";
		}
	}

	public static void setProfile(String profile) {
		AdminUser user = AdminUser.findById(CacheUtils.getUser().id);
		user.profile = profile;
		user.update();
		loginCache.put(user.authToken, user);
	}

	public static void setWorkspace(int workspace) {
		AdminUser user = AdminUser.findById(CacheUtils.getUser().id);
		user.workspace = workspace;
		user.update();
		loginCache.put(user.authToken, user);
	}

	public static Integer getWorkspaceId() {
		AdminUser user = getUser();
		if (user != null) 
			return user.workspace;
		else
			return null;
	}

	public static String getWorkspaceName() {
		Integer id = getWorkspaceId();
		if (id == null) id = getUser().workspace;

		if (id != null) {
			AdminWorkspace ws = AdminWorkspace.findById(id);
			if (ws != null) return ws.name;
		}
		return null;
	}

	public static void removeMenu() {
		remove(false, CacheKeys.MENU.value);
	}

	/*********************************************************
	 * These methods are for Model classes (such as admin tables).
	 *********************************************************/

	public static <T extends Model> void setById(Class<T> clazz, Integer id, T model) {
		set(true, clazz.getSimpleName() + CacheKeys.BY_ID.value + id, model);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Model> T getById(Class<T> clazz, Integer id) {
		return (T) get(true, clazz.getSimpleName() + CacheKeys.BY_ID.value + id);
	}

	public static <T extends Model> void setValue(Class<T> clazz, String key, String value) {
		set(true, clazz.getSimpleName() + CacheKeys.BY_VALUE.value + key, value);
	}

	public static <T extends Model> String getValue(Class<T> clazz, String key) {
		return get(true, clazz.getSimpleName() + CacheKeys.BY_VALUE.value + key);
	}

	public static <T extends Model> void setByKeyValue(Class<T> clazz, String key, String value, T model) {
		set(true, clazz.getSimpleName() + CacheKeys.BY_KEY_VALUE.value + key + ":" + value, model);
	}

	public static <T extends Model> T getByKeyValue(Class<T> clazz, String key, String value) {
		return get(true, clazz.getSimpleName() + CacheKeys.BY_KEY_VALUE.value + key + ":" + value);
	}

	public static <T extends Model> void setListOptions(Class<T> clazz, List<String> nameList) {
		set(true, clazz.getSimpleName() + CacheKeys.OPTIONS.value, nameList);
	}

	public static <T extends Model> List<String> getListOptions(Class<T> clazz) {
		return get(true, clazz.getSimpleName() + CacheKeys.OPTIONS.value);
	}

	public static <T extends Model> void setMapOptions(Class<T> clazz, Map<String, String> kvMap, String addInfo) {
		set(true, clazz.getSimpleName() + CacheKeys.OPTIONS.value + "." + addInfo, kvMap);
	}

	public static <T extends Model> void setMapOptions(Class<T> clazz, Map<String, String> kvMap) {
		set(true, clazz.getSimpleName() + CacheKeys.OPTIONS.value, kvMap);
	}

	public static <T extends Model> Map<String, String> getMapOptions(Class<T> clazz) {
		return get(true, clazz.getSimpleName() + CacheKeys.OPTIONS.value);
	}

	public static <T extends Model> Map<String, String> getMapOptions(Class<T> clazz, String addInfo) {
		return get(true, clazz.getSimpleName() + CacheKeys.OPTIONS.value + "." + addInfo);
	}

	public static <T extends Model> void setListAll(Class<T> clazz, List<T> modelList) {
		set(true, clazz.getSimpleName() + CacheKeys.LIST_ALL.value, modelList);
	}

	public static <T extends Model> List<T> getListAll(Class<T> clazz) {
		return get(true, clazz.getSimpleName() + CacheKeys.LIST_ALL.value);
	}

	public static <T extends Model> void cleanAll(Class<T> clazz, Right right) {
		cleanAll(clazz, right, null);
	}

	public static <T extends Model> void cleanAll(Class<T> clazz, Right right, CacheKeys ckey) {
		if (isLoggedIn()) {
			Integer token = getUser().workspace;
			if (token == null) token = -1;
	
			Map<String, Object> cacheMap = globalCache.get(token);
			if (cacheMap != null) {
				for (CacheKeys key : CacheKeys.getGlobalKeys()) {
					remove(true, clazz.getSimpleName() + key.value);
				}
				Iterator<Entry<String, Object>> it = cacheMap.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object> entry = it.next();
					if (entry.getKey().startsWith(clazz.getSimpleName() + ".")) {
						it.remove();
						log.info(" -DEL- Global Cache -> (" + entry.getKey() + ") icin deger silindi!");
					} if (right != null && entry.getKey().startsWith(right.name())) {
						it.remove();
						log.info(" -DEL- Global Cache -> (" + entry.getKey() + ") icin deger silindi!");
					}
				}				
			}
			if (ckey != null) {
				remove(true, ckey.value);
				remove(false, ckey.value);
			}
		}
	}

	public static String getAppKey(String base, Object arg0) {
		return getAppKey(base, arg0, null, null);
	}

	public static String getAppKey(String base, Object arg0, Object arg1) {
		return getAppKey(base, arg0, arg1, null);
	}

	public static String getAppKey(String base, Object arg0, Object arg1, Object arg2) {
		StringBuilder sb = new StringBuilder(CacheKeys.APP.value + Application.getLang() + "." + base);
		
		if (arg0 != null) {
			sb.append(".");
			sb.append(arg0);
		}
		if (arg1 != null) {
			sb.append(".");
			sb.append(arg1);
		}
		if (arg2 != null) {
			sb.append(".");
			sb.append(arg2);
		}
		
		return sb.toString();
	}

}

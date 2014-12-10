/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package utils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Configuration;
import play.Play;

/**
 * @author mdpinar
*/
public class GlobalCons {

	private final static Logger log = LoggerFactory.getLogger(GlobalCons.class);

	public static String dbVendor;
	public static String langs;
	public static String units;
	public static String defaultLang;
	public static String defaultExcCode;
	public static String TRUE;
	public static String FALSE;

	static {
		Configuration conf = Play.application().configuration();

		dbVendor = conf.getString("db.default.url").split("\\:")[1].toLowerCase();

		langs = conf.getString("seyhan.langs");
		units = conf.getString("seyhan.units");
		defaultLang = conf.getString("seyhan.default.lang");
		defaultExcCode = conf.getString("seyhan.default.currency");

		if (dbVendor.equals("sqlserver")) {
			TRUE = "1";
			FALSE = "0";
		} else {
			TRUE = "true";
			FALSE = "false";
		}
	}

	private static Map<String, String> langMap;
	private static ResourceBundle bundle;

	public static Map<String, String> getLangMap() {
		if (langMap == null) {
			langMap = new LinkedHashMap<String, String>();
			String[] langParts = langs.split("[:,]");

			int i = 0;
			for (int j = 0; j < langParts.length / 2; j++) {
				langMap.put(langParts[i++], langParts[i++]);
			}
		}

		return langMap;
	}

	public static ResourceBundle getMessages() {
		if (bundle != null) {
			return bundle;
		} else {
			FileInputStream fis;
			try {
				fis = new FileInputStream(Play.application().path().getAbsolutePath() + "/conf/messages." + defaultLang);
				return new PropertyResourceBundle(new InputStreamReader(fis, "UTF-8"));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}

		return null;
	}

}

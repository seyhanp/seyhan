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

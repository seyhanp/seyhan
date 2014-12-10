/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

import java.util.EnumSet;


public enum CacheKeys {

	//User keys
	USER("user"),
	MENU("menu"),
	RIGHTS("rights"),
	WORKSPACES("workspaces"),

	//Global keys
	BY_ID(".id."),
	BY_NAME(".name."),
	BY_VALUE(".value."),
	BY_KEY_VALUE(".keyvalue."),
	OPTIONS(".options."),
	
	LIST_ALL(".listAll"),
	ID_MAP(".idmap"),
	NAME_MAP(".namemap"),
	NAME_LIST(".namelist"),
	
	//dahili cache islemleri icin
	APP("app.")
	;

	public String value;

	CacheKeys(String value) {
		this.value = value;
	}

	public static EnumSet<CacheKeys> getUserKeys() {
		return EnumSet.range(USER, WORKSPACES);
	}

	public static EnumSet<CacheKeys> getGlobalKeys() {
		return EnumSet.range(BY_ID, NAME_LIST);
	}

}

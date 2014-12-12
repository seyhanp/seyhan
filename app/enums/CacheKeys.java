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

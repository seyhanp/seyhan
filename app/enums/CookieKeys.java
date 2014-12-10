/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

public enum CookieKeys {

	WS_LIST("ws.list"),
	USER_TOKEN("user.token");

	public String value;

	CookieKeys(String value) {
		this.value = value;
	}

}

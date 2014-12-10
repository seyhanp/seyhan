/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

import com.avaje.ebean.annotation.EnumValue;

public enum UserLogLevel {

	@EnumValue("Login")
	Login,

	@EnumValue("Logout")
	Logout,

	@EnumValue("Insert")
	Insert,

	@EnumValue("Update")
	Update,

	@EnumValue("Delete")
	Delete;

	public String key = "enum." + name();

}

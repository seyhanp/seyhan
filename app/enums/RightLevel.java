/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

import com.avaje.ebean.annotation.EnumValue;

public enum RightLevel {

	@EnumValue("Disable")
	Disable,

	@EnumValue("Enable")
	Enable,

	@EnumValue("Insert")
	Insert,

	@EnumValue("Update")
	Update,

	@EnumValue("Delete")
	Delete;

	public String key = "enum." + name();

	public static RightLevel findLevel(String name) {
		if (name != null) {
			return valueOf(name);
		}

		return Disable;
	}

}

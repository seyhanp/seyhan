/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

import com.avaje.ebean.annotation.EnumValue;

public enum ChqbllSort {

	@EnumValue("Cheque")
	Cheque,

	@EnumValue("Bill")
	Bill;

	public String key = "enum.cqbl." + name();

	public static ChqbllSort find(String name) {
		try {
			return valueOf(name);
		} catch (Exception e) {
			return Cheque;
		}
	}

}

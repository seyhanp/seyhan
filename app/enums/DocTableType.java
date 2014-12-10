/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

import com.avaje.ebean.annotation.EnumValue;

public enum DocTableType {

	@EnumValue("NONE")
	NONE,
	
	@EnumValue("TAX_1")
	TAX_1,

	@EnumValue("EXCHANGE_1")
	EXCHANGE_1,

	@EnumValue("CURRENCY_1")
	CURRENCY_1,

	@EnumValue("FACTOR_1")
	FACTOR_1;

}

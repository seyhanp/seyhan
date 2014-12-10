/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package enums;

import com.avaje.ebean.annotation.EnumValue;

public enum FieldType {

	@EnumValue("STRING")
	STRING,

	@EnumValue("DATE")
	DATE,
	
	@EnumValue("LONGDATE")
	LONGDATE,

	@EnumValue("INTEGER")
	INTEGER,
	
	@EnumValue("LONG")
	LONG,

	@EnumValue("DOUBLE")
	DOUBLE,

	@EnumValue("RATE")
	RATE,
	
	@EnumValue("TAX")
	TAX,
	
	@EnumValue("CURRENCY")
	CURRENCY,

	@EnumValue("BOOLEAN")
	BOOLEAN,

	@EnumValue("MESSAGE")
	MESSAGE,

	@EnumValue("SYS_DATE")
	SYS_DATE,

	@EnumValue("SYS_TIME")
	SYS_TIME,

	@EnumValue("SYS_DATE_FULL")
	SYS_DATE_FULL,

	@EnumValue("PAGE_NUMBER")
	PAGE_NUMBER,
	
	@EnumValue("PAGE_COUNT")
	PAGE_COUNT,

	@EnumValue("ROW_NO")
	ROW_NO,

	@EnumValue("LINE")
	LINE,

	@EnumValue("STATIC_TEXT")
	STATIC_TEXT,

	@EnumValue("NUMBER_TO_TEXT")
	NUMBER_TO_TEXT,

	@EnumValue("DEBT_SUM")
	DEBT_SUM,

	@EnumValue("CREDIT_SUM")
	CREDIT_SUM,

	@EnumValue("BALANCE")
	BALANCE,

	@EnumValue("SUM_OF")
	SUM_OF,

	@EnumValue("REF_NO")
	REF_NO,

	@EnumValue("REF_NAME")
	REF_NAME,

	@EnumValue("REF_AMOUNT")
	REF_AMOUNT,

	@EnumValue("REF_CURRENCY")
	REF_CURRENCY,

	@EnumValue("TABLE")
	TABLE;

}

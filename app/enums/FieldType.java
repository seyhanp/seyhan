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

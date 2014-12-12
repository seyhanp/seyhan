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
package documents;

import play.i18n.Messages;
import enums.DocTableType;
import enums.FieldType;
import enums.Module;

/**
 * @author mdpinar
*/
public class Field {
	
	public Module module;

	/*
	 * verinin turu (ozellikle format kisminda kullanilmak uzere)
	 */
	public FieldType type = FieldType.STRING;

	/*
	 * veritabani alani ise sql sorgularinda kullanilacak
	 * degilse value olarak kullanilacak
	 */
	public String name;

	public String nickName;

	public String hiddenField;

	/*
	 * Kullanicinin gorecegi etiket 
	 * coklu dil destegi icin dogrudan degil de messages key kullanilacak 
	 */
	public String labelKey;

	public int width;
	public int row;
	public int column;
	public String format;
	public String prefix;
	public String suffix;

	/*
	 * Mesaj dosyasindan okunacak bir deger ise, on eki
	 */
	public String msgPrefix = "";
	
	/*
	 * Deger null oldugunda kullanilacak
	 */
	public String defauld = "";

	/*
	 * veritabani alani degilse bu deger kullanilacak
	 */
	public String value;

	public boolean isDbField = true;

	public DocTableType tableType = DocTableType.NONE;

	/**
	 * LINE, STATIC_TEXT
	 */
	public Field(FieldType type, String value) {
		this(type, 30, value, null);
	}

	/**
	 * SYSTEM FIELDS (DATE, TIME...)
	 */
	public Field(FieldType type, int width, String format) {
		this(type, width, null, format);
	}

	/**
	 * DB FIELDS
	 */
	public Field(String name, String labelKey, int width) {
		this(name, labelKey, width, FieldType.STRING);
	}
	
	/**
	 * DB FIELDS FROM MESSAGE
	 */
	public Field(String name, String labelKey, int width, String msgPrefix) {
		this(name, labelKey, width, FieldType.MESSAGE);
		this.msgPrefix = msgPrefix;
	}
	
	/**
	 * SUMMARY INFO (SUM OF DEBT, CREDIT AND BALANCE)
	 */
	public Field(FieldType type, int width, String labelKey, Module module, String hiddenField) {
		this(type.name(), labelKey, width, type);
		this.module = module;
		this.hiddenField = hiddenField;
		this.isDbField = false;
		switch (type) {
			case BALANCE: {
				this.format = Messages.get("formats.balance");
				break;
			}
			default : {
				this.format = Messages.get("formats.currency");
			}
		}
	}
	
	/**
	 * TABLES (TAX, CURRENCY...) 
	 */
	public Field(DocTableType tableType, String labelKey, int width) {
		this(tableType.name(), labelKey, width, FieldType.TABLE);
		this.tableType = tableType;
		this.isDbField = false;
	}
	
	/**
	 * NUMBER_TO_TEXT
	 */
	public Field(FieldType type, String labelKey, int width, String name) {
		this.type = type;
		this.labelKey = labelKey;
		this.name = name;
		this.width = width;
		this.isDbField = true;
		this.nickName = name.replaceAll("\\.", "\\_");
		this.hiddenField = name.replaceAll("\\.", "\\_");
	}

	/**
	 * FOR THE OTHER CONSTRUCTORS THAT ARE ABOVE
	 * AND REFLECTION ACCOUNTS
	 */
	public Field(FieldType type, int width, String value, String format) {
		super();
		this.type = type;
		this.isDbField = false;
		this.name = type.name();
		this.width = width;
		this.format = format;
		if (type.name().startsWith("REF_")) {
			this.labelKey = value;
		} else {
			this.labelKey = type.name();
			this.value = value;
		}
	}
	
	/**
	 * FOR THE FORMULATED DB FILEDS 
	 */
	public Field(String name, String nickName, String labelKey, int width, FieldType type) {
		this(name, labelKey, width, type);
		this.nickName = nickName;
	}

	/**
	 * FOR THE OTHER CONSTRUCTORS THAT ARE ABOVE, AND NON STRING DB FILEDS 
	 */
	public Field(String name, String labelKey, int width, FieldType type) {
		this.name = name;
		this.type = type;
		this.labelKey = labelKey;
		this.width = width;
		this.isDbField = true;
		this.nickName = name.replaceAll("\\.", "\\_");

		switch (type) {
			case LONG:
			case ROW_NO:
			case PAGE_NUMBER:
			case PAGE_COUNT: {
				this.format = Messages.get("formats.integer");
				break;
			}
			case BOOLEAN: {
				this.defauld = Messages.get("no").toUpperCase();
				break;
			}
			case DATE:
			case SYS_DATE: {
				this.format = Messages.get("formats.date");
				break;
			}
			case SYS_TIME: {
				this.format = Messages.get("formats.time");
				break;
			}
			case LONGDATE:
			case SYS_DATE_FULL: {
				this.format = Messages.get("formats.longdate");
				break;
			}
			case TAX:
			case RATE: {
				this.format = Messages.get("formats." + type.name().toLowerCase());
				break;
			}
			case SUM_OF:
			case DOUBLE:
			case CURRENCY:
			case DEBT_SUM:
			case CREDIT_SUM:
			case BALANCE: {
				this.defauld = "0";
				this.format = Messages.get("formats." + (FieldType.DOUBLE.equals(type) ? "double" : "currency"));
				break;
			}
		}
	}
	
}

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

import java.util.ArrayList;
import java.util.List;

import play.cache.Cache;
import play.i18n.Messages;
import utils.CacheUtils;
import enums.FieldType;

@SuppressWarnings("unchecked")
/**
 * @author mdpinar
*/
public class BaseDocTransFields {
	
	private static List<Field> refFieldList;
	
	static {
		refFieldList = new ArrayList<Field>();
		refFieldList.add(new Field(FieldType.REF_NO, 20, "ref.no", null));
		refFieldList.add(new Field(FieldType.REF_NAME, 30, "ref.name", null));
		refFieldList.add(new Field(FieldType.REF_AMOUNT, 13, "ref.amount", Messages.get("formats.currency")));
		refFieldList.add(new Field(FieldType.REF_CURRENCY, 3, "ref.currency", null));
	}

	public static List<Field> getFields(String tableName, boolean hasDebtAndCredit) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, tableName, hasDebtAndCredit);

		List<Field> fieldList = (List<Field>) Cache.get(cacheKey);
		if (fieldList != null) {
			return fieldList;
		} else {
			fieldList = new ArrayList<Field>();
		}

		fieldList.add(new Field(tableName + ".receipt_no", "receipt_no", 6, FieldType.INTEGER));
		fieldList.add(new Field(tableName + "._right", "transaction", 50, "enum."));
		fieldList.add(new Field(tableName + ".trans_date", "date", 10, FieldType.DATE));
		fieldList.add(new Field(tableName + ".trans_no", "trans.no", 20));
		fieldList.add(new Field(tableName + ".trans_type", "trans.type", 6, "enum."));
		if (hasDebtAndCredit) {
			fieldList.add(new Field(tableName + ".amount", "amount", 13, FieldType.CURRENCY));
			fieldList.add(new Field(tableName + ".debt", "debt", 13, FieldType.CURRENCY));
			fieldList.add(new Field(tableName + ".credit", "credit", 13, FieldType.CURRENCY));
			fieldList.add(new Field(FieldType.NUMBER_TO_TEXT, "with_writing/amount", 80, tableName + ".amount"));
		}
		fieldList.add(new Field(tableName + ".exc_code", "currency", 3));
		fieldList.add(new Field(tableName + ".exc_rate", "exchange_rate", 6, FieldType.RATE));
		fieldList.add(new Field(tableName + ".exc_equivalent", "exc_equivalent", 13, FieldType.CURRENCY));
		fieldList.add(new Field(tableName + ".trans_year", "trans.year", 4, FieldType.INTEGER));
		fieldList.add(new Field(tableName + ".trans_month", "trans.month", 7));
		fieldList.add(new Field(tableName + ".insert_by", "insert_by", 20));
		fieldList.add(new Field(tableName + ".insert_at", "insert_at", 20, FieldType.DATE));
		fieldList.add(new Field(tableName + ".description", "description", 100));

		Cache.set(cacheKey, fieldList, CacheUtils.ONE_DAY);

		return fieldList;
	}

	public static List<Field> getRefFields() {
		return refFieldList;
	}

}

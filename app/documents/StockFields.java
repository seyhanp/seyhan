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

import models.AdminExtraFields;
import play.cache.Cache;
import utils.CacheUtils;
import enums.FieldType;
import enums.Module;

@SuppressWarnings("unchecked")
/**
 * @author mdpinar
*/
public class StockFields {

	public static List<Field> getFields(String tableName) {
		final String cacheKey = CacheUtils.getAppKey(CacheUtils.FIELDS, tableName);

		List<Field> fieldList = (List<Field>) Cache.get(cacheKey);
		if (fieldList != null) {
			return fieldList;
		} else {
			fieldList = new ArrayList<Field>();
		}

		fieldList.add(new Field("stock.code", "code", 30));
		fieldList.add(new Field((tableName != null ? tableName : "stock") + ".name", "name", 100));
		fieldList.add(new Field("stock.unit1", "stock.unit/1", 6));
		fieldList.add(new Field("stock.unit2", "stock.unit/2", 6));
		fieldList.add(new Field("stock.unit3", "stock.unit/3", 6));
		fieldList.add(new Field("stock.buy_price", "buy_price", 13, FieldType.CURRENCY));
		fieldList.add(new Field("stock.sell_price", "sell_price", 13, FieldType.CURRENCY));
		fieldList.add(new Field("stock.provider_code", "provider_code", 30));
		fieldList.add(new Field("stock.exc_code", "currency", 3));
		fieldList.add(new Field("stock.buy_tax", "buy_tax", 3, FieldType.TAX));
		fieldList.add(new Field("stock.sell_tax", "sell_tax", 3, FieldType.TAX));
		fieldList.add(new Field("stock.tax_rate2", "vof.taxRate2", 3, FieldType.TAX));
		fieldList.add(new Field("stock.tax_rate3", "vof.taxRate3", 3, FieldType.TAX));

		fieldList.add(new Field("stock_category.name", "category", 30));

		List<AdminExtraFields> extraFieldList = AdminExtraFields.listAll(Module.stock.name());
		if (extraFieldList != null && extraFieldList.size() > 0) {
			for (AdminExtraFields ef : extraFieldList) {
				fieldList.add(new Field("stock_extra_fields.name", ef.name, 30));
			}
		}

		Cache.set(cacheKey, fieldList, CacheUtils.ONE_DAY);
		
		return fieldList;
	}

}

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

import enums.DocTableType;
import enums.Module;

/**
 * @author mdpinar
*/
public class TablesFields {

	public static List<Field> getFields(Module module) {
		List<Field> fieldList = new ArrayList<Field>();

		fieldList.add(new Field(DocTableType.EXCHANGE_1, "tableof.exchange1", 30));
		switch (module) {
			case order:
			case waybill:{
				fieldList.add(new Field(DocTableType.FACTOR_1, "tableof.factor1", 30));
				break;
			}
			case stock:
			case invoice: {
				fieldList.add(new Field(DocTableType.TAX_1, "tableof.tax1", 30));
				fieldList.add(new Field(DocTableType.CURRENCY_1, "tableof.currency1", 30));
				fieldList.add(new Field(DocTableType.FACTOR_1, "tableof.factor1", 30));
				break;
			}
		}

		return fieldList;
	}

}

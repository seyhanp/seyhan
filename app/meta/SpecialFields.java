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
package meta;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mdpinar
*/
public class SpecialFields {

	public static Map<String, Boolean> stock;
	public static Map<String, Boolean> invoice;

	static {
		stock = new LinkedHashMap<String, Boolean>();
		stock.put("depot", Boolean.FALSE);
		stock.put("serialNo", Boolean.FALSE);
		stock.put("taxRate2", Boolean.FALSE);
		stock.put("taxRate3", Boolean.FALSE);

		invoice = new LinkedHashMap<String, Boolean>();
		invoice.put("serialNo", Boolean.FALSE);
		invoice.put("taxRate2", Boolean.FALSE);
		invoice.put("taxRate3", Boolean.FALSE);
	}

}

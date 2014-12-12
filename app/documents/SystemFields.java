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

import enums.FieldType;

/**
 * @author mdpinar
*/
public class SystemFields {

	public static List<Field> getFields() {
		List<Field> fieldList = new ArrayList<Field>();

		fieldList.add(new Field(FieldType.SYS_DATE, 10, "dd/MM/yyyy"));
		fieldList.add(new Field(FieldType.SYS_TIME, 8, "HH:mm:ss"));
		fieldList.add(new Field(FieldType.SYS_DATE_FULL, 19, "dd/MM/yyyy HH:mm:ss"));
		fieldList.add(new Field(FieldType.PAGE_NUMBER, 2, "###"));
		fieldList.add(new Field(FieldType.PAGE_COUNT, 3, "###"));

		return fieldList;
	}

}

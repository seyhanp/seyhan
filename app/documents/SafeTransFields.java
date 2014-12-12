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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import enums.Module;
import utils.TemplateHelper;

/**
 * @author mdpinar
*/
public class SafeTransFields {

	private static Map<String, String> fieldMap;

	static {
		List<Field> fieldList = new ArrayList<Field>();
		fieldList.addAll(BaseDocTransFields.getFields("safe_trans", true));
		fieldList.add(new Field("safe_expense.name", "expense", 30));
		fieldList.add(new Field("safe_trans_source.name", "trans.source", 30));

		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(TemplateHelper.buildOptions("safe", SafeFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("action", fieldList));
		fieldMap.putAll(TemplateHelper.buildOptions("ref.account", BaseDocTransFields.getRefFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("tables", TablesFields.getFields(Module.safe)));
		fieldMap.putAll(TemplateHelper.buildOptions("constant", ConstantFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("system", SystemFields.getFields()));
	}

	public static Map<String, String> getOptions() {
		return fieldMap;
	}

}

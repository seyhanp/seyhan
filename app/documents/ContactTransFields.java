/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package documents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import utils.TemplateHelper;
import enums.FieldType;
import enums.Module;

/**
 * @author mdpinar
*/
public class ContactTransFields {

	private static Map<String, String> fieldMap;

	static {
		List<Field> fieldList = new ArrayList<Field>();
		fieldList.addAll(BaseDocTransFields.getFields("contact_trans", true));
		fieldList.add(new Field("contact_trans.maturity", "maturity", 10, FieldType.DATE));
		fieldList.add(new Field("contact_trans_source.name", "trans.source", 30));
		fieldList.add(new Field("global_trans_point.name", "trans.point", 30));
		fieldList.add(new Field("global_private_code.name", "private_code", 30));

		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(TemplateHelper.buildOptions("contact", ContactFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("action", fieldList));
		fieldMap.putAll(TemplateHelper.buildOptions("ref.account", BaseDocTransFields.getRefFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("tables", TablesFields.getFields(Module.contact)));
		fieldMap.putAll(TemplateHelper.buildOptions("constant", ConstantFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("system", SystemFields.getFields()));
	}

	public static Map<String, String> getOptions() {
		return fieldMap;
	}
	
}

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
public class BankTransFields {

	private static Map<String, String> fieldMap;

	static {
		List<Field> fieldList = new ArrayList<Field>();
		fieldList.addAll(BaseDocTransFields.getFields("bank_trans", true));
		fieldList.add(new Field("bank_expense.name", "expense", 30));
		fieldList.add(new Field("bank_trans.expense_amount", "expense", 13, FieldType.CURRENCY));
		fieldList.add(new Field("bank_trans_source.name", "trans.source", 30));

		fieldMap = new LinkedHashMap<String, String>();

		fieldMap.putAll(TemplateHelper.buildOptions("bank", BankFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("action", fieldList));
		fieldMap.putAll(TemplateHelper.buildOptions("ref.account", BaseDocTransFields.getRefFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("tables", TablesFields.getFields(Module.bank)));
		fieldMap.putAll(TemplateHelper.buildOptions("constant", ConstantFields.getFields()));
		fieldMap.putAll(TemplateHelper.buildOptions("system", SystemFields.getFields()));
	}

	public static Map<String, String> getOptions() {
		return fieldMap;
	}

}

/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package documents;

import java.util.ArrayList;
import java.util.List;

import enums.FieldType;
import enums.Module;

/**
 * @author mdpinar
*/
public class SafeFields {

	private static List<Field> fieldList;

	static {
		fieldList = new ArrayList<Field>();
		
		fieldList.add(new Field("safe.name", "safe.name", 50));
		fieldList.add(new Field("safe.responsible", "responsible", 30));

		fieldList.add(new Field(FieldType.DEBT_SUM, 13, "debt.sum", Module.safe, "Debt"));
		fieldList.add(new Field(FieldType.CREDIT_SUM, 13, "credit.sum", Module.safe, "Credit"));
		fieldList.add(new Field(FieldType.BALANCE, 13, "balance", Module.safe, null));
	}

	public static List<Field> getFields() {
		return fieldList;
	}

}

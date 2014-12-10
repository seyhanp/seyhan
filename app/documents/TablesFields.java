/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
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

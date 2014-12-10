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
public class BankFields {
	
	private static List<Field> fieldList;
	
	static {
		fieldList = new ArrayList<Field>();

		fieldList.add(new Field("bank.account_no", "account.no", 15));
		fieldList.add(new Field("bank.name", "bank.name", 100));
		fieldList.add(new Field("bank.iban", "IBAN", 26));
		fieldList.add(new Field("bank.branch", "branch", 30));
		fieldList.add(new Field("bank.city", "city", 15));

		fieldList.add(new Field(FieldType.DEBT_SUM, 13, "debt.sum", Module.bank, "Debt"));
		fieldList.add(new Field(FieldType.CREDIT_SUM, 13, "credit.sum", Module.bank, "Credit"));
		fieldList.add(new Field(FieldType.BALANCE, 13, "balance", Module.bank, null));
	}

	public static List<Field> getFields() {
		return fieldList;
	}

}

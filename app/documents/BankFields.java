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

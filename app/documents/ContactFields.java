/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package documents;

import java.util.ArrayList;
import java.util.List;

import models.AdminExtraFields;
import enums.FieldType;
import enums.Module;

/**
 * @author mdpinar
*/
public class ContactFields {
	
	private static List<Field> fieldList;
	
	static {
		fieldList = new ArrayList<Field>();

		fieldList.add(new Field("contact.code", "contact.code", 30));
		fieldList.add(new Field("contact.name", "name", 100));
		fieldList.add(new Field("contact.tax_office", "contact.tax.office", 20));
		fieldList.add(new Field("contact.tax_number", "contact.tax.no", 15));
		fieldList.add(new Field("contact.tc_kimlik", "TC Kimlik No", 11));
		fieldList.add(new Field("contact.address1", "address/1", 100));
		fieldList.add(new Field("contact.address2", "address/2", 100));
		fieldList.add(new Field("contact.city", "city", 20));
		fieldList.add(new Field("contact.country", "country", 20));
		fieldList.add(new Field("contact.phone", "phone", 15));
		fieldList.add(new Field("contact.fax", "Fax", 15));
		fieldList.add(new Field("contact.mobile_phone", "mobile_phone", 15));
		fieldList.add(new Field("contact.email", "Email", 100));
		fieldList.add(new Field("contact.website", "contact.website", 100));
		fieldList.add(new Field("contact.status", "status", 12));
		fieldList.add(new Field("contact.relevant", "contact.relevant", 30));

		fieldList.add(new Field("contact_category.name", "category", 30));
		fieldList.add(new Field("sale_seller.name", "seller", 30));

		List<AdminExtraFields> extraFieldList = AdminExtraFields.listAll(Module.contact.name());
		if (extraFieldList != null && extraFieldList.size() > 0) {
			for (AdminExtraFields ef : extraFieldList) {
				fieldList.add(new Field("contact_extra_fields.name", ef.name, 30));
			}
		}

		fieldList.add(new Field(FieldType.DEBT_SUM, 13, "debt.sum", Module.contact, "Debt"));
		fieldList.add(new Field(FieldType.CREDIT_SUM, 13, "credit.sum", Module.contact, "Credit"));
		fieldList.add(new Field(FieldType.BALANCE, 13, "balance", Module.contact, null));
	}

	public static List<Field> getFields() {
		return fieldList;
	}

}

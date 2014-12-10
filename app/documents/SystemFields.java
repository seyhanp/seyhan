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

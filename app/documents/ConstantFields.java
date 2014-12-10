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
public class ConstantFields {

	public static List<Field> getFields() {
		List<Field> fieldList = new ArrayList<Field>();

		fieldList.add(new Field(FieldType.LINE, "-"));
		fieldList.add(new Field(FieldType.STATIC_TEXT, ""));

		return fieldList;
	}

}

/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package meta;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mdpinar
*/
public class SpecialFields {

	public static Map<String, Boolean> stock;
	public static Map<String, Boolean> invoice;

	static {
		stock = new LinkedHashMap<String, Boolean>();
		stock.put("serialNo", Boolean.FALSE);
		stock.put("taxRate2", Boolean.FALSE);
		stock.put("taxRate3", Boolean.FALSE);

		invoice = new LinkedHashMap<String, Boolean>();
		invoice.put("serialNo", Boolean.FALSE);
		invoice.put("taxRate2", Boolean.FALSE);
		invoice.put("taxRate3", Boolean.FALSE);
	}

}

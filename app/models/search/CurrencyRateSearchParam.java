/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.search;

import java.util.Date;

import play.data.format.Formats.DateTime;

/**
 * @author mdpinar
*/
public class CurrencyRateSearchParam extends AbstractSearchParam {

	public String excCode;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date date;

}

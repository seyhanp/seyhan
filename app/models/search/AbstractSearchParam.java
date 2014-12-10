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
public class AbstractSearchParam {

	public Integer pageIndex = 0;
	public Boolean showStatus = Boolean.FALSE;

	public String fullText;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date startDate;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date endDate;

}

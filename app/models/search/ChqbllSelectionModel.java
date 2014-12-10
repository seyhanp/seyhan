/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.search;

import java.util.Date;

import models.Contact;
import play.data.format.Formats.DateTime;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.Right;

/**
 * @author mdpinar
*/
public class ChqbllSelectionModel {

	public Right selRight;
	public ChqbllSort selSort;
	public ChqbllStep step;
	public Contact refContact;
	public Integer selPortfolioNo;
	public String selSerialNo;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date startDate;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date endDate;

	public String alreadySelected;

	public ChqbllSelectionModel() {
		;
	}

	public ChqbllSelectionModel(ChqbllSort sort, Right right, ChqbllStep step, String alreadySelected) {
		super();
		this.selRight = right;
		this.selSort = sort;
		this.step = step;
		this.alreadySelected = alreadySelected;
	}

}

/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import java.util.ArrayList;
import java.util.List;

import models.ChqbllDetailPartial;
import enums.ChqbllSort;

/**
 * @author mdpinar
*/
public class ChqbllPartialList {

	public Integer detailId;

	public ChqbllSort sort; 
	public Boolean isCustomer;
	public String contactName;
	public Integer portfolioNo; 
	public String serialNo;
	public String dueDate;
	public Double amount;
	public String excCode;
	public String cbtype;
	public String owner;
	public String bankName;
	public String surety;
	public String paymentPlace;
	public String description;

	public Double paid = 0d;
	public Double remaining = 0d;

	public List<ChqbllDetailPartial> details = new ArrayList<ChqbllDetailPartial>();

}

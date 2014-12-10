/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import java.util.Date;

import enums.Right;

/**
 * @author mdpinar
*/
public class InvTrans {

	public Integer id;
	public Right right;

	public Double debt;
	public Double credit;

	public String link;

	/**
	 * For stock
	 */
	public String title;
	public Date date;
	public Double quantity;
	public Double price;
	public String excCode;

	public String depot;
	public String transType;

}

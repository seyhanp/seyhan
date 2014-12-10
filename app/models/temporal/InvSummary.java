/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

/**
 * @author mdpinar
*/
public class InvSummary {

	public String title;
	public Boolean isImportant = Boolean.FALSE;

	public Double debt;
	public Double credit;
	public Double balance;

	/**
	 * For stocks
	 */
	public Double netInput;
	public Double netInTotal;

	public Double netOutput;
	public Double netOutTotal;

	public Double retInput;
	public Double retInTotal;

	public Double retOutput;
	public Double retOutTotal;

}

/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
/**
 * @author mdpinar
*/
public class InvoiceTransCurrency extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@ManyToOne
	public InvoiceTrans trans;

	public String currency;
	public Double amount = 0d;

}

/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints;
import enums.TransStatus;

@Entity
/**
 * @author mdpinar
*/
public class InvoiceTransDetail extends AbstractStockTransDetail {

	private static final long serialVersionUID = 1L;

	public TransStatus status = TransStatus.Waiting;

	@ManyToOne
	public InvoiceTrans trans;

	@ManyToOne
	public InvoiceTransSource transSource;

	public Double taxRate2;
	public Double taxRate3;

	@Constraints.MaxLength(100)
	public String serialNo;

	public Boolean isReturn = Boolean.FALSE;

	public Double retInput = 0d;
	public Double retOutput = 0d;
	public Double retInTotal = 0d;
	public Double retOutTotal = 0d;

	public Boolean hasCostEffect = Boolean.TRUE;

}

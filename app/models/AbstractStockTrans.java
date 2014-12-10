/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.Date;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import play.data.format.Formats.DateTime;
import controllers.global.Profiles;

@MappedSuperclass
/**
 * @author mdpinar
*/
public abstract class AbstractStockTrans extends AbstractBaseTrans {

	private static final long serialVersionUID = 1L;

	@DateTime(pattern = "dd/MM/yyyy HH:mm:ss")
	public Date realDate = new Date();

	@DateTime(pattern = "dd/MM/yyyy HH:mm:ss")
	public Date deliveryDate;

	@ManyToOne
	public Contact contact;

	public boolean isTaxInclude;

	@ManyToOne
	public StockDepot depot;

	public String contactName;
	public String contactTaxOffice;
	public String contactTaxNumber;
	public String contactAddress1;
	public String contactAddress2;
	public String consigner;
	public String recepient;
	public Integer roundingDigits = Profiles.chosen().stok_roundingDigits;
	public Double totalDiscountRate = 0d;

	public Double total = 0d;
	public Double roundingDiscount = 0d;
	public Double discountTotal = 0d;
	public Double subtotal = 0d;
	public Double plusFactorTotal = 0d;
	public Double minusFactorTotal = 0d;
	public Double taxTotal = 0d;
	public Double netTotal = 0d;

	@ManyToOne
	public SaleSeller seller;

	public AbstractStockTrans() {
		this.depot = Profiles.chosen().stok_depot;
		this.isTaxInclude = Profiles.chosen().stok_isTaxInclude;
	}

}

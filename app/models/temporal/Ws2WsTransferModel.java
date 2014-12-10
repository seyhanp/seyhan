/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import java.util.Date;

import javax.persistence.ManyToMany;

import models.AdminWorkspace;
import models.StockCosting;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;

/**
 * @author mdpinar
*/
public class Ws2WsTransferModel {

	@ManyToMany
	@Constraints.Required
	public AdminWorkspace sourceWS;

	@ManyToMany
	@Constraints.Required
	public AdminWorkspace targetWS;
	
	@Constraints.Required
	@DateTime(pattern = "dd/MM/yyyy")
	public Date transDate = new Date();

	public String description;

	@ManyToMany
	public StockCosting costing;

	public boolean stockInfo;
	public boolean stockTrans;

	public boolean contactInfo;
	public boolean contactTrans;

	public boolean safeInfo;
	public boolean safeTrans;

	public boolean bankInfo;
	public boolean bankTrans;

	public boolean orderInfo;
	public boolean orderTrans;

	public boolean waybillInfo;
	public boolean waybillTrans;
	
	public boolean invoiceInfo;

	public boolean chqbllInfo;
	public boolean chqbllTrans;

	public boolean saleInfo;
	public boolean globalInfo;

	public Ws2WsTransferModel() {
		this.stockInfo = true;
		this.stockTrans = true;
		this.contactInfo = true;
		this.contactTrans = true;
		this.safeInfo = true;
		this.safeTrans = true;
		this.bankInfo = true;
		this.bankTrans = true;
		this.orderInfo = true;
		this.waybillInfo = true;
		this.invoiceInfo = true;
		this.chqbllInfo = true;
		this.chqbllTrans = true;
		this.saleInfo = true;
		this.globalInfo = true;
	}

}

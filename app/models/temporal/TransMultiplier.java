/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import java.util.Date;

import javax.persistence.ManyToOne;

import models.Bank;
import models.Contact;
import models.Safe;
import models.SaleSeller;
import models.StockDepot;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import enums.Right;

/**
 * @author mdpinar
*/
public class TransMultiplier {

	@Constraints.Required
	public Integer id;

	public Contact contact;
	public Safe safe;
	public Bank bank;

	public Right right;

	@Constraints.Required
	@DateTime(pattern = "dd/MM/yyyy")
	public Date transDate = new Date();

	@Constraints.MaxLength(20)
	public String transNo;

	@Constraints.MaxLength(100)
	public String description;

	/**
	 * For Stock Transactions
	 */
	@ManyToOne
	public StockDepot depot;

	@ManyToOne
	public StockDepot refDepot; //transfer depot

	@ManyToOne
	public SaleSeller seller;

}

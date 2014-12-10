/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.search;

import java.util.Date;

import models.Contact;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.SaleSeller;
import models.StockTransSource;
import play.data.format.Formats.DateTime;

/**
 * @author mdpinar
*/
public class StockTransSearchParam extends AbstractSearchParam {

	public Integer receiptNo;
	public String transNo;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date deliveryDate;

	public Contact refContact;
	public SaleSeller seller;
	public StockTransSource transSource;
	public GlobalTransPoint transPoint;
	public GlobalPrivateCode privateCode;

}

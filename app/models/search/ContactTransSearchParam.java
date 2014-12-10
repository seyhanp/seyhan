/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.search;

import java.util.Date;

import models.Contact;
import models.ContactTransSource;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import play.data.format.Formats.DateTime;

/**
 * @author mdpinar
*/
public class ContactTransSearchParam extends AbstractSearchParam {

	public Integer receiptNo;
	public String transNo;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date maturity;

	public Contact refContact;
	public ContactTransSource transSource;
	public GlobalTransPoint transPoint;
	public GlobalPrivateCode privateCode;

}

/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.search;

import models.Bank;
import models.Contact;
import models.ContactTransSource;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.Safe;
import play.data.validation.Constraints;
import enums.ChqbllSort;
import enums.ChqbllStep;

/**
 * @author mdpinar
*/
public class ChqbllTransSearchParam extends AbstractSearchParam {

	/*
	 * For Only I/O Payrolls
	 */
	public Contact contact;

	/*
	 * For Only Trans Payrolls
	 */
	public ChqbllStep toStep;
	public Bank bank;
	public Safe safe;

	/*
	 * Shared fields
	 */
	@Constraints.Required
	public ChqbllSort sort = ChqbllSort.Cheque;

	public Integer receiptNo;
	public String transNo;

	public ContactTransSource transSource;
	public GlobalTransPoint transPoint;
	public GlobalPrivateCode privateCode;

}

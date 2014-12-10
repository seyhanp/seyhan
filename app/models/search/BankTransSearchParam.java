/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.search;

import models.Bank;
import models.BankTransSource;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;

/**
 * @author mdpinar
*/
public class BankTransSearchParam extends AbstractSearchParam {

	public Integer receiptNo;
	public String transNo;

	public Bank bank;
	public BankTransSource transSource;
	public GlobalTransPoint transPoint;
	public GlobalPrivateCode privateCode;

}

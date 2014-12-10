/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models.temporal;

import enums.Right;
import enums.TransStatus;

/**
 * @author mdpinar
*/
public class ReceiptListModel {

	public Integer id;
	public TransStatus status;
	public Right right;
	public Boolean isSelected = Boolean.FALSE;
	public Integer receiptNo;
	public Integer contactId;
	public String contactName;
	public String date;
	public String transNo;
	public String amount;
	public String excCode;
	public String description;

}

/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package data.transfer.ws2ws;

import java.util.Date;

import enums.Module;

public interface ITransfer {

	void transferInfo(int sourceWS, int targetWS);
	void transferTransaction(Date transDate, String description, int sourceWS, int targetWS);
	
	void destroyInfo(int targetWS);
	void destroyTransaction(int targetWS, boolean willBeDestroyedAll);
	
	Module getModule();

}

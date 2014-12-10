/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package data.transfer.ws2ws;

import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import enums.Module;
/**
 * @author mdpinar
*/
class GlobalTransfer extends BaseTransfer {

	@Override
	public void transferInfo(int sourceWS, int targetWS) {
		executeInsertQueryForInfoTables(new GlobalPrivateCode(), sourceWS, targetWS, false);
		executeInsertQueryForInfoTables(new GlobalTransPoint(), sourceWS, targetWS, false);
	}

	@Override
	public void destroyInfo(int targetWS) {
		executeDeleteQueryForInfoTables("global_private_code", targetWS);
		executeDeleteQueryForInfoTables("global_trans_point", targetWS);
	}

	@Override
	public Module getModule() {
		return Module.global;
	}

}

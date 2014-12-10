/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package data.transfer.ws2ws;

import com.avaje.ebean.Ebean;

import models.InvoiceTransSource;
import enums.Module;
/**
 * @author mdpinar
*/
class InvoiceTransfer extends BaseTransfer {

	@Override
	public void transferInfo(int sourceWS, int targetWS) {
		executeInsertQueryForInfoTables(new InvoiceTransSource(), sourceWS, targetWS);
	}

	@Override
	public void destroyInfo(int targetWS) {
		executeDeleteQueryForInfoTables("invoice_trans_source", targetWS);
	}

	@Override
	public void destroyTransaction(int targetWS, boolean willBeDestroyedAll) {
		if (willBeDestroyedAll) {
			Ebean.createSqlUpdate("delete from invoice_trans_tax").execute();
			Ebean.createSqlUpdate("delete from invoice_trans_factor").execute();
			Ebean.createSqlUpdate("delete from invoice_trans_currency").execute();
			Ebean.createSqlUpdate("delete from invoice_trans_relation").execute();
			Ebean.createSqlUpdate("delete from invoice_trans_detail where workspace = :workspace").setParameter("workspace", targetWS).execute();
			Ebean.createSqlUpdate("delete from invoice_trans where workspace = :workspace").setParameter("workspace", targetWS).execute();
		}
	}

	@Override
	public Module getModule() {
		return Module.invoice;
	}

}

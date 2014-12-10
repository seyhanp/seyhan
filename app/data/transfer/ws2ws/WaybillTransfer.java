/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package data.transfer.ws2ws;

import java.util.Date;
import java.util.List;

import com.avaje.ebean.Ebean;

import utils.CloneUtils;
import models.WaybillTrans;
import models.WaybillTransDetail;
import models.WaybillTransSource;
import enums.Module;
/**
 * @author mdpinar
*/
class WaybillTransfer extends BaseTransfer {

	@Override
	public void transferInfo(int sourceWS, int targetWS) {
		executeInsertQueryForInfoTables(new WaybillTransSource(), sourceWS, targetWS);
	}

	@Override
	public void destroyInfo(int targetWS) {
		executeDeleteQueryForInfoTables("waybill_trans_source", targetWS);
	}

	@Override
	public void transferTransaction(Date transDate, String description, int sourceWS, int targetWS) {
		List<WaybillTrans> waybillList = WaybillTrans.findTransferList(sourceWS);
		for (WaybillTrans waybillTrans : waybillList) {
			WaybillTrans clone = CloneUtils.cloneTransaction(waybillTrans);
			clone.workspace = targetWS;
			clone.contact = findContactInTargetWS(clone.contact.id, targetWS);
			clone.isTransfer = Boolean.TRUE;
			clone.depot = findDepotInTargetWS(clone.depot.id, targetWS);
			if (clone.seller != null) clone.seller = findSellerInTargetWS(clone.seller.id, targetWS);

			for (WaybillTransDetail wtd : clone.details) {
				wtd.id = null;
				wtd.workspace = targetWS;
				wtd.trans = clone;
				wtd.contact = clone.contact;
				wtd.isTransfer = Boolean.TRUE;
				wtd.depot = clone.depot;
				if (wtd.seller != null) wtd.seller = findSellerInTargetWS(wtd.seller.id, targetWS);

				CloneUtils.resetModel(wtd);
			}
			
			clone.save();
		}
	}

	@Override
	public void destroyTransaction(int targetWS, boolean willBeDestroyedAll) {
		if (willBeDestroyedAll) {
			Ebean.createSqlUpdate("delete from waybill_trans_factor").execute();
			Ebean.createSqlUpdate("delete from waybill_trans_relation").execute();
			Ebean.createSqlUpdate("delete from waybill_trans_detail where workspace = :workspace").setParameter("workspace", targetWS).execute();
			Ebean.createSqlUpdate("delete from waybill_trans where workspace = :workspace").setParameter("workspace", targetWS).execute();
		} else {
			Ebean.createSqlUpdate("delete from waybill_trans_detail where workspace = :workspace and is_transfer = :is_transfer")
						.setParameter("workspace", targetWS)
						.setParameter("is_transfer", Boolean.TRUE)
					.execute();
			Ebean.createSqlUpdate("delete from waybill_trans where workspace = :workspace and is_transfer = :is_transfer")
						.setParameter("workspace", targetWS)
						.setParameter("is_transfer", Boolean.TRUE)
					.execute();
		}
	}

	@Override
	public Module getModule() {
		return Module.waybill;
	}

}

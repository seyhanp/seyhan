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

import models.OrderTrans;
import models.OrderTransDetail;
import models.OrderTransSource;
import utils.CloneUtils;
import enums.Module;
/**
 * @author mdpinar
*/
class OrderTransfer extends BaseTransfer {

	@Override
	public void transferInfo(int sourceWS, int targetWS) {
		executeInsertQueryForInfoTables(new OrderTransSource(), sourceWS, targetWS);
	}

	@Override
	public void destroyInfo(int targetWS) {
		executeDeleteQueryForInfoTables("order_trans_source", targetWS);
	}

	@Override
	public void transferTransaction(Date transDate, String description, int sourceWS, int targetWS) {
		List<OrderTrans> orderList = OrderTrans.findTransferList(sourceWS);
		for (OrderTrans orderTrans : orderList) {
			OrderTrans clone = CloneUtils.cloneTransaction(orderTrans);
			clone.workspace = targetWS;
			clone.contact = findContactInTargetWS(clone.contact.id, targetWS);
			clone.isTransfer = Boolean.TRUE;
			clone.depot = findDepotInTargetWS(clone.depot.id, targetWS);
			if (clone.seller != null) clone.seller = findSellerInTargetWS(clone.seller.id, targetWS);

			for (OrderTransDetail otd : clone.details) {
				otd.id = null;
				otd.workspace = targetWS;
				otd.trans = clone;
				otd.contact = clone.contact;
				otd.isTransfer = Boolean.TRUE;
				otd.depot = clone.depot;
				if (otd.seller != null) otd.seller = findSellerInTargetWS(otd.seller.id, targetWS);

				CloneUtils.resetModel(otd);
			}
			
			clone.save();
		}
	}

	@Override
	public void destroyTransaction(int targetWS, boolean willBeDestroyedAll) {
		if (willBeDestroyedAll) {
			Ebean.createSqlUpdate("delete from order_trans_factor").execute();
			Ebean.createSqlUpdate("delete from order_trans_detail where workspace = :workspace").setParameter("workspace", targetWS).execute();
			Ebean.createSqlUpdate("delete from order_trans where workspace = :workspace").setParameter("workspace", targetWS).execute();
		} else {
			Ebean.createSqlUpdate("delete from order_trans_detail where workspace = :workspace and is_transfer = :is_transfer")
						.setParameter("workspace", targetWS)
						.setParameter("is_transfer", Boolean.TRUE)
					.execute();
			Ebean.createSqlUpdate("delete from order_trans where workspace = :workspace and is_transfer = :is_transfer")
						.setParameter("workspace", targetWS)
						.setParameter("is_transfer", Boolean.TRUE)
					.execute();
		}
	}

	@Override
	public Module getModule() {
		return Module.order;
	}

}

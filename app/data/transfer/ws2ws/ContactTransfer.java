/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package data.transfer.ws2ws;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Contact;
import models.ContactExtraFields;
import models.ContactCategory;
import models.ContactTrans;
import models.ContactTransSource;
import models.SaleSeller;
import models.StockPriceList;
import utils.CurrencyUtils;
import utils.DateUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import enums.Module;
import enums.Right;
import enums.TransType;
/**
 * @author mdpinar
*/
class ContactTransfer extends BaseTransfer {

	private final Right RIGHT = Right.CARI_ACILIS_ISLEMI;

	@Override
	public void transferInfo(int sourceWS, int targetWS) {
		executeInsertQueryForInfoTables(new ContactExtraFields(), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new StockPriceList(), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new SaleSeller(""), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new ContactCategory(), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new ContactTransSource(), sourceWS, targetWS);
		
		Set<String> rnmForContact = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			rnmForContact.add("extra_fields"+i);
		}
		rnmForContact.add("seller");
		rnmForContact.add("category");
		executeInsertQueryForInfoTables(new Contact(), sourceWS, targetWS, rnmForContact);

		for (int i = 0; i < 10; i++) {
			updateRelation("contact", "contact_extra_fields", "extra_fields"+i+"_id",  "name", sourceWS, targetWS);
		}
		
		updateRelation("contact", "sale_seller", "seller_id", "name", sourceWS, targetWS);
		updateRelation("contact", "contact_category", "category_id", "name", sourceWS, targetWS);
		updateRelation("contact", "stock_price_list", "price_id", "name", sourceWS, targetWS);
	}

	@Override
	public void transferTransaction(Date transDate, String description, int sourceWS, int targetWS) {
		String query = "select contact_id, exc_code, AVG(exc_rate) as erate, SUM(debt-credit) as balance " +
						"from contact_trans " +
						"where workspace = :workspace " + 
						"group by contact_id, exc_code " +
						"having SUM(debt-credit) <> 0";
		
		List<SqlRow> rows = Ebean.createSqlQuery(query)
									.setParameter("workspace", sourceWS)
								.findList();
		
		int receiptNo = 1;
		if (rows != null && rows.size() > 0) {
			for(SqlRow row: rows) {
				String excCode = row.getString("exc_code");
				Double balance = row.getDouble("balance");
				
				Contact contact = findContactInTargetWS(row.getInteger("contact_id"), targetWS);
				if (contact != null) {
					ContactTrans trans = new ContactTrans(RIGHT);
					trans.contact = contact;
					trans.workspace = targetWS;
					trans.receiptNo = receiptNo++;
					trans.transType = (balance.doubleValue() > 0 ? TransType.Debt : TransType.Credit);
	
					trans.amount = Math.abs(balance);
					if (trans.transType.equals(TransType.Debt)) {
						trans.debt = trans.amount;
						trans.credit = 0d;
					} else {
						trans.debt = 0d;
						trans.credit = trans.amount;
					}
					trans.excCode = excCode;
					trans.excRate = CurrencyUtils.findTodayRate(excCode, TransType.Credit.equals(trans.transType));
					trans.excEquivalent = trans.amount * trans.excRate;
			
					trans.transDate = transDate;
					trans.description = description;
					trans.transYear = DateUtils.getYear(transDate);
					trans.transMonth = DateUtils.getYearMonth(transDate);
			
					trans.insertBy = "super";
					trans.insertAt = new Date();
			
					trans.saveForOpening();
				}
			}
		}
	}

	@Override
	public void destroyInfo(int targetWS) {
		executeDeleteQueryForInfoTables("contact", targetWS);
		executeDeleteQueryForInfoTables("contact_extra_fields", targetWS);
		executeDeleteQueryForInfoTables("contact_category", targetWS);
		executeDeleteQueryForInfoTables("contact_trans_source", targetWS);
	}

	@Override
	public void destroyTransaction(int targetWS, boolean willBeDestroyedAll) {
		if (willBeDestroyedAll) {
			Ebean.createSqlUpdate("delete from contact_trans where workspace = :workspace")
						.setParameter("workspace", targetWS)
					.execute();
		} else {
			Ebean.createSqlUpdate("delete from contact_trans where workspace = :workspace and _right = :right")
						.setParameter("workspace", targetWS)
						.setParameter("right", RIGHT)
					.execute();
		}
	}

	@Override
	public Module getModule() {
		return RIGHT.module;
	}

}

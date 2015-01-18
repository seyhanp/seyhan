/**
* Copyright (c) 2015 Mustafa DUMLUPINAR, mdumlupinar@gmail.com
*
* This file is part of seyhan project.
*
* seyhan is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package data.transfer.ws2ws;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Contact;
import models.ContactCategory;
import models.ContactExtraFields;
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
		executeInsertQueryForInfoTables(new SaleSeller(""), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new ContactCategory(), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new ContactTransSource(), sourceWS, targetWS);
		
		Set<String> privateDeniedListForExtraFields = new HashSet<String>();
		privateDeniedListForExtraFields.add("extraFields");
		executeInsertQueryForInfoTables(new ContactExtraFields(), sourceWS, targetWS, privateDeniedListForExtraFields);

		Set<String> privateDeniedListForStockPriceList = new HashSet<String>();
		privateDeniedListForStockPriceList.add("category");
		for (int i = 0; i < 10; i++) {
			privateDeniedListForStockPriceList.add("extraField" + i);
		}
		executeInsertQueryForInfoTables(new StockPriceList(), sourceWS, targetWS, privateDeniedListForStockPriceList);
		
		Set<String> rnmForContact = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			rnmForContact.add("extraField"+i);
		}
		rnmForContact.add("seller");
		rnmForContact.add("category");
		rnmForContact.add("priceList");
		executeInsertQueryForInfoTables(new Contact(), sourceWS, targetWS, rnmForContact);

		for (int i = 0; i < 10; i++) {
			updateRelation("contact", "contact_extra_fields", "extra_field"+i+"_id",  "name", sourceWS, targetWS);
		}
		
		updateRelation("contact", "sale_seller", "seller_id", "name", sourceWS, targetWS);
		updateRelation("contact", "contact_category", "category_id", "name", sourceWS, targetWS);
		updateRelation("contact", "stock_price_list", "price_list_id", "name", sourceWS, targetWS);
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

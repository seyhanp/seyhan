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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Stock;
import models.StockBarcode;
import models.StockCategory;
import models.StockCostFactor;
import models.StockCosting;
import models.StockDepot;
import models.StockExtraFields;
import models.StockTrans;
import models.StockTransDetail;
import models.StockTransSource;
import models.StockUnit;
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
class StockTransfer extends BaseTransfer {

	private final Right RIGHT = Right.STOK_ACILIS_ISLEMI;
	
	private StockCosting costing;
	
	public void setCosting(StockCosting costing) {
		this.costing = costing;
	}

	@Override
	public void transferInfo(int sourceWS, int targetWS) {
		executeInsertQueryForInfoTables(new StockUnit(""), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new StockExtraFields(), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new StockDepot(), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new StockCostFactor(""), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new StockTransSource(), sourceWS, targetWS);
		executeInsertQueryForInfoTables(new StockCategory(), sourceWS, targetWS, false);

		Set<String> privateDeniedListForStock = new HashSet<String>();
		privateDeniedListForStock.add("barcode");
		privateDeniedListForStock.add("barcodes");

		Set<String> rnmForStock = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			rnmForStock.add("extra_fields"+i);
		}
		rnmForStock.add("category");
		executeInsertQueryForInfoTables(new Stock(), sourceWS, targetWS, rnmForStock, privateDeniedListForStock);

		for (int i = 0; i < 10; i++) {
			updateRelation("stock", "stock_extra_fields", "extra_fields"+i+"_id",  "name", sourceWS, targetWS);
		}
		updateRelation("stock", "stock_category", "category_id", "name", sourceWS, targetWS);
		
		Set<String> rnmForBarcode = new HashSet<String>();
		rnmForBarcode.add("stock");
		executeInsertQueryForInfoTables(new StockBarcode(""), sourceWS, targetWS, rnmForBarcode, false);

		updateRelation("stock_barcode", "stock", "stock_id",  "code", sourceWS, targetWS);
	}

	@Override
	public void transferTransaction(Date transDate, String description, int sourceWS, int targetWS) {
		List<SqlRow> transRows = Ebean.createSqlQuery("select depot_id, stock_id, SUM(net_input-net_output) as remain "
													+ "from stock_trans_detail "
													+ "where workspace = :workspace "
													+ "group by depot_id, stock_id "
													+ "having SUM(net_input-net_output) > 0")
											.setParameter("workspace", sourceWS)
										.findList();

		if (transRows != null && transRows.size() > 0) {

			int rowNo = 0;
			int receiptNo = 0;
			Integer depotId = null;
			StockTrans trans = null;
			
			double totalAmount = 0;

			for(SqlRow transRow: transRows) {

				if (depotId == null || ! depotId.equals(transRow.getLong("depot_id"))) {
					if (rowNo > 0) {
						trans.total = totalAmount;
						trans.excEquivalent = totalAmount;
						trans.netTotal = totalAmount;
						trans.saveForOpening();
					}

					rowNo = 0;
					totalAmount = 0;
					depotId = transRow.getInteger("depot_id");

					trans = new StockTrans();
					trans.workspace = targetWS;
					trans.right = RIGHT;
					trans.receiptNo = ++receiptNo;
					trans.transType = TransType.Input;
					trans.description = description;
					trans.transDate = transDate;
					trans.transYear = DateUtils.getYear(transDate);
					trans.transMonth = DateUtils.getYearMonth(transDate);
					trans.depot = findDepotInTargetWS(depotId, targetWS);
					trans.details = new ArrayList<StockTransDetail>(); 
				}
		
				Stock stock = findStockInTargetWS(transRow.getInteger("stock_id"), targetWS);
				if (stock != null) {
					double price = findPrice(stock.id, stock.buyPrice);
					
					StockTransDetail detail = new StockTransDetail();
					detail.workspace = targetWS;
					detail.right = RIGHT;
					detail.receiptNo = receiptNo;
					detail.stock = stock;
					detail.transDate = transDate;
					detail.transType = trans.transType;
					detail.depot = trans.depot;
	
					detail.rowNo = ++rowNo;
					detail.name = stock.name;
					detail.quantity = transRow.getDouble("remain");
					detail.unit = stock.unit1;
					detail.unitRatio = 1d;
					detail.basePrice = price;
					detail.price = detail.basePrice;
					detail.taxRate = stock.buyTax;
					detail.amount = price * detail.quantity;
					detail.total = detail.amount;
					detail.transYear = trans.transYear; 
					detail.transMonth = trans.transMonth;
					detail.unit1 = stock.unit1;
					detail.unit2 = stock.unit2;
					detail.unit3 = stock.unit3;
					detail.unit2Ratio = stock.unit2Ratio;
					detail.unit3Ratio = stock.unit3Ratio;
					detail.input = detail.quantity;
					detail.inTotal = detail.amount;
					detail.netInput = detail.input;
					detail.netInTotal = detail.inTotal;
					detail.excCode = stock.excCode;
					detail.excRate = CurrencyUtils.findTodayRate(stock.excCode, true);
					detail.excEquivalent = detail.excRate * detail.amount;
					
					totalAmount += detail.excEquivalent;
	
					trans.details.add(detail);
				
					if (rowNo > 49) depotId = null;
				}
			}
			if (rowNo > 0) {
				trans.total = totalAmount;
				trans.excEquivalent = totalAmount;
				trans.netTotal = totalAmount;
				trans.saveForOpening();
			}
		}
	}

	@Override
	public void destroyInfo(int targetWS) {
		executeDeleteQueryForInfoTables("stock_barcode", targetWS);
		executeDeleteQueryForInfoTables("stock", targetWS);
		executeDeleteQueryForInfoTables("stock_unit", targetWS);
		executeDeleteQueryForInfoTables("contact_extra_fields", targetWS);
		executeDeleteQueryForInfoTables("stock_category", targetWS);
		executeDeleteQueryForInfoTables("stock_depot", targetWS);
		executeDeleteQueryForInfoTables("stock_cost_factor", targetWS);
		executeDeleteQueryForInfoTables("stock_trans_source", targetWS);
	}

	@Override
	public void destroyTransaction(int targetWS, boolean willBeDestroyedAll) {
		if (willBeDestroyedAll) {
			Ebean.createSqlUpdate("delete from stock_trans_tax").execute();
			Ebean.createSqlUpdate("delete from stock_trans_factor").execute();
			Ebean.createSqlUpdate("delete from stock_trans_currency").execute();
			Ebean.createSqlUpdate("delete from stock_trans_detail where workspace = :workspace").setParameter("workspace", targetWS).execute();
			Ebean.createSqlUpdate("delete from stock_trans where workspace = :workspace").setParameter("workspace", targetWS).execute();

			Ebean.createSqlUpdate("delete from stock_costing").execute();
			Ebean.createSqlUpdate("delete from stock_price_update where workspace = :workspace").setParameter("workspace", targetWS).execute();
		} else {
			Ebean.createSqlUpdate("delete from stock_trans_detail where workspace = :workspace and _right = :right")
						.setParameter("workspace", targetWS)
						.setParameter("right", RIGHT)
					.execute();
			Ebean.createSqlUpdate("delete from stock_trans where workspace = :workspace and _right = :right")
						.setParameter("workspace", targetWS)
						.setParameter("right", RIGHT)
					.execute();
		}
	}

	@Override
	public Module getModule() {
		return RIGHT.module;
	}

	private double findPrice(Integer id, Double defaultPrice) {
		if (costing == null || costing.id == null) return defaultPrice;

		SqlRow priceRow = Ebean.createSqlQuery("select price from stock_costing_inventory where costing_id = :costing_id and stock_id = :stock_id")
									.setParameter("stock_id", id)
									.setParameter("costing_id", costing.id)
								.findUnique();
		return (priceRow != null && priceRow.getDouble("price") != null ? priceRow.getDouble("price") : defaultPrice);
	}
	
}

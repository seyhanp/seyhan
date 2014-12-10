/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.StockCosting;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;

import enums.CostingType;

/**
 * @author mdpinar
*/
public class CostingUtils {

//	private final static Logger log = LoggerFactory.getLogger(CostingUtils.class);

	public static void execute(StockCosting costing) {
		firstInit(costing);

		switch (costing.costingType) {

			case Simple:
			case Weighted: {
				findSimpleAvgCost(costing);
				break;
			}

			case Moving: {
				findMovingAvgCost(costing);
				break;
			}

			case FIFO:
			case LIFO: {
				findxFOCost(costing);
				break;
			}

		}

		lastProcesses(costing);
	}

	private static void findxFOCost(StockCosting costing) {
		/**
		 * Alislardan maliyet bulunur
		 */
		StringBuilder querySB = new StringBuilder();

		querySB.append("select stock_id, trans_date, base_price, sum(input) as sumInput, sum(total) as sumTotal from stock_trans_detail as t ");
		querySB.append(getStandartQueryPart(null, costing.calcDate));
		querySB.append(" group by stock_id, trans_date, (total/input)");
		querySB.append(" order by stock_id, trans_date ");

		if (costing.costingType.equals(CostingType.LIFO)) {
			querySB.append(" desc");
		}

		List<SqlRow> buyingList = Ebean.createSqlQuery(querySB.toString()).setParameter("tru", Boolean.TRUE).findList();
		Map<Integer, List<xFOModel>> xFOMap = new HashMap<Integer, List<xFOModel>>();
		if (buyingList != null) {

			Integer lastId = null;
			List<xFOModel> lastxFOList = null;
			for (SqlRow buyingRow : buyingList) {

				Integer stockId = buyingRow.getInteger("stock_id");
				if (! stockId.equals(lastId)) {
					if (lastId != null) {
						xFOMap.put(lastId, lastxFOList);
					}
					lastId = stockId;
					lastxFOList = new ArrayList<xFOModel>();
				}

				lastxFOList.add(
					new xFOModel(
						buyingRow.getDate("trans_date"),
						buyingRow.getDouble("sumInput"),
						buyingRow.getDouble("base_price"),
						buyingRow.getDouble("sumTotal")
					)
				);
			}

			if (lastxFOList != null && lastxFOList.size() > 0) {
				xFOMap.put(lastId, lastxFOList);
			}
		}

		/**
		 * Her bir stok icin maliyetlendirme yapilir
		 */
		querySB = new StringBuilder();
		querySB.append("select stock_id, sell_date, sell_quantity, sell_cost_amount from stock_costing_detail ");
		querySB.append("where costing_id = " + costing.id);

		List<SqlRow> sellingList = Ebean.createSqlQuery(querySB.toString()).findList();
		if (sellingList != null) {
			for (SqlRow sellingRow : sellingList) {

				Integer stockId = sellingRow.getInteger("stock_id");
				double output  = sellingRow.getDouble("sell_quantity");

				List<xFOModel> xFOList = xFOMap.get(stockId);
				if (xFOList != null) {

					for (int i = 0; i < xFOList.size(); i++) {
						xFOModel xFOBase = xFOList.get(i);

						if (output == 0) break;
						if (xFOBase.remain.doubleValue() >= xFOBase.input.doubleValue()) continue;

						double sellAmount = sellingRow.getDouble("sell_cost_amount");
						double sellPrice = (sellAmount / sellingRow.getDouble("sell_quantity"));

						if (xFOBase.remain.doubleValue() == 0) {

							if (xFOBase.input.doubleValue() <= output) {

								double buyCostAmount = (xFOBase.price * xFOBase.input);
								double sellCostAmount = (sellPrice * xFOBase.input);
								double plAmount = sellCostAmount - buyCostAmount;

								querySB = new StringBuilder("update stock_costing_detail ");
								querySB.append("set buy_cost_price =" + xFOBase.price);
								querySB.append(", buy_cost_amount =" + buyCostAmount);
								querySB.append(", sell_quantity =" + xFOBase.input);
								querySB.append(", sell_cost_price = " + sellPrice);
								querySB.append(", sell_cost_amount = " + sellCostAmount);
								querySB.append(", profit_loss_amount = " + plAmount);
								querySB.append(" where costing_id = " + costing.id);
								querySB.append("   and stock_id = " + stockId);
								querySB.append("   and sell_date = ");  querySB.append(DateUtils.formatDateForDB(sellingRow.getDate("sell_date")));

								Ebean.createSqlUpdate(querySB.toString()).execute();

								xFOBase.remain = xFOBase.input;
								output -= xFOBase.input;

							} else {

								double buyCostAmount = (xFOBase.price * output);
								double sellCostAmount = (sellPrice * output);
								double plAmount = sellCostAmount - buyCostAmount;

								querySB = new StringBuilder("insert into stock_costing_detail ");
								querySB.append("(costing_id, stock_id, sell_date, sell_quantity, sell_cost_price, sell_cost_amount,");
								querySB.append(" buy_cost_price, buy_cost_amount, profit_loss_amount, trans_year, trans_month)");

								querySB.append(" values ");
								querySB.append("(:costing_id, :stock_id, :sell_date, :sell_quantity, :sell_cost_price, :sell_cost_amount,");
								querySB.append(" :buy_cost_price, :buy_cost_amount, :profit_loss_amount, :trans_year, :trans_month)");

								SqlUpdate insert = Ebean.createSqlUpdate(querySB.toString());
								insert.setParameter("costing_id", costing.id);
								insert.setParameter("stock_id", stockId);
								insert.setParameter("sell_date", sellingRow.getDate("sell_date"));
								insert.setParameter("sell_quantity", output);
								insert.setParameter("sell_cost_price", sellPrice);
								insert.setParameter("sell_cost_amount", sellCostAmount);
								insert.setParameter("buy_cost_price", xFOBase.price);
								insert.setParameter("buy_cost_amount", buyCostAmount);
								insert.setParameter("profit_loss_amount", plAmount);
								insert.setParameter("trans_year", DateUtils.getYear(sellingRow.getDate("sell_date")));
								insert.setParameter("trans_month", DateUtils.getYearMonth(sellingRow.getDate("sell_date")));

								insert.execute();

								xFOBase.remain += output;
								output = 0;
							}

	 					} else {

	 						double realRemain = xFOBase.input.doubleValue() - xFOBase.remain.doubleValue();
							if (realRemain > output) {
								realRemain = output;
								xFOBase.remain += realRemain;
							} else {
								xFOBase.remain = xFOBase.input;
							}
							output -= realRemain;

							double buyCostAmount = (xFOBase.price * realRemain);
							double sellCostAmount = (sellPrice * realRemain);
							double plAmount = sellCostAmount - buyCostAmount;

							querySB = new StringBuilder("insert into stock_costing_detail ");
							querySB.append("(costing_id, stock_id, sell_date, sell_quantity, sell_cost_price, sell_cost_amount,");
							querySB.append(" buy_cost_price, buy_cost_amount, profit_loss_amount, trans_year, trans_month)");

							querySB.append(" values ");
							querySB.append("(:costing_id, :stock_id, :sell_date, :sell_quantity, :sell_cost_price, :sell_cost_amount,");
							querySB.append(" :buy_cost_price, :buy_cost_amount, :profit_loss_amount, :trans_year, :trans_month)");

							SqlUpdate insert = Ebean.createSqlUpdate(querySB.toString());
							insert.setParameter("costing_id", costing.id);
							insert.setParameter("stock_id", stockId);
							insert.setParameter("sell_date", sellingRow.getDate("sell_date"));
							insert.setParameter("sell_quantity", realRemain);
							insert.setParameter("sell_cost_price", sellPrice);
							insert.setParameter("sell_cost_amount", sellCostAmount);
							insert.setParameter("buy_cost_price", xFOBase.price);
							insert.setParameter("buy_cost_amount", sellCostAmount);
							insert.setParameter("profit_loss_amount", plAmount);
							insert.setParameter("trans_year", DateUtils.getYear(sellingRow.getDate("sell_date")));
							insert.setParameter("trans_month", DateUtils.getYearMonth(sellingRow.getDate("sell_date")));

							insert.execute();

	 					}
					}
				}
			}
		}

		/**
		 * Envanter degerleri
		 */
		if (xFOMap != null && xFOMap.size() > 0) {
			for (Entry<Integer, List<xFOModel>> entry: xFOMap.entrySet()) {
				List<xFOModel> xFOList = entry.getValue();
				for (xFOModel xFOBase : xFOList) {

					double invQuantity = xFOBase.input - xFOBase.remain;
					if (invQuantity > 0) {
						querySB = new StringBuilder("insert into stock_costing_inventory ");
						querySB.append("(costing_id, stock_id, _date, input, remain, price, amount)");
						querySB.append(" values ");
						querySB.append("(:costing_id, :stock_id, :_date, :input, :remain, :price, :amount)");

						SqlUpdate insert = Ebean.createSqlUpdate(querySB.toString());
						insert.setParameter("costing_id", costing.id);
						insert.setParameter("stock_id", entry.getKey());
						insert.setParameter("_date", xFOBase.date);
						insert.setParameter("input", xFOBase.input);
						insert.setParameter("remain", invQuantity);
						insert.setParameter("price", xFOBase.price);
						insert.setParameter("amount", (xFOBase.price * invQuantity));

						insert.execute();
					}
				}
			}
		}

	}

	private static void findSimpleAvgCost(StockCosting costing) {
		/**
		 * Her bir stok icin maliyetlendirme yapilir
		 */
		StringBuilder sqlSB = new StringBuilder();
		sqlSB.append("select stock_id, sum(sell_quantity) as sumOutput from stock_costing_detail ");
		sqlSB.append("where costing_id = " + costing.id);
		sqlSB.append(" group by stock_id");

		List<SqlRow> stockList = Ebean.createSqlQuery(sqlSB.toString()).findList();
		if (stockList != null) {
			for (SqlRow stockRow : stockList) {
				/**
				 * Alislardan maliyet bulunur
				 */
				StringBuilder querySB = new StringBuilder();
				if (costing.costingType.equals(CostingType.Simple)) {
					querySB.append("select sum(input) as sumInput, avg(total / input) as sumTotal from stock_trans_detail as t ");
				} else {
					querySB.append("select sum(input) as sumInput, sum(total) as sumTotal from stock_trans_detail as t ");
				}
				querySB.append(getStandartQueryPart(stockRow.getInteger("stock_id"), costing.calcDate));

				SqlRow costingRow = Ebean.createSqlQuery(querySB.toString()).setParameter("tru", Boolean.TRUE).findUnique();

				if (costingRow != null 
					&& costingRow.getDouble("sumTotal") != null 
					&& costingRow.getDouble("sumInput") != null
					&& costingRow.getDouble("sumTotal") * costingRow.getDouble("sumInput") > 0) {

					double input = 1d;
					double total = costingRow.getDouble("sumTotal");

					if (costing.costingType.equals(CostingType.Weighted)) {
						input = costingRow.getDouble("sumInput");
					}
					/**
					 * Bulunan maliyeti ve envanter degeri yansitilir
					 */
					if (total > 0) {

						double buyCostPrice = 0;
						if (total > 0 && input > 0) buyCostPrice = total / input;

						/**
						 * Maliyeti
						 */
						querySB = new StringBuilder("update stock_costing_detail ");
						querySB.append("set buy_cost_price = " + buyCostPrice);
						querySB.append(", buy_cost_amount = (sell_quantity * " + buyCostPrice + ")");
						querySB.append(", sell_cost_price = (sell_cost_amount / sell_quantity)");
						querySB.append(", profit_loss_amount = sell_cost_amount - (sell_quantity * " + buyCostPrice + ")");
						querySB.append(" where costing_id = " + costing.id);
						querySB.append("   and stock_id = " + stockRow.getInteger("stock_id"));

						Ebean.createSqlUpdate(querySB.toString()).execute();

						/**
						 * Envanter degeri
						 */
						double invQuantity = costingRow.getDouble("sumInput") - stockRow.getDouble("sumOutput");

						querySB = new StringBuilder("insert into stock_costing_inventory ");
						querySB.append("(costing_id, stock_id, input, remain, price, amount)");
						querySB.append(" values ");
						querySB.append("(:costing_id, :stock_id, :input, :remain, :price, :amount)");

						SqlUpdate insert = Ebean.createSqlUpdate(querySB.toString());
						insert.setParameter("costing_id", costing.id);
						insert.setParameter("stock_id", stockRow.getInteger("stock_id"));
						insert.setParameter("input", costingRow.getDouble("sumInput"));
						insert.setParameter("remain", invQuantity);
						insert.setParameter("price", (total / input));
						insert.setParameter("amount", ((total / input) * invQuantity));

						insert.execute();
					}
				}
			}
		}

	}

	private static void findMovingAvgCost(StockCosting costing) {
		/**
		 * Her bir stok icin maliyetlendirme yapilir
		 */
		StringBuilder sqlSB = new StringBuilder();
		sqlSB.append("select stock_id, sell_date, sum(sell_quantity) as sumOutput from stock_costing_detail ");
		sqlSB.append("where costing_id = " + costing.id);
		sqlSB.append(" group by stock_id, sell_date");

		Map<Integer, Double> invPriceMap = new HashMap<Integer, Double>();

		List<SqlRow> stockList = Ebean.createSqlQuery(sqlSB.toString()).findList();
		if (stockList != null) {
			for (int i = 0; i < stockList.size(); i++) {

				SqlRow stockRow = stockList.get(i);
				Integer stockId = stockRow.getInteger("stock_id");

				/**
				 * Alislardan maliyet bulunur
				 */
				StringBuilder querySB = new StringBuilder();
				querySB.append("select sum(input) as sumInput, sum(total) as sumTotal from stock_trans_detail as t ");
				querySB.append(getStandartQueryPart(stockId, stockRow.getDate("sell_date")));

				SqlRow costingRow = Ebean.createSqlQuery(querySB.toString()).setParameter("tru", Boolean.TRUE).findUnique();
				if (costingRow != null 
					&& costingRow.getDouble("sumTotal") != null 
					&& costingRow.getDouble("sumInput") != null
					&& costingRow.getDouble("sumTotal") * costingRow.getDouble("sumInput") > 0) {

					double total = costingRow.getDouble("sumTotal");
					double input = costingRow.getDouble("sumInput");
					double buyCostPrice = 0;
					if (total > 0 && input > 0) buyCostPrice = total / input;

					/**
					 * Bulunan maliyet yansitilir
					 */
					if (total > 0) {
						/**
						 * Maliyeti
						 */
						querySB = new StringBuilder("update stock_costing_detail ");
						querySB.append("set buy_cost_price = " + buyCostPrice);
						querySB.append(", buy_cost_amount = (sell_quantity * " + buyCostPrice + ")");
						querySB.append(", sell_cost_price = (sell_cost_amount / sell_quantity)");
						querySB.append(", profit_loss_amount = sell_cost_amount - (sell_quantity * " + buyCostPrice + ")");
						querySB.append(" where costing_id = " + costing.id);
						querySB.append("   and stock_id = " + stockId);
						querySB.append("   and sell_date = ");  querySB.append(DateUtils.formatDateForDB(stockRow.getDate("sell_date")));

						Ebean.createSqlUpdate(querySB.toString()).execute();

						/**
						 * Envanter degeri hesaplamalarinda kullanilacak birim maliyet fiyatlari toparlanir
						 */
						invPriceMap.put(stockId, (total / input));
					}
				}
			}

			/**
			 * Envanter degeri
			 */
			if (invPriceMap != null && invPriceMap.size() > 0) {
				for (Map.Entry<Integer, Double> invEntry: invPriceMap.entrySet()) {
					StringBuilder querySB = new StringBuilder();
					querySB = new StringBuilder("insert into stock_costing_inventory ");
					querySB.append("(costing_id, stock_id, input, remain, price, amount)");
					querySB.append(" values ");
					querySB.append("(:costing_id, :stock_id, :input, :remain, :price, :amount)");

					SqlRow statusRow = QueryUtils.findStockSums(invEntry.getKey(), costing.calcDate);
					if (statusRow != null) {

						double input = statusRow.getDouble("sumInput");
						double output = statusRow.getDouble("sumOutput");

						SqlUpdate insert = Ebean.createSqlUpdate(querySB.toString());
						insert.setParameter("costing_id", costing.id);
						insert.setParameter("stock_id", invEntry.getKey());
						insert.setParameter("input", input);
						insert.setParameter("remain", (input-output));
						insert.setParameter("price", invEntry.getValue());
						insert.setParameter("amount", (invEntry.getValue() * (input-output)));

						insert.execute();
					}
				}
			}

		}

	}

	private static void firstInit(StockCosting costing) {
		/**
		 * Eskiden yapilmis olan maliyetlendirme varsa silinir
		 */
		Ebean.createSqlUpdate("delete from stock_costing_detail where costing_id = :costing_id")
				.setParameter("costing_id", costing.id)
			.execute();
		/**
		 * Maliyetlendirmeleri yapilacak olan satislar "stock_costing_detail" tablosuna aktarilir
		 */
		StringBuilder querySB = new StringBuilder();
		querySB.append("insert into stock_costing_detail (costing_id, trans_year, trans_month, stock_id, sell_date, sell_quantity, sell_cost_amount) ");
		querySB.append("select " + costing.id + ", " + DateUtils.getYearForSQL("trans_date") + ", " + DateUtils.getYearMonthForSQL("trans_date") + ", t.stock_id, t.trans_date, sum(t.output), sum(t.total) ");
		querySB.append("from stock as s, stock_trans_detail as t ");

		if (costing.transPoint != null && costing.transPoint.id != null) querySB.append(InstantSQL.buildTransPointSQL(costing.transPoint.id));

		querySB.append(" where s.workspace = " + CacheUtils.getWorkspaceId());
		querySB.append("   and s.is_active = :tru");
		querySB.append("   and s.workspace = t.workspace");
		querySB.append("   and s.id = t.stock_id");
		querySB.append("   and t.trans_type = 'Output'");
		querySB.append("   and t.has_cost_effect = :tru");
		querySB.append("   and t.trans_date <= ");  querySB.append(DateUtils.formatDateForDB(costing.calcDate));

		if (costing.category != null && costing.category.id != null) querySB.append(InstantSQL.buildCategorySQL(costing.category.id));

		if (costing.stock != null && costing.stock.id != null) {
			querySB.append("  and s.id = " + costing.stock.id);
		} else {
			if (costing.providerCode != null && ! costing.providerCode.isEmpty()) querySB.append(" and s.providerCode = '" + costing.providerCode + "'");
			if (costing.depot != null && costing.depot.id != null) querySB.append(" and t.depot_id = " + costing.depot.id);
			if (costing.extraField0 != null && costing.extraField0.id != null) querySB.append(" and s.extra_field0_id = " + costing.extraField0.id);
			if (costing.extraField1 != null && costing.extraField1.id != null) querySB.append(" and s.extra_field1_id = " + costing.extraField1.id);
			if (costing.extraField2 != null && costing.extraField2.id != null) querySB.append(" and s.extra_field2_id = " + costing.extraField2.id);
			if (costing.extraField3 != null && costing.extraField3.id != null) querySB.append(" and s.extra_field3_id = " + costing.extraField3.id);
			if (costing.extraField4 != null && costing.extraField4.id != null) querySB.append(" and s.extra_field4_id = " + costing.extraField4.id);
			if (costing.extraField5 != null && costing.extraField5.id != null) querySB.append(" and s.extra_field5_id = " + costing.extraField5.id);
			if (costing.extraField6 != null && costing.extraField6.id != null) querySB.append(" and s.extra_field6_id = " + costing.extraField6.id);
			if (costing.extraField7 != null && costing.extraField7.id != null) querySB.append(" and s.extra_field7_id = " + costing.extraField7.id);
			if (costing.extraField8 != null && costing.extraField8.id != null) querySB.append(" and s.extra_field8_id = " + costing.extraField8.id);
			if (costing.extraField9 != null && costing.extraField9.id != null) querySB.append(" and s.extra_field9_id = " + costing.extraField9.id);
		}

		querySB.append(" group by t.stock_id, t.trans_date");

		Ebean.createSqlUpdate(querySB.toString()).setParameter("tru", Boolean.TRUE).execute();

		/**
		 * Envanter tablosu silinir
		 */
		querySB = new StringBuilder();
		querySB.append("delete from stock_costing_inventory ");
		querySB.append(" where costing_id = " + costing.id);

		Ebean.createSqlUpdate(querySB.toString()).execute();
	}

	private static void lastProcesses(StockCosting costing) {
		StringBuilder querySB = new StringBuilder();
		querySB.append("delete from stock_costing_detail ");
		querySB.append(" where costing_id = " + costing.id);
		querySB.append("   and sell_quantity = 0 or buy_cost_price = 0");

		Ebean.createSqlUpdate(querySB.toString()).execute();
	}

	private static String getStandartQueryPart(Integer stockId, Date date) {
		StringBuilder querySB = new StringBuilder();
		querySB.append(" where t.workspace = " + CacheUtils.getWorkspaceId());
		querySB.append("   and t.trans_type = 'Input'");
		querySB.append("   and t.has_cost_effect = :tru");

		if (stockId != null) {
			querySB.append("  and t.stock_id = " + stockId);
		}

		querySB.append("  and t.trans_date <= ");  querySB.append(DateUtils.formatDateForDB(date));

		return querySB.toString();
	}

	static class xFOModel {

		Date date;
		Double input;
		Double remain;
		Double price;
		Double total;
		Double cost;

		public xFOModel(Date date, Double input, Double price, Double total) {
			super();
			this.date = date;
			this.input = input;
			this.price = price;
			this.total = total;
			this.cost = total / input; 
			this.remain = 0d;
		}
	}

}

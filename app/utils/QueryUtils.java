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

import models.BaseModel;
import models.temporal.ExtraFieldsForContact;
import models.temporal.ExtraFieldsForStock;
import models.temporal.InvSummary;
import models.temporal.InvTrans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;

import enums.Module;
import enums.Right;
import enums.TransType;

/**
 * @author mdpinar
*/
public class QueryUtils {

	private final static Logger log = LoggerFactory.getLogger(QueryUtils.class);

	private static Map<Module, String> moduleIdMap;

	static {
		moduleIdMap = new HashMap<Module, String>();
		moduleIdMap.put(Module.contact, "contact_id");
		moduleIdMap.put(Module.safe, "safe_id");
		moduleIdMap.put(Module.bank,"bank_id");
	}

	public static double findBalance(Module module, Integer modelId) {
		String query = String.format("select sum(debt - credit) as balance from %s_trans where workspace = %d and %s_id = %d group by workspace", module.name(), CacheUtils.getWorkspaceId(), module.name(), modelId);
		SqlRow row = Ebean.createSqlQuery(query).findUnique();

		double balance = 0d;
		if (row != null && ! row.isEmpty() && row.getDouble("balance") != null) {
			balance = row.getDouble("balance");
		}

		return balance;
	}

	public static double findTotal(Module module, Integer modelId, TransType type) {
		String query = String.format("select sum("+type.name().toLowerCase()+") as _total from %s_trans " +
									"where workspace = %d and %s_id = %d " +
									"group by workspace", module.name(), CacheUtils.getWorkspaceId(), module.name(), modelId);
		SqlRow row = Ebean.createSqlQuery(query).findUnique();

		double total = 0d;
		if (row != null && ! row.isEmpty() && row.getDouble("_total") != null) {
			total = row.getDouble("_total");
		}

		return total;
	}

	public static double findStockBalance(Integer id) {
		return findStockBalance(id, null, null);
	}

	public static double findStockBalance(Integer id, Integer depotId, Integer excepId) {
		String exceptPart = (excepId != null ? " and id != " + excepId : "");
		SqlRow row = Ebean.createSqlQuery("select sum(net_input - net_output) as balance from stock_trans_detail " + 
											"where workspace = " + CacheUtils.getWorkspaceId() + 
											"  and stock_id = " + id + exceptPart +
											(depotId != null ? "  and depot_id = " + depotId : "") +
											" group by workspace"
										).findUnique();

		double balance = 0d;
		if (row != null && ! row.isEmpty() && row.getDouble("balance") != null) {
			balance = row.getDouble("balance");
		}

		return balance;
	}

	public static SqlRow findStockSums(Integer id, Date date) {
		String query = "select sum(input) as sumInput, sum(output) as sumOutput from stock_trans_detail " + 
						"where workspace = " + CacheUtils.getWorkspaceId() +
						"  and stock_id = " + id +
						(date != null ? " and trans_date <= " + DateUtils.formatDateForDB(date) : "") +
						" group by workspace";

		return Ebean.createSqlQuery(query).findUnique();
	}
	
	public static Double findStockTotal(Integer id, TransType type) {
		String query = "select sum("+type.name().toLowerCase()+") as _total from stock_trans_detail " + 
						"where workspace = " + CacheUtils.getWorkspaceId() + "  and stock_id = " + id +
						" group by workspace";
		
		SqlRow row = Ebean.createSqlQuery(query).findUnique();

		double total = 0d;
		if (row != null && ! row.isEmpty() && row.getDouble("_total") != null) {
			total = row.getDouble("_total");
		}

		return total;
	}

	public static double findStockLastPrice(Integer id, boolean isBuying) {
		String query = "select base_price as price from stock_trans_detail " +
						"where workspace = :workspace " +
						"  and is_return = false " +
						"  and stock_id = :id " +
						"  and trans_type = :trans_type " +
						"order by trans_date desc";
		SqlRow row = Ebean.createSqlQuery(query)
							.setParameter("workspace", CacheUtils.getWorkspaceId())
							.setParameter("id", id)
							.setParameter("trans_type", (isBuying ? TransType.Input.name() : TransType.Output.name()))
						.setMaxRows(1)
						.findUnique();

		double price = 0d;
		if (row != null && row.getDouble("price") != null) {
			price = row.getDouble("price");
		}

		return price;
	}

	public static List<InvTrans> inspectStockTrans(Integer id) {
		List<InvTrans> result = new ArrayList<InvTrans>();

		String query = "select t.id, t._right, t.contact_name, t.trans_date, td.price, t.exc_code, t.ref_id, d.name as dep_name, t.trans_type, abs(sum(td.input-td.output)) as quantity from stock_trans as t " +
						"inner join stock_trans_detail as td on td.trans_id = t.id " +
						"left  join stock_depot as d on d.id = t.depot_id " +
						"where t.workspace = " + CacheUtils.getWorkspaceId() +
						"  and stock_id = " + id +
						" group by t.id, t._right, t.contact_name, t.trans_date, td.price, t.exc_code, t.ref_id, d.name, t.trans_type " +
						" order by t.trans_date desc, t.trans_type ";

		List<SqlRow> rows = Ebean.createSqlQuery(query).setMaxRows(20).findList();
		if (rows != null && rows.size() > 0) {
			for(SqlRow row: rows) {
				InvTrans trans = new InvTrans();
				trans.id = row.getInteger("id");
				trans.right = Right.valueOf(row.getString("_right"));
				trans.title = row.getString("contact_name");
				trans.date = row.getDate("trans_date");
				trans.quantity = row.getDouble("quantity");
				trans.price = row.getDouble("price");
				trans.excCode = row.getString("exc_code");

				trans.depot = row.getString("dep_name");
				trans.transType = Messages.get("short." + row.getString("trans_type"));

				if (trans.right.isShadow) {
					trans.link = String.format("/%ss/trans/%d?rightBind=%s", Module.stock.name(), row.getInteger("ref_id"), Right.STOK_TRANSFER_FISI.name());
				} else {
					trans.link = String.format("/%ss/trans/%d?rightBind=%s", trans.right.module.name(), trans.id, trans.right.name());
				}

				result.add(trans);
			}
		}

		return result;
	}

	public static List<InvSummary> inspectStockSummary(Integer id) {
		List<InvSummary> result = new ArrayList<InvSummary>();

		String query = "select trans_month, d.name as dname, " +
						"  sum(net_input) as netInput, sum(net_in_total) as netInTotal, " +
						"  sum(net_output) as netOutput, sum(net_out_total) as netOutTotal, " +
						"  sum(ret_input) as retInput, sum(ret_in_total) as retInTotal, " +
						"  sum(ret_output) as retOutput, sum(ret_out_total) as retOutTotal " +
						"from stock_trans_detail as t " +
						"left join stock_depot as d on d.id = t.depot_id " +
						"where t.workspace = " + CacheUtils.getWorkspaceId() +
						"  and stock_id = " + id +
						" group by trans_month, d.name " +
						" order by trans_month, d.name ";

		String dname = null;
		double netInput = 0d;
		double netInTotal = 0d;
		double netOutput = 0d;
		double netOutTotal = 0d;
		double retInput = 0d;
		double retInTotal = 0d;
		double retOutput = 0d;
		double retOutTotal = 0d;
		double balance = 0d;

		List<SqlRow> rows = Ebean.createSqlQuery(query).findList();
		if (rows != null && rows.size() > 0) {
			for(SqlRow row: rows) {
				InvSummary summary = new InvSummary();

				String title = row.getString("dname");
				if (dname != null && ! title.equals(dname)) {
					InvSummary sum = new InvSummary();
					sum.isImportant = Boolean.TRUE;
					sum.title = dname;
					sum.netInput = netInput;
					sum.netInTotal = netInTotal;
					sum.netOutput = netOutput;
					sum.netOutTotal = netOutTotal;
					sum.retInput = retInput;
					sum.retInTotal = retInTotal;
					sum.retOutput = retOutput;
					sum.retOutTotal = retOutTotal;
					sum.balance = balance;
					result.add(sum);

					netInput = 0d;
					netInTotal = 0d;
					netOutput = 0d;
					netOutTotal = 0d;
					retInput = 0d;
					retInTotal = 0d;
					retOutput = 0d;
					retOutTotal = 0d;
					balance = 0d;
				}
				dname = title;

				summary.title = row.getString("trans_month");
				summary.netInput = row.getDouble("netInput");
				summary.netInTotal = row.getDouble("netInTotal");
				summary.netOutput = row.getDouble("netOutput");
				summary.netOutTotal = row.getDouble("netOutTotal");
				summary.retInput = row.getDouble("retInput");
				summary.retInTotal = row.getDouble("retInTotal");
				summary.retOutput = row.getDouble("retOutput");
				summary.retOutTotal = row.getDouble("retOutTotal");
				summary.balance = (summary.netInput + summary.retOutput) - (summary.netOutput + summary.retInput);

				netInput += summary.netInput;
				netInTotal += summary.netInTotal;
				netOutput += summary.netOutput;
				netOutTotal += summary.netOutTotal;
				retInput += summary.retInput;
				retInTotal += summary.retInTotal;
				retOutput += summary.retOutput;
				retOutTotal += summary.retOutTotal;
				balance += summary.balance;

				result.add(summary);
			}

			if (balance != 0) {
				InvSummary sum = new InvSummary();
				sum.isImportant = Boolean.TRUE;
				sum.title = dname;
				sum.netInput = netInput;
				sum.netInTotal = netInTotal;
				sum.netOutput = netOutput;
				sum.netOutTotal = netOutTotal;
				sum.retInput = retInput;
				sum.retInTotal = retInTotal;
				sum.retOutput = retOutput;
				sum.retOutTotal = retOutTotal;
				sum.balance = balance;
				result.add(sum);
			}
		}

		return result;
	}

	public static List<InvTrans> inspectXTrans(Module module, Integer id) {
		List<InvTrans> result = new ArrayList<InvTrans>();

		String query = "select id, _right, ref_module, description, trans_date, exc_code, sum(debt) as sumDebt, sum(credit) as sumCredit from :transTable as t " +
						"where t.workspace = " + CacheUtils.getWorkspaceId() +
						"  and t.:id = " + id +
						" group by id, _right, ref_module, description, trans_date, exc_code " +
						" order by trans_date desc";
		query = query.replaceAll("\\:transTable", module.name() + "_trans")
					.replaceAll("\\:id", module.name() + "_id");

		List<SqlRow> rows = Ebean.createSqlQuery(query).setMaxRows(15).findList();
		if (rows != null && rows.size() > 0) {
			for(SqlRow row: rows) {
				InvTrans trans = new InvTrans();
				trans.id = row.getInteger("id");
				trans.right = Right.valueOf(row.getString("_right"));
				trans.title = row.getString("description");
				trans.date = row.getDate("trans_date");
				trans.debt = row.getDouble("sumDebt");
				trans.credit = row.getDouble("sumCredit");
				trans.excCode = row.getString("exc_code");

				if (trans.right.isShadow) {
					Module refModule = Module.valueOf(row.getString("ref_module"));
					trans.link = String.format("/%ss/trans/%d?rightBind=%s", refModule.name(), trans.id, findShadowRight(refModule, row.getInteger("id")));
				} else {
					trans.link = String.format("/%ss/%s/%d?rightBind=%s", 
							trans.right.module.name(), 
							(trans.right.module.equals(Module.cheque) || trans.right.module.equals(Module.bill) ? "payrolls" : "trans"), 
							trans.id, trans.right.name());
				}

				result.add(trans);
			}
		}

		return result;
	}

	private static Right findShadowRight(Module refModule, Integer refId) {
		SqlRow row = Ebean.createSqlQuery("select _right from " + refModule.name() + "_trans where workspace = :workspace and ref_id = :ref_id")
							.setParameter("workspace", CacheUtils.getWorkspaceId())
							.setParameter("ref_id", refId)
						.findUnique();
		if (row != null) {
			String right = row.getString("_right");
			if (right != null) {
				return Right.valueOf(right);
			}
		}

		return null;
	}

	public static List<InvSummary> inspectXSummary(Module module, Integer id) {
		List<InvSummary> result = new ArrayList<InvSummary>();

		String query = "select trans_month, exc_code, sum(debt) as sumDebt, sum(credit) as sumCredit " +
						"from  :transTable " +
						"where workspace = " + CacheUtils.getWorkspaceId() +
						"  and :id = " + id +
						" group by trans_month, exc_code " +
						" order by trans_month, exc_code ";
		query = query.replaceAll("\\:transTable", module.name() + "_trans")
					.replaceAll("\\:id", module.name() + "_id");

		String excCode = null;
		double debt = 0d;
		double credit = 0d;

		List<SqlRow> rows = Ebean.createSqlQuery(query).findList();
		if (rows != null && rows.size() > 0) {
			for(SqlRow row: rows) {

				String exc_code = row.getString("exc_code");
				if (excCode != null && ! excCode.equals(exc_code)) {
					InvSummary sum = new InvSummary();
					sum.isImportant = Boolean.TRUE;
					sum.title = String.format("(%s) %s", excCode, Messages.get("totals"));
					sum.debt = debt;
					sum.credit = credit;
					sum.balance = (debt - credit);
					result.add(sum);

					debt = 0d;
					credit = 0d;
				}
				excCode = exc_code;

				InvSummary summary = new InvSummary();
				summary.title = row.getString("trans_month");
				summary.debt = row.getDouble("sumDebt");
				summary.credit = row.getDouble("sumCredit");
				summary.balance = (summary.debt - summary.credit);

				debt += summary.debt;
				credit += summary.credit;

				result.add(summary);
			}

			if ((debt - credit) != 0) {
				InvSummary sum = new InvSummary();
				sum.isImportant = Boolean.TRUE;
				sum.title = String.format("(%s) %s", excCode, Messages.get("totals"));
				sum.debt = debt;
				sum.credit = credit;
				sum.balance = (debt - credit);
				result.add(sum);
			}
		}

		return result;
	}

	public static void prepareForContactAgingReport(String balanceQuery, boolean isDebt, Date baseDate) {
		Ebean.beginTransaction();
		try {
			//Varsa eskiden kalan veriler silinir
			Ebean.createSqlUpdate("delete from temp_contact_aging "
								+ "where username = '" + CacheUtils.getUser().username + "'")
							.execute();

			//Borclu/Alacakli hesaplar secilir
			List<SqlRow> balanceRows = Ebean.createSqlQuery(balanceQuery).findList();
			if (balanceRows != null && balanceRows.size() > 0) {
				for(SqlRow bRow: balanceRows) {
					Integer contact_id = bRow.getInteger("contact_id");
					String exc_code = bRow.getString("exc_code");
					Double balance = bRow.getDouble("balance");

					StringBuilder transQSB = new StringBuilder("select c.name as contact_name, t.receipt_no, t.trans_date, t.trans_no, t._right, t.amount, t.exc_code, t.description ");
					transQSB.append("from  contact_trans t ");
					transQSB.append("inner join contact c on c.id = t.contact_id ");
					transQSB.append("where t.workspace = :workspace");
					transQSB.append("  and contact_id = :contact_id");
					transQSB.append("  and exc_code = :exc_code");
					transQSB.append("  and "+ (isDebt ? "debt" : "credit") +" > 0 ");
					transQSB.append("order by trans_date desc");

					SqlQuery selectTrans = Ebean.createSqlQuery(transQSB.toString());
					selectTrans.setParameter("workspace", CacheUtils.getWorkspaceId());
					selectTrans.setParameter("contact_id", contact_id);
					selectTrans.setParameter("exc_code", exc_code);
					selectTrans.setParameter("trans_date", DateUtils.formatDateForDB(baseDate));

					List<SqlRow> transRows = selectTrans.findList();
					if (transRows != null && transRows.size() > 0) {
						for(SqlRow tRow: transRows) {
							if (balance > 0) {

								double paid   = (balance >= tRow.getDouble("amount") ? 0 : tRow.getDouble("amount") - balance);
								double remain = (balance >= tRow.getDouble("amount") ? tRow.getDouble("amount") : balance);

								StringBuilder insertQSB = new StringBuilder("insert into temp_contact_aging ");
								insertQSB.append("(username, contact_name, receipt_no, trans_date, trans_no, _right, amount, paid, remain, exc_code, description)");
								insertQSB.append(" values ");
								insertQSB.append("(:username, :contact_name, :receipt_no, :trans_date, :trans_no, :_right, :amount, :paid, :remain, :exc_code, :description)");

								SqlUpdate insert = Ebean.createSqlUpdate(insertQSB.toString());
								insert.setParameter("username", CacheUtils.getUser().username);
								insert.setParameter("contact_name", tRow.getString("contact_name"));
								insert.setParameter("receipt_no", tRow.getInteger("receipt_no"));
								insert.setParameter("trans_date", tRow.getDate("trans_date"));
								insert.setParameter("trans_no", tRow.getString("trans_no"));
								insert.setParameter("_right", tRow.getString("_right"));
								insert.setParameter("exc_code", tRow.getString("exc_code"));
								insert.setParameter("description", tRow.getString("description"));
								insert.setParameter("amount", tRow.getDouble("amount"));
								insert.setParameter("paid", paid);
								insert.setParameter("remain", remain);
								insert.execute();

								balance -= tRow.getDouble("amount");
							} else {
								break;
							}
						}
					}
				}
			}
			Ebean.commitTransaction();
		} catch (Exception e) {
			Ebean.rollbackTransaction();
			log.error(e.getMessage());
		}
	}

	/**
	 * Cek/Senet - Firma/Musteri Acilis Bordrolari icin kapanma durumunu doner.
	 *  
	 * @param id
	 * @return boolean
	 */
	public static boolean isChqbllPayrollClosed(Integer id) {
		List<SqlRow> rows = Ebean.createSqlQuery("select detail_id, count(detail_id) as cnt from chqbll_detail_history "
												+ "where detail_id in (select id from chqbll_payroll_detail where trans_id = " + id +") "
												+ "group by detail_id")
											.findList();

		boolean result = false;
		if (rows != null && rows.size() > 0) {
			for (SqlRow row : rows) {
				result = (row.getInteger("cnt") != null && row.getInteger("cnt").intValue() > 1);
				if (result) break;
			}
		}

		/*
		 * Devir fisleri icin ozel olarak parcali odeme/tahsilat durumundaki detaylarda odeme olmus mu diye de kontrol edilir
		 */
		if (! result) {
			SqlRow row = Ebean.createSqlQuery("select count(trans_id) as cnt from chqbll_payroll_detail "
											+ "where trans_id = " + id
											+ "  and total_paid > 0 ")
										.findUnique();
			
			result = (row != null && row.getInteger("cnt") != null && row.getInteger("cnt").intValue() > 0);
		}

		return result;
	}

	public static void addExtraFieldsCriterias(ExtraFieldsForContact params, StringBuilder query) {
		addExtraFieldsCriterias(params, query, "");
	}

	public static void addExtraFieldsCriterias(ExtraFieldsForContact params, StringBuilder query, String alias) {
		if (params.extraField0 != null && params.extraField0.id != null) query.append(" and " + alias + "extra_field0_id = " + params.extraField0.id);
		if (params.extraField1 != null && params.extraField1.id != null) query.append(" and " + alias + "extra_field1_id = " + params.extraField1.id);
		if (params.extraField2 != null && params.extraField2.id != null) query.append(" and " + alias + "extra_field2_id = " + params.extraField2.id);
		if (params.extraField3 != null && params.extraField3.id != null) query.append(" and " + alias + "extra_field3_id = " + params.extraField3.id);
		if (params.extraField4 != null && params.extraField4.id != null) query.append(" and " + alias + "extra_field4_id = " + params.extraField4.id);
		if (params.extraField5 != null && params.extraField5.id != null) query.append(" and " + alias + "extra_field5_id = " + params.extraField5.id);
		if (params.extraField6 != null && params.extraField6.id != null) query.append(" and " + alias + "extra_field6_id = " + params.extraField6.id);
		if (params.extraField7 != null && params.extraField7.id != null) query.append(" and " + alias + "extra_field7_id = " + params.extraField7.id);
		if (params.extraField8 != null && params.extraField8.id != null) query.append(" and " + alias + "extra_field8_id = " + params.extraField8.id);
		if (params.extraField9 != null && params.extraField9.id != null) query.append(" and " + alias + "extra_field9_id = " + params.extraField9.id);
	}

	public static void addExtraFieldsCriterias(ExtraFieldsForStock params, StringBuilder query) {
		addExtraFieldsCriterias(params, query, "");
	}

	public static void addExtraFieldsCriterias(ExtraFieldsForStock params, StringBuilder query, String alias) {
		if (params.extraField0 != null && params.extraField0.id != null) query.append(" and " + alias + "extra_field0_id = " + params.extraField0.id);
		if (params.extraField1 != null && params.extraField1.id != null) query.append(" and " + alias + "extra_field1_id = " + params.extraField1.id);
		if (params.extraField2 != null && params.extraField2.id != null) query.append(" and " + alias + "extra_field2_id = " + params.extraField2.id);
		if (params.extraField3 != null && params.extraField3.id != null) query.append(" and " + alias + "extra_field3_id = " + params.extraField3.id);
		if (params.extraField4 != null && params.extraField4.id != null) query.append(" and " + alias + "extra_field4_id = " + params.extraField4.id);
		if (params.extraField5 != null && params.extraField5.id != null) query.append(" and " + alias + "extra_field5_id = " + params.extraField5.id);
		if (params.extraField6 != null && params.extraField6.id != null) query.append(" and " + alias + "extra_field6_id = " + params.extraField6.id);
		if (params.extraField7 != null && params.extraField7.id != null) query.append(" and " + alias + "extra_field7_id = " + params.extraField7.id);
		if (params.extraField8 != null && params.extraField8.id != null) query.append(" and " + alias + "extra_field8_id = " + params.extraField8.id);
		if (params.extraField9 != null && params.extraField9.id != null) query.append(" and " + alias + "extra_field9_id = " + params.extraField9.id);
	}

	public static <T extends ExtraFieldsForContact> String buildExtraFieldsQueryForContact(T efModel) {
		StringBuilder efsSB = new StringBuilder("");
		
		addLineForEF(Module.contact, 0, efModel.extraField0, efsSB);
		addLineForEF(Module.contact, 1, efModel.extraField1, efsSB);
		addLineForEF(Module.contact, 2, efModel.extraField2, efsSB);
		addLineForEF(Module.contact, 3, efModel.extraField3, efsSB);
		addLineForEF(Module.contact, 4, efModel.extraField4, efsSB);
		addLineForEF(Module.contact, 5, efModel.extraField5, efsSB);
		addLineForEF(Module.contact, 6, efModel.extraField6, efsSB);
		addLineForEF(Module.contact, 7, efModel.extraField7, efsSB);
		addLineForEF(Module.contact, 8, efModel.extraField8, efsSB);
		addLineForEF(Module.contact, 9, efModel.extraField9, efsSB);
		
		return efsSB.toString();
	}
	
	public static <T extends ExtraFieldsForStock> String buildExtraFieldsQueryForStock(T efModel) {
		StringBuilder efsSB = new StringBuilder("");
		
		addLineForEF(Module.stock, 0, efModel.extraField0, efsSB);
		addLineForEF(Module.stock, 1, efModel.extraField1, efsSB);
		addLineForEF(Module.stock, 2, efModel.extraField2, efsSB);
		addLineForEF(Module.stock, 3, efModel.extraField3, efsSB);
		addLineForEF(Module.stock, 4, efModel.extraField4, efsSB);
		addLineForEF(Module.stock, 5, efModel.extraField5, efsSB);
		addLineForEF(Module.stock, 6, efModel.extraField6, efsSB);
		addLineForEF(Module.stock, 7, efModel.extraField7, efsSB);
		addLineForEF(Module.stock, 8, efModel.extraField8, efsSB);
		addLineForEF(Module.stock, 9, efModel.extraField9, efsSB);
		
		return efsSB.toString();
	}

	public static <T extends ExtraFieldsForContact> String buildExtraFieldsQueryForContact(T efModel, String groupField) {
		StringBuilder efsSB = new StringBuilder("");
		
		addLineForEF(Module.contact, "ef0", 0, efModel.extraField0, efsSB);
		addLineForEF(Module.contact, "ef1", 1, efModel.extraField1, efsSB);
		addLineForEF(Module.contact, "ef2", 2, efModel.extraField2, efsSB);
		addLineForEF(Module.contact, "ef3", 3, efModel.extraField3, efsSB);
		addLineForEF(Module.contact, "ef4", 4, efModel.extraField4, efsSB);
		addLineForEF(Module.contact, "ef5", 5, efModel.extraField5, efsSB);
		addLineForEF(Module.contact, "ef6", 6, efModel.extraField6, efsSB);
		addLineForEF(Module.contact, "ef7", 7, efModel.extraField7, efsSB);
		addLineForEF(Module.contact, "ef8", 8, efModel.extraField8, efsSB);
		addLineForEF(Module.contact, "ef9", 9, efModel.extraField9, efsSB);
		
		return efsSB.toString();
	}
	
	public static <T extends ExtraFieldsForStock> String buildExtraFieldsQueryForStock(T efModel, String groupField) {
		StringBuilder efsSB = new StringBuilder("");
		
		addLineForEF(Module.stock, "ef0", 0, efModel.extraField0, efsSB);
		addLineForEF(Module.stock, "ef1", 1, efModel.extraField1, efsSB);
		addLineForEF(Module.stock, "ef2", 2, efModel.extraField2, efsSB);
		addLineForEF(Module.stock, "ef3", 3, efModel.extraField3, efsSB);
		addLineForEF(Module.stock, "ef4", 4, efModel.extraField4, efsSB);
		addLineForEF(Module.stock, "ef5", 5, efModel.extraField5, efsSB);
		addLineForEF(Module.stock, "ef6", 6, efModel.extraField6, efsSB);
		addLineForEF(Module.stock, "ef7", 7, efModel.extraField7, efsSB);
		addLineForEF(Module.stock, "ef8", 8, efModel.extraField8, efsSB);
		addLineForEF(Module.stock, "ef9", 9, efModel.extraField9, efsSB);
		
		return efsSB.toString();
	}

	private static void addLineForEF(Module module, int idno, BaseModel ef, StringBuilder toSB) {
		if (ef != null && ef.id != null) {
			toSB.append((" left join " + module.name() + "_extra_fields efç on efç.id = " + module.name().charAt(0)  + ".extra_fieldç_id ").replaceAll("ç", ""+idno));
		}
	}

	private static void addLineForEF(Module module, String groupField, int idno, BaseModel ef, StringBuilder toSB) {
		String starts = "ef"+idno;
		if (groupField.startsWith(starts) || (ef != null && ef.id != null)) {
			toSB.append(((! groupField.startsWith(starts) ? " inner " : " left ") + "join " + module.name() + "_extra_fields efç on efç.id = " + module.name().charAt(0)  + ".extra_fieldç_id ").replaceAll("ç", ""+idno));
		}
	}

}

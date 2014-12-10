/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package data.transfer.ws2ws;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.Bank;
import models.Contact;
import models.Safe;
import models.SaleSeller;
import models.Stock;
import models.StockDepot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.ebean.Model;
import utils.DateUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

/**
 * @author mdpinar
*/
abstract class BaseTransfer implements ITransfer {

	final static Logger log = LoggerFactory.getLogger(BaseTransfer.class);

	static Set<String> globalDeniedFieldSet;
	private static final String destroyQuery = "delete from %s where workspace = %d";

	static {
		globalDeniedFieldSet = new HashSet<String>();
		globalDeniedFieldSet.add("_idGetSet");
		globalDeniedFieldSet.add("id");
		globalDeniedFieldSet.add("workspace");
		globalDeniedFieldSet.add("insertBy");
		globalDeniedFieldSet.add("insertAt");
		globalDeniedFieldSet.add("updateBy");
		globalDeniedFieldSet.add("updateAt");
	}

	@Override
	public void transferTransaction(Date transDate, String description, int sourceWS, int targetWS) {
		;
	}
	
	@Override
	public void destroyTransaction(int targetWS, boolean willBeDestroyedAll) {
		;
	}
	
	int executeDeleteQueryForInfoTables(String tableName, int targetWS) {
		log.info(tableName + " verileri siliniyor...");
		return Ebean.createSqlUpdate(String.format(destroyQuery, tableName, targetWS)).execute();
	}

	int executeInsertQueryForInfoTables(Model model, int sourceWS, int targetWS) {
		return executeInsertQueryForInfoTables(model, sourceWS, targetWS, null);
	}

	int executeInsertQueryForInfoTables(Model model, int sourceWS, int targetWS, boolean isActiveWillBeAsked) {
		return executeInsertQueryForInfoTables(model, sourceWS, targetWS, null, isActiveWillBeAsked, null, null);
	}

	int executeInsertQueryForInfoTables(Model model, int sourceWS, int targetWS, Set<String> relNameSet) {
		return executeInsertQueryForInfoTables(model, sourceWS, targetWS, relNameSet, true, null, null);
	}

	int executeInsertQueryForInfoTables(Model model, int sourceWS, int targetWS, Set<String> relNameSet, boolean isActiveWillBeAsked) {
		return executeInsertQueryForInfoTables(model, sourceWS, targetWS, relNameSet, isActiveWillBeAsked, null, null);
	}

	int executeInsertQueryForInfoTables(Model model, int sourceWS, int targetWS, Set<String> relNameSet, Set<String> privateDeniedFieldSet) {
		return executeInsertQueryForInfoTables(model, sourceWS, targetWS, relNameSet, true, privateDeniedFieldSet, null);
	}

	int executeInsertQueryForInfoTables(Model model, int sourceWS, int targetWS, Set<String> relNameSet, boolean isActiveWillBeAsked, Set<String> privateDeniedFieldSet, String extraQuery) {
		String tableName = getNameConvention(model.getClass().getSimpleName());

		//Daha onceden girilmis kayit varsa islemden vazgecilir
		SqlRow row = Ebean.createSqlQuery("select count(workspace) as count from " + tableName + " where workspace = " + targetWS).findUnique();
		if (row != null) {
	        Integer count = row.getInteger("count");
			if (count != null && count.intValue() > 0) return 0;
		}

		//Tablo bos, kayitlar eklensin diye query donulur
		StringBuilder fieldNames = new StringBuilder();
		String[] names = model._ebean_getFieldNames();
		for (String name : names) {
			if (privateDeniedFieldSet != null && privateDeniedFieldSet.contains(name)) {
				continue;
			}
			if (! globalDeniedFieldSet.contains(name)) {
				if (relNameSet != null && relNameSet.contains(name)) {
					fieldNames.append(getNameConvention(name) + "_id");
				} else {
					fieldNames.append(getNameConvention(name));
				}
				fieldNames.append(", ");
			}
		}

		StringBuilder querySB = new StringBuilder("insert into ");
		querySB.append(tableName);
		querySB.append(" (");
		querySB.append(fieldNames);
		querySB.append("workspace, insert_by, insert_at");
		querySB.append(") ");

		querySB.append("select ");
		querySB.append(fieldNames);
		querySB.append(targetWS);
		querySB.append(", ");
		querySB.append("'super'");
		querySB.append(", ");
		querySB.append(DateUtils.formatLongDateForDB(new Date()));

		querySB.append(" from ");
		querySB.append(tableName);
		querySB.append(" where workspace = ");
		querySB.append(sourceWS);
		if (isActiveWillBeAsked) {
			querySB.append(" and is_active = :active ");
		}
		if (extraQuery != null) {
			querySB.append(extraQuery);
		}

		log.info(tableName + " is transferring...");

		return Ebean.createSqlUpdate(querySB.toString()).setParameter("active", Boolean.TRUE).execute();
	}
	
	void updateRelation(String mainTable, String relationTable, String updateField, String finderField, int sourceWS, int targetWS) {
		String updateQuery = "update " + mainTable + " set " + updateField + " = :update_id where " + updateField + " = :finder_id and workspace = :workspace";

		Map<Integer, Integer> ididMap = findNewRelations(mainTable, relationTable, finderField, sourceWS, targetWS);
		if (ididMap.size() > 0) {
			for (Entry<Integer, Integer> entry : ididMap.entrySet()) {
				Ebean.createSqlUpdate(updateQuery)
									.setParameter("update_id", entry.getValue())
									.setParameter("finder_id", entry.getKey())
									.setParameter("workspace", targetWS)
								.execute();
			}
		} else { //set null them all
			Ebean.createSqlUpdate("update " + mainTable + " set " + updateField + " = null where workspace = :workspace").setParameter("workspace", targetWS).execute();
		}
	}

	Map<Integer, Integer> findNewRelations(String mainTable, String relationTable, String finderField, int sourceWS, int targetWS) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();

		String query = "select id, " + finderField + " from " + relationTable + " where workspace = :workspace";
		Map<String, Integer> map1 = new HashMap<String, Integer>();

		List<SqlRow> mainRows = Ebean.createSqlQuery(query)
									.setParameter("workspace", sourceWS)
								.findList();
		
		if (mainRows != null && mainRows.size() > 0) {
			for(SqlRow row: mainRows) {
				map1.put(row.getString(finderField), row.getInteger("id"));
			}

			Map<String, Integer> map2 = new HashMap<String, Integer>();
			List<SqlRow> slaveRows = Ebean.createSqlQuery(query)
										.setParameter("workspace", targetWS)
									.findList();
			
			if (slaveRows != null && slaveRows.size() > 0) {
				for(SqlRow row: slaveRows) {
					map2.put(row.getString(finderField), row.getInteger("id"));
				}

				for (Entry<String, Integer> entry : map2.entrySet()) {
					result.put(map1.get(entry.getKey()), entry.getValue());
				}
			} else {
				for (Integer id: map1.values()) {
					result.put(id, null);
				}
			}
		}

		return result;
	}

	String getNameConvention(String name) {
		String result = name
						.replaceAll("(.)(\\p{Lu})", "$1_$2").toLowerCase()   //converts BenimAdimMustafa to benim_adim_mustafa
						.replaceAll("(.)(\\d)(\\_)", "$1$2").toLowerCase();  //converts Bugun5MartSali to bugun5mart_sali
		return result;
	}

	Contact findContactInTargetWS(Integer id, int targetWS) {
		if (id == null) return null;
		
		SqlRow row = Ebean.createSqlQuery("select id from contact where workspace = :workspace and code = (select code from contact where id = :id )")
							.setParameter("id", id)
							.setParameter("workspace", targetWS)
						.findUnique();
		if (row != null) {
			Integer idValue = row.getInteger("id");
			if (idValue != null) {
				return Ebean.find(Contact.class, idValue);
			}
		}
		return null;
	}

	Safe findAccordingToSourceWS(Integer id, int targetWS) {
		if (id == null) return null;

		SqlRow row = Ebean.createSqlQuery("select id from safe where workspace = :workspace and code = (select code from safe where id = :id )")
							.setParameter("id", id)
							.setParameter("workspace", targetWS)
						.findUnique();
		if (row != null) {
	        Integer idValue = row.getInteger("id");
			if (idValue != null) {
				return Ebean.find(Safe.class, idValue);
			}
		}
		return null;
	}

	Bank findBankInTargetWS(Integer id, int targetWS) {
		if (id == null) return null;

		SqlRow row = Ebean.createSqlQuery("select id from bank where workspace = :workspace and account_no = (select account_no from bank where id = :id )")
							.setParameter("id", id)
							.setParameter("workspace", targetWS)
						.findUnique();
		if (row != null) {
	        Integer idValue = row.getInteger("id");
			if (idValue != null) {
				return Ebean.find(Bank.class, idValue);
			}
		}
		return null;
	}

	Stock findStockInTargetWS(Integer id, int targetWS) {
		if (id == null) return null;

		SqlRow row = Ebean.createSqlQuery("select id from stock where workspace = :workspace and code = (select code from stock where id = :id )")
							.setParameter("id", id)
							.setParameter("workspace", targetWS)
						.findUnique();
		if (row != null) {
	        Integer idValue = row.getInteger("id");
			if (idValue != null) {
				return Ebean.find(Stock.class, idValue);
			}
		}
		return null;
	}

	StockDepot findDepotInTargetWS(Integer id, int targetWS) {
		if (id == null) return null;

		SqlRow row = Ebean.createSqlQuery("select id from stock_depot where workspace = :workspace and code = (select code from stock_depot where id = :id )")
							.setParameter("id", id)
							.setParameter("workspace", targetWS)
						.findUnique();
		if (row != null) {
	        Integer idValue = row.getInteger("id");
			if (idValue != null) {
				return Ebean.find(StockDepot.class, idValue);
			}
		}
		return null;
	}

	SaleSeller findSellerInTargetWS(Integer id, int targetWS) {
		if (id == null) return null;
		
		SqlRow row = Ebean.createSqlQuery("select id from sale_seller where workspace = :workspace and name = (select name from sale_seller where id = :id )")
				.setParameter("id", id)
				.setParameter("workspace", targetWS)
				.findUnique();
		if (row != null) {
			Integer idValue = row.getInteger("id");
			if (idValue != null) {
				return Ebean.find(SaleSeller.class, idValue);
			}
		}
		return null;
	}

}

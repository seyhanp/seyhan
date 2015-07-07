package utils;

import java.util.Date;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import enums.Module;

/**
 * Siparis, Irsaliye ve Fatura kayitlari ekstra olarak durum tarihcesi (xxx_trans_status_history) tutarlar, 
 * tarihce kayitlari da bu utils sinifi araciligi ile yonetilirler
 * 
 * @author mdpinar
 *
 */
public class TransStatusHistoryUtils {

	/**
	 * Bir hareket kaydina ait tutulmus olan tarihçenin tamamı silinir
	 *  
	 * @param trans id
	 * @param module
	 */
	public static void deleteAllHistory(Module module, int id) {
		Ebean.createSqlUpdate("delete from " + module.name() + "_trans_status_history where trans_id = :trans_id")
								.setParameter("trans_id", id)
							.execute();
	}

	/**
	 * Bir hareketin yeni durumunu tarihceye ekler
	 * 
	 * @param trans
	 * @param newStatus
	 * @param description
	 */
	public static void goForward(Module module, int transId, int statusId, String description) {
		Ebean.createSqlUpdate("insert into " + module.name() + "_trans_status_history (trans_id, status_id, username, description, trans_time) " +
								"values (:trans_id, :status_id, :username, :description, :trans_time) ")
							.setParameter("trans_id", transId)
							.setParameter("status_id", statusId)
							.setParameter("username", CacheUtils.getUser().username)
							.setParameter("description", description)
							.setParameter("trans_time", new Date())
						.execute();
		setTransStatus(module, transId, statusId);
	}

	/**
	 * Bir hareketin son durumunu silerek bir onceki duruma getirir
	 * 
	 * @param module
	 * @param transId
	 */
	public static void goBack(Module module, int transId) {
		List<SqlRow> rowList = Ebean.createSqlQuery("select id, status_id from " + module.name() + "_trans_status_history where trans_id = :trans_id order by id desc")
													.setParameter("trans_id", transId)
													.setMaxRows(2)
												.findList();

		if (rowList != null && rowList.size() > 0) {
			Ebean.createSqlUpdate("delete from " + module.name() + "_trans_status_history where id = :id ")
								.setParameter("id", rowList.get(0).getInteger("id"))
							.execute();
			
			Integer statusId = null;
			if (rowList.size() > 1) {
				statusId = rowList.get(1).getInteger("status_id");
			}
			setTransStatus(module, transId, statusId);
		}
	}

	/**
	 * Hareketin son durumunu setler
	 * 
	 * @param module
	 * @param transId
	 * @param statusId
	 */
	private static void setTransStatus(Module module, Integer transId, Integer statusId) {
		Ebean.createSqlUpdate("update " + module.name() + "_trans set status_id = :status_id where id = :id")
				.setParameter("id", transId)
				.setParameter("status_id", statusId)
			.execute();
		
		Ebean.createSqlUpdate("update " + module.name() + "_trans_detail set status_id = :status_id where trans_id = :trans_id")
				.setParameter("trans_id", transId)
				.setParameter("status_id", statusId)
			.execute();
	}
	
}

package db.migration;

import java.sql.Connection;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.GlobalCons;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;

/**
 * AdminUserAudit tablosudaki 'Description' alaninin uzunlugu 100'den 255'e cikarildi
 *  
 * @author mdpinar
 */

public class V1_0_6__Increasing_desdciption_length_in_AdminUserAudit_table implements JdbcMigration {

	private final static Logger log = LoggerFactory.getLogger(V1_0_6__Increasing_desdciption_length_in_AdminUserAudit_table.class);

	@Override
	public void migrate(Connection con) throws Exception {
		log.info("Altering AdminUserAudit table to increase Description field length is executing...");

		if (! GlobalCons.isInitScriptExecuted) {
			Statement sta = con.createStatement();
			sta.executeUpdate("alter table admin_user_audit add column descr varchar(255)");
			sta.executeUpdate("update admin_user_audit set descr = description");
			sta.executeUpdate("alter table admin_user_audit drop column description");
			
			if (GlobalCons.dbVendor.equals("mysql")) {
				sta.executeUpdate("alter table admin_user_audit change descr description varchar(255)");
			} else if (GlobalCons.dbVendor.equals("sqlserver")) {
				sta.executeUpdate("EXEC sp_rename 'admin_user_audit.descr', 'description', 'COLUMN';");
			} else if (GlobalCons.dbVendor.equals("h2")) {
				sta.executeUpdate("alter table admin_user_audit alter column descr rename to description");
			} else {
				sta.executeUpdate("alter table admin_user_audit rename column descr to description");
			}
		}

		log.info("AdminUserAudit table has been altered.");
	}

}

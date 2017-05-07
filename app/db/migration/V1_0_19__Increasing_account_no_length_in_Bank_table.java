package db.migration;

import java.sql.Connection;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.GlobalCons;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;

/**
 * Bank tablosudaki 'account_no' alaninin uzunlugu 15'den 26'ya cikarildi
 *  
 * @author mdpinar
 */

public class V1_0_19__Increasing_account_no_length_in_Bank_table implements JdbcMigration {

	private final static Logger log = LoggerFactory.getLogger(V1_0_19__Increasing_account_no_length_in_Bank_table.class);

	@Override
	public void migrate(Connection con) throws Exception {
		log.info("Altering Bank table to increase account_no field length is executing...");

		if (! GlobalCons.isInitScriptExecuted) {
			Statement sta = con.createStatement();
			sta.executeUpdate("alter table bank add column acc_no varchar(26)");
			sta.executeUpdate("update bank set acc_no = account_no");
			sta.executeUpdate("alter table bank drop column account_no");
			
			if (GlobalCons.dbVendor.equals("mysql")) {
				sta.executeUpdate("alter table bank change acc_no account_no varchar(255)");
			} else if (GlobalCons.dbVendor.equals("sqlserver")) {
				sta.executeUpdate("EXEC sp_rename 'bank.acc_no', 'account_no', 'COLUMN';");
			} else if (GlobalCons.dbVendor.equals("h2")) {
				sta.executeUpdate("alter table bank alter column acc_no rename to account_no");
			} else {
				sta.executeUpdate("alter table bank rename column acc_no to account_no");
			}
		}

		log.info("Bank table has been altered.");
	}

}

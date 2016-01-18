package db.migration;

import java.sql.Connection;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.GlobalCons;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;

/**
 * Cari, Kasa ve Banka hareket tablolarina 'trans_dir' alani eklendi
 * Bu alan uzerinde hareketin borc ya da alacak olup olmadigi 0 - 1 yapisinda tutulacak 
 *  
 * @author mdpinar
 */

public class V1_0_12__Adding_transDir_field_to_DocumentBased_Trans_Tables implements JdbcMigration {

	private final static Logger log = LoggerFactory.getLogger(V1_0_12__Adding_transDir_field_to_DocumentBased_Trans_Tables.class);

	@Override
	public void migrate(Connection con) throws Exception {
		log.info("Altering ContactTrans, SafeTrans and BankTrans tables in order to add trans_dir field...");

		if (! GlobalCons.isInitScriptExecuted) {
			Statement sta = con.createStatement();
			
			if (GlobalCons.dbVendor.equals("mysql") || GlobalCons.dbVendor.equals("h2")) {
				sta.executeUpdate("alter table contact_trans add column trans_dir int(1) default 0;");
				sta.executeUpdate("alter table safe_trans add column trans_dir int(1) default 0;");
				sta.executeUpdate("alter table bank_trans add column trans_dir int(1) default 0;");
			} else if (GlobalCons.dbVendor.equals("sqlserver")) {
				sta.executeUpdate("alter table contact_trans add column trans_dir numeric(1) default 0;");
				sta.executeUpdate("alter table safe_trans add column trans_dir numeric(1) default 0;");
				sta.executeUpdate("alter table bank_trans add column trans_dir numeric(1) default 0;");
			} else {
				sta.executeUpdate("alter table contact_trans add column trans_dir smallint default 0;");
				sta.executeUpdate("alter table safe_trans add column trans_dir smallint default 0;");
				sta.executeUpdate("alter table bank_trans add column trans_dir smallint default 0;");
			}

			sta.executeUpdate("update contact_trans set trans_dir = 1 where credit > 0;");
			sta.executeUpdate("update safe_trans set trans_dir = 1 where credit > 0;");
			sta.executeUpdate("update bank_trans set trans_dir = 1 where credit > 0;");
		}

		log.info("ContactTrans, SafeTrans and BankTrans tables have been altered.");
	}

}

package db.migration;

import java.sql.Connection;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.GlobalCons;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;

public class V1_0_4__Changing_TC_No_data_type_in_Contact_table implements JdbcMigration {

	private final static Logger log = LoggerFactory.getLogger(V1_0_4__Changing_TC_No_data_type_in_Contact_table.class);

	@Override
	public void migrate(Connection con) throws Exception {
		log.info("Altering Contact table for TC No field type changing...");

		Statement sta = con.createStatement();
		sta.executeUpdate("alter table contact add column tc varchar(11)");
		sta.executeUpdate("update contact set tc = tc_kimlik");
		sta.executeUpdate("alter table contact drop column tc_kimlik");
		
		if (GlobalCons.dbVendor.equals("mysql")) {
			sta.executeUpdate("alter table contact change tc tc_kimlik varchar(11)");
		} else  if (GlobalCons.dbVendor.equals("mssql")) {
			sta.executeUpdate("EXEC sp_rename 'tc', 'tc_kimlik_no', 'COLUMN';");
		} else {
			sta.executeUpdate("alter table contact rename column tc to tc_kimlik");
		}

		log.info("Contact table has altered.");
	}

}

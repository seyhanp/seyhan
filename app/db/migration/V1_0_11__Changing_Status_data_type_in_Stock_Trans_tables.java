package db.migration;

import java.sql.Connection;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.GlobalCons;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;

/**
 * Siparis, Irsaliye ve Fatura hareket tablolarindaki Status alani version 1.0.11 ten once enum tipindeydi,
 * bu sinif ile tanimlanabilir hale getirilip hareket tablolari icin lookup table yapisina (many-to-one) cevrildi.
 * 
 * status_id integer,
 *  
 * @author mdpinar
 */

public class V1_0_11__Changing_Status_data_type_in_Stock_Trans_tables implements JdbcMigration {

	private final static Logger log = LoggerFactory.getLogger(V1_0_11__Changing_Status_data_type_in_Stock_Trans_tables.class);

	@Override
	public void migrate(Connection con) throws Exception {
		log.info("Altering order, waybill and invoice trans tables on " + GlobalCons.dbVendor + " to change Status field type.");
		
		if (! GlobalCons.isInitScriptExecuted) {
			String[] tables = {"order", "waybill", "invoice"};
			
			Statement sta = con.createStatement();
			for (String table : tables) {
				log.info(" altering " + table + "_trans on " + GlobalCons.dbVendor + " to change Status field type is executing...");

				log.info("  -- creating status tables for " + table);
				if (GlobalCons.dbVendor.equals("h2")) executeScripts_H2(sta, table);
				if (GlobalCons.dbVendor.equals("mysql")) executeScripts_MYSQL(sta, table);
				if (GlobalCons.dbVendor.equals("postgresql")) executeScripts_POSTGRESQL(sta, table);
				if (GlobalCons.dbVendor.equals("sqlserver")) executeScripts_SQLSERVER(sta, table);

				log.info("  -- creating status tables' indexes and relations for " + table);
				executeScripts_ToCreateIndexesAndRelations(sta, table);

				log.info("  -- adding new status definitions for " + table);
				executeScripts_ToAddNewStatus(sta, table);

				log.info("  -- modifying old trans table indexes for " + table);
				executeScripts_ToModifyTransIndex(sta, table);

				log.info("  -- changing status column's type to a relation for " + table);
				executeScripts_ToChangeStatusColumnType(sta, table);

				log.info(" " + table + "_trans is altered successfuly.");
			}

			log.info("  adding is_completed field to all stock trans tables");
			executeScripts_ToAddIsCompletedFieldToStockTransTables(sta);
			
			sta.executeBatch();
		}

		log.info("order, waybill and invoice trans tables are altered.");
	}

	private void executeScripts_H2(Statement sta, String table) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("create table " + table + "_trans_status (");
		sb.append("  id                        int auto_increment not null,");
		sb.append("  parent_id                 int,");
		sb.append("  name                      varchar(30) not null,");
		sb.append("  insert_by                 varchar(20),");
		sb.append("  insert_at                 datetime,");
		sb.append("  update_by                 varchar(20),");
		sb.append("  update_at                 datetime,");
		sb.append("  is_active                 boolean default true,");
		sb.append("  workspace                 int not null,");
		sb.append("  version                   int default 0,");
		sb.append("  primary key (id)");
		sb.append("); ");
		sb.append("create sequence " + table + "_trans_status_seq; ");

		sta.addBatch(sb.toString());
		sb.setLength(0);

		sb.append("create table " + table + "_trans_status_history (");
		sb.append("  id                        int auto_increment not null,");
		sb.append("  trans_id                  int not null,");
		sb.append("  status_id                 int not null,");
		sb.append("  trans_time                datetime,");
		sb.append("  username                  varchar(20),");
		sb.append("  description               varchar(150),");
		sb.append("  primary key (id)");
		sb.append("); ");
		sb.append("create sequence " + table + "_trans_status_history_seq; ");

		sta.addBatch(sb.toString());
	}

	private void executeScripts_MYSQL(Statement sta, String table) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		sb.append("create table " + table + "_trans_status (");
		sb.append("  id                        int auto_increment not null,");
		sb.append("  parent_id                 int,");
		sb.append("  name                      varchar(30) not null,");
		sb.append("  insert_by                 varchar(20),");
		sb.append("  insert_at                 datetime,");
		sb.append("  update_by                 varchar(20),");
		sb.append("  update_at                 datetime,");
		sb.append("  is_active                 tinyint(1) default 1,");
		sb.append("  workspace                 int not null,");
		sb.append("  version                   int default 0,");
		sb.append("  primary key (id)");
		sb.append(") engine=innodb default charset=utf8; ");

		sta.addBatch(sb.toString());
		sb.setLength(0);

		sb.append("create table " + table + "_trans_status_history (");
		sb.append("  id                        int auto_increment not null,");
		sb.append("  trans_id                  int not null,");
		sb.append("  status_id                 int not null,");
		sb.append("  trans_time                datetime,");
		sb.append("  username                  varchar(20),");
		sb.append("  description               varchar(150),");
		sb.append("  primary key (id)");
		sb.append(") engine=innodb default charset=utf8; ");

		sta.addBatch(sb.toString());
	}

	private void executeScripts_POSTGRESQL(Statement sta, String table) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		sb.append("create table " + table + "_trans_status (");
		sb.append("  id                        serial not null,");
		sb.append("  parent_id                 integer,");
		sb.append("  name                      varchar(30) not null,");
		sb.append("  insert_by                 varchar(20),");
		sb.append("  insert_at                 timestamp,");
		sb.append("  update_by                 varchar(20),");
		sb.append("  update_at                 timestamp,");
		sb.append("  is_active                 boolean default true,");
		sb.append("  workspace                 integer not null,");
		sb.append("  version                   integer default 0,");
		sb.append("  primary key (id)");
		sb.append("); ");
		
		sta.addBatch(sb.toString());
		sta.addBatch("create sequence " + table + "_trans_status_seq; ");
		sb.setLength(0);
		
		sb.append("create table " + table + "_trans_status_history (");
		sb.append("  id                        serial not null,");
		sb.append("  trans_id                  integer not null,");
		sb.append("  status_id                 integer not null,");
		sb.append("  trans_time                timestamp,");
		sb.append("  username                  varchar(20),");
		sb.append("  description               varchar(150),");
		sb.append("  primary key (id)");
		sb.append("); ");
		sb.append("create sequence " + table + "_trans_status_history_seq; ");

		sta.addBatch(sb.toString());
	}

	private void executeScripts_SQLSERVER(Statement sta, String table) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		sb.append("create table " + table + "_trans_status (");
		sb.append("  id                        integer identity(1,1) primary key,");
		sb.append("  parent_id                 integer,");
		sb.append("  name                      varchar(30) not null,");
		sb.append("  insert_by                 varchar(20),");
		sb.append("  insert_at                 datetime,");
		sb.append("  update_by                 varchar(20),");
		sb.append("  update_at                 datetime,");
		sb.append("  is_active                 boolean default true,");
		sb.append("  workspace                 integer not null,");
		sb.append("  version                   integer default 0");
		sb.append("); ");
		
		sta.addBatch(sb.toString());
		sb.setLength(0);
		
		sb.append("create table " + table + "_trans_status_history (");
		sb.append("  id                        integer identity(1,1) primary key,");
		sb.append("  trans_id                  integer not null,");
		sb.append("  status_id                 integer not null,");
		sb.append("  trans_time                datetime,");
		sb.append("  username                  varchar(20),");
		sb.append("  description               varchar(150)");
		sb.append("); ");

		sta.addBatch(sb.toString());
	}

	private void executeScripts_ToAddNewStatus(Statement sta, String table) throws Exception {
		if (GlobalCons.dbVendor.equals("sqlserver")) {
			sta.addBatch("insert into " + table + "_trans_status (name) values ('Beklemede'); ");
			sta.addBatch("insert into " + table + "_trans_status (name, parent_id) values ('Kapandı', (select id from " + table + "_trans_status where name = 'Beklemede')); ");
		} else {
			sta.addBatch("insert into " + table + "_trans_status (id, name) values (1, 'Beklemede'); ");
			sta.addBatch("insert into " + table + "_trans_status (id, name, parent_id) values (2, 'Kapandı', 1); ");
		}
	}

	private void executeScripts_ToModifyTransIndex(Statement sta, String table) throws Exception {
		if (GlobalCons.dbVendor.equals("mysql")) {
			sta.addBatch("drop index " + table + "_trans_ix1 on " + table + "_trans; ");
		} else if (GlobalCons.dbVendor.equals("sqlserver")) {
			sta.addBatch("drop index " + table + "_trans." + table + "_trans_ix1; ");
		} else {
			sta.addBatch("drop index " + table + "_trans_ix1; ");
		}

		sta.addBatch("create index " + table + "_trans_ix1 on " + table + "_trans (workspace, _right, trans_date); ");
	}

	private void executeScripts_ToCreateIndexesAndRelations(Statement sta, String table) throws Exception {
		sta.addBatch("create index " + table + "_trans_status_ix1 on " + table + "_trans_status (name); ");
		sta.addBatch("alter table " + table + "_trans_status add foreign key (parent_id) references " + table + "_trans_status (id); ");
		sta.addBatch("alter table " + table + "_trans_status_history add foreign key (trans_id) references " + table + "_trans (id); ");
		sta.addBatch("alter table " + table + "_trans_status_history add foreign key (status_id) references " + table + "_trans_status (id); ");
	}
	
	private void executeScripts_ToChangeStatusColumnType(Statement sta, String table) throws Exception {

		sta.addBatch("alter table " + table + "_trans add column temp_status_id integer; ");
		sta.addBatch("alter table " + table + "_trans_detail add column temp_status_id integer; ");

		sta.addBatch("update " + table + "_trans set temp_status_id = 1 where status <> 'Kapandı'; ");
		sta.addBatch("update " + table + "_trans_detail set temp_status_id = 1 where status <> 'Kapandı'; ");

		sta.addBatch("update " + table + "_trans set temp_status_id = 2 where status = 'Kapandı'; ");
		sta.addBatch("update " + table + "_trans_detail set temp_status_id = 2 where status = 'Kapandı'; ");

		sta.addBatch("alter table " + table + "_trans drop column status; ");
		sta.addBatch("alter table " + table + "_trans_detail drop column status; ");

		if (GlobalCons.dbVendor.equals("mysql")) {
			sta.addBatch("alter table " + table + "_trans change temp_status_id status_id int; ");
			sta.addBatch("alter table " + table + "_trans_detail change temp_status_id status_id int; ");
		} else if (GlobalCons.dbVendor.equals("sqlserver")) {
			sta.addBatch("EXEC sp_rename " + table + "'_trans.temp_status_id', 'status_id', 'COLUMN'; ");
			sta.addBatch("EXEC sp_rename " + table + "'_trans_detail.temp_status_id', 'status_id', 'COLUMN'; ");
		} else if (GlobalCons.dbVendor.equals("h2")) {
			sta.addBatch("alter table " + table + "_trans alter column temp_status_id rename to status_id; ");
			sta.addBatch("alter table " + table + "_trans_detail alter column temp_status_id rename to status_id; ");
		} else {
			sta.addBatch("alter table " + table + "_trans rename column temp_status_id to status_id; ");
			sta.addBatch("alter table " + table + "_trans_detail rename column temp_status_id to status_id; ");
		}

		sta.addBatch("alter table " + table + "_trans add foreign key (status_id) references " + table + "_trans_status (id); ");
		sta.addBatch("alter table " + table + "_trans_detail add foreign key (status_id) references " + table + "_trans_status (id); ");
	}

	private void executeScripts_ToAddIsCompletedFieldToStockTransTables(Statement sta) throws Exception {
		if (GlobalCons.dbVendor.equals("mysql")) {
			sta.addBatch("alter table stock_trans add column is_completed tinyint(1) default 0; ");
			sta.addBatch("alter table order_trans add column is_completed tinyint(1) default 0; ");
			sta.addBatch("alter table waybill_trans add column is_completed tinyint(1) default 0; ");
			sta.addBatch("alter table invoice_trans add column is_completed tinyint(1) default 0; ");
		} else if (GlobalCons.dbVendor.equals("sqlserver")) {
			sta.addBatch("alter table stock_trans add column is_completed bit default 0; ");
			sta.addBatch("alter table order_trans add column is_completed bit default 0; ");
			sta.addBatch("alter table waybill_trans add column is_completed bit default 0; ");
			sta.addBatch("alter table invoice_trans add column is_completed bit default 0; ");
		} else {
			sta.addBatch("alter table stock_trans add column is_completed boolean default false; ");
			sta.addBatch("alter table order_trans add column is_completed boolean default false; ");
			sta.addBatch("alter table waybill_trans add column is_completed boolean default false; ");
			sta.addBatch("alter table invoice_trans add column is_completed boolean default false; ");
		}
		
	}

}

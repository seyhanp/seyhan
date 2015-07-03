package db.migration;

import java.sql.Connection;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.GlobalCons;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;

/**
 * Siparis, Irsaliye ve Fatura hareket tablolarindaki tipindeki Status alani version 1.0.11 ten once enum tipindeydi,
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
		String[] tables = {"order", "waybill", "invoice"};
		
		Statement sta = con.createStatement();
		for (String table : tables) {
			log.info("Altering " + table + "_trans on " + GlobalCons.dbVendor + " to change Status field type is executing...");

			if (GlobalCons.dbVendor.equals("h2")) executeScripts_H2(sta, table);
			if (GlobalCons.dbVendor.equals("mysql")) executeScripts_MYSQL(sta, table);
			if (GlobalCons.dbVendor.equals("postgresql")) executeScripts_POSTGRESQL(sta, table);
			if (GlobalCons.dbVendor.equals("sqlserver")) executeScripts_SQLSERVER(sta, table);

			executeScripts_ToAddNewStatus(sta, table);
			executeScripts_ToChnageStatusColumnType(sta, table);

			log.info("" + table + "_trans is altered.");
		}
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
		sb.append("  version                   int default 0,");
		sb.append("  primary key (id)");
		sb.append("); ");
		sb.append("create index " + table + "_trans_status_ix1 on invoice_trans_status (workspace, name); ");
		sb.append("create sequence " + table + "_trans_status_seq; ");

		sb.append("create table " + table + "_trans_status_history (");
		sb.append("  id                        int auto_increment not null,");
		sb.append("  trans_time                datetime,");
		sb.append("  trans_id                  int not null,");
		sb.append("  status_id                 int not null,");
		sb.append("  username                  varchar(20),");
		sb.append("  primary key (id)");
		sb.append("); ");
		sb.append("create sequence " + table + "_trans_status_history_seq; ");

		sb.append("alter table " + table + "_trans_status add foreign key (parent_id) references " + table + "_trans_status (id); ");
		sb.append("alter table " + table + "_trans_status_history add foreign key (trans_id) references " + table + "_trans (id); ");
		sb.append("alter table " + table + "_trans_status_history add foreign key (status_id) references " + table + "_trans_status (id); ");

		
		sta.executeUpdate("alter table " + table + "_trans add column t_status_id int");

		sta.executeUpdate(sb.toString());
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
		sb.append("  version                   int default 0,");
		sb.append("  primary key (id)");
		sb.append(") engine=innodb default charset=utf8; ");
		sb.append("create index " + table + "_trans_status_ix1 on invoice_trans_status (workspace, name); ");
		
		sb.append("create table " + table + "_trans_status_history (");
		sb.append("  id                        int auto_increment not null,");
		sb.append("  trans_time                datetime,");
		sb.append("  trans_id                  int not null,");
		sb.append("  status_id                 int not null,");
		sb.append("  username                  varchar(20),");
		sb.append("  primary key (id)");
		sb.append(") engine=innodb default charset=utf8; ");

		sb.append("alter table " + table + "_trans_status add foreign key (parent_id) references " + table + "_trans_status (id); ");
		sb.append("alter table " + table + "_trans_status_history add foreign key (trans_id) references " + table + "_trans (id); ");
		sb.append("alter table " + table + "_trans_status_history add foreign key (status_id) references " + table + "_trans_status (id); ");

		sta.executeUpdate(sb.toString());
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
		sb.append("  version                   integer default 0,");
		sb.append("  primary key (id)");
		sb.append("); ");
		sb.append("create index " + table + "_trans_status_ix1 on invoice_trans_status (workspace, name); ");
		sb.append("create sequence " + table + "_trans_status_seq; ");
		
		sb.append("create table " + table + "_trans_status_history (");
		sb.append("  id                        serial not null,");
		sb.append("  trans_time                timestamp,");
		sb.append("  trans_id                  integer not null,");
		sb.append("  status_id                 integer not null,");
		sb.append("  username                  varchar(20),");
		sb.append("  primary key (id)");
		sb.append("); ");
		sb.append("create sequence " + table + "_trans_status_history_seq; ");

		sb.append("alter table " + table + "_trans_status add foreign key (parent_id) references " + table + "_trans_status (id); ");
		sb.append("alter table " + table + "_trans_status_history add foreign key (trans_id) references " + table + "_trans (id); ");
		sb.append("alter table " + table + "_trans_status_history add foreign key (status_id) references " + table + "_trans_status (id); ");

		sta.executeUpdate(sb.toString());
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
		sb.append("  version                   integer default 0");
		sb.append("); ");
		sb.append("create index " + table + "_trans_status_ix1 on invoice_trans_status (workspace, name); ");
		
		sb.append("create table " + table + "_trans_status_history (");
		sb.append("  id                        integer identity(1,1) primary key,");
		sb.append("  trans_time                datetime,");
		sb.append("  trans_id                  integer not null,");
		sb.append("  status_id                 integer not null,");
		sb.append("  username                  varchar(20)");
		sb.append("); ");

		sb.append("alter table " + table + "_trans_status add foreign key (parent_id) references " + table + "_trans_status (id); ");
		sb.append("alter table " + table + "_trans_status_history add foreign key (trans_id) references " + table + "_trans (id); ");
		sb.append("alter table " + table + "_trans_status_history add foreign key (status_id) references " + table + "_trans_status (id); ");

		sta.executeUpdate(sb.toString());
	}

	private void executeScripts_ToAddNewStatus(Statement sta, String table) throws Exception {
		StringBuilder sb = new StringBuilder();

		if (GlobalCons.dbVendor.equals("sqlserver")) {
			sb.append("insert into " + table + "_trans_status (name) values ('Beklemede'); ");
			sb.append("insert into " + table + "_trans_status (name, parent_id) values ('Kapandı', (select id from " + table + "_trans_status where name = 'Beklemede')); ");
		} else {
			sb.append("insert into " + table + "_trans_status (id, name) values (1, 'Beklemede'); ");
			sb.append("insert into " + table + "_trans_status (id, name, parent_id) values (2, 'Kapandı', 1); ");
		}
		
		sta.executeUpdate(sb.toString());
	}

	private void executeScripts_ToChnageStatusColumnType(Statement sta, String table) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("alter table " + table + "_trans add column temp_status_id integer; ");
		sb.append("alter table " + table + "_trans_detail add column temp_status_id integer; ");
		
		sb.append("update " + table + "_trans set temp_status_id = 1 where status <> 'Kapandı'; ");
		sb.append("update " + table + "_trans_detail set temp_status_id = 1 where status <> 'Kapandı'; ");

		sb.append("update " + table + "_trans set temp_status_id = 2 where status = 'Kapandı'; ");
		sb.append("update " + table + "_trans_detail set temp_status_id = 2 where status = 'Kapandı'; ");

		sb.append("alter table " + table + "_trans drop column status; ");
		sb.append("alter table " + table + "_trans_detail drop column status; ");

		if (GlobalCons.dbVendor.equals("mysql")) {
			sb.append("alter table " + table + "_trans change temp_status_id status_id int; ");
			sb.append("alter table " + table + "_trans_detail change temp_status_id status_id int; ");
		} else if (GlobalCons.dbVendor.equals("sqlserver")) {
			sb.append("EXEC sp_rename " + table + "'_trans.temp_status_id', 'status_id', 'COLUMN'; ");
			sb.append("EXEC sp_rename " + table + "'_trans_detail.temp_status_id', 'status_id', 'COLUMN'; ");
		} else if (GlobalCons.dbVendor.equals("h2")) {
			sb.append("alter table " + table + "_trans alter column temp_status_id rename to status_id; ");
			sb.append("alter table " + table + "_trans_detail alter column temp_status_id rename to status_id; ");
		} else {
			sb.append("alter table " + table + "_trans rename column temp_status_id to status_id; ");
			sb.append("alter table " + table + "_trans_detail rename column temp_status_id to status_id; ");
		}

		sb.append("alter table " + table + "_trans add foreign key (status_id) references " + table + "_trans_status (id); ");
		sb.append("alter table " + table + "_trans_detail add foreign key (status_id) references " + table + "_trans_status (id); ");
		
		sta.executeUpdate(sb.toString());
	}
	
}

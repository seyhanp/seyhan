/**
* Copyright (c) 2015 Mustafa DUMLUPINAR, mdumlupinar@gmail.com
*
* This file is part of seyhan project.
*
* seyhan is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package external;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Application;
import play.GlobalSettings;
import play.db.DB;
import play.libs.Akka;
import play.libs.F.Promise;
import play.mvc.Http.RequestHeader;
import play.mvc.Results;
import play.mvc.SimpleResult;
import scala.concurrent.duration.Duration;
import utils.CacheUtils;
import utils.CookieUtils;
import views.html.tools.errors.page_not_found;
import akka.dispatch.ExecutionContexts;

import com.googlecode.flyway.core.Flyway;
import com.seyhanproject.pserver.Printing;
import com.seyhanproject.pserver.Server;

/**
 * @author mdpinar
*/
public class Global extends GlobalSettings {

	private final static Logger log = LoggerFactory.getLogger(Global.class);

	@Override
	public void onStart(Application app) {
		try {
			checkDBMigrations();
		} catch (SQLException e) {
			log.error("ERROR", e);
		}

		Akka.system().scheduler().schedule(
		        Duration.Zero(),
		        Duration.create(1, TimeUnit.HOURS),
		        new Runnable() {
                    @Override
                    public void run() {
                    	ExchangeRatesUpdateActor.doIt();
                    }
                }, ExecutionContexts.global()
		);

		if (app.configuration().getBoolean("seyhan.mq.open.at.startup").booleanValue()) {
			Server.start(app.configuration().getString("seyhan.mq.ip"));
		}

		log.info("Application has started!");
	}
	
	@Override
	public Promise<SimpleResult> onHandlerNotFound(RequestHeader app) {
		return Promise.<SimpleResult>pure(Results.notFound(
			page_not_found.render(app.uri())
		));
	}
	
	@Override
	public void onStop(Application app) {
		CacheUtils.destroy();
		CookieUtils.destroy();
		Printing.stopQueues();
		super.onStop(app);
		
		System.gc();
		log.info("Application has stopped!");
	}
	
	private void checkDBMigrations() throws SQLException {
		DataSource ds = DB.getDataSource("default");

		Connection con = ds.getConnection();
		DatabaseMetaData dbm = con.getMetaData();
		ResultSet flySchemaTable = dbm.getTables(null, null, "schema_version", null);

		boolean isOnlyInit = false;
		if (! flySchemaTable.next()) {
			ResultSet seyhanFirstTable = dbm.getTables(null, null, "admin_user", null);
			isOnlyInit = (seyhanFirstTable.next());
		}

		log.info("Checking the DB migrations...");
		try {
			Flyway flyway = new Flyway();
	        flyway.setDataSource(ds);
	        if (isOnlyInit) {
	        	flyway.init();
	        	log.info("DB initialization has done.");
	        } else {
	        	flyway.migrate();
	        	log.info("DB migration has done.");
	        }
		} catch (Exception e) {
			log.warn("ERROR", e);
		}

		con.close();
	}

}

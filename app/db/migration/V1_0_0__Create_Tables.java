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
package db.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.GlobalCons;

import com.google.common.base.Charsets;
import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;

/**
 * @author mdpinar
*/
public class V1_0_0__Create_Tables implements JdbcMigration {

	private final static Logger log = LoggerFactory.getLogger(V1_0_0__Create_Tables.class);

	@Override
	public void migrate(Connection con) throws Exception {
		String fileName = String.format("conf/evolutions/%s.sql", GlobalCons.dbVendor);
		File initFile = new File(fileName);
	
		if (! initFile.exists()) {
			log.error("ERROR : " + fileName + " not found!!!");
			return;
		} else {
			log.info("DB migrations are executing for : " + fileName);
		
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(initFile), Charsets.UTF_8));
	
			String line = null;
			StringBuilder queries = new StringBuilder();
			PreparedStatement statement = null;
	
			while ((line = br.readLine()) != null) {
				if (! line.trim().isEmpty() && ! line.startsWith("--")) {
					if (queries.length() == 0) log.info("  " + line + (line.endsWith(";") ? "" : " ..."));
					queries.append(line.replace("\\n", ""));
					if (line.indexOf(";") > 0) {
						statement = con.prepareStatement(queries.toString());
						try {
							statement.execute();
						} catch (Exception e) {
							log.error("ERROR", e);
						} finally {
							statement.close();
						}
						queries.setLength(0);
					}
				}
			}
			br.close();

			log.info("DB migrations have executed for : " + fileName);
		}
	}

}

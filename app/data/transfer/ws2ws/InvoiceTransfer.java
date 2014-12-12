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
package data.transfer.ws2ws;

import com.avaje.ebean.Ebean;

import models.InvoiceTransSource;
import enums.Module;
/**
 * @author mdpinar
*/
class InvoiceTransfer extends BaseTransfer {

	@Override
	public void transferInfo(int sourceWS, int targetWS) {
		executeInsertQueryForInfoTables(new InvoiceTransSource(), sourceWS, targetWS);
	}

	@Override
	public void destroyInfo(int targetWS) {
		executeDeleteQueryForInfoTables("invoice_trans_source", targetWS);
	}

	@Override
	public void destroyTransaction(int targetWS, boolean willBeDestroyedAll) {
		if (willBeDestroyedAll) {
			Ebean.createSqlUpdate("delete from invoice_trans_tax").execute();
			Ebean.createSqlUpdate("delete from invoice_trans_factor").execute();
			Ebean.createSqlUpdate("delete from invoice_trans_currency").execute();
			Ebean.createSqlUpdate("delete from invoice_trans_relation").execute();
			Ebean.createSqlUpdate("delete from invoice_trans_detail where workspace = :workspace").setParameter("workspace", targetWS).execute();
			Ebean.createSqlUpdate("delete from invoice_trans where workspace = :workspace").setParameter("workspace", targetWS).execute();
		}
	}

	@Override
	public Module getModule() {
		return Module.invoice;
	}

}

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
package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Ebean;

import play.db.ebean.Model;
import utils.CacheUtils;

@Entity
/**
 * @author mdpinar
*/
public class InvoiceTransStatusHistory extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@ManyToOne
	public InvoiceTrans trans;

	@ManyToOne
	public InvoiceTransStatus status;

	public String username;
	public String description;
	public Date transTime;

	/*------------------------------------------------------------------------------------*/

	private static Model.Finder<Integer, InvoiceTransStatusHistory> find = new Model.Finder<Integer, InvoiceTransStatusHistory>(Integer.class, InvoiceTransStatusHistory.class);

	public static void goForward(InvoiceTrans trans, InvoiceTransStatus newStatus, String description) {
		InvoiceTransStatusHistory history = new InvoiceTransStatusHistory();
		history.trans = trans;
		history.status = newStatus;
		history.username = CacheUtils.getUser().username;
		history.transTime = new Date();
		history.description = description;
		history.save();

		setTransStatus(trans.id, newStatus.id);
	}

	public static void goBack(InvoiceTrans trans) {
		List<InvoiceTransStatusHistory> historyList = find
														.where().eq("trans", trans)
														.order("id desc")
														.setMaxRows(2)
													.findList();
		if (historyList != null) {
			historyList.get(0).delete();
			
			Integer status_id = null;
			if (historyList.size() > 1) status_id = historyList.get(1).id;
			setTransStatus(trans.id, status_id);
		}
	}
	
	private static void setTransStatus(int trans_id, int status_id) {
		Ebean.createSqlUpdate("update invoice_trans set status_id = :status_id where id in = :id)")
				.setParameter("id", trans_id)
				.setParameter("status_id", status_id)
			.execute();
		
		Ebean.createSqlUpdate("update invoice_trans_detail set status_id = :status_id where id trans_id = :trans_id)")
				.setParameter("trans_id", trans_id)
				.setParameter("status_id", status_id)
			.execute();
	}

}

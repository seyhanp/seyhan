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

import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;

import com.avaje.ebean.Ebean;

import enums.ChqbllSort;
import enums.ChqbllStep;

@Entity
/**
 * @author mdpinar
*/
public class ChqbllDetailHistory extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@ManyToOne
	public ChqbllPayrollDetail detail;

	@Constraints.Required
	public ChqbllSort sort;

	@Constraints.Required
	public Date stepDate = new Date();

	@Constraints.Required
	public ChqbllStep step;

	@ManyToOne
	public Contact contact;

	@ManyToOne
	public Bank bank;

	@ManyToOne
	public Safe safe;

	public String insertBy;
	public Date insertAt = new Date();

	/*------------------------------------------------------------------------------------*/

	private static Model.Finder<Integer, ChqbllDetailHistory> find = new Model.Finder<Integer, ChqbllDetailHistory>(Integer.class, ChqbllDetailHistory.class);

	public static void goForward(ChqbllPayrollDetail det, ChqbllStep toStep, Contact contact, Bank bank, Safe safe) {
		ChqbllDetailHistory history = new ChqbllDetailHistory();
		history.detail = det;
		history.step = toStep;
		history.sort = det.sort;
		history.contact = contact;
		history.bank = bank;
		history.safe = safe;
		history.insertBy = CacheUtils.getUser().username;

		det.lastStep = history.step;
		if (ChqbllStep.Endorsed.equals(toStep)) det.lastContactName = contact.name;

		det.save();
		history.save();
	}

	public static void goBack(ChqbllPayrollDetail det) {
		List<ChqbllDetailHistory> historyList = findHistoryList(det, 2);
		if (historyList != null) {
			if (historyList.size() > 1) {
				historyList.get(0).delete();
				det.totalPaid = 0d; //her ihtimale karsi, parcali tahsilat/odeme durumunda ise bu deger sifirlanir
				det.lastStep = historyList.get(1).step;
	
				Contact contact = historyList.get(1).contact;
				if (contact != null) {
					det.lastContactName = contact.name;
				}
	
				det.update();
			} else { //devir fisinden gelmis olabilir
				det.totalPaid = 0d;
				det.update();
			}
		}
	}

	public static void setStep(Integer detailId, ChqbllStep step) {
		Ebean.createSqlUpdate("update chqbll_detail_history set step = :step").setParameter("step", step).execute();
	}

	public static List<ChqbllDetailHistory> findHistoryList(ChqbllPayrollDetail det, int maxRows) {
		return find
				.fetch("contact")
				.fetch("bank")
				.fetch("safe")
				.where()
					.eq("detail", det)
				.order("id desc")
				.setMaxRows(maxRows)
			.findList();
	}

}

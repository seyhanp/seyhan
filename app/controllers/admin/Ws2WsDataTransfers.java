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
package controllers.admin;

import static play.data.Form.form;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.temporal.Ws2WsTransferModel;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CacheUtils;
import views.html.admins.data_transfer.ws2ws_form;

import com.avaje.ebean.Ebean;

import controllers.Application;
import data.transfer.ws2ws.Ws2WsTransferManager;

/**
 * @author mdpinar
*/
public class Ws2WsDataTransfers extends Controller {

	private final static Logger log = LoggerFactory.getLogger(Ws2WsDataTransfers.class);
	private final static Form<Ws2WsTransferModel> dataForm = form(Ws2WsTransferModel.class);

	public static Result show() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(ws2ws_form.render(dataForm.fill(new Ws2WsTransferModel())));
	}

	public static Result transfer() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<Ws2WsTransferModel> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(ws2ws_form.render(filledForm));
		} else {

			Ws2WsTransferModel model = filledForm.get();
			checkConstraints(filledForm);

			if(filledForm.hasErrors()) {
				return badRequest(ws2ws_form.render(filledForm));
			}
			
			Ebean.beginTransaction();
			try {
				Ws2WsTransferManager.transfer(model);
				Ebean.commitTransaction();
				flash("success", Messages.get("saved", "Transfer"));
			} catch (PersistenceException pe) {
				Ebean.rollbackTransaction();
				flash("error", Messages.get("unexpected.problem.occured", pe.getMessage()));
				log.error("ERROR", pe);
				return badRequest(ws2ws_form.render(filledForm));
			}

		}

		return ok(ws2ws_form.render(filledForm));
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<Ws2WsTransferModel> filledForm) {
		Ws2WsTransferModel model = filledForm.get();

		if (model.transDate == null) {
			filledForm.reject("transDate", Messages.get("is.not.null", Messages.get("date")));
		}

		if (model.sourceWS == null || model.sourceWS.id == null) {
			filledForm.reject("sourceWS.id", Messages.get("is.not.null", ""));
		}

		if (model.targetWS == null || model.targetWS.id == null) {
			filledForm.reject("targetWS.id", Messages.get("is.not.null", ""));
		}

		if (model.sourceWS != null && model.sourceWS.id != null 
		&&  model.targetWS != null && model.targetWS.id != null
		&&  model.sourceWS.id.equals(model.targetWS.id)) {
			filledForm.reject("targetWS.id", Messages.get("workspaces.same"));
		}
	}

}

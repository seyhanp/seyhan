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
package controllers.invoice;

import static play.data.Form.form;

import java.util.ArrayList;

import models.InvoiceTrans;
import models.InvoiceTransStatusHistory;
import models.search.OrderTransSearchParam;
import models.temporal.ReceiptListModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import views.html.invoices.trans_approval.form;

import com.avaje.ebean.Ebean;

import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class TransApprovals extends Controller {

	private final static Logger log = LoggerFactory.getLogger(TransApprovals.class);

	private final static Right RIGHT = Right.FATR_ONAYLAMA_ADIMLARI;
	private final static Form<OrderTransSearchParam> dataForm = form(OrderTransSearchParam.class);

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		OrderTransSearchParam sp = new OrderTransSearchParam();
		sp.transType = Right.FATR_SATIS_FATURASI;

		return ok(form.render(dataForm.fill(sp), new ArrayList<ReceiptListModel>()));
	}

	public static Result submit() {
		Result hasProblem = AuthManager.hasProblem(RIGHT, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<OrderTransSearchParam> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest();
		} else {
			OrderTransSearchParam model = filledForm.get();
			if (model.formAction != null) {
			    if ("search".equals(model.formAction)) {
			    	return search(filledForm);
			    } else {
			    	if (model.details != null && model.details.size() > 0) {
			    
			    		Ebean.beginTransaction();
			    		try {
			    			int transCount = 0;
		    				for (ReceiptListModel rlm : model.details) {
								if (rlm.isSelected) {
									InvoiceTransStatusHistory.goForward(InvoiceTrans.findById(rlm.id), model.invoiceTransStatus, model.description);
									transCount++;
								}
							}
				    		if (transCount > 0) {
			    				flash("success", Messages.get("has.been.closed", transCount, Messages.get(model.transType.key)));
			    			} else {
			    				flash("error", Messages.get("has.not.been.closed"));
			    			}
				    		Ebean.commitTransaction();
				    
			    		} catch (Exception e) {
			    			Ebean.rollbackTransaction();
			    			flash("error", Messages.get("unexpected.problem.occured", e.getMessage()));
			    			log.error(e.getMessage(), e);
			    		}

			    	} else {
			    		flash("error", Messages.get("has.not.been.closed"));
			    	}
			    	return search(filledForm);
			    }
			}

			flash("error", Messages.get("not.found", "action"));
			return search(filledForm);
		}

	}

	private static Result search(Form<OrderTransSearchParam> filledForm) {
		return ok(form.render(filledForm, InvoiceTrans.findReceiptList(filledForm.get())));
	}

}

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
import models.search.TransSearchParam;
import models.temporal.InvoiceTransStatusForm;
import models.temporal.ReceiptListModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.TransStatusHistoryUtils;
import views.html.invoices.trans_approval.change_status;
import views.html.invoices.trans_approval.form;

import com.avaje.ebean.Ebean;

import enums.Module;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class TransApprovals extends Controller {

	private final static Logger log = LoggerFactory.getLogger(TransApprovals.class);

	private final static Right RIGHT = Right.FATR_ONAYLAMA_ADIMLARI;
	private final static Form<TransSearchParam> dataForm = form(TransSearchParam.class);
	private final static Form<InvoiceTransStatusForm> statusForm = form(InvoiceTransStatusForm.class);

	private static int targetCount;
	
	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		TransSearchParam sp = new TransSearchParam();
		sp.transType = Right.FATR_SATIS_FATURASI;

		return ok(form.render(dataForm.fill(sp), new ArrayList<ReceiptListModel>()));
	}

	public static Result submit() {
		Result hasProblem = AuthManager.hasProblem(RIGHT, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		targetCount = 0;

		Form<TransSearchParam> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest();
		} else {
			TransSearchParam model = filledForm.get();
			if (model.formAction != null) {
				if ("search".equals(model.formAction)) {
			    	return search(filledForm);
			    } else {
			    	if (model.details != null && model.details.size() > 0) {
			    
			    		Ebean.beginTransaction();
			    		try {
			    			boolean isStatusChange = false;
		    				if ("change-status".equals(model.formAction)) {
		    					changeStatus(model);
		    				} else if ("redo".equals(model.formAction)) {
		    					redo(model.redoTransId);
		    				} else {
		    					isStatusChange = false;
			    				for (ReceiptListModel rlm : model.details) {
									if (rlm.isSelected && ! rlm.isCompleted) {
										TransStatusHistoryUtils.goForward(Module.invoice, rlm.id, model.invoiceTransStatus.id, model.description);
										targetCount++;
									}
								}
		    				}
		    				if (isStatusChange) {
					    		if (targetCount > 0) {
				    				flash("success", Messages.get("has.been.changed", targetCount));
				    			} else {
				    				flash("error", Messages.get("has.not.been.changed"));
				    			}
		    				} else {
					    		if (targetCount > 0) {
				    				flash("success", Messages.get("has.been.closed", targetCount, Messages.get(model.transType.key)));
				    			} else {
				    				flash("error", Messages.get("has.not.been.closed"));
				    			}
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

			flash("error", Messages.get("not.found", Messages.get("action")));
			return search(filledForm);
		}

	}

	private static Result search(Form<TransSearchParam> filledForm) {
		return ok(form.render(filledForm, InvoiceTrans.findReceiptList(filledForm.get())));
	}

	public static Result getChangeStatusForm(Integer oldStatusId) {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		return ok(
			change_status.render(statusForm.fill(new InvoiceTransStatusForm()), oldStatusId).body()
		);
	}

	private static void changeStatus(TransSearchParam model) {
		if (model.newInvoiceTransStatus == null) return;
		for (ReceiptListModel detail : model.details) {
			if (detail.isSelected && ! detail.isCompleted) {
				TransStatusHistoryUtils.goForward(Module.invoice, detail.id, model.newInvoiceTransStatus.id, model.description);
				targetCount++;
			}
		}
	}

	private static void redo(Integer transId) {
		if (transId != null) {
			TransStatusHistoryUtils.goBack(Module.invoice, transId);
			targetCount++;
		}
	}

}

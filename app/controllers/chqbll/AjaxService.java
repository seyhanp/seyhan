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
package controllers.chqbll;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.ChqbllDetailHistory;
import models.ChqbllPayrollDetail;
import models.search.ChqbllSelectionModel;
import models.temporal.ChqbllHistory;
import models.temporal.Pair;

import com.fasterxml.jackson.databind.node.ObjectNode;

import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CacheUtils;
import utils.DateUtils;
import utils.Format;
import utils.StringUtils;
import views.html.chqblls.other.investigation_form;
import views.html.chqblls.other.sequential_form;
import views.html.chqblls.selection.form;
import views.html.chqblls.selection.result;
import views.html.tools.instant.record_not_found;
import controllers.Application;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.Module;
import enums.Right;

/**
 * @author mdpinar
*/
public class AjaxService extends Controller {

	private final static Form<ChqbllSelectionModel> detailForm = form(ChqbllSelectionModel.class);
	private final static Form<ChqbllPayrollDetail> seqForm = form(ChqbllPayrollDetail.class);

	public static Result create(String sortStr, String rightStr, String fromStep, String alreadySelected) {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		ChqbllSort sort = ChqbllSort.Cheque;
		try {
			sort = ChqbllSort.valueOf(sortStr);
		} catch (Exception e) { }

		ChqbllStep step = ChqbllStep.InPortfolio;
		try {
			step = ChqbllStep.valueOf(fromStep);
		} catch (Exception e) { }

		Right right = Right.findRight(rightStr);

		return ok(form.render(detailForm.fill(new ChqbllSelectionModel(sort, right, step, alreadySelected))));
	}

	public static Result search() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<ChqbllSelectionModel> filledForm = detailForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {
			List<ChqbllPayrollDetail> detailList = ChqbllPayrollDetail.getListBySearchModel(filledForm.get());
			if (detailList.size() > 0) {
				return ok(result.render(detailList, filledForm.get().selSort, filledForm.get().selRight));
			} else {
				return ok(record_not_found.render(""));
			}
		}
	}

	public static Result investigation(Integer id) {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		ChqbllPayrollDetail detail = ChqbllPayrollDetail.getDetailedById(id);

		List<ChqbllHistory> historyList = new ArrayList<ChqbllHistory>();
		for (ChqbllDetailHistory hist : detail.histories) {
			ChqbllHistory history = new ChqbllHistory();
			history.date = DateUtils.formatDateStandart(hist.stepDate);
			history.bank = (hist.bank != null ? hist.bank.name : "");
			history.safe = (hist.safe != null ? hist.safe.name : "");
			history.step = Messages.get(hist.step.key);
			history.user = hist.insertBy;

			historyList.add(history);
		}

		List<Pair> properties = new ArrayList<Pair>();
		properties.add(new Pair(Messages.get("portfolio.no") + " / " + Messages.get("serial.no"), detail.portfolioNo.toString() + " / " + detail.serialNo));
		properties.add(new Pair(Messages.get("maturity"), DateUtils.formatDateStandart(detail.dueDate)));
		properties.add(new Pair(Messages.get("amount"), Format.asMoney(detail.amount) + " " + detail.excCode));
		properties.add(new Pair(Messages.get("type"), (detail.cbtype !=  null ? detail.cbtype.name : "")));
		if (detail.sort.equals(ChqbllSort.Cheque)) {
			properties.add(new Pair(Messages.get("bank.name"), detail.bankName));
		} else if (detail.isCustomer) {
			properties.add(new Pair(Messages.get("surety"), detail.surety));
		}
		if (detail.isCustomer) {
			properties.add(new Pair(Messages.get("owner"), detail.owner));
			properties.add(new Pair(Messages.get("payment_place"), detail.paymentPlace));
		}
		ObjectNode result = Json.newObject();

		result.put("title", detail.lastContactName);
		result.put("body", investigation_form.render(StringUtils.getChqbllTitle(detail), historyList, properties).body());

		return ok(result);
	}

	public static Result sequentialForm(String rightStr) {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		Right right = Right.findRight(rightStr);
		ChqbllSort sort = (right.module.equals(Module.cheque) ? ChqbllSort.Cheque : ChqbllSort.Bill);

		return ok(
			sequential_form.render(seqForm.fill(new ChqbllPayrollDetail()), sort, ChqbllStep.isCustomer(right)).body()
		);
	}

	public static Result sequentialValidation(String sortStr, boolean isCustomer) {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		Form<ChqbllPayrollDetail> filledForm = seqForm.bindFromRequest();
		ChqbllSort sort = ChqbllSort.find(sortStr);

		if(filledForm.hasErrors()) {
			return badRequest(sequential_form.render(filledForm, sort, isCustomer));
		} else {
			ChqbllPayrollDetail model = filledForm.get();

			if (model.portfolioNo == null || model.portfolioNo < 1 || model.portfolioNo > 36) {
				filledForm.reject("portfolioNo", Messages.get("error.numberRange", 1, 36));
			}

			if (model.rowNo == null || model.rowNo < 1 || model.rowNo > 365) {
				filledForm.reject("rowNo", Messages.get("error.numberRange", 1, 365));
			}

			if(filledForm.hasErrors()) {
				return badRequest(sequential_form.render(filledForm, sort, isCustomer));
			} else {
				return ok(Json.toJson(model));
			}
		}
	}

	public static Result transSteps(String fromStep, String toStep) {
		ObjectNode result = Json.newObject();

		ChqbllStep fs = ChqbllStep.InPortfolio;
		ChqbllStep ts = null;
		try {
			fs = ChqbllStep.valueOf(fromStep);
		} catch (Exception e) { }
		try {
			ts = ChqbllStep.valueOf(toStep);
		} catch (Exception e) { }

		Map<String, String> targetSteps = ChqbllStep.targetOptions(fromStep);
		result.put("steps",  Json.toJson(targetSteps));
		result.put("module", Json.toJson(ChqbllStep.findRefModule(fs, ts, targetSteps)));

		return ok(result);
	}

}

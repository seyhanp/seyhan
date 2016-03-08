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
package controllers.safe.reports;

import static play.data.Form.form;

import java.util.Date;

import models.Safe;
import play.data.Form;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import reports.ReportParams;
import reports.ReportService;
import reports.ReportService.ReportResult;
import utils.AuthManager;
import utils.CacheUtils;
import utils.DateUtils;
import views.html.safes.reports.cashbook_list;
import controllers.global.Profiles;
import controllers.safe.Safes;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class CashbookList extends Controller {

	private final static Right RIGHT_SCOPE = Right.KASA_KASA_DEFTERI;
	private final static String REPORT_NAME = "CashbookList";

	private final static Form<CashbookList.Parameter> parameterForm = form(CashbookList.Parameter.class);

	public static class Parameter {

		public ReportUnit unit;

		@Constraints.Required
		public Safe safe = Profiles.chosen().gnel_safe;

		@Constraints.Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date date = new Date();

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		if (params.safe != null && params.safe.id != null) {
			queryBuilder.append(" and safe_id = ");
			queryBuilder.append(params.safe.id);
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<CashbookList.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(cashbook_list.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			if (params.date == null) {
				filledForm.reject("date", Messages.get("is.not.null", Messages.get("date")));
				return badRequest(cashbook_list.render(filledForm));
			}

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME;
			repPar.reportUnit = params.unit;
			repPar.query = getQueryString(params);

			/*
			 * Parametrik degerlerin gecisi
			 */
			Safe safe = Safe.findById(params.safe.id);
			repPar.paramMap.put("SAFE_NAME", safe.name);
			repPar.paramMap.put("BASE_DATE", DateUtils.formatDateForDB(params.date));
			repPar.paramMap.put("YESTERDAY_TRANSFER", Safes.findBalance(safe.id, params.date));

			repPar.paramMap.put("REPORT_INFO", Messages.get("report.info.date", DateUtils.formatDateStandart(params.date)));

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(cashbook_list.render(filledForm));
			} else if (ReportService.isToDotMatrix(repPar)) {
				flash("success", Messages.get("printed.success"));
			}
			return ReportService.sendReport(repPar, repRes, cashbook_list.render(filledForm));
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(cashbook_list.render(parameterForm.fill(new Parameter())));
	}

}

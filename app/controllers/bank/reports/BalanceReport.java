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
package controllers.bank.reports;

import static play.data.Form.form;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import models.Bank;
import play.data.Form;
import play.data.format.Formats.DateTime;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import reports.ReportParams;
import reports.ReportService;
import reports.ReportService.ReportResult;
import utils.AuthManager;
import utils.CacheUtils;
import utils.DateUtils;
import views.html.banks.reports.balance_report;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class BalanceReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.BANK_DURUM_RAPORU;
	private final static String REPORT_NAME = "BalanceReport";

	private final static Form<BalanceReport.Parameter> parameterForm = form(BalanceReport.Parameter.class);

	public static class Parameter {

		public String having;
		public ReportUnit unit;

		public Bank bank;
		public String excCode;

		@DateTime(pattern = "dd/MM/yyyy")
		public Date date = new Date();

		public static Map<String, String> options() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("HAVING SUM(t.debt) > SUM(t.credit)", Messages.get("debt"));
			options.put("HAVING SUM(t.debt) < SUM(t.credit)", Messages.get("credit"));
			options.put("HAVING SUM(t.debt) = SUM(t.credit)", Messages.get("equal"));

			return options;
		}

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		if (params.bank != null && params.bank.id != null) {
			queryBuilder.append(" and bank_id = ");
			queryBuilder.append(params.bank.id);
		}

		if (params.date != null) {
			queryBuilder.append(" and trans_date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.date));
		}

		if (params.excCode != null && ! params.excCode.isEmpty()) {
			queryBuilder.append(" and t.exc_code = '");
			queryBuilder.append(params.excCode);
			queryBuilder.append("'");
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<BalanceReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(balance_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME;
			repPar.reportUnit = params.unit;
			repPar.query = getQueryString(params);
			repPar.having = params.having;

			repPar.paramMap.put("REPORT_INFO", Messages.get("report.info.date", DateUtils.formatDateStandart(params.date)));

			ReportResult repRes = ReportService.generateReport(repPar, response());
			return ReportService.sendReport(repPar, repRes, balance_report.render(filledForm));
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(balance_report.render(parameterForm.fill(new Parameter())));
	}

}

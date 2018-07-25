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
package controllers.novaposhta.reports;

import static play.data.Form.form;

import java.util.Date;

import models.NovaposhtaCargo;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import reports.ReportParams;
import reports.ReportService;
import reports.ReportService.ReportResult;
import utils.AuthManager;
import utils.CacheUtils;
import utils.DateUtils;
import views.html.novaposhtas.reports.daily_report;
import controllers.novaposhta.Cargos;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class DailyReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.NOVAPOSHTA_GUNLUK_ISLEM_LISTESI;
	private final static String REPORT_NAME = "DailyReport";

	private final static Form<DailyReport.Parameter> parameterForm = form(DailyReport.Parameter.class);

	public static class Parameter {

		public ReportUnit unit;

		public NovaposhtaCargo cargo;

		@Required
		public String month = DateUtils.formatDate(new Date(), "yyyy-MM");

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		if (params.cargo != null && params.cargo.id != null) {
			queryBuilder.append(" and t.cargo_id = ");
			queryBuilder.append(params.cargo.id);
		}

		if (params.month != null && ! params.month.trim().isEmpty()) {
			queryBuilder.append(" and t.trans_month = '");
			queryBuilder.append(params.month);
			queryBuilder.append("'");
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<DailyReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(daily_report.render(filledForm));
		} else {
			
			Parameter params = filledForm.get();

			if (params.month == null || params.month.length() < 7) {
				filledForm.reject("month", Messages.get("must.be.in.format", Messages.get("trans.month"), "YYYY-MM"));
				return badRequest(daily_report.render(filledForm));
			}

			if (params.cargo == null || params.cargo.id == null) {
				filledForm.reject("cargo", Messages.get("is.not.null", Messages.get("novaposhta.cargo.company")));
				return badRequest(daily_report.render(filledForm));
			}
			
			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME;
			repPar.reportUnit = params.unit;
			repPar.query = getQueryString(params);

			/*
			 * Parametrik degerlerin gecisi
			 */
			repPar.paramMap.put("MAXIMUM_DAY", findTheMaximumDay(params.month));
			repPar.paramMap.put("YEAR_MONTH", params.month);
			repPar.paramMap.put("REPORT_INFO", params.month);
			repPar.paramMap.put("TRANSFER", Cargos.findBalance(params.cargo.id, DateUtils.findFirstDay(params.month.replace("-", "/"))));
			
			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(daily_report.render(filledForm));
			} else if (ReportService.isToDotMatrix(repPar)) {
				flash("success", Messages.get("printed.success"));
			}
			return ReportService.sendReport(repPar, repRes, daily_report.render(filledForm));
		}

	}
	
	private static int findTheMaximumDay(String yearMonth) {
		return DateUtils.findLastDayOfGivenDate(yearMonth.replace("-", "/"));
	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(daily_report.render(parameterForm.fill(new Parameter())));
	}

}

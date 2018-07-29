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
import play.data.format.Formats.DateTime;
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

		public String excCode;

		@Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = new Date();

		@Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		if (params.cargo != null && params.cargo.id != null) {
			queryBuilder.append(" and t.cargo_id = ");
			queryBuilder.append(params.cargo.id);
		}

		if (params.startDate != null) {
			queryBuilder.append(" and t.trans_date >= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.startDate));
		}

		if (params.endDate != null) {
			queryBuilder.append(" and t.trans_date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.endDate));
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

		Form<DailyReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(daily_report.render(filledForm));
		} else {
			
			Parameter params = filledForm.get();

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
			repPar.paramMap.put("EXC_CODE", params.excCode);
			repPar.paramMap.put("REPORT_INFO", DateUtils.formatDateStandart(params.startDate) + " - " + DateUtils.formatDateStandart(params.endDate));
			repPar.paramMap.put("TRANSFER", Cargos.findBalance(params.cargo.id, params.startDate));
			
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

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(daily_report.render(parameterForm.fill(new Parameter())));
	}

}

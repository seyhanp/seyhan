/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.safe.reports;

import static play.data.Form.form;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import models.Safe;
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
import views.html.safes.reports.balance_report;
import controllers.global.Profiles;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class BalanceReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.KASA_DURUM_RAPORU;
	private final static String REPORT_NAME = "BalanceReport";

	private final static Form<BalanceReport.Parameter> parameterForm = form(BalanceReport.Parameter.class);

	public static class Parameter {

		public String having;
		public ReportUnit unit;

		public Safe safe = Profiles.chosen().gnel_safe;
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

		if (params.safe != null && params.safe.id != null) {
			queryBuilder.append(" and safe_id = ");
			queryBuilder.append(params.safe.id);
		}

		if (params.date != null) {
			queryBuilder.append(" and trans_date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.date));
		}

		if (params.excCode != null && ! params.excCode.isEmpty()) {
			queryBuilder.append(" and exc_code = '");
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
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(balance_report.render(filledForm));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(balance_report.render(parameterForm.fill(new Parameter())));
	}

}

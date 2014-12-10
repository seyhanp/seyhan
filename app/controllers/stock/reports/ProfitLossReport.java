/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.stock.reports;

import static play.data.Form.form;

import java.util.LinkedHashMap;
import java.util.Map;

import models.StockCosting;
import models.temporal.ExtraFieldsForStock;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import reports.ReportParams;
import reports.ReportService;
import reports.ReportService.ReportResult;
import utils.AuthManager;
import utils.CacheUtils;
import utils.GlobalCons;
import utils.InstantSQL;
import utils.QueryUtils;
import views.html.stocks.reports.profit_loss_report;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class ProfitLossReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_KAR_ZARAR_RAPORU;
	private final static String REPORT_NAME = "ProfitLossReportXBased";

	private final static Form<ProfitLossReport.Parameter> parameterForm = form(ProfitLossReport.Parameter.class);

	public static class Parameter extends ExtraFieldsForStock {

		public String orderBy;
		public ReportUnit unit;

		@Constraints.Required
		public StockCosting costing;

		public String reportType;

		public static Map<String, String> reportTypes() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			options.put("Stock", Messages.get("report.type.stock_based"));
			options.put("Monthly", Messages.get("report.type.month_based"));
			options.put("Yearly", Messages.get("report.type.year_based"));

			return options;
		}

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder(" and s.is_active = " + GlobalCons.TRUE);

		queryBuilder.append(" and s.workspace = " + CacheUtils.getWorkspaceId());

		if (params.costing != null || params.costing.id != null) {
			queryBuilder.append(" and costing_id = ");
			queryBuilder.append(params.costing.id);
		}

		QueryUtils.addExtraFieldsCriterias(params, queryBuilder, "s.");

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<ProfitLossReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(profit_loss_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			if (params.costing == null || params.costing.id == null) {
				filledForm.reject("costing.id", Messages.get("is.not.null", Messages.get("costing")));
				return badRequest(profit_loss_report.render(filledForm));
			}

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME;
			repPar.reportNameExtra = REPORT_NAME;
			repPar.query = getQueryString(params);
			repPar.reportUnit = params.unit;

			String field = "";
			String label = "";

			if (params.reportType.equals("Stock")) {
				repPar.reportName = "ProfitLossReportStockBased";
			} else {
				if (params.reportType.equals("Monthly")) {
					field = "cos.trans_month";
					label = Messages.get("trans.month");
				}
				if (params.reportType.equals("Yearly")) {
					field = "cos.trans_year";
					label = Messages.get("trans.year");
				}

				repPar.paramMap.put("GROUP_FIELD", field);
				repPar.paramMap.put("GROUP_LABEL", label);
			}
			/*
			 * Parametrik degerlerin gecisi
			 */
			repPar.paramMap.put("COSTING_ID", params.costing.id);

			repPar.paramMap.put("CATEGORY_SQL", "");
			if (params.category != null && params.category.id != null) {
				repPar.paramMap.put("CATEGORY_SQL", InstantSQL.buildCategorySQL(params.category.id));
			}

			StockCosting costing = StockCosting.findById(params.costing.id);
			repPar.paramMap.put("REPORT_INFO", costing.properties);

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(profit_loss_report.render(filledForm));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(profit_loss_report.render(parameterForm.fill(new Parameter())));
	}

}

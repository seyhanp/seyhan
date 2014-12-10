/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.stock.reports;

import static play.data.Form.form;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import models.StockCosting;
import models.temporal.ExtraFieldsForStock;
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
import utils.GlobalCons;
import utils.InstantSQL;
import utils.QueryUtils;
import views.html.stocks.reports.waiting_stocks_report;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class WaitingStocksReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_BEKLEYEN_STOKLAR_RAPORU;
	private final static String REPORT_NAME = "WaitingStocksReport";

	private final static Form<WaitingStocksReport.Parameter> parameterForm = form(WaitingStocksReport.Parameter.class);

	public static class Parameter extends ExtraFieldsForStock {

		public String orderBy;
		public ReportUnit unit;

		@DateTime(pattern = "dd/MM/yyyy")
		public Date date = DateUtils.findLastDayOfPastMonth();

		@Constraints.Required
		public StockCosting costing;

		public static Map<String, String> options() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("s.code, e._date", Messages.get("stock.code"));
			options.put("s.name, e._date", Messages.get("stock.name"));

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

		if (params.date != null) {
			queryBuilder.append(" and e._date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.date));
		}

		if (params.stock != null && params.stock.id != null) {
			queryBuilder.append(" and s.id = ");
			queryBuilder.append(params.stock.id);
		} else {
			if (params.providerCode != null && ! params.providerCode.isEmpty()) {
				queryBuilder.append(" and s.providerCode = '");
				queryBuilder.append(params.providerCode);
				queryBuilder.append("'");
			}
			QueryUtils.addExtraFieldsCriterias(params, queryBuilder, "s.");
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<WaitingStocksReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(waiting_stocks_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			if (params.costing == null || params.costing.id == null) {
				filledForm.reject("costing.id", Messages.get("is.not.null", Messages.get("costing")));
				return badRequest(waiting_stocks_report.render(filledForm));
			}

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME;
			repPar.reportUnit = params.unit;
			repPar.query = getQueryString(params);
			repPar.orderBy = params.orderBy;

			/*
			 * Parametrik degerlerin gecisi
			 */
			repPar.paramMap.put("EXTRA_FIELDS_SQL", QueryUtils.buildExtraFieldsQueryForStock(params));

			repPar.paramMap.put("CATEGORY_SQL", "");
			if (params.stock == null || params.stock.id == null) {
				if (params.category != null && params.category.id != null) {
					repPar.paramMap.put("CATEGORY_SQL", InstantSQL.buildCategorySQL(params.category.id));
				}
			}
			repPar.paramMap.put("REPORT_INFO", Messages.get("report.info.date", DateUtils.formatDateStandart(params.date)));

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(waiting_stocks_report.render(filledForm));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(waiting_stocks_report.render(parameterForm.fill(new Parameter())));
	}

}

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
import views.html.stocks.reports.inventory_report;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class InventoryReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_ENVANTER_RAPORU;
	private final static String REPORT_NAME = "InventoryReport";

	private final static Form<InventoryReport.Parameter> parameterForm = form(InventoryReport.Parameter.class);

	public static class Parameter extends ExtraFieldsForStock {

		public String orderBy;
		public ReportUnit unit;
		
		public String price;

		@Constraints.Required
		public StockCosting costing;

		public static Map<String, String> options() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("s.name", Messages.get("stock.name"));
			options.put("s.code", Messages.get("stock.code"));

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

		Form<InventoryReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(inventory_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			if (params.costing == null || params.costing.id == null) {
				filledForm.reject("costing.id", Messages.get("is.not.null", Messages.get("costing")));
				return badRequest(inventory_report.render(filledForm));
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
			repPar.paramMap.put("COSTING_ID", params.costing.id);
			repPar.paramMap.put("EXTRA_FIELDS_SQL", QueryUtils.buildExtraFieldsQueryForStock(params));

			repPar.paramMap.put("CATEGORY_SQL", "");
			if (params.category != null && params.category.id != null) {
				repPar.paramMap.put("CATEGORY_SQL", InstantSQL.buildCategorySQL(params.category.id));
			}
			if (params.price != null && ! params.price.isEmpty()) {
				repPar.paramMap.put("PRICE_FIELD", params.price);
			}

			StockCosting costing = StockCosting.findById(params.costing.id);
			repPar.paramMap.put("REPORT_INFO", costing.properties);

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(inventory_report.render(filledForm));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(inventory_report.render(parameterForm.fill(new Parameter())));
	}

}

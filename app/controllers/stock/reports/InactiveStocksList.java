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
import views.html.stocks.reports.inactive_stocks_list;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class InactiveStocksList extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_HAREKETSIZ_STOKLAR_LISTESI;
	private final static String REPORT_NAME = "InactiveList";

	private final static Form<InactiveStocksList.Parameter> parameterForm = form(InactiveStocksList.Parameter.class);

	public static class Parameter extends ExtraFieldsForStock {

		public String orderBy;
		public ReportUnit unit;

		@Constraints.Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.findFirstDayOfPastMonth();

		@Constraints.Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = DateUtils.findLastDayOfPastMonth();

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

		if (params.providerCode != null && ! params.providerCode.isEmpty()) {
			queryBuilder.append(" and s.providerCode = '");
			queryBuilder.append(params.providerCode);
			queryBuilder.append("'");
		}

		QueryUtils.addExtraFieldsCriterias(params, queryBuilder, "s.");

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<InactiveStocksList.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(inactive_stocks_list.render(filledForm));
		} else {

			Parameter params = filledForm.get();

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
			if (params.category != null && params.category.id != null) {
				repPar.paramMap.put("CATEGORY_SQL", InstantSQL.buildCategorySQL(params.category.id));
			}

			if (params.startDate != null) {
				repPar.paramMap.put("START_DATE", DateUtils.formatDateForDB(params.startDate));
			}
			if (params.endDate != null) {
				repPar.paramMap.put("END_DATE", DateUtils.formatDateForDB(params.endDate));
			}
			repPar.paramMap.put("REPORT_INFO", Messages.get("report.info.date_range", DateUtils.formatDateStandart(params.startDate), DateUtils.formatDateStandart(params.endDate)));

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(inactive_stocks_list.render(filledForm));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(inactive_stocks_list.render(parameterForm.fill(new Parameter())));
	}

}

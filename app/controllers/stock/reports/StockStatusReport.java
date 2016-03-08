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
package controllers.stock.reports;

import static play.data.Form.form;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import models.StockDepot;
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
import views.html.stocks.reports.stock_status_report;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class StockStatusReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_DURUM_RAPORU;
	private final static String REPORT_NAME = "StatusReport";

	private final static Form<StockStatusReport.Parameter> parameterForm = form(StockStatusReport.Parameter.class);

	public static class Parameter extends ExtraFieldsForStock {

		public String orderBy;
		public ReportUnit unit;

		@Constraints.Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.getFirstDayOfMonth();

		@Constraints.Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

		public String providerCode;
		public String price;

		public Integer showType = 1;

		public StockDepot depot;

		public static Map<String, String> options() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("s.name", Messages.get("stock.name"));
			options.put("s.code", Messages.get("stock.code"));

			return options;
		}

		public static Map<String, String> showTypes() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("1", Messages.get("gt.zero"));
			options.put("0", Messages.get("all"));
			options.put("2", Messages.get("eq.zero"));
			options.put("3", Messages.get("lt.zero"));

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

		if (params.depot != null && params.depot.id != null) {
			queryBuilder.append(" and depot_id = ");
			queryBuilder.append(params.depot.id);
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<StockStatusReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(stock_status_report.render(filledForm));
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
			repPar.paramMap.put("CATEGORY_SQL", "");
			if (params.category != null && params.category.id != null) {
				repPar.paramMap.put("CATEGORY_SQL", InstantSQL.buildCategorySQL(params.category.id));
			}
			if (params.price != null && ! params.price.isEmpty()) {
				repPar.paramMap.put("PRICE_FIELD", params.price);
			}
			if (params.startDate != null) {
				repPar.paramMap.put("START_DATE", params.startDate);
			}
			if (params.endDate != null) {
				repPar.paramMap.put("END_DATE", params.endDate);
			}
			repPar.paramMap.put("SHOW_TYPE", params.showType);
			repPar.paramMap.put("REPORT_INFO", Messages.get("report.info.date_range", DateUtils.formatDateStandart(params.startDate), DateUtils.formatDateStandart(params.endDate)));

			ReportResult repRes = ReportService.generateReport(repPar, response());
			return ReportService.sendReport(repPar, repRes, stock_status_report.render(filledForm));
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(stock_status_report.render(parameterForm.fill(new Parameter())));
	}

}

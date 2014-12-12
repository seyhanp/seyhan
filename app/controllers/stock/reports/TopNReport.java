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

import models.GlobalTransPoint;
import models.StockDepot;
import models.temporal.ExtraFieldsForStock;
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
import utils.InstantSQL;
import utils.QueryUtils;
import views.html.stocks.reports.topn_report;
import controllers.global.Profiles;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class TopNReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_TOPN_RAPORU;
	private final static String REPORT_NAME = "TopNReport";

	private final static Form<TopNReport.Parameter> parameterForm = form(TopNReport.Parameter.class);

	public static class Parameter extends ExtraFieldsForStock {

		public String orderField = Messages.get("total_output");
		public String orderDir = " desc";
		public ReportUnit unit;

		public StockDepot depot;

		public GlobalTransPoint transPoint = Profiles.chosen().gnel_transPoint;

		public Integer rowLimit = 25;

		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.getFirstDayOfMonth();

		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

		public static Map<String, String> orderFieldOptions() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("sum(net_output)", Messages.get("total_output"));
			options.put("sum(net_out_total)", Messages.get("total_out_amount"));
			options.put("sum(net_input)", Messages.get("total_input"));
			options.put("sum(net_in_total)", Messages.get("total_in_amount"));
			options.put("sum(ret_input)", Messages.get("total_ret_input"));
			options.put("sum(ret_in_total)", Messages.get("total_ret_in_amount"));
			options.put("sum(ret_output)", Messages.get("total_ret_output"));
			options.put("sum(ret_out_total)", Messages.get("total_ret_out_amount"));

			return options;
		}

		public static Map<String, String> orderDirOptions() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put(" desc", Messages.get("descending"));
			options.put(" asc", Messages.get("ascending"));

			return options;
		}

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and s.workspace = " + CacheUtils.getWorkspaceId());

		QueryUtils.addExtraFieldsCriterias(params, queryBuilder, "s.");

		if (params.depot != null && params.depot.id != null) {
			queryBuilder.append(" and t.depot_id = ");
			queryBuilder.append(params.depot.id);
		}

		if (params.startDate != null) {
			queryBuilder.append(" and t.trans_date >= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.startDate));
		}

		if (params.endDate != null) {
			queryBuilder.append(" and t.trans_date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.endDate));
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<TopNReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(topn_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME;
			repPar.query = getQueryString(params);
			repPar.reportUnit = params.unit;
			repPar.orderBy = params.orderField + params.orderDir;

			/*
			 * Parametrik degerlerin gecisi
			 */
			repPar.paramMap.put("ROW_LIMIT", params.rowLimit);

			repPar.paramMap.put("TRANS_POINT_SQL", "");
			if (params.transPoint != null && params.transPoint.id != null) {
				repPar.paramMap.put("TRANS_POINT_SQL", InstantSQL.buildTransPointSQL(params.transPoint.id));
			}
			repPar.paramMap.put("CATEGORY_SQL", "");
			if (params.category != null && params.category.id != null) {
				repPar.paramMap.put("CATEGORY_SQL", InstantSQL.buildCategorySQL(params.category.id));
			}


			String par1 = Parameter.orderFieldOptions().get(params.orderField) + " " + Parameter.orderDirOptions().get(params.orderDir) + " " + Messages.get("ordered");
			String par2 = "(" + DateUtils.formatDateStandart(params.startDate) + " - " + DateUtils.formatDateStandart(params.endDate) +")";
			repPar.paramMap.put("REPORT_INFO", par1 + " - " + par2);

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(topn_report.render(filledForm));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(topn_report.render(parameterForm.fill(new Parameter())));
	}

}

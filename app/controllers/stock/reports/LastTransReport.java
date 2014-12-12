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

import models.GlobalTransPoint;
import models.StockDepot;
import models.StockTransSource;
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
import utils.GlobalCons;
import utils.InstantSQL;
import utils.QueryUtils;
import views.html.stocks.reports.last_trans_report;
import controllers.global.Profiles;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class LastTransReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_SON_ISLEM_RAPORU;
	private final static String REPORT_NAME = "LastTransReport";

	private final static Form<LastTransReport.Parameter> parameterForm = form(LastTransReport.Parameter.class);

	public static class Parameter extends ExtraFieldsForStock {

		public ReportUnit unit;

		public StockDepot depot;

		public GlobalTransPoint transPoint = Profiles.chosen().gnel_transPoint;
		public Right transType;
		public StockTransSource transSource;

		public Boolean hasReturns = Boolean.FALSE;

		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.getFirstDayOfMonth();

		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

	}

	private static String getQueryStringPart1(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		if (params.stock != null && params.stock.id != null) {
			queryBuilder.append(" and t.stock_id = ");
			queryBuilder.append(params.stock.id);
		}

		QueryUtils.addExtraFieldsCriterias(params, queryBuilder, "s.");

		return queryBuilder.toString();
	}

	private static String getQueryStringPart2(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		if (params.stock != null && params.stock.id != null) {
			queryBuilder.append(" and t.stock_id = ");
			queryBuilder.append(params.stock.id);
		}

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

		if (params.transType != null) {
			queryBuilder.append(" and t._right = '");
			queryBuilder.append(params.transType);
			queryBuilder.append("'");
		}

		if (params.transSource != null && params.transSource.id != null) {
			queryBuilder.append(" and t.trans_source_id = ");
			queryBuilder.append(params.transSource.id);
		}

		if (params.hasReturns != null && ! params.hasReturns) {
			queryBuilder.append(" and t.is_return = " + GlobalCons.FALSE + " ");
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<LastTransReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(last_trans_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME;
			repPar.reportUnit = params.unit;

			/*
			 * Parametrik degerlerin gecisi
			 */

			repPar.query = "";
			repPar.paramMap.put("QUERY_STRING_PART_1",  getQueryStringPart1(params));
			repPar.paramMap.put("QUERY_STRING_PART_2",  getQueryStringPart2(params));

			repPar.paramMap.put("TRANS_POINT_SQL", "");
			if (params.transPoint != null && params.transPoint.id != null) {
				repPar.paramMap.put("TRANS_POINT_SQL", InstantSQL.buildTransPointSQL(params.transPoint.id));
			}
			repPar.paramMap.put("CATEGORY_SQL", "");
			if (params.category != null && params.category.id != null) {
				repPar.paramMap.put("CATEGORY_SQL", InstantSQL.buildCategorySQL(params.category.id));
			}
			repPar.paramMap.put("REPORT_INFO", Messages.get("report.info.date_range", DateUtils.formatDateStandart(params.startDate), DateUtils.formatDateStandart(params.endDate)));

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(last_trans_report.render(filledForm));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(last_trans_report.render(parameterForm.fill(new Parameter())));
	}

}

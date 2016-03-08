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

import java.util.LinkedHashMap;
import java.util.Map;

import models.temporal.ExtraFieldsForStock;
import play.data.Form;
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
import views.html.stocks.reports.stock_list;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class StockList extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_LISTESI;
	private final static String REPORT_NAME = "List";

	private final static Form<StockList.Parameter> parameterForm = form(StockList.Parameter.class);

	public static class Parameter extends ExtraFieldsForStock {

		public String orderBy;
		public ReportUnit unit;

		public String excCode;

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
			queryBuilder.append(" and s.provider_code = '");
			queryBuilder.append(params.providerCode);
			queryBuilder.append("'");
		}

		if (params.excCode != null && ! params.excCode.isEmpty()) {
			queryBuilder.append(" and s.exc_code = '");
			queryBuilder.append(params.excCode);
			queryBuilder.append("'");
		}

		QueryUtils.addExtraFieldsCriterias(params, queryBuilder, "s.");

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<StockList.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(stock_list.render(filledForm));
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

			ReportResult repRes = ReportService.generateReport(repPar, response());
			return ReportService.sendReport(repPar, repRes, stock_list.render(filledForm));
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(stock_list.render(parameterForm.fill(new Parameter())));
	}

}

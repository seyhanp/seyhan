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
package controllers.contact.reports;

import static play.data.Form.form;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import models.temporal.ExtraFieldsForContact;
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
import utils.QueryUtils;
import views.html.contacts.reports.inactive_contacts_list;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class InactiveContactsList extends Controller {

	private final static Right RIGHT_SCOPE = Right.CARI_HAREKETSIZ_CARILER_LISTESI;
	private final static String REPORT_NAME = "InactiveList";

	private final static Form<InactiveContactsList.Parameter> parameterForm = form(InactiveContactsList.Parameter.class);

	public static class Parameter extends ExtraFieldsForContact {

		public String orderBy;
		public ReportUnit unit;

		@Constraints.Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.getFirstDayOfYear();

		@Constraints.Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = DateUtils.today();

		public static Map<String, String> options() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("c.name", Messages.get("name"));
			options.put("cc.name", Messages.get("category"));

			return options;
		}

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and c.workspace = " + CacheUtils.getWorkspaceId());

		if (params.category != null && params.category.id != null) {
			queryBuilder.append(" and category_id = ");
			queryBuilder.append(params.category.id);
		}

		if (params.seller != null && params.seller.id != null) {
			queryBuilder.append(" and seller_id = ");
			queryBuilder.append(params.seller.id);
		}

		QueryUtils.addExtraFieldsCriterias(params, queryBuilder);

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<InactiveContactsList.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(inactive_contacts_list.render(filledForm));
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
			if (params.startDate != null) {
				repPar.paramMap.put("START_DATE", DateUtils.formatDateForDB(params.startDate));
			}
			if (params.endDate != null) {
				repPar.paramMap.put("END_DATE", DateUtils.formatDateForDB(params.endDate));
			}
			repPar.paramMap.put("REPORT_INFO", Messages.get("report.info.date_range", DateUtils.formatDateStandart(params.startDate), DateUtils.formatDateStandart(params.endDate)));

			ReportResult repRes = ReportService.generateReport(repPar, response());
			return ReportService.sendReport(repPar, repRes, inactive_contacts_list.render(filledForm));
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(inactive_contacts_list.render(parameterForm.fill(new Parameter())));
	}

}

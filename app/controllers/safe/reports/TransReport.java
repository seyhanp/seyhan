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
package controllers.safe.reports;

import static play.data.Form.form;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.Safe;
import models.SafeTransSource;
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
import views.html.safes.reports.trans_report;
import controllers.global.Profiles;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class TransReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.KASA_HAREKET_RAPORU;
	private final static String REPORT_NAME = "TransReportXBased";

	private final static Form<TransReport.Parameter> parameterForm = form(TransReport.Parameter.class);

	public static class Parameter {

		public ReportUnit unit;

		public Safe safe;

		public GlobalTransPoint transPoint = Profiles.chosen().gnel_transPoint;
		public GlobalPrivateCode privateCode = Profiles.chosen().gnel_privateCode;

		public Right transType;
		public String transNo;
		public SafeTransSource transSource;

		public String excCode;

		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.getFirstDayOfMonth();

		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

		public String reportType;
		public String showType;

		public static Map<String, String> reportTypes() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			options.put("Safe", Messages.get("report.type.safe_based"));
			options.put("Monthly", Messages.get("report.type.month_based"));
			options.put("Yearly", Messages.get("report.type.year_based"));
			options.put("Daily", Messages.get("report.type.day_based"));
			options.put("PrivateCode", Messages.get("report.type.private_code_based"));
			options.put("TransPoint", Messages.get("report.type.trans_point_based"));
			options.put("ReceiptType", Messages.get("report.type.receipt_type_based"));
			options.put("TransSource", Messages.get("report.type.trans_source_based"));

			return options;
		}

		public static Map<String, String> showTypes() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			options.put("Summary", Messages.get("report.show.summary"));
			options.put("Detailed", Messages.get("report.show.detail"));

			return options;
		}

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		if (params.safe != null && params.safe.id != null) {
			queryBuilder.append(" and t.safe_id = ");
			queryBuilder.append(params.safe.id);
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

		if (params.transNo != null && ! params.transNo.isEmpty()) {
			queryBuilder.append(" and t.trans_no = '");
			queryBuilder.append(params.transNo);
			queryBuilder.append("'");
		}

		if (params.transSource != null && params.transSource.id != null) {
			queryBuilder.append(" and t.trans_source_id = ");
			queryBuilder.append(params.transSource.id);
		}

		if (params.excCode != null && ! params.excCode.isEmpty()) {
			queryBuilder.append(" and t.exc_code = '");
			queryBuilder.append(params.excCode);
			queryBuilder.append("'");
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<TransReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(trans_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME + params.showType;
			repPar.reportNameExtra = REPORT_NAME;
			repPar.reportUnit = params.unit;
			repPar.query = getQueryString(params);

			String field = "";
			String label = "";
			String type = "String";

			if (params.reportType.equals("Monthly")) {
				field = "t.trans_month";
				label = Messages.get("trans.month");
			}
			if (params.reportType.equals("Yearly")) {
				field = "t.trans_year";
				label = Messages.get("trans.year");
				type = "Integer";
			}
			if (params.reportType.equals("Daily")) {
				field = "t.trans_date";
				label = Messages.get("date");
				type = "Date";
			}
			if (params.reportType.equals("PrivateCode")) {
				field = "pc.name";
				label = Messages.get("private_code");
			}
			if (params.reportType.equals("TransPoint")) {
				field = "tp.name";
				label = Messages.get("trans.point");
			}
			if (params.reportType.equals("ReceiptType")) {
				field = "t._right";
				label = Messages.get("trans.type");
				type = "Right";
			}
			if (params.reportType.equals("TransSource")) {
				field = "ts.name";
				label = Messages.get("trans.source");
			}
			if (params.reportType.equals("Safe")) {
				field = "s.name";
				label = Messages.get("safe");
				repPar.reportName = "TransReportSafeBasedDetailed";
				repPar.paramMap.put("FIRST_DATE", params.startDate);
			}

			repPar.paramMap.put("GROUP_FIELD", field);
			repPar.paramMap.put("GROUP_LABEL", label);
			repPar.paramMap.put("GROUP_TYPE",  type);

			/*
			 * Parametrik degerlerin gecisi
			 */
			repPar.paramMap.put("TRANS_POINT_SQL", "");
			if (params.transPoint != null && params.transPoint.id != null) {
				repPar.paramMap.put("TRANS_POINT_SQL", InstantSQL.buildTransPointSQL(params.transPoint.id));
			} else if (params.reportType.equals("TransPoint")) {
				repPar.paramMap.put("TRANS_POINT_SQL", "left join global_trans_point tp on tp.id = t.trans_point_id ");
			}

			repPar.paramMap.put("PRIVATE_CODE_SQL", "");
			if (params.privateCode != null && params.privateCode.id != null) {
				repPar.paramMap.put("PRIVATE_CODE_SQL", InstantSQL.buildPrivateCodeSQL(params.privateCode.id));
			} else if (params.reportType.equals("PrivateCode")) {
				repPar.paramMap.put("PRIVATE_CODE_SQL", "left join global_private_code pc on pc.id = t.private_code_id ");
			}

			String par1 = Parameter.reportTypes().get(params.reportType) + ", " + Parameter.showTypes().get(params.showType);
			String par2 = "(" + DateUtils.formatDateStandart(params.startDate) + " - " + DateUtils.formatDateStandart(params.endDate) +")";
			repPar.paramMap.put("REPORT_INFO", par1 + " - " + par2);

			ReportResult repRes = ReportService.generateReport(repPar, response());
			return ReportService.sendReport(repPar, repRes, trans_report.render(filledForm));
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(trans_report.render(parameterForm.fill(new Parameter())));
	}

}

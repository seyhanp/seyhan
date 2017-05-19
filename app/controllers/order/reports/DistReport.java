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
package controllers.order.reports;

import static play.data.Form.form;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.AdminExtraFields;
import models.Contact;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.SaleSeller;
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
import views.html.orders.reports.dist_report;
import controllers.global.Profiles;
import enums.Module;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class DistReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.SPRS_DAGILIM_RAPORU;

	private final static Form<DistReport.Parameter> parameterForm = form(DistReport.Parameter.class);

	public static class Parameter extends ExtraFieldsForStock {

		public String orderBy;
		public ReportUnit unit;

		public String price;

		public Contact contact;

		public GlobalTransPoint transPoint = Profiles.chosen().gnel_transPoint;
		public GlobalPrivateCode privateCode = Profiles.chosen().gnel_privateCode;

		public Right transType;
		public String transNo;
		public StockTransSource transSource;

		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.getFirstDayOfMonth();

		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

		@DateTime(pattern = "dd/MM/yyyy")
		public Date deliveryDate;

		public StockDepot depot;
		public SaleSeller seller;

		public String reportType;
		public String showType;

		public static Map<String, String> reportTypes() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			options.put("Stock", Messages.get("report.type.stock_based"));
			options.put("Contact", Messages.get("report.type.contact_based"));
			options.put("Monthly", Messages.get("report.type.month_based"));
			options.put("Yearly", Messages.get("report.type.year_based"));
			options.put("Daily", Messages.get("report.type.day_based"));
			options.put("DeliveryDate", Messages.get("report.type.deldate_based"));
			options.put("PrivateCode", Messages.get("report.type.private_code_based"));
			options.put("TransPoint", Messages.get("report.type.trans_point_based"));
			options.put("ReceiptType", Messages.get("report.type.receipt_type_based"));
			options.put("TransSource", Messages.get("report.type.trans_source_based"));
			//options.put("Category", Messages.get("report.type.category_based"));
			options.put("Depot", Messages.get("report.type.depot_based"));

			List<AdminExtraFields> extraFieldList = AdminExtraFields.listAll(Module.stock.name());
			for (AdminExtraFields ef : extraFieldList) {
				options.put("ExtraFields"+ef.idno, Messages.get("report.type.x_based", ef.name));
			}

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
		StringBuilder queryBuilder = new StringBuilder(" and s.is_active = " + GlobalCons.TRUE);

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		if (params.stock != null && params.stock.id != null) {
			queryBuilder.append(" and t.stock_id = ");
			queryBuilder.append(params.stock.id);
		}

		if (params.providerCode != null && ! params.providerCode.isEmpty()) {
			queryBuilder.append(" and s.providerCode = '");
			queryBuilder.append(params.providerCode);
			queryBuilder.append("'");
		}

		QueryUtils.addExtraFieldsCriterias(params, queryBuilder, "s.");

		/************************************************************************/

		if (params.contact != null && params.contact.id != null) {
			queryBuilder.append(" and t.contact_id = ");
			queryBuilder.append(params.contact.id);
		}

		if (params.startDate != null) {
			queryBuilder.append(" and t.trans_date >= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.startDate));
		}

		if (params.endDate != null) {
			queryBuilder.append(" and t.trans_date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.endDate));
		}

		if (params.deliveryDate != null) {
			queryBuilder.append(" and st.delivery_date = ");
			queryBuilder.append(DateUtils.formatDateForDB(params.deliveryDate));
		}

		if (params.depot != null && params.depot.id != null) {
			queryBuilder.append(" and t.depot_id = ");
			queryBuilder.append(params.depot.id);
		}

		if (params.seller != null && params.seller.id != null) {
			queryBuilder.append(" and t.seller_id = ");
			queryBuilder.append(params.seller.id);
		}

		if (params.transType != null) {
			queryBuilder.append(" and t._right = '");
			queryBuilder.append(params.transType);
			queryBuilder.append("'");
		}

		if (params.transNo != null && ! params.transNo.isEmpty()) {
			queryBuilder.append(" and st.trans_no = '");
			queryBuilder.append(params.transNo);
			queryBuilder.append("'");
		}

		if (params.transSource != null && params.transSource.id != null) {
			queryBuilder.append(" and t.trans_source_id = ");
			queryBuilder.append(params.transSource.id);
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<DistReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(dist_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.query = getQueryString(params);
			repPar.reportUnit = params.unit;

			String field = "";
			if (params.reportType.equals("Stock")) {
				repPar.reportName = "DistReportStockBased";
			} else {
				String label = "";
				String type = "String";

				if (params.reportType.equals("Contact")) {
					field = "st.contact_name";
					label = Messages.get("contact.name");
				}
				/*
				if (params.reportType.equals("Category")) {
					field = "st.contact_name";
					label = Messages.get("contact.name");
					if (params.category != null && params.category.id != null) {
						field = "sc.name";
						label = Messages.get("category");
					}
				}
				*/
				if (params.reportType.equals("Depot")) {
					field = "d.name";
					label = Messages.get("depot");
				}
				if (params.reportType.equals("Monthly")) {
					field = "t.trans_month";
					label = Messages.get("trans.month");
				}
				if (params.reportType.equals("Daily")) {
					field = "t.trans_date";
					label = Messages.get("date");
					type = "Date";
				}
				if (params.reportType.equals("Yearly")) {
					field = "t.trans_year";
					label = Messages.get("trans.year");
					type = "Integer";
				}
				if (params.reportType.equals("DeliveryDate")) {
					field = "st.delivery_date";
					label = Messages.get("date.delivery");
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
				if (params.reportType.startsWith("ExtraFields")) {
					Integer extraFieldsId = Integer.parseInt(""+params.reportType.charAt(params.reportType.length()-1));
					field = "ef"+extraFieldsId+".name";
					AdminExtraFields aef = AdminExtraFields.findById(Module.stock.name(), extraFieldsId);
					label = aef.name;
				}

				repPar.reportName = "DistReportXBased";
				repPar.paramMap.put("GROUP_FIELD", field);
				repPar.paramMap.put("GROUP_LABEL", label);
				repPar.paramMap.put("GROUP_TYPE",  type);
			}

			repPar.reportNameExtra = repPar.reportName;
			repPar.reportName = repPar.reportName + params.showType;
			/*
			 * Parametrik degerlerin gecisi
			 */
			repPar.paramMap.put("EXTRA_FIELDS_SQL", QueryUtils.buildExtraFieldsQueryForStock(params, field));

			repPar.paramMap.put("CATEGORY_SQL", "");
			if (params.category != null && params.category.id != null) {
				repPar.paramMap.put("CATEGORY_SQL", InstantSQL.buildCategorySQL(params.category.id));
			}

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
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(dist_report.render(filledForm));
			} else if (ReportService.isToDotMatrix(repPar)) {
				flash("success", Messages.get("printed.success"));
			}
			return ReportService.sendReport(repPar, repRes, dist_report.render(filledForm));
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(dist_report.render(parameterForm.fill(new Parameter())));
	}

}

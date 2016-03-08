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
package controllers.sale.reports;

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
import models.Stock;
import models.StockDepot;
import models.StockTransSource;
import models.temporal.ExtraFieldsForContact;
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
import views.html.sales.reports.selling_report;
import controllers.global.Profiles;
import enums.Module;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class SellingReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.SATS_SATIS_RAPORU;
	private final static String REPORT_NAME = "SellingReport";

	private final static Form<SellingReport.Parameter> parameterForm = form(SellingReport.Parameter.class);

	public static class Parameter extends ExtraFieldsForContact {

		public String orderBy;
		public ReportUnit unit;

		public Stock stock;
		public String providerCode;
		
		public Contact contact;

		public GlobalPrivateCode privateCode = Profiles.chosen().gnel_privateCode;
		public GlobalTransPoint transPoint = Profiles.chosen().gnel_transPoint;
		public StockTransSource transSource;

		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.getFirstDayOfMonth();

		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

		public StockDepot depot;
		public SaleSeller seller;

		public String reportType;

		public static Map<String, String> reportTypes() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			options.put("Stock", Messages.get("report.type.stock_based"));
			options.put("Contact", Messages.get("report.type.contact_based"));
			options.put("Depot", Messages.get("report.type.depot_based"));
			options.put("Monthly", Messages.get("report.type.month_based"));
			options.put("TransSource", Messages.get("report.type.trans_source_based"));

			List<AdminExtraFields> extraFieldList = AdminExtraFields.listAll(Module.contact.name());
			for (AdminExtraFields ef : extraFieldList) {
				options.put("ExtraFields"+ef.idno, Messages.get("report.type.x_based", ef.name));
			}

			return options;
		}

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder(" and s.is_active = " + GlobalCons.TRUE + " and t.seller_id is not null ");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		if (params.stock != null && params.stock.id != null) {
			queryBuilder.append(" and stock_id = ");
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
			queryBuilder.append(" and contact_id = ");
			queryBuilder.append(params.contact.id);
		}

		if (params.startDate != null) {
			queryBuilder.append(" and trans_date >= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.startDate));
		}

		if (params.endDate != null) {
			queryBuilder.append(" and trans_date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.endDate));
		}

		if (params.depot != null && params.depot.id != null) {
			queryBuilder.append(" and depot_id = ");
			queryBuilder.append(params.depot.id);
		}

		if (params.seller != null && params.seller.id != null) {
			queryBuilder.append(" and t.seller_id = ");
			queryBuilder.append(params.seller.id);
		}

		if (params.transSource != null && params.transSource.id != null) {
			queryBuilder.append(" and trans_source_id = ");
			queryBuilder.append(params.transSource.id);
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<SellingReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(selling_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.query = getQueryString(params);
			repPar.reportUnit = params.unit;
			repPar.reportName = REPORT_NAME;

			String field = "";
			String label = "";

			if (params.reportType.equals("Stock")) {
				field = "s.name";
				label = Messages.get("stock.name");
			}
			if (params.reportType.equals("Contact")) {
				field = "c.name";
				label = Messages.get("contact.name");
			}
			if (params.reportType.equals("Depot")) {
				field = "d.name";
				label = Messages.get("depot");
			}
			if (params.reportType.equals("Monthly")) {
				field = "t.trans_month";
				label = Messages.get("trans.month");
			}
			if (params.reportType.equals("TransSource")) {
				field = "ts.name";
				label = Messages.get("trans.source");
			}
			if (params.reportType.startsWith("ExtraFields")) {
				Integer extraFieldsId = Integer.parseInt("0"+params.reportType.charAt(params.reportType.length()-1));
				field = "ef"+extraFieldsId+".name";
				AdminExtraFields aef = AdminExtraFields.findById(Module.contact.name(), extraFieldsId);
				label = aef.name;
			}

			repPar.paramMap.put("GROUP_FIELD", field);
			repPar.paramMap.put("GROUP_LABEL", label);

			/*
			 * Parametrik degerlerin gecisi
			 */
			repPar.paramMap.put("EXTRA_FIELDS_SQL", QueryUtils.buildExtraFieldsQueryForContact(params, field));

			repPar.paramMap.put("CATEGORY_SQL", "");
			if (params.category != null && params.category.id != null) {
				repPar.paramMap.put("CATEGORY_SQL", InstantSQL.buildCategorySQL(params.category.id));
			}

			repPar.paramMap.put("TRANS_POINT_SQL", "");
			if (params.transPoint != null && params.transPoint.id != null) {
				repPar.paramMap.put("TRANS_POINT_SQL", InstantSQL.buildTransPointSQL(params.transPoint.id));
			}

			repPar.paramMap.put("PRIVATE_CODE_SQL", "");
			if (params.privateCode != null && params.privateCode.id != null) {
				repPar.paramMap.put("PRIVATE_CODE_SQL", InstantSQL.buildPrivateCodeSQL(params.privateCode.id));
			}

			repPar.paramMap.put("REPORT_INFO", Messages.get("report.info.date_range", DateUtils.formatDateStandart(params.startDate), DateUtils.formatDateStandart(params.endDate)));

			ReportResult repRes = ReportService.generateReport(repPar, response());
			return ReportService.sendReport(repPar, repRes, selling_report.render(filledForm));
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(selling_report.render(parameterForm.fill(new Parameter())));
	}

}

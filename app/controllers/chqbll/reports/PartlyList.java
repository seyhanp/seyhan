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
package controllers.chqbll.reports;

import static play.data.Form.form;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import models.Bank;
import models.ChqbllType;
import models.Contact;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;
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
import views.html.chqblls.reports.partly_list;
import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class PartlyList extends Controller {

	private final static String REPORT_NAME = "PartlyList";
	private final static Form<PartlyList.Parameter> parameterForm = form(PartlyList.Parameter.class);

	public static class Parameter {

		public ChqbllSort sort;

		public String orderby;
		public String orderdir;
		public ReportUnit unit;

		public Contact contact;
		public Bank bank;

		public GlobalTransPoint transPoint = Profiles.chosen().gnel_transPoint;
		public GlobalPrivateCode privateCode = Profiles.chosen().gnel_privateCode;

		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.getFirstDayOfYear();

		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

		public ChqbllType cbtype;

		public Boolean balanceType;
		public Boolean direction;
		public String showType;
		public String reportType;

		public static Map<String, String> options() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("due_date", Messages.get("maturity"));
			options.put("last_step", Messages.get("last_status"));
			options.put("last_contact_name", Messages.get("contact.name"));
			options.put("bank_name", Messages.get("bank.name"));
			options.put("portfolio_no", Messages.get("portfolio.no"));
			options.put("serial_no", Messages.get("serial.no"));

			return options;
		}

		public static Map<String, String> orderdirOptions() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put(" desc", Messages.get("descending"));
			options.put(" asc", Messages.get("ascending"));

			return options;
		}

		public static Map<String, String> showTypes() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			options.put("Summary", Messages.get("report.show.summary"));
			options.put("Detailed", Messages.get("report.show.detail"));

			return options;
		}

		public static Map<String, String> balanceTypes() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			options.put(Boolean.TRUE.toString(),  Messages.get("balance.open"));
			options.put(Boolean.FALSE.toString(), Messages.get("balance.completed"));

			return options;
		}

		public static Map<String, String> direction() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			options.put(Boolean.TRUE.toString(),  Messages.get("collecting"));
			options.put(Boolean.FALSE.toString(), Messages.get("payment"));

			return options;
		}

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		queryBuilder.append(" and t.sort = '");
		queryBuilder.append(params.sort);
		queryBuilder.append("'");

		queryBuilder.append(" and t.is_customer = ");
		queryBuilder.append(params.direction.toString());

		queryBuilder.append(" and last_step = '");
		if (params.direction) {
			queryBuilder.append(ChqbllStep.PartCollection);
		} else {
			queryBuilder.append(ChqbllStep.PartPayment);
		}
		queryBuilder.append("'");

		if (params.balanceType != null) {
			if (params.balanceType) {
				queryBuilder.append(" and t.amount > total_paid");
			} else {
				queryBuilder.append(" and t.amount = total_paid");
			}
		}

		if (params.contact != null && params.contact.id != null) {
			queryBuilder.append(" and contact_id = ");
			queryBuilder.append(params.contact.id);
		}

		if (params.bank != null && params.bank.id != null) {
			queryBuilder.append(" and bank_id = ");
			queryBuilder.append(params.bank.id);
		}

		if (params.startDate != null) {
			queryBuilder.append(" and due_date >= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.startDate));
		}

		if (params.endDate != null) {
			queryBuilder.append(" and due_date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.endDate));
		}

		if (params.cbtype != null && params.cbtype.id != null) {
			queryBuilder.append(" and cbtype_id = ");
			queryBuilder.append(params.cbtype.id);
		}

		return queryBuilder.toString();
	}

	public static Result index(String sortStr) {
		ChqbllSort sort = ChqbllSort.Cheque;
		try {
			sort = ChqbllSort.valueOf(sortStr);
		} catch (Exception e) { }

		if (ChqbllSort.Cheque.equals(sort))
			return index(Right.CEK_PARCALI_LISTESI, sort);
		else
			return index(Right.SENET_PARCALI_LISTESI, sort);
	}

	public static Result generate(String sortStr) {
		ChqbllSort sort = ChqbllSort.Cheque;
		try {
			sort = ChqbllSort.valueOf(sortStr);
		} catch (Exception e) { }

		if (ChqbllSort.Cheque.equals(sort))
			return generate(Right.CEK_PARCALI_LISTESI, sort);
		else
			return generate(Right.SENET_PARCALI_LISTESI, sort);
	}

	private static Result generate(Right right, ChqbllSort sort) {
		Result hasProblem = AuthManager.hasProblem(right, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<PartlyList.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(partly_list.render(filledForm, sort));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = "chqbll";
			repPar.reportName = REPORT_NAME + params.showType;
			repPar.reportUnit = params.unit;
			repPar.query = getQueryString(params);
			repPar.orderBy = params.orderby + params.orderdir + ("Detailed".equals(params.showType) ? ", trans_date" : "");

			repPar.paramMap.put("SORT",  params.sort.name());

			/*
			 * Parametrik degerlerin gecisi
			 */
			repPar.paramMap.put("TRANS_POINT_SQL", "");
			if (params.transPoint != null && params.transPoint.id != null) {
				repPar.paramMap.put("TRANS_POINT_SQL", InstantSQL.buildTransPointSQL(params.transPoint.id));
			}

			repPar.paramMap.put("PRIVATE_CODE_SQL", "");
			if (params.privateCode != null && params.privateCode.id != null) {
				repPar.paramMap.put("PRIVATE_CODE_SQL", InstantSQL.buildPrivateCodeSQL(params.privateCode.id));
			}

			String par1 = Messages.get("chqbll.ofs", Messages.get(params.direction ? "enum.cqbl.Customer" : "enum.cqbl.Firm"),  Messages.get(params.sort.key));
			String par2 = "(" + DateUtils.formatDateStandart(params.startDate) + " - " + DateUtils.formatDateStandart(params.endDate) +")";
			repPar.paramMap.put("REPORT_INFO", par1 + " - " + par2);

			ReportResult repRes = ReportService.generateReport(repPar, response());
			return ReportService.sendReport(repPar, repRes, partly_list.render(filledForm, sort));
		}

	}

	private static Result index(Right right, ChqbllSort sort) {
		Result hasProblem = AuthManager.hasProblem(right, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(partly_list.render(parameterForm.fill(new Parameter()), sort));
	}

}

/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.chqbll.reports;

import static play.data.Form.form;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import models.Bank;
import models.ChqbllPayrollSource;
import models.Contact;
import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.Safe;
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
import views.html.chqblls.reports.trans_list;
import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class TransList extends Controller {

	private final static String REPORT_NAME = "TransList";
	private final static Form<TransList.Parameter> parameterForm = form(TransList.Parameter.class);

	public static class Parameter {

		public ChqbllSort sort;
		public Right right;

		public ChqbllStep toStep;

		public String orderby;
		public ReportUnit unit;

		public Contact contact;
		public Safe safe;
		public Bank bank;

		public GlobalTransPoint transPoint = Profiles.chosen().gnel_transPoint;
		public GlobalPrivateCode privateCode = Profiles.chosen().gnel_privateCode;

		public String transNo;
		public ChqbllPayrollSource transSource;

		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = DateUtils.getFirstDayOfMonth();

		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

		@DateTime(pattern = "dd/MM/yyyy")
		public Date avarageDate;

		public String showType;

		public static Map<String, String> options() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("t.trans_date, b.name", Messages.get("date"));
			options.put("b.name, t.trans_date", Messages.get("bank.name"));
			options.put("t.avarage_date", Messages.get("date.avarage") + " " + Messages.get("ascending"));
			options.put("t.avarage_date desc", Messages.get("date.avarage") + " " + Messages.get("descending"));

			return options;
		}

		public static Map<String, String> showTypes() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			options.put("Summary", Messages.get("report.show.summary"));
			options.put("Detailed", Messages.get("report.show.detail"));

			return options;
		}

		public static Map<String, String> rights(ChqbllSort sort) {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();

			if (ChqbllSort.Cheque.equals(sort)) {
				options.put(Right.CEK_MUSTERI_HAREKETLERI.name(), Messages.get(Right.CEK_MUSTERI_HAREKETLERI.key));
				options.put(Right.CEK_FIRMA_HAREKETLERI.name(), Messages.get(Right.CEK_FIRMA_HAREKETLERI.key));
			} else {
				options.put(Right.SENET_MUSTERI_HAREKETLERI.name(), Messages.get(Right.SENET_MUSTERI_HAREKETLERI.key));
				options.put(Right.SENET_FIRMA_HAREKETLERI.name(), Messages.get(Right.SENET_FIRMA_HAREKETLERI.key));
			}

			return options;
		}

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		queryBuilder.append(" and t.sort = '");
		queryBuilder.append(params.sort);
		queryBuilder.append("'");

		queryBuilder.append(" and t._right = '");
		queryBuilder.append(params.right.name());
		queryBuilder.append("'");

		if (params.toStep != null) {
			queryBuilder.append(" and t.to_step = '");
			queryBuilder.append(params.toStep);
			queryBuilder.append("'");
		}

		if (params.contact != null && params.contact.id != null) {
			queryBuilder.append(" and t.contact_id = ");
			queryBuilder.append(params.contact.id);
		}

		if (params.safe != null && params.safe.id != null) {
			queryBuilder.append(" and t.safe_id = ");
			queryBuilder.append(params.safe.id);
		}

		if (params.bank != null && params.bank.id != null) {
			queryBuilder.append(" and t.bank_id = ");
			queryBuilder.append(params.bank.id);
		}

		if (params.startDate != null) {
			queryBuilder.append(" and t.trans_date >= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.startDate));
		}

		if (params.endDate != null) {
			queryBuilder.append(" and t.trans_date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.endDate));
		}

		if (params.avarageDate != null) {
			queryBuilder.append(" and t.avarage_date = ");
			queryBuilder.append(DateUtils.formatDateForDB(params.avarageDate));
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

		return queryBuilder.toString();
	}

	public static Result index(String sortStr) {
		ChqbllSort sort = ChqbllSort.Cheque;
		try {
			sort = ChqbllSort.valueOf(sortStr);
		} catch (Exception e) { }

		if (ChqbllSort.Cheque.equals(sort))
			return index(Right.CEK_HAREKET_BORDRO_LISTESI, sort);
		else
			return index(Right.SENET_HAREKET_BORDRO_LISTESI, sort);
	}

	public static Result generate(String sortStr) {
		ChqbllSort sort = ChqbllSort.Cheque;
		try {
			sort = ChqbllSort.valueOf(sortStr);
		} catch (Exception e) { }

		if (ChqbllSort.Cheque.equals(sort))
			return generate(Right.CEK_HAREKET_BORDRO_LISTESI, sort);
		else
			return generate(Right.SENET_HAREKET_BORDRO_LISTESI, sort);
	}

	private static Result generate(Right right, ChqbllSort sort) {
		Result hasProblem = AuthManager.hasProblem(right, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<TransList.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(trans_list.render(filledForm, sort));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = "chqbll";
			repPar.reportName = REPORT_NAME + params.showType;
			repPar.reportUnit = params.unit;
			repPar.query = getQueryString(params);
			repPar.orderBy = params.orderby;

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

			String par1 = Messages.get("chqbll.ofs", Messages.get(ChqbllStep.isCustomer(params.right) ? "enum.cqbl.Customer" : "enum.cqbl.Firm"),  Messages.get(params.sort.key));
			String par2 = "(" + DateUtils.formatDateStandart(params.startDate) + " - " + DateUtils.formatDateStandart(params.endDate) +")";
			repPar.paramMap.put("REPORT_INFO", par1 + " - " + par2);

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(trans_list.render(filledForm, sort));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index(Right right, ChqbllSort sort) {
		Result hasProblem = AuthManager.hasProblem(right, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(trans_list.render(parameterForm.fill(new Parameter()), sort));
	}

}

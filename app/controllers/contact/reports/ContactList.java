/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.contact.reports;

import static play.data.Form.form;

import java.util.LinkedHashMap;
import java.util.Map;

import models.temporal.ExtraFieldsForContact;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import reports.ReportParams;
import reports.ReportService;
import reports.ReportService.ReportResult;
import utils.AuthManager;
import utils.CacheUtils;
import utils.QueryUtils;
import views.html.contacts.reports.contact_list;
import enums.ContactStatus;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class ContactList extends Controller {

	private final static Right RIGHT_SCOPE = Right.CARI_HESAP_LISTESI;
	private final static String REPORT_NAME = "List";

	private final static Form<ContactList.Parameter> parameterForm = form(ContactList.Parameter.class);

	public static class Parameter extends ExtraFieldsForContact {

		public String orderBy;
		public ReportUnit unit;

		public ContactStatus status;

		public static Map<String, String> options() {
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("c.name", Messages.get("name"));
			options.put("c.code", Messages.get("code"));

			return options;
		}

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and workspace = " + CacheUtils.getWorkspaceId());

		if (params.category != null && params.category.id != null) {
			queryBuilder.append(" and category_id = ");
			queryBuilder.append(params.category.id);
		}

		if (params.seller != null && params.seller.id != null) {
			queryBuilder.append(" and seller_id = ");
			queryBuilder.append(params.seller.id);
		}

		if (params.status != null) {
			queryBuilder.append(" and status = '");
			queryBuilder.append(params.status);
			queryBuilder.append("'");
		}

		QueryUtils.addExtraFieldsCriterias(params, queryBuilder);

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<ContactList.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(contact_list.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME;
			repPar.reportUnit = params.unit;
			repPar.query = getQueryString(params);
			repPar.orderBy = params.orderBy;

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(contact_list.render(filledForm));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(contact_list.render(parameterForm.fill(new Parameter())));
	}

}

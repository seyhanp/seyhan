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
package controllers.safe;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.Safe;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.QueryUtils;
import utils.StringUtils;
import views.html.safes.safe.form;
import views.html.safes.safe.index;
import views.html.safes.safe.list;
import views.html.tools.templates.investigation_form;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;

import controllers.Application;
import controllers.global.Profiles;
import enums.Module;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Safes extends Controller {

	private final static Right RIGHT_SCOPE = Right.KASA_TANITIMI;

	private final static Logger log = LoggerFactory.getLogger(Safes.class);
	private final static Form<Safe> dataForm = form(Safe.class);

	private static String lastSaved;

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), true).sortable("name"));
		if (Profiles.chosen().gnel_hasExchangeSupport) {
			headerList.add(new GridHeader(Messages.get("currency"), "4%", "center", null));
		}
		headerList.add(new GridHeader(Messages.get("responsible"), "20%"));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<Safe> buildPage() {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<Safe> modelList = Safe.page();
		if (modelList != null && modelList.size() > 0) {
			for (Safe model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);
				if (Profiles.chosen().gnel_hasExchangeSupport) {
					dataMap.put(i++, model.excCode);
				}
				dataMap.put(i++, model.responsible);
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<Safe>(getHeaderList(), dataList, null);
	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(
			index.render(buildPage())
		);
	}

	public static Result options(boolean hasBlankOption) {
		Result result = ok(StringUtils.buildOptionTag(Safe.options(), lastSaved, hasBlankOption));
		lastSaved = null;

		return result;
	}

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(
			list.render(buildPage())
		);
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<Safe> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			Safe model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) return badRequest(editingConstraintError);

			checkConstraints(filledForm);

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}

			try {
				if (model.id == null) {
					model.save();
				} else {
					if (model.id.equals(Profiles.chosen().gnel_safe.id) && ! model.isActive) model.isActive = Boolean.TRUE;
					model.update();
				}
			} catch (OptimisticLockException e) {
				flash("error", Messages.get("exception.optimistic.lock"));
				return badRequest(form.render(dataForm.fill(model)));
			}
			lastSaved = model.name;

			flash("success", Messages.get("saved", model.name));
			if (Profiles.chosen().gnel_continuouslyRecording)
				return create();
			else
				return ok();
		}
	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		return ok(form.render(dataForm.fill(new Safe())));
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			Safe model = Safe.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("safe")));
			} else {
				return ok(form.render(dataForm.fill(model)));
			}
		}
	}

	/**
	 * Duzenlemek icin acilmis olan kaydi siler
	 * 
	 * @param id
	 */
	public static Result remove(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Delete);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			Safe model = Safe.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("safe")));
			} else if (model.id.equals(Profiles.chosen().gnel_safe.id)) {
				flash("error", Messages.get("delete.violation.for.default.safe"));
				return badRequest(Messages.get("delete.violation.for.default.safe"));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) return badRequest(editingConstraintError);
				try {
					model.delete();
					flash("success", Messages.get("deleted", model.name));
					return ok();
				} catch (PersistenceException pe) {
					flash("error", Messages.get("delete.violation", model.name));
					log.error("ERROR", pe);
					return badRequest(Messages.get("delete.violation", model.name));
				}
			}
		}
	}

	public static Result investigation(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Safe safe = Safe.findById(id);

		ObjectNode result = Json.newObject();

		result.put("title", safe.name);
		result.put("body", investigation_form.render(
								QueryUtils.inspectXTrans(Module.safe, safe.id),
								QueryUtils.inspectXSummary(Module.safe, safe.id),
								null, null
							).body()
					);

		return ok(result);
	}

	public static double findBalance(Integer safeId, Date date) {
		StringBuilder querySB = new StringBuilder();
		querySB.append("select sum(debt) as sumDebt, sum(credit) as sumCredit from safe_trans ");
		querySB.append("where workspace = :workspace ");
		querySB.append("  and safe_id = :safe_id ");
		if (date != null) {
			querySB.append("  and trans_date < :trans_date");
		}
		querySB.append(" group by workspace ");

		SqlQuery query = Ebean.createSqlQuery(querySB.toString());
		query.setParameter("workspace", CacheUtils.getWorkspaceId());
		query.setParameter("safe_id", safeId);
		query.setParameter("trans_date", date);
		SqlRow row = query.findUnique();

		double result = 0;
		if (row != null && row.size() > 0) {
			if (row.getDouble("sumDebt") != null) result += row.getDouble("sumDebt").doubleValue();
			if (row.getDouble("sumCredit") != null) result -= row.getDouble("sumCredit").doubleValue();
		}

		return result;
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<Safe> filledForm) {
		Safe model = filledForm.get();

		if (Safe.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}

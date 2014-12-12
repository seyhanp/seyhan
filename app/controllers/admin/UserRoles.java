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
package controllers.admin;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.AdminUserRight;
import models.AdminUserRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CacheUtils;
import views.html.admins.user_role.form;
import views.html.admins.user_role.list;
import controllers.Application;
import controllers.global.Profiles;

/**
 * @author mdpinar
*/
public class UserRoles extends Controller {

	private final static Logger log = LoggerFactory.getLogger(UserRoles.class);
	private final static Form<AdminUserRole> dataForm = form(AdminUserRole.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), true).sortable("name"));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<AdminUserRole> buildPage() {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<AdminUserRole> modelList = AdminUserRole.page();
		if (modelList != null && modelList.size() > 0) {
			for (AdminUserRole model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<AdminUserRole>(getHeaderList(), dataList, null);
	}

	public static Result GO_HOME = redirect(
		controllers.admin.routes.UserRoles.list()
	);

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(list.render(buildPage()));
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<AdminUserRole> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, UserRights.definedRights(filledForm.data())));
		} else {

			AdminUserRole model = filledForm.get();
			checkConstraints(filledForm);

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm, model.rights));
			}

			if (model.id == null) {
				model.save();
			} else {
				List<AdminUserRight> willDelete = new ArrayList<AdminUserRight>();
				for (int i = 0; i < model.rights.size(); i++) {
					AdminUserRight ur = model.rights.get(i);
					if (ur.rightLevel.ordinal() < 1) willDelete.add(ur);
				}
				for (int i = 0; i < willDelete.size(); i++) {
					model.rights.remove(willDelete.get(i));
				}
				model.update();
			}

			flash("success", Messages.get("saved", model.name));
			if (Profiles.chosen().gnel_continuouslyRecording)
				return create();
			else
				return GO_HOME;
		}

	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		AdminUserRole neu = new AdminUserRole("");
		return ok(form.render(dataForm.fill(neu), UserRights.definedRights()));
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			AdminUserRole model = AdminUserRole.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("role")));
			} else {
				return ok(form.render(dataForm.fill(model), model.getRights()));
			}
		}
		return GO_HOME;
	}

	/**
	 * Duzenlemek icin acilmis olan kaydi siler
	 * 
	 * @param id
	 */
	public static Result remove(Integer id) {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			AdminUserRole model = AdminUserRole.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("role")));
			} else {
				try {
					model.delete();
					flash("success", Messages.get("deleted", model.name));
				} catch (PersistenceException pe) {
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", model.name));
					return badRequest(form.render(dataForm.fill(model), model.getRights()));
				}
			}
		}
		return GO_HOME;
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<AdminUserRole> filledForm) {
		AdminUserRole model = filledForm.get();

		if (AdminUserRole.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}

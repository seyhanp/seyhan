/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.admin;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.AdminUserGivenRole;
import models.AdminUserGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CacheUtils;
import views.html.admins.user_group.form;
import views.html.admins.user_group.list;
import controllers.Application;
import controllers.global.Profiles;

/**
 * @author mdpinar
*/
public class UserGroups extends Controller {

	private final static Logger log = LoggerFactory.getLogger(UserGroups.class);
	private final static Form<AdminUserGroup> dataForm = form(AdminUserGroup.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), "18%", true, null).sortable("name"));
		headerList.add(new GridHeader(Messages.get("description")));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<AdminUserGroup> buildPage() {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<AdminUserGroup> modelList = AdminUserGroup.page();
		if (modelList != null && modelList.size() > 0) {
			for (AdminUserGroup model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);
				dataMap.put(i++, model.description);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<AdminUserGroup>(getHeaderList(), dataList, null);
	}

	public static Result GO_HOME = redirect(
		controllers.admin.routes.UserGroups.list()
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

		Form<AdminUserGroup> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			AdminUserGroup model = filledForm.get();
			checkConstraints(filledForm);

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}

			List<AdminUserGivenRole> roles = new ArrayList<AdminUserGivenRole>();
			for (AdminUserGivenRole role: model.roles) {
				if (role.workspace.id == null || role.userRole.id == null) {
					continue;
				}

				roles.add(role);
			}

			if (roles.size() == 0) {
				filledForm.reject("roles", Messages.get("table.min.row.alert"));
				return badRequest(form.render(filledForm));
			}

			model.roles = roles;
			try {
				if (model.id == null) {
					model.save();
				} else {
					model.update();
				}
			} catch (OptimisticLockException e) {
				flash("error", Messages.get("exception.optimistic.lock"));
				return badRequest(form.render(dataForm.fill(model)));
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

		AdminUserGroup neu = new AdminUserGroup();
		neu.loadMissingRoles();

		return ok(form.render(dataForm.fill(neu)));
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
			AdminUserGroup model = AdminUserGroup.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("role")));
			} else {
				model.loadMissingRoles();
				return ok(form.render(dataForm.fill(model)));
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
			AdminUserGroup model = AdminUserGroup.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("role")));
			} else {
				try {
					model.delete();
					flash("success", Messages.get("deleted", model.name));
				} catch (PersistenceException pe) {
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", model.name));
					return badRequest(form.render(dataForm.fill(model)));
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
	private static void checkConstraints(Form<AdminUserGroup> filledForm) {
		AdminUserGroup model = filledForm.get();

		if (AdminUserGroup.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}

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

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.AdminUser;
import models.search.NameOnlySearchParam;
import models.temporal.UserData;
import models.temporal.UserMultiplierData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import views.html.admins.user.form;
import views.html.admins.user.list;
import views.html.admins.user.multiplier;
import views.html.admins.user.restricted_form;

import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;

/**
 * @author mdpinar
*/
public class Users extends Controller {

	private final static Logger log = LoggerFactory.getLogger(Users.class);
	private final static Form<AdminUser> dataForm = form(AdminUser.class);
	private final static Form<NameOnlySearchParam> paramForm = form(NameOnlySearchParam.class);

	private static List<GridHeader> headerList;

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		if (headerList == null) {
			headerList = new ArrayList<GridHeader>();
			headerList.add(new GridHeader(Messages.get("username"), "12%").sortable("username"));
			headerList.add(new GridHeader(Messages.get("name"), true).sortable("title"));
			headerList.add(new GridHeader("Email", "25%"));
			headerList.add(new GridHeader(Messages.get("group"), "15%"));
			headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));
		}

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<AdminUser> buildPage(NameOnlySearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<AdminUser> page = AdminUser.page(searchParam);
		List<AdminUser> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (AdminUser model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.username);
				dataMap.put(i++, model.title);
				dataMap.put(i++, model.email);
				dataMap.put(i++, (model.userGroup != null ? model.userGroup.toString() : ""));
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<AdminUser>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME = redirect(
		controllers.admin.routes.Users.list()
	);

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<NameOnlySearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(list.render(buildPage(filledParamForm.get()), filledParamForm));
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<AdminUser> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			AdminUser model = filledForm.get();
			checkConstraints(filledForm);

			if(filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}
			model.passwordHash = AuthManager.md5Hash(model.password);

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

			flash("success", Messages.get("saved", model.username));
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

		return ok(form.render(dataForm.fill(new AdminUser())));
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
			AdminUser model = AdminUser.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("user")));
			} else {
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
			AdminUser model = AdminUser.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("user")));
			} else {
				if (model.id == 1) {
					flash("error", Messages.get("cannot.delete", "super user"));
				} else {
					try {
						model.delete();
						flash("success", Messages.get("deleted", model.username));
					} catch (PersistenceException pe) {
						log.error(pe.getMessage());
						flash("error", Messages.get("delete.violation", model.username));
						return badRequest(form.render(dataForm.fill(model)));
					}
				}
			}
		}
		return GO_HOME;
	}

	/**
	 * Secilen kaydin kopyasini olusturur
	 * 
	 * @param id
	 */
	public static Result createClone(Integer id) {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		AdminUser source = AdminUser.findById(id);

		UserMultiplierData im = new UserMultiplierData();
		im.id = id;
		im.title =  source.title;

		Form<UserMultiplierData> imDataForm = form(UserMultiplierData.class);

		return ok(
			multiplier.render(imDataForm.fill(im))
		);
	}

	/**
	 * Yeni kopyayi kaydeder
	 */
	public static Result saveClone() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<UserMultiplierData> filledForm = form(UserMultiplierData.class).bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(multiplier.render(filledForm));
		} else {

			checkConstraintsForMultiplier(filledForm);
			if (filledForm.hasErrors()) {
				return badRequest(multiplier.render(filledForm));
			}

			UserMultiplierData im = filledForm.get();

			AdminUser source = AdminUser.findById(im.id);
			AdminUser clone = new AdminUser(im.username);

			clone.username = im.username;
			clone.title = im.title;
			clone.email = im.email;
			clone.passwordHash = AuthManager.md5Hash(im.password);
			clone.userGroup = source.userGroup;

			clone.save();
			return ok(Messages.get("saved", clone.username));
		}
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraintsForMultiplier(Form<UserMultiplierData> filledForm) {
		UserMultiplierData model = filledForm.get();

		if (AdminUser.isUsedForElse("username", model.username, model.id)) {
			filledForm.reject("username", Messages.get("not.unique", model.username));
		}

		if (! model.password.equals(model.repeatPassword)) {
			filledForm.reject("repeatPassword", Messages.get("passwords.arent.equal"));
		}
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<AdminUser> filledForm) {
		AdminUser model = filledForm.get();

		if (AdminUser.isUsedForElse("username", model.username, model.id)) {
			filledForm.reject("username", Messages.get("not.unique", model.username));
		}

		if (! model.password.equals(model.repeatPassword)) {
			filledForm.reject("repeatPassword", Messages.get("passwords.arent.equal"));
		}
	}

	public static Result saveRestricted() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<UserData> resFilledForm = form(UserData.class).bindFromRequest();

		if(resFilledForm.hasErrors()) {
			return badRequest(restricted_form.render(resFilledForm));
		} else {

			AdminUser model = AdminUser.findById(CacheUtils.getUser().id);

			checkRestrictedConstraints(model, resFilledForm);

			if(resFilledForm.hasErrors()) {
				return badRequest(restricted_form.render(resFilledForm));
			}
			UserData resModel = resFilledForm.get();

			model.title = resModel.title;
			model.email = resModel.email;
			model.passwordHash = AuthManager.md5Hash(resModel.password);

			model.update();

			return ok(Messages.get("saved", model.username));
		}

	}

	public static Result editRestricted() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		UserData data = new UserData();
		data.title = CacheUtils.getUser().title;
		data.email = data.email;

		return ok(restricted_form.render(form(UserData.class).fill(data)));
	}

	private static void checkRestrictedConstraints(AdminUser model, Form<UserData> resFilledForm) {
		UserData resModel = resFilledForm.get();
		resModel.passwordHash = AuthManager.md5Hash(resModel.oldPassword);

		if (! model.passwordHash.equals(resModel.passwordHash)) {
			resFilledForm.reject("oldPassword", Messages.get("old.password.is.wrong"));
		}

		if (! resModel.password.equals(resModel.repeatPassword)) {
			resFilledForm.reject("repeatPassword", Messages.get("passwords.arent.equal"));
		}

	}

}

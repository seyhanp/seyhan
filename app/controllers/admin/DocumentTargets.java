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
import models.AdminDocumentTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CacheUtils;
import views.html.admins.document_target.form;
import views.html.admins.document_target.index;
import views.html.admins.document_target.list;
import controllers.Application;
import controllers.global.Profiles;

/**
 * @author mdpinar
*/
public class DocumentTargets extends Controller {

	private final static Logger log = LoggerFactory.getLogger(DocumentTargets.class);
	private final static Form<AdminDocumentTarget> dataForm = form(AdminDocumentTarget.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("location"), "7%", "center", null));
		headerList.add(new GridHeader(Messages.get("name", "6%"), true).sortable("name"));
		headerList.add(new GridHeader(Messages.get("print_target.type"), "15%", "center", null));
		headerList.add(new GridHeader(Messages.get("path")));
		headerList.add(new GridHeader(Messages.get("description"), "15%"));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<AdminDocumentTarget> buildPage() {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<AdminDocumentTarget> modelList = AdminDocumentTarget.page();
		if (modelList != null && modelList.size() > 0) {
			for (AdminDocumentTarget model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, Messages.get(model.isLocal ? "local" : "remote"));
				dataMap.put(i++, model.name);
				dataMap.put(i++, Messages.get("print_target." + model.targetType));
				dataMap.put(i++, model.path);
				dataMap.put(i++, model.description);
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<AdminDocumentTarget>(getHeaderList(), dataList, null);
	}

	public static Result index() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(
			index.render(buildPage())
		);
	}

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(
			list.render(buildPage())
		);
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<AdminDocumentTarget> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			AdminDocumentTarget model = filledForm.get();
			checkConstraints(filledForm);

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}

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
				return ok();
		}
	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(form.render(dataForm.fill(new AdminDocumentTarget())));
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			AdminDocumentTarget model = AdminDocumentTarget.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("target")));
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
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			AdminDocumentTarget model = AdminDocumentTarget.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("target")));
			} else {
				try {
					model.delete();
					flash("success", Messages.get("deleted", model.name));
					return ok();
				} catch (PersistenceException pe) {
					log.error("ERROR", pe);
					flash("error", Messages.get("delete.violation", model.name));
					return badRequest(Messages.get("delete.violation", model.name));
				}
			}
		}
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<AdminDocumentTarget> filledForm) {
		AdminDocumentTarget model = filledForm.get();

		if (AdminDocumentTarget.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}

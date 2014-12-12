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

import meta.GridHeader;
import meta.PageExtend;
import models.AdminExtraFields;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CacheUtils;
import views.html.admins.extra_fields_for_stock.form;
import views.html.admins.extra_fields_for_stock.index;
import views.html.admins.extra_fields_for_stock.list;
import controllers.Application;
import enums.Module;

/**
 * @author mdpinar
*/
public class ExtraFieldsForStocks extends Controller {

	private final static Form<AdminExtraFields> dataForm = form(AdminExtraFields.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name")));
		headerList.add(new GridHeader(Messages.get("constraint.required"), "8%", true));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<AdminExtraFields> buildPage() {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<AdminExtraFields> modelList = AdminExtraFields.page(Module.stock.name());
		if (modelList != null && modelList.size() > 0) {
			for (AdminExtraFields model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.idno.toString());
				dataMap.put(i++, model.name);
				dataMap.put(i++, model.isRequired.toString());
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<AdminExtraFields>(getHeaderList(), dataList, null);
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

		Form<AdminExtraFields> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			AdminExtraFields model = filledForm.get();
			checkConstraints(filledForm);

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}

			model.update();

			flash("success", Messages.get("saved", model.name));
			return ok();
		}

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
			AdminExtraFields model = AdminExtraFields.findById(Module.stock.name(), id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("extra_field")));
			} else {
				return ok(form.render(dataForm.fill(model)));
			}
		}
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<AdminExtraFields> filledForm) {
		AdminExtraFields model = filledForm.get();

		if (AdminExtraFields.isUsedForElse("name", model.name, model.id, Module.stock.name())) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}

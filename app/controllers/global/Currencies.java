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
package controllers.global;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.GlobalCurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import views.html.globals.currency.form;
import views.html.globals.currency.index;
import views.html.globals.currency.list;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Currencies extends Controller {

	private final static Right RIGHT_SCOPE = Right.GNEL_DOVIZ_BIRIMLERI;

	private final static Logger log = LoggerFactory.getLogger(Currencies.class);
	private final static Form<GlobalCurrency> dataForm = form(GlobalCurrency.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("code"), "10%").sortable("code"));
		headerList.add(new GridHeader(Messages.get("name"), true).sortable("name"));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true).sortable("isActive"));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<GlobalCurrency> buildPage() {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<GlobalCurrency> modelList = GlobalCurrency.page();
		if (modelList != null && modelList.size() > 0) {
			for (GlobalCurrency model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.code);
				dataMap.put(i++, model.name);
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<GlobalCurrency>(getHeaderList(), dataList, null);
	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(
			index.render(buildPage())
		);
	}

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		return ok(
			list.render(buildPage())
		);
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		Form<GlobalCurrency> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			GlobalCurrency model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			checkConstraints(filledForm);

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}

			if (Profiles.chosen().gnel_excCode.equals(model.code)) {
				model.isActive = true;
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

			CacheUtils.remove(true, "exchanges");

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
		if (hasProblem != null) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		return ok(form.render(dataForm.fill(new GlobalCurrency())));
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			GlobalCurrency model = GlobalCurrency.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("currency")));
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
		if (hasProblem != null) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			GlobalCurrency model = GlobalCurrency.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("currency")));
			} else if (Profiles.chosen().gnel_excCode.equals(model.code)) {
				return badRequest(Messages.get("cannot.delete", model.name));
			} else {
				try {
					model.delete();
					CacheUtils.remove(true, "exchanges");

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

	public static Map<String, GlobalCurrency> getCurrencyMap() {
		Map<String, GlobalCurrency> result = CacheUtils.get(true, "exchanges");

		if (result == null) {
			result = new HashMap<String, GlobalCurrency>();
			List<GlobalCurrency> curList = GlobalCurrency.getAll();
			for (GlobalCurrency cur : curList) {
				result.put(cur.code, cur);
			}
			CacheUtils.set(true, "exchanges", result);
		}

		return result;
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<GlobalCurrency> filledForm) {
		GlobalCurrency model = filledForm.get();

		if (GlobalCurrency.isUsedForElse("code", model.code, model.id)) {
			filledForm.reject("code", Messages.get("not.unique", model.code));
		}

		if (GlobalCurrency.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}

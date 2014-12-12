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
package controllers.stock;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.StockPriceUpdate;
import models.search.NameOnlySearchParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.DateUtils;
import utils.Format;
import views.html.stocks.price_update.form;
import views.html.stocks.price_update.list;

import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.EffectType;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class PriceUpdates extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_FIYAT_GUNCELLEME;

	private final static Logger log = LoggerFactory.getLogger(PriceUpdates.class);
	private final static Form<StockPriceUpdate> dataForm = form(StockPriceUpdate.class);
	private final static Form<NameOnlySearchParam> paramForm = form(NameOnlySearchParam.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), "20%", true).sortable("name"));
		headerList.add(new GridHeader(Messages.get("date.execution"), "12%", "center", null).sortable("execDate"));
		headerList.add(new GridHeader(Messages.get("description")));
		headerList.add(new GridHeader(Messages.get("effect_type"),  "8%", "center", null));
		headerList.add(new GridHeader(Messages.get("effect"),  "8%", "center", null));
		headerList.add(new GridHeader(Messages.get("effect_dir"),  "10%", "center", null));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<StockPriceUpdate> buildPage(NameOnlySearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<StockPriceUpdate> page = StockPriceUpdate.page(searchParam);
		List<StockPriceUpdate> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (StockPriceUpdate model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);
				dataMap.put(i++, DateUtils.formatDate(model.execDate, "dd/MM/yyyy HH:mm"));
				dataMap.put(i++, model.description);
				dataMap.put(i++, Messages.get(model.effectType.name().toLowerCase()));
				dataMap.put(i++, Format.asMoney(model.effect));
				dataMap.put(i++, Messages.get(model.effectDirection.name().toLowerCase()));

				dataList.add(dataMap);
			}
		}

		return new PageExtend<StockPriceUpdate>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME = redirect(
		controllers.stock.routes.PriceUpdates.list()
	);

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<NameOnlySearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(
			list.render(buildPage(filledParamForm.get()), filledParamForm)
		);
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<StockPriceUpdate> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			StockPriceUpdate model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) {
				flash("error", editingConstraintError);
				return badRequest(form.render(dataForm.fill(model)));
			}

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
				return GO_HOME;
		}
	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		return ok(form.render(dataForm.fill(new StockPriceUpdate())));
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
			flash("error", Messages.get("id.is.null"));
		} else {
			StockPriceUpdate model = StockPriceUpdate.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("stock.price_update")));
			} else {
				return ok(form.render(dataForm.fill(model)));
			}
		}
		return GO_HOME;
	}

	public static Result undoLast() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Delete);
		if (hasProblem != null) return hasProblem;

		StockPriceUpdate model = StockPriceUpdate.findLastOne();
		if (model == null) {
			flash("error", Messages.get("not.found", Messages.get("stock.price_update")));
		} else {
			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) {
				flash("error", editingConstraintError);
				return badRequest(form.render(dataForm.fill(model)));
			}
			try {
				model.delete();
				flash("success", Messages.get("rolled_back", Messages.get("stock.price_update")));
			} catch (PersistenceException pe) {
				log.error(pe.getMessage());
				flash("error", Messages.get("delete.violation", model.name));
				return badRequest(form.render(dataForm.fill(model)));
			}
		}

		return GO_HOME;
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<StockPriceUpdate> filledForm) {
		StockPriceUpdate model = filledForm.get();

		if (StockPriceUpdate.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}

		if (model.effectType.equals(EffectType.Percent) && model.effect.doubleValue() > 99.99) {
			filledForm.reject("effect", Messages.get("error.max", 99.99));
		}

		if (model.effect == null || model.effect.doubleValue() <= 0) {
			filledForm.reject("effect", Messages.get("error.min", 1));
		}

		boolean isPriceSelected = false;

		if (! isPriceSelected && (model.buyPrice != null && model.buyPrice.equals(Boolean.TRUE))) isPriceSelected = true;
		if (! isPriceSelected && (model.sellPrice != null && model.sellPrice.equals(Boolean.TRUE))) isPriceSelected = true;

		if (! isPriceSelected) {
			filledForm.reject("prices", "");
		}
	}

}

/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
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
import models.StockPriceList;
import models.search.NameOnlySearchParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.Format;
import views.html.stocks.price_list.form;
import views.html.stocks.price_list.list;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.EffectType;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class PriceLists extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_FIYAT_LISTESI;

	private final static Logger log = LoggerFactory.getLogger(PriceLists.class);
	private final static Form<StockPriceList> dataForm = form(StockPriceList.class);
	private final static Form<NameOnlySearchParam> paramForm = form(NameOnlySearchParam.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), "20%").sortable("name"));
		headerList.add(new GridHeader(Messages.get("description")));
		headerList.add(new GridHeader(Messages.get("effect_type"),  "8%", "center", null));
		headerList.add(new GridHeader(Messages.get("effect"),  "8%", "center", null));
		headerList.add(new GridHeader(Messages.get("effect_dir"),  "10%", "center", null));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<StockPriceList> buildPage(NameOnlySearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<StockPriceList> page = StockPriceList.page(searchParam);
		List<StockPriceList> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (StockPriceList model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);
				dataMap.put(i++, model.description);
				dataMap.put(i++, Messages.get(model.effectType.name().toLowerCase()));
				dataMap.put(i++, Format.asMoney(model.effect));
				dataMap.put(i++, Messages.get(model.effectDirection.name().toLowerCase()));
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<StockPriceList>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME = redirect(
		controllers.stock.routes.PriceLists.list()
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

		Form<StockPriceList> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			StockPriceList model = filledForm.get();

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

		return ok(form.render(dataForm.fill(new StockPriceList())));
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
			StockPriceList model = StockPriceList.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("stock.price_list")));
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
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Delete);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			StockPriceList model = StockPriceList.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("stock.price_list")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) {
					flash("error", editingConstraintError);
					return badRequest(form.render(dataForm.fill(model)));
				}
				try {
					model.delete();
					Ebean.commitTransaction();
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
	private static void checkConstraints(Form<StockPriceList> filledForm) {
		StockPriceList model = filledForm.get();

		if (StockPriceList.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}

		if (model.effectType.equals(EffectType.Percent) && model.effect.doubleValue() > 99.99) {
			filledForm.reject("effect", Messages.get("error.max", 99.99));
		}

		if (model.effect == null || model.effect.doubleValue() <= 0) {
			filledForm.reject("effect", Messages.get("error.min", 1));
		}
	}

}

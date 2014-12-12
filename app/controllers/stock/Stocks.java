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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.Stock;
import models.StockBarcode;
import models.search.StockSearchParam;
import models.temporal.InfoMultiplier;
import models.temporal.Pair;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.CloneUtils;
import utils.Format;
import utils.QueryUtils;
import views.html.stocks.stock.form;
import views.html.stocks.stock.investigation_form;
import views.html.stocks.stock.list;
import views.html.tools.components.info_multiplier;

import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Stocks extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_TANITIMI;

	private final static Logger log = LoggerFactory.getLogger(Stocks.class);
	private final static Form<Stock> dataForm = form(Stock.class);
	private final static Form<StockSearchParam> paramForm = form(StockSearchParam.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("code"), "12%").sortable("code"));
		headerList.add(new GridHeader(Messages.get("name"), true).sortable("name"));
		headerList.add(new GridHeader(Messages.get("buy_price"), "8%", "right", "green"));
		headerList.add(new GridHeader(Messages.get("sell_price"), "8%", "right", "red"));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<Stock> buildPage(StockSearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<Stock> page = Stock.page(searchParam);
		List<Stock> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (Stock model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.code);
				dataMap.put(i++, model.name);
				dataMap.put(i++, Format.asMoney(model.buyPrice));
				dataMap.put(i++, Format.asMoney(model.sellPrice));

				dataList.add(dataMap);
			}
		}

		return new PageExtend<Stock>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME = redirect(
		controllers.stock.routes.Stocks.list()
	);

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<StockSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(
			list.render(
				buildPage(filledParamForm.get()), filledParamForm
			)
		);
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<Stock> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			Stock model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) {
				flash("error", editingConstraintError);
				return badRequest(form.render(dataForm.fill(model)));
			}

			/*
			 * Barkod ayarlari (denetlenecekleri icin bu kisimda setleniyorlar)
			 */
			String primaryBarcode = filledForm.data().get("barcode");

			if (primaryBarcode != null && ! primaryBarcode.trim().isEmpty()) {
				model.barcodes.add(new StockBarcode(primaryBarcode));
			}

			List<StockBarcode> removeList = new ArrayList<StockBarcode>();
			for (StockBarcode other : model.barcodes) {
				if (other.unitNo.intValue() == 2 && model.unit2 == null || model.unit2.trim().isEmpty()) other.unitNo = 1;
				if (other.unitNo.intValue() == 3 && model.unit3 == null || model.unit3.trim().isEmpty()) other.unitNo = 1;
				if (other.barcode == null || other.barcode.trim().isEmpty()) {
					removeList.add(other);
				} else {
					other.workspace = model.workspace;
				}
			}
			model.barcodes.removeAll(removeList);

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

		Stock stock = new Stock();
		return ok(form.render(dataForm.fill(stock)));
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
			Stock model = Stock.findByIdWithBarcodes(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("stock")));
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
			Stock model = Stock.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("stock")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) {
					flash("error", editingConstraintError);
					return badRequest(form.render(dataForm.fill(model)));
				}
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

	public static Result investigation(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Stock stock = Stock.findById(id);

		List<Pair> properties = new ArrayList<Pair>();
		properties.add(new Pair(Messages.get("stock.code"), stock.code));
		properties.add(new Pair(Messages.get("name"), stock.name));
		properties.add(new Pair(Messages.get("provider_code"), stock.providerCode));
		properties.add(new Pair(Messages.get("units"), stock.unit1 + " / " +
														(stock.unit2 != null ? stock.unit2 + "( " + stock.unit2Ratio.toString() + " )" : "") +  " / " +
														(stock.unit3 != null ? stock.unit3 + "( " + stock.unit3Ratio.toString() + " )" : "")));
		properties.add(Pair.EMPTY);
		properties.add(new Pair(Messages.get("buy_price"),  Format.asMoney(stock.buyPrice)));
		properties.add(new Pair(Messages.get("sell_price"), Format.asMoney(stock.sellPrice)));
		properties.add(new Pair(Messages.get("last_buy_price"), Format.asMoney(QueryUtils.findStockLastPrice(id, true))));
		properties.add(new Pair(Messages.get("last_sell_price"), Format.asMoney(QueryUtils.findStockLastPrice(id, false))));
		properties.add(Pair.EMPTY);
		properties.add(new Pair(Messages.get("prim_rate"), (stock.primRate != null ? stock.primRate.toString() : "")));
		properties.add(new Pair(Messages.get("tax_both"), stock.buyTax.toString() + " / " + stock.sellTax.toString()));
		properties.add(new Pair(Messages.get("both.limit"), (stock.minLimit != null ? stock.minLimit.toString() : "") + " / " + (stock.minLimit != null ? stock.minLimit.toString() : "")));

		ObjectNode result = Json.newObject();

		result.put("title", stock.name);
		result.put("body", investigation_form.render(
								QueryUtils.inspectStockTrans(id),
								QueryUtils.inspectStockSummary(id),
								properties
							).body()
					);

		return ok(result);
	}

	/**
	 * Secilen kaydin kopyasini olusturur
	 * 
	 * @param id
	 */
	public static Result createClone(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		Stock source = Stock.findById(id);

		InfoMultiplier im = new InfoMultiplier();
		im.id = id;
		im.code =  source.code;
		im.name =  source.name;

		Form<InfoMultiplier> imDataForm = form(InfoMultiplier.class);

		return ok(
			info_multiplier.render(imDataForm.fill(im), controllers.stock.routes.Stocks.list().url(), Stocks.class.getSimpleName())
		);
	}

	/**
	 * Yeni kopyayi kaydeder
	 */
	public static Result saveClone() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<InfoMultiplier> stmDataForm = form(InfoMultiplier.class);
		Form<InfoMultiplier> filledForm = stmDataForm.bindFromRequest();

		InfoMultiplier im = filledForm.get();

		checkCloneConstraints(filledForm);
		if (filledForm.hasErrors()) {
			return badRequest(info_multiplier.render(filledForm, controllers.stock.routes.Stocks.list().url(), Stocks.class.getSimpleName()));
		}

		Stock clone = CloneUtils.cloneBaseModel(Stock.findById(im.id));
		clone.code = im.code;
		clone.name = im.name;
		clone.barcodes = new ArrayList<StockBarcode>();
		clone.save();

		return ok(Messages.get("saved", clone.name));
	}

	private static void checkCloneConstraints(Form<InfoMultiplier> filledForm) {
		InfoMultiplier model = filledForm.get();

		if (model.id == null) {
			filledForm.reject("code", Messages.get("id.is.null"));
		}

		if (Stock.isUsedForElse("code", model.code, null)) {
			filledForm.reject("code", Messages.get("not.unique", model.code));
		}
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<Stock> filledForm) {
		Stock model = filledForm.get();

		if (Stock.isUsedForElse("code", model.code, model.id)) {
			filledForm.reject("code", Messages.get("not.unique", model.code));
		}
		
		if (Stock.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}

		if (model.unit2 != null && ! model.unit2.isEmpty()) {
			if (model.unit2Ratio == null || model.unit2Ratio.doubleValue() <= 0) {
				filledForm.reject("unit2Ratio", Messages.get("is.not.null", Messages.get("stock.unit_ratio", 1)));
			}
		}

		if (model.unit3 != null && ! model.unit3.isEmpty()) {
			if (model.unit3Ratio == null || model.unit3Ratio.doubleValue() <= 0) {
				filledForm.reject("unit3Ratio", Messages.get("is.not.null", Messages.get("stock.unit_ratio", 2)));
			}
		}

		if (model.barcodes != null || model.barcodes.size() > 0) {
			List<ValidationError> veList = new ArrayList<ValidationError>();

			Set<String> barcodeSet = new HashSet<String>();

			for (int i = 1; i < model.barcodes.size() + 1; i++) {
				StockBarcode sb = model.barcodes.get(i-1);

				if (sb.barcode == null || sb.barcode.trim().isEmpty()) {
					veList.add(new ValidationError("barcodes", Messages.get("is.not.null.for.table", i, Messages.get("barcode"))));
				} else {
					if (sb.barcode.length() > 50) {
						veList.add(new ValidationError("barcodes", Messages.get("too.long.for.table", i, Messages.get("barcode"), 50)));
					}
				}
				if (sb.prefix != null && sb.prefix.length() > 30) {
					veList.add(new ValidationError("barcodes", Messages.get("too.long.for.table", i, Messages.get("prefix"), 30)));
				}
				if (sb.suffix != null && sb.suffix.length() > 30) {
					veList.add(new ValidationError("barcodes", Messages.get("too.long.for.table", i, Messages.get("suffix"), 30)));
				}

				barcodeSet.add(sb.barcode);

			}

			if (barcodeSet.size() != model.barcodes.size()) {
				veList.add(new ValidationError("barcodes", Messages.get("must.be.unique", Messages.get("barcode"))));
			}

			if (StockBarcode.areUsedForElse(barcodeSet, model.id)) {
				filledForm.reject("barcodes", Messages.get("not.unique", Messages.get("barcode")));
			}

			if (veList.size() > 0) {
				filledForm.errors().put("barcodes", veList);
			}

		}

	}

}

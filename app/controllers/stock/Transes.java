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
import html.trans_form_rows.StockTransRows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import meta.RightBind;
import meta.RowCombining;
import models.SaleSeller;
import models.Stock;
import models.StockTrans;
import models.StockTransCurrency;
import models.StockTransDetail;
import models.StockTransFactor;
import models.StockTransTax;
import models.search.StockTransSearchParam;
import models.temporal.Pair;
import models.temporal.TransMultiplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.CloneUtils;
import utils.DateUtils;
import utils.DocNoUtils;
import utils.Format;
import utils.NumericUtils;
import utils.QueryUtils;
import utils.RefModuleUtil;
import views.html.stocks.transaction.form;
import views.html.stocks.transaction.list;
import views.html.tools.components.trans_multiplier;

import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.DocNoIncType;
import enums.Module;
import enums.Right;
import enums.RightLevel;
import enums.TransType;

/**
 * @author mdpinar
*/
public class Transes extends Controller {

	private final static Right[] ACCEPTABLE_RIGHTS = {
		Right.STOK_ACILIS_ISLEMI,
		Right.STOK_GIRIS_FISI,
		Right.STOK_CIKIS_FISI,
		Right.STOK_GIRIS_IADE_FISI,
		Right.STOK_CIKIS_IADE_FISI,
		Right.STOK_TRANSFER_FISI
	};

	private final static Logger log = LoggerFactory.getLogger(Transes.class);
	private final static Form<StockTrans> dataForm = form(StockTrans.class);
	private final static Form<StockTransSearchParam> paramForm = form(StockTransSearchParam.class);

	private static List<GridHeader> getHeaderList(Right right) {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("receipt_no"), "6%", "right", null).sortable("receiptNo"));
		headerList.add(new GridHeader(Messages.get("date"), "8%", "center", null).sortable("transDate"));
		if (right.equals(Right.STOK_TRANSFER_FISI)) {
			headerList.add(new GridHeader(Messages.get("depot"), "12%", "center", null));
			headerList.add(new GridHeader(Messages.get("ref.depot"), "12%", "center", null));
		} else if (! right.equals(Right.STOK_ACILIS_ISLEMI)) {
			headerList.add(new GridHeader(Messages.get("contact"), "25%", false, true).sortable("contact.name"));
		}
		headerList.add(new GridHeader(Messages.get("amount"), "9%", "right", "red"));
		if (Profiles.chosen().gnel_hasExchangeSupport) {
			headerList.add(new GridHeader(Messages.get("currency"), "4%", "center", null));
		}
		headerList.add(new GridHeader(Messages.get("description")));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<StockTrans> buildPage(StockTransSearchParam searchParam, Right right) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<StockTrans> page = StockTrans.page(searchParam, right);
		List<StockTrans> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (StockTrans model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.receiptNo.toString());
				dataMap.put(i++, DateUtils.formatDateStandart(model.transDate));
				if (right.equals(Right.STOK_TRANSFER_FISI)) {
					dataMap.put(i++, (model.depot != null ? model.depot.toString() : ""));
					dataMap.put(i++, (model.refDepot != null ? model.refDepot.toString() : ""));
				} else if (! right.equals(Right.STOK_ACILIS_ISLEMI)) {
					dataMap.put(i++, (model.contact != null ? model.contact.name : ""));
				}
				dataMap.put(i++, Format.asMoney(model.netTotal));
				if (Profiles.chosen().gnel_hasExchangeSupport) {
					dataMap.put(i++, model.excCode);
				}
				dataMap.put(i++, model.description);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<StockTrans>(getHeaderList(right), dataList, page);
	}

	public static Result GO_HOME(RightBind rightBind) {
		return redirect(
			controllers.stock.routes.Transes.list(rightBind)
		);
	}

	public static Result list(RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		Form<StockTransSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(
			list.render(
				buildPage(filledParamForm.get(), rightBind.value), rightBind, filledParamForm
			)
		);
	}

	public static Result save(RightBind rightBind) {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<StockTrans> filledForm = dataForm.bindFromRequest();
		StockTrans model = filledForm.get();

		Result hasProblem = AuthManager.hasProblem(rightBind.value, (model.id == null ? RightLevel.Insert : RightLevel.Update), ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		checkFirstConstraints(filledForm);
		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, rightBind, StockTransRows.build(model)));
		}

		String editingConstraintError = model.checkEditingConstraints();
		if (editingConstraintError != null) {
			flash("error", editingConstraintError);
			return badRequest(form.render(filledForm, rightBind, StockTransRows.build(model)));
		}

		model.workspace = CacheUtils.getWorkspaceId();
		model.right = rightBind.value;
		model.transType = rightBind.value.transType;
		model.transYear = DateUtils.getYear(model.transDate);
		model.transMonth = DateUtils.getYearMonth(model.transDate);
		if (model.right.equals(Right.STOK_ACILIS_ISLEMI)) model.netTotal = model.total;
		model.excEquivalent = model.netTotal;

		/*
		 * Stok ayarlari
		 */
		Map<Double, StockTransTax> taxMap = new TreeMap<Double, StockTransTax>();
		Map<String, StockTransCurrency> currencyMap = new TreeMap<String, StockTransCurrency>();

		int rowNo = 0;

		List<StockTransDetail> removeStockList = new ArrayList<StockTransDetail>();
		Map<Integer, SaleSeller> sellerMap = SaleSeller.getModelMap();
		try {
			for (StockTransDetail detail: model.details) {
				if (detail.stock.id == null) {
					removeStockList.add(detail);
					continue;
				}
				detail.trans = model;
				detail.workspace = model.workspace;
				detail.receiptNo = model.receiptNo;
				detail.contact = model.contact;
				detail.transPoint = model.transPoint;
				detail.privateCode = model.privateCode;
				detail.transSource = model.transSource;
				detail.right = model.right;
				detail.transDate = model.transDate;
				detail.deliveryDate = model.deliveryDate;
				detail.transType = model.transType;
				detail.rowNo = ++rowNo;

				if (detail.depot == null || detail.depot.id == null) {
					detail.depot = model.depot;
				}

				detail.isReturn = rightBind.value.isReturn;
				detail.hasCostEffect = Boolean.TRUE;
				if (! detail.isReturn && model.transSource != null) {
					detail.hasCostEffect = model.transSource.hasCostEffect;
				}
				if (model.right.equals(Right.STOK_TRANSFER_FISI)) {
					detail.hasCostEffect = false;
				} else if (model.right.equals(Right.STOK_ACILIS_ISLEMI)) {
					detail.total = detail.amount;
				}

				if (detail.seller != null && detail.seller.id != null) {
					SaleSeller seller = sellerMap.get(detail.seller.id);
					if (seller != null) detail.seller = seller;
				} else if (model.seller != null && model.seller.id != null) {
					detail.seller = model.seller;
				}

				detail.input = 0d;
				detail.inTotal = 0d;
				detail.output = 0d;
				detail.outTotal = 0d;
				detail.retInput = 0d;
				detail.retInTotal = 0d;
				detail.retOutput = 0d;
				detail.retOutTotal = 0d;

				if (model.transType.equals(TransType.Input)) {
					detail.input = detail.quantity.doubleValue() * detail.unitRatio.doubleValue();
					detail.inTotal = detail.total;
				} else {
					detail.output = detail.quantity.doubleValue() * detail.unitRatio.doubleValue();
					detail.outTotal = detail.total;
				}

				if (detail.isReturn) {
					detail.retInput = detail.output;
					detail.retInTotal = detail.outTotal;
					detail.retOutput = detail.input;
					detail.retOutTotal = detail.inTotal;
				} else {
					detail.netInput = detail.input;
					detail.netInTotal = detail.inTotal;
					detail.netOutput = detail.output;
					detail.netOutTotal = detail.outTotal;
				}

				if (detail.amount.doubleValue() * model.plusFactorTotal.doubleValue() > 0) {
					detail.plusFactorAmount = 
						NumericUtils.round((detail.amount.doubleValue() / model.subtotal) * model.plusFactorTotal.doubleValue());
				}
				if (detail.amount.doubleValue() * model.minusFactorTotal.doubleValue() > 0) {
					detail.minusFactorAmount = 
						NumericUtils.round((detail.amount.doubleValue() / model.subtotal) * model.minusFactorTotal.doubleValue(), Profiles.chosen().gnel_pennyDigitNumber);
				}

				detail.transYear = model.transYear;
				detail.transMonth = model.transMonth;

				/*
				 * Fise bagli ParaBirimi map inin olusturulmasi
				 */
				if (detail.excCode != null) {
					StockTransCurrency stockCurrency = currencyMap.get(detail.excCode);
					if (stockCurrency == null) stockCurrency = new StockTransCurrency();
					stockCurrency.currency = detail.excCode;
					stockCurrency.amount = NumericUtils.round(stockCurrency.amount + (detail.total == null ? detail.amount : detail.total));
					currencyMap.put(detail.excCode, stockCurrency);
				}

				/*
				 * Fise bagli KDV map inin olusturulmasi
				 */
				if (detail.taxRate != null) {
					StockTransTax stockTax = taxMap.get(detail.taxRate);
					if (stockTax == null) stockTax = new StockTransTax();
					stockTax.taxRate = detail.taxRate;
					stockTax.basis  = NumericUtils.round(stockTax.basis + (detail.total == null ? detail.amount : detail.total));
					stockTax.amount = NumericUtils.round(stockTax.amount + detail.taxAmount);
					taxMap.put(detail.taxRate, stockTax);
				}
			}
		} catch (Exception e) {
			log.error("ERROR", e);
		}
		model.details.removeAll(removeStockList);

		List<StockTransFactor> removeList = new ArrayList<StockTransFactor>();
		for (StockTransFactor other : model.factors) {
			if (other.factor.id == null) {
				removeList.add(other);
				continue;
			}
			other.trans = model;
		}
		model.factors.removeAll(removeList);
		model.taxes =  new ArrayList<StockTransTax>(taxMap.values());
		model.currencies =  new ArrayList<StockTransCurrency>(currencyMap.values());

		checkSecondConstraints(filledForm);
		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, rightBind, StockTransRows.build(model)));
		}

		if (Profiles.chosen().stok_isRowCombining) doRowCombining(model);

		String res = RefModuleUtil.save(model, Module.stock, model.contact);
		if (res != null) {
			flash("error", Messages.get(res));
			return badRequest(form.render(filledForm, rightBind, StockTransRows.build(model)));
		}

		flash("success", Messages.get("saved", Messages.get(rightBind.value.key)));
		if (Profiles.chosen().gnel_continuouslyRecording)
			return create(rightBind);
		else
			return GO_HOME(rightBind);

	}

	private static void doRowCombining(StockTrans model) {
		Map<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
		Map<String, RowCombining> combineMap = new HashMap<String, RowCombining>();
		List<StockTransDetail> removeStockList = new ArrayList<StockTransDetail>();

		List<StockTransDetail> details = model.details;
		for (int i = 0; i < details.size(); i++) {
			StockTransDetail detail = details.get(i);
			String key = detail.stock.id+"-"+detail.price;
			RowCombining combining = combineMap.get(key);
			if (combining == null) {
				combining = new RowCombining(i, detail.stock.id, detail.price);
				combineMap.put(key, combining);
			} else {
				removeStockList.add(detail);
				rowMap.put(i, combining.row);
			}
		}

		for (Map.Entry<Integer, Integer> entry : rowMap.entrySet()) {
			StockTransDetail sourceDetail = details.get(entry.getKey());
			StockTransDetail targetDetail = details.get(entry.getValue());

			targetDetail.quantity += sourceDetail.quantity;
			targetDetail.amount += sourceDetail.amount;
			targetDetail.discountAmount += sourceDetail.discountAmount;
			targetDetail.taxAmount += sourceDetail.taxAmount;
			targetDetail.total += sourceDetail.total;
			targetDetail.input += sourceDetail.input;
			targetDetail.inTotal += sourceDetail.inTotal;
			targetDetail.output += sourceDetail.output;
			targetDetail.outTotal += sourceDetail.outTotal;
			targetDetail.retInput += sourceDetail.retInput;
			targetDetail.retInTotal += sourceDetail.retInTotal;
			targetDetail.retOutput += sourceDetail.retOutput;
			targetDetail.retOutTotal += sourceDetail.retOutTotal;

			model.details.set(entry.getValue(), targetDetail);
		}

		model.details.removeAll(removeStockList);
	}

	public static Result create(RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Insert, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		StockTrans neu = new StockTrans();
		neu.right = rightBind.value;
		neu.transType = rightBind.value.transType;
		if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) neu.transNo = DocNoUtils.findLastTransNo(rightBind.value);
		neu.receiptNo = DocNoUtils.findLastReceiptNo(rightBind.value);

		return ok(form.render(dataForm.fill(neu), rightBind, StockTransRows.build(neu)));
	}

	public static Result edit(Integer id, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			StockTrans model = StockTrans.findById(id);

			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				RefModuleUtil.setTransientFields(model);
				return ok(form.render(dataForm.fill(model), rightBind, StockTransRows.build(model)));
			}
		}
		return GO_HOME(rightBind);
	}

	public static Result remove(Integer id, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Delete, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			StockTrans model = StockTrans.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) {
					flash("error", editingConstraintError);
					return badRequest(form.render(dataForm.fill(model), rightBind, StockTransRows.build(model)));
				}
				try {
					RefModuleUtil.remove(model);
					flash("success", Messages.get("deleted", Messages.get(rightBind.value.key)));
				} catch (PersistenceException pe) {
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", Messages.get(rightBind.value.key)));
					return badRequest(form.render(dataForm.fill(model), rightBind, StockTransRows.build(model)));
				}
			}
		}
		return GO_HOME(rightBind);
	}

	/**
	 * Secilen kaydin kopyasini olusturur
	 * 
	 * @param id
	 */
	public static Result createClone(Integer id) {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		StockTrans source = StockTrans.findById(id);

		Result hasProblem = AuthManager.hasProblem(source.right, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		TransMultiplier stm = new TransMultiplier();
		stm.id = id;
		stm.contact =  source.contact;
		stm.transNo = source.transNo;
		stm.depot = source.depot;
		stm.refDepot = source.refDepot;
		stm.seller = source.seller;
		stm.description = source.description;

		Form<TransMultiplier> stmDataForm = form(TransMultiplier.class);

		return ok(
			trans_multiplier.render(stmDataForm.fill(stm), source.right)
		);
	}

	/**
	 * Yeni kopyayi kaydeder
	 */
	public static Result saveClone() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<TransMultiplier> stmDataForm = form(TransMultiplier.class);
		Form<TransMultiplier> filledForm = stmDataForm.bindFromRequest();

		Right right = Right.valueOf(filledForm.data().get("right"));
		if(filledForm.hasErrors()) {
			return badRequest(trans_multiplier.render(filledForm, right));
		} else {
			TransMultiplier stm = filledForm.get();

			checkCloneConstraints(filledForm);
			if (filledForm.hasErrors()) {
				return badRequest(trans_multiplier.render(filledForm, right));
			}

			StockTrans stockTrans = StockTrans.findById(stm.id);

			StockTrans clone = CloneUtils.cloneTransaction(stockTrans);
			clone.isCompleted = Boolean.FALSE;
			clone.transDate = stm.transDate;
			clone.transMonth = DateUtils.getYearMonth(stm.transDate);
			clone.transYear = DateUtils.getYear(stm.transDate);
			clone.deliveryDate = stockTrans.deliveryDate;
			clone.transNo = stm.transNo;
			clone.depot = stm.depot;
			clone.refDepot = stm.refDepot;
			clone.seller = stm.seller;
			clone.description = stm.description;
			clone.contact = stm.contact;
			if (clone.contact != null && clone.contact.id != null) {
				clone.contactName = clone.contact.name;
				clone.contactTaxNumber = clone.contact.taxNumber;
				clone.contactTaxOffice = clone.contact.taxOffice;
				clone.contactAddress1 = clone.contact.address1;
				clone.contactAddress2 = clone.contact.address2;
			}

			for (StockTransDetail std : clone.details) {
				std.id = null;
				std.trans = clone;
				std.receiptNo = clone.receiptNo;
				std.contact = clone.contact;
				std.seller = clone.seller;

				CloneUtils.resetModel(std);
			}
			for (StockTransFactor stf : clone.factors) {
				stf.id = null;
				stf.trans = clone;

				CloneUtils.resetModel(stf);
			}

			if (right.equals(Right.STOK_TRANSFER_FISI)) {
				clone.refId = null;
				clone.refModule = null;
			}

			String res = RefModuleUtil.save(clone, Module.stock, clone.contact);
			if (res != null) {
				flash("error", Messages.get(res));
				return badRequest(trans_multiplier.render(filledForm, right));
			}

			return ok(Messages.get("saved", Messages.get(clone.right.key)));
		}
	}

	private static void checkCloneConstraints(Form<TransMultiplier> filledForm) {
		TransMultiplier model = filledForm.get();

		if (model.depot == null || model.depot.id == null) {
			filledForm.reject("depot.id", Messages.get("is.not.null", Messages.get("depot")));
		} else if (model.right.equals(Right.STOK_TRANSFER_FISI)) {
			if (model.refDepot == null || model.refDepot.id == null) {
				filledForm.reject("refDepot.id", Messages.get("is.not.null", Messages.get("ref.depot")));
			} else if (model.depot.equals(model.refDepot)) {
				filledForm.reject("refDepot.id", Messages.get("depots.same"));
			}
		}
	}

	private static void checkFirstConstraints(Form<StockTrans> filledForm) {
		StockTrans model = filledForm.get();

		if (model.transDate == null) {
			filledForm.reject("transDate", Messages.get("error.required"));
		}

		if (model.depot == null || model.depot.id == null) {
			filledForm.reject("depot.id", Messages.get("is.not.null", Messages.get("depot")));
		} else if (model.right.equals(Right.STOK_TRANSFER_FISI)) {
			if (model.refDepot == null || model.refDepot.id == null) {
				filledForm.reject("refDepot.id", Messages.get("is.not.null", Messages.get("ref.depot")));
			} else if (model.depot.equals(model.refDepot)) {
				filledForm.reject("refDepot.id", Messages.get("depots.same"));
			}
		}

		if (model.deliveryDate != null && model.deliveryDate.before(model.transDate)) {
			filledForm.reject("deliveryDate", Messages.get("error.dateBefore", Messages.get("date.delivery")));
		}
	}

	private static void checkSecondConstraints(Form<StockTrans> filledForm) {
		StockTrans model = filledForm.get();
		List<ValidationError> veList = new ArrayList<ValidationError>();

		if (model.details != null && model.details.size() > 0) {

			for (int i = 1; i < model.details.size() + 1; i++) {
				StockTransDetail std = model.details.get(i-1);

				if (std.taxRate == null) std.taxRate = 0d;
				if (std.discountRate1 == null) std.discountRate1 = 0d;
				if (std.discountRate2 == null) std.discountRate2 = 0d;
				if (std.discountRate3 == null) std.discountRate3 = 0d;

				if (std.quantity == null || std.quantity <= 0) {
					veList.add(new ValidationError("stocks", Messages.get("cannot.be.zero.table", i)));
				}
				if (std.name != null && std.name.length() > 100) {
					veList.add(new ValidationError("stocks", Messages.get("too.long.for.table", i, Messages.get("name"), 100)));
				}
				if (std.unit == null) {
					veList.add(new ValidationError("stocks", Messages.get("is.not.null.for.table", i, Messages.get("unit"))));
				}
				if (std.taxRate.doubleValue() > 100) {
					veList.add(new ValidationError("stocks", Messages.get("too.high.for.table", i, Messages.get("tax_rate"), 100)));
				}
				if (std.discountRate1.doubleValue() > 100) {
					veList.add(new ValidationError("stocks", Messages.get("too.high.for.table", i, Messages.get("stock.discount", 1), 100)));
				}
				if (std.discountRate2.doubleValue() > 100) {
					veList.add(new ValidationError("stocks", Messages.get("too.high.for.table", i, Messages.get("stock.discount", 2), 100)));
				}
				if (std.discountRate3.doubleValue() > 100) {
					veList.add(new ValidationError("stocks", Messages.get("too.high.for.table", i, Messages.get("stock.discount", 3), 100)));
				}

				if (std.discountRate1 != null && std.discountRate2 != null && std.discountRate3 != null) {
					if (std.discountRate1.doubleValue() + std.discountRate2.doubleValue() + std.discountRate3.doubleValue() > 100) {
						veList.add(new ValidationError("stocks", Messages.get("too.high.for.table", i, Messages.get("stock.discount_rate_total"), 100)));
					}
				}

				if (std.description != null && std.description.length() > 100) {
					veList.add(new ValidationError("stocks", Messages.get("too.long.for.table", i, Messages.get("description"), 100)));
				}

				if (std.stock.id != null && Profiles.chosen().stok_hasLimitControls) {
					Stock stock = Stock.findById(std.stock.id);
					if (stock.maxLimit != null && stock.minLimit != null && (stock.maxLimit.doubleValue() > 0 || stock.minLimit.doubleValue() > 0)) {
						double balance = Math.abs(QueryUtils.findStockBalance(stock.id, model.depot.id, std.id));
						boolean isInput = model.transType.equals(TransType.Input);
	
						if (isInput && balance + std.netInput.doubleValue() > stock.maxLimit.doubleValue()) {
							veList.add(new ValidationError("stocks", Messages.get("greater.than.maximum.table", i, stock.maxLimit, (stock.maxLimit.doubleValue() - balance))));
						} else if ((balance - std.netOutput.doubleValue() < stock.minLimit.doubleValue())) {
							veList.add(new ValidationError("stocks", Messages.get("less.than.minimum.table", i, (stock.minLimit != null ? stock.minLimit : 0), balance)));
						}
					}
				}
			}

		} else {
			veList.add(new ValidationError("stocks", Messages.get("table.min.row.alert")));
		}

		if (veList.size() > 0) {
			filledForm.errors().put("stocks", veList);
		}

		Pair pair = RefModuleUtil.checkForRefAccounts(model);
		if (pair.key != null) {
			filledForm.reject(pair.key, pair.value);
		}

	}

}

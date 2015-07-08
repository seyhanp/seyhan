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
package controllers.invoice;

import static play.data.Form.form;
import html.trans_form_rows.InvoiceTransRows;

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
import models.InvoiceTrans;
import models.InvoiceTransCurrency;
import models.InvoiceTransDetail;
import models.InvoiceTransFactor;
import models.InvoiceTransTax;
import models.Safe;
import models.SaleSeller;
import models.Stock;
import models.search.TransSearchParam;
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
import utils.TransStatusHistoryUtils;
import views.html.invoices.transaction.form;
import views.html.invoices.transaction.list;
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
		Right.FATR_ALIS_FATURASI,
		Right.FATR_SATIS_FATURASI,
		Right.FATR_ALIS_IADE_FATURASI,
		Right.FATR_SATIS_IADE_FATURASI
	};

	private final static Logger log = LoggerFactory.getLogger(Transes.class);
	private final static Form<InvoiceTrans> dataForm = form(InvoiceTrans.class);
	private final static Form<TransSearchParam> paramForm = form(TransSearchParam.class);

	private static List<GridHeader> getHeaderList(Right right) {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("trans.no"), "8%").sortable("transNo"));
		if (! right.isReturn) {
			headerList.add(new GridHeader(Messages.get("status"), "8%", "center", "green").sortable("status"));
		}
		headerList.add(new GridHeader(Messages.get("date"), "8%", "center", null).sortable("transDate"));
		headerList.add(new GridHeader(Messages.get("contact"), "25%", false, true).sortable("contact.name"));
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
	private static PageExtend<InvoiceTrans> buildPage(TransSearchParam searchParam, Right right) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<InvoiceTrans> page = InvoiceTrans.page(searchParam, right);
		List<InvoiceTrans> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (InvoiceTrans model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.transNo);
				if (! right.isReturn) {
					dataMap.put(i++, (model.status != null ? model.status.name : ""));
				}
				dataMap.put(i++, DateUtils.formatDateStandart(model.transDate));
				dataMap.put(i++, (model.contact != null ? model.contact.name : ""));
				dataMap.put(i++, Format.asMoney(model.netTotal));
				if (Profiles.chosen().gnel_hasExchangeSupport) {
					dataMap.put(i++, model.excCode);
				}
				dataMap.put(i++, model.description);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<InvoiceTrans>(getHeaderList(right), dataList, page);
	}

	public static Result GO_HOME(RightBind rightBind) {
		return redirect(
			controllers.invoice.routes.Transes.list(rightBind)
		);
	}

	public static Result list(RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		Form<TransSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(
			list.render(
				buildPage(filledParamForm.get(), rightBind.value), rightBind, filledParamForm
			)
		);
	}

	public static Result save(RightBind rightBind) {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<InvoiceTrans> filledForm = dataForm.bindFromRequest();
		InvoiceTrans model = filledForm.get();

		Result hasProblem = AuthManager.hasProblem(rightBind.value, (model.id == null ? RightLevel.Insert : RightLevel.Update), ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		checkFirstConstraints(filledForm);
		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, rightBind, InvoiceTransRows.build(model)));
		}

		String editingConstraintError = model.checkEditingConstraints();
		if (editingConstraintError != null) {
			flash("error", editingConstraintError);
			return badRequest(form.render(filledForm, rightBind, InvoiceTransRows.build(model)));
		}
		
		if (model.isCompleted != null && model.isCompleted ) {
			flash("error", Messages.get("edit.striction.for_controller"));
			return badRequest(form.render(filledForm, rightBind, InvoiceTransRows.build(model)));
		}

		model.workspace = CacheUtils.getWorkspaceId();
		model.right = rightBind.value;
		model.transType = rightBind.value.transType;
		model.transYear = DateUtils.getYear(model.transDate);
		model.transMonth = DateUtils.getYearMonth(model.transDate);
		model.excEquivalent = model.netTotal;

		if (model.isCash) {
			model.refModule = Module.safe;
			if (model.refSafe == null || model.refSafe.id == null) model.refSafe = Safe.findById(1);
			model.refExcCode = model.excCode;
			model.refExcRate = model.excRate;
			model.refExcEquivalent = model.excEquivalent;
		}

		/*
		 * Stok ayarlari
		 */
		Map<Double, InvoiceTransTax> taxMap = new TreeMap<Double, InvoiceTransTax>();
		Map<String, InvoiceTransCurrency> currencyMap = new TreeMap<String, InvoiceTransCurrency>();

		int rowNo = 0;

		List<InvoiceTransDetail> removeInvoiceList = new ArrayList<InvoiceTransDetail>();
		Map<Integer, SaleSeller> sellerMap = SaleSeller.getModelMap();
		for (InvoiceTransDetail detail: model.details) {
			if (detail.stock.id == null) {
				removeInvoiceList.add(detail);
				continue;
			}

			detail.trans = model;
			detail.workspace = model.workspace;
			detail.receiptNo = model.receiptNo;
			detail.contact = model.contact;
			detail.transPoint = model.transPoint;
			detail.privateCode = model.privateCode;
			detail.transSource = model.transSource;
			detail.status = model.status;
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
			 * Faturaya bagli ParaBirimi map inin olusturulmasi
			 */
			if (detail.excCode != null) {
				InvoiceTransCurrency invoiceCurrency = currencyMap.get(detail.excCode);
				if (invoiceCurrency == null) invoiceCurrency = new InvoiceTransCurrency();
				invoiceCurrency.currency = detail.excCode;
				invoiceCurrency.amount = NumericUtils.round(invoiceCurrency.amount + detail.total);
				currencyMap.put(detail.excCode, invoiceCurrency);
			}

			/*
			 * Faturaya bagli KDV map inin olusturulmasi
			 */
			if (detail.taxRate != null) {
				InvoiceTransTax invoiceTax = taxMap.get(detail.taxRate);
				if (invoiceTax == null) invoiceTax = new InvoiceTransTax();
				invoiceTax.taxRate = detail.taxRate;
				invoiceTax.basis  = NumericUtils.round(invoiceTax.basis + detail.total);
				invoiceTax.amount = NumericUtils.round(invoiceTax.amount + detail.taxAmount);
				taxMap.put(detail.taxRate, invoiceTax);
			}
		}
		model.details.removeAll(removeInvoiceList);

		List<InvoiceTransFactor> removeList = new ArrayList<InvoiceTransFactor>();
		for (InvoiceTransFactor other : model.factors) {
			if (other.factor.id == null) {
				removeList.add(other);
				continue;
			}
			other.trans = model;
		}
		model.factors.removeAll(removeList);
		model.taxes =  new ArrayList<InvoiceTransTax>(taxMap.values());
		model.currencies =  new ArrayList<InvoiceTransCurrency>(currencyMap.values());

		checkSecondConstraints(filledForm);
		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, rightBind, InvoiceTransRows.build(model)));
		}

		if (Profiles.chosen().stok_isRowCombining) {
			doRowCombining(model);
			for (int i = 0; i < model.details.size(); i++) {
				model.details.get(i).rowNo = i + 1;
			}
		}
		
		boolean isNew = (model.id == null);

		String res = RefModuleUtil.save(model, Module.invoice, model.contact);
		if (res != null) {
			flash("error", Messages.get(res));
			return badRequest(form.render(filledForm, rightBind, InvoiceTransRows.build(model)));
		}

		if (isNew && model.status != null && model.status.id != null) {
			TransStatusHistoryUtils.goForward(Module.invoice, model.id, model.status.id, Messages.get("first.init"));
		}

		flash("success", Messages.get("saved", Messages.get(rightBind.value.key)));
		if (Profiles.chosen().gnel_continuouslyRecording)
			return create(rightBind);
		else
			return GO_HOME(rightBind);

	}

	private static void doRowCombining(InvoiceTrans model) {
		Map<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
		Map<String, RowCombining> combineMap = new HashMap<String, RowCombining>();
		List<InvoiceTransDetail> removeStockList = new ArrayList<InvoiceTransDetail>();

		List<InvoiceTransDetail> details = model.details;
		for (int i = 0; i < details.size(); i++) {
			InvoiceTransDetail detail = details.get(i);
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
			InvoiceTransDetail sourceDetail = details.get(entry.getKey());
			InvoiceTransDetail targetDetail = details.get(entry.getValue());

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

		InvoiceTrans neu = new InvoiceTrans();
		neu.right = rightBind.value;
		neu.transType = rightBind.value.transType;
		if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) neu.transNo = DocNoUtils.findLastTransNo(rightBind.value);
		neu.receiptNo = DocNoUtils.findLastReceiptNo(rightBind.value);

		return ok(form.render(dataForm.fill(neu), rightBind, InvoiceTransRows.build(neu)));
	}

	public static Result edit(Integer id, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			InvoiceTrans model = InvoiceTrans.findById(id);

			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				RefModuleUtil.setTransientFields(model);
				return ok(form.render(dataForm.fill(model), rightBind, InvoiceTransRows.build(model)));
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
			InvoiceTrans model = InvoiceTrans.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				if (model.isCompleted != null && model.isCompleted) {
					flash("error", Messages.get("edit.striction.for_controller"));
					return badRequest(form.render(dataForm.fill(model), rightBind, InvoiceTransRows.build(model)));
				} else {
					String editingConstraintError = model.checkEditingConstraints();
					if (editingConstraintError != null) {
						flash("error", editingConstraintError);
						return badRequest(form.render(dataForm.fill(model), rightBind, InvoiceTransRows.build(model)));
					}
					try {
						RefModuleUtil.remove(model);
						TransStatusHistoryUtils.deleteAllHistory(Module.invoice, model.id);
						flash("success", Messages.get("deleted", Messages.get(rightBind.value.key)));
					} catch (PersistenceException pe) {
						log.error(pe.getMessage());
						flash("error", Messages.get("delete.violation", Messages.get(rightBind.value.key)));
						return badRequest(form.render(dataForm.fill(model), rightBind, InvoiceTransRows.build(model)));
					}
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

		InvoiceTrans source = InvoiceTrans.findById(id);

		Result hasProblem = AuthManager.hasProblem(source.right, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		TransMultiplier stm = new TransMultiplier();
		stm.id = id;
		stm.contact =  source.contact;
		stm.transNo = source.transNo;
		stm.depot = source.depot;
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

			InvoiceTrans invoiceTrans = InvoiceTrans.findById(stm.id);

			InvoiceTrans clone = CloneUtils.cloneTransaction(invoiceTrans);
			clone.transDate = stm.transDate;
			clone.transMonth = DateUtils.getYearMonth(stm.transDate);
			clone.transYear = DateUtils.getYear(stm.transDate);
			clone.deliveryDate = invoiceTrans.deliveryDate;
			clone.transNo = stm.transNo;
			clone.depot = stm.depot;
			clone.seller = stm.seller;
			clone.description = stm.description;
			clone.contact = stm.contact;
			clone.contactName = clone.contact.name;
			clone.contactTaxNumber = clone.contact.taxNumber;
			clone.contactTaxOffice = clone.contact.taxOffice;
			clone.contactAddress1 = clone.contact.address1;
			clone.contactAddress2 = clone.contact.address2;

			for (InvoiceTransDetail std : clone.details) {
				std.id = null;
				std.trans = clone;
				std.receiptNo = clone.receiptNo;
				std.contact = clone.contact;
				std.seller = clone.seller;

				CloneUtils.resetModel(std);
			}
			for (InvoiceTransFactor stf : clone.factors) {
				stf.id = null;
				stf.trans = clone;

				CloneUtils.resetModel(stf);
			}

			if (right.equals(Right.STOK_TRANSFER_FISI)) {
				clone.refId = null;
				clone.refModule = null;
			}

			String res = RefModuleUtil.save(clone, Module.invoice, clone.contact);
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
		} else {
			if (Right.STOK_TRANSFER_FISI.equals(model.right) && model.depot.equals(model.refDepot)) {
				filledForm.reject("refDepot.id", Messages.get("depots.same"));
			}
		}
	}

	private static void checkFirstConstraints(Form<InvoiceTrans> filledForm) {
		InvoiceTrans model = filledForm.get();

		if (model.transDate == null) {
			filledForm.reject("transDate", Messages.get("error.required"));
		}

		if (model.depot == null || model.depot.id == null) {
			filledForm.reject("depot.id", Messages.get("is.not.null", Messages.get("depot")));
		}

		if (model.deliveryDate != null && model.deliveryDate.before(model.transDate)) {
			filledForm.reject("deliveryDate", Messages.get("error.dateBefore", Messages.get("date.delivery")));
		}

		if (model.contact.id == null) {
			filledForm.reject("contact.name", Messages.get("is.not.null", Messages.get("contact")));
		}
	}

	private static void checkSecondConstraints(Form<InvoiceTrans> filledForm) {
		InvoiceTrans model = filledForm.get();
		List<ValidationError> veList = new ArrayList<ValidationError>();

		if (model.details != null && model.details.size() > 0) {

			for (int i = 1; i < model.details.size() + 1; i++) {
				InvoiceTransDetail std = model.details.get(i-1);

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
					veList.add(new ValidationError("stocks", Messages.get("too.high.for.table", i, Messages.get("invoice.discount", 1), 100)));
				}
				if (std.discountRate2.doubleValue() > 100) {
					veList.add(new ValidationError("stocks", Messages.get("too.high.for.table", i, Messages.get("invoice.discount", 2), 100)));
				}
				if (std.discountRate3.doubleValue() > 100) {
					veList.add(new ValidationError("stocks", Messages.get("too.high.for.table", i, Messages.get("invoice.discount", 3), 100)));
				}

				if (std.discountRate1.doubleValue() + std.discountRate2.doubleValue() + std.discountRate3.doubleValue() > 100) {
					veList.add(new ValidationError("stocks", Messages.get("too.high.for.table", i, Messages.get("invoice.discount_rate_total"), 100)));
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

		switch (model.refModule) {
			case safe: {
				if (model.refSafe.id == null) {
					filledForm.reject("refSafe.id", Messages.get("is.not.null", Messages.get("ref.safe")));
				}
				break;
			}
			case bank: {
				if (model.refBank.id == null) {
					filledForm.reject("refBank.id", Messages.get("is.not.null", Messages.get("ref.bank")));
				}
				break;
			}
		}
	}

}

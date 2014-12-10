/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.chqbll;

import static play.data.Form.form;
import html.trans_form_rows.ChqbllTransRows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import meta.RightBind;
import models.ChqbllPayrollDetail;
import models.ChqbllTrans;
import models.ChqbllTransDetail;
import models.search.ChqbllTransSearchParam;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.DateUtils;
import utils.DocNoUtils;
import utils.Format;
import views.html.chqblls.transaction.form;
import views.html.chqblls.transaction.list;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.DocNoIncType;
import enums.Module;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Trans extends Controller {

	private final static Right[] ACCEPTABLE_RIGHTS = {
		Right.CEK_MUSTERI_HAREKETLERI,
		Right.CEK_FIRMA_HAREKETLERI,
		Right.SENET_MUSTERI_HAREKETLERI,
		Right.SENET_FIRMA_HAREKETLERI
	};

	private final static Logger log = LoggerFactory.getLogger(Trans.class);
	private final static Form<ChqbllTrans> dataForm = form(ChqbllTrans.class);
	private final static Form<ChqbllTransSearchParam> paramForm = form(ChqbllTransSearchParam.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("receipt_no"), "6%", "right", null).sortable("receiptNo"));
		headerList.add(new GridHeader(Messages.get("contact.name"), "30%", true, null).sortable("contact.name"));
		headerList.add(new GridHeader(Messages.get("to.where"), "12%").sortable("toStep"));
		headerList.add(new GridHeader(Messages.get("row_count"), "5%", "right", null));
		headerList.add(new GridHeader(Messages.get("date.avarage"), "8%", "center", null).sortable("avarageDate"));
		headerList.add(new GridHeader(Messages.get("amount"), "8%", "right", "red"));
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
	private static PageExtend<ChqbllTrans> buildPage(ChqbllTransSearchParam searchParam, Right right) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<ChqbllTrans> page = ChqbllTrans.page(searchParam, right);
		List<ChqbllTrans> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (ChqbllTrans model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.receiptNo.toString());
				dataMap.put(i++, (model.contact != null ? model.contact.name : ""));
				dataMap.put(i++, Messages.get(model.toStep.key));
				dataMap.put(i++, model.rowCount.toString());
				dataMap.put(i++, DateUtils.formatDateStandart(model.avarageDate));
				dataMap.put(i++, Format.asMoney(model.total));
				if (Profiles.chosen().gnel_hasExchangeSupport) {
					dataMap.put(i++, model.excCode);
				}
				dataMap.put(i++, model.description);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<ChqbllTrans>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME(ChqbllSort sort, RightBind rightBind) {
		return redirect(
			(ChqbllSort.Cheque.equals(sort)
				? controllers.chqbll.routes.TransForCheque.list(rightBind)
				: controllers.chqbll.routes.TransForBill.list(rightBind)
			)
		);
	}

	public static Result list(ChqbllSort sort, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		Form<ChqbllTransSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(
			list.render(
				buildPage(filledParamForm.get(), rightBind.value), sort, rightBind, filledParamForm
			)
		);
	}

	public static Result save(ChqbllSort sort, RightBind rightBind) {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<ChqbllTrans> filledForm = dataForm.bindFromRequest();
		ChqbllTrans model = filledForm.get();


		Result hasProblem = AuthManager.hasProblem(rightBind.value, (model.id == null ? RightLevel.Insert : RightLevel.Update), ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		checkFirstConstraints(filledForm);
		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, sort, rightBind, ChqbllTransRows.build(model)));
		}

		String editingConstraintError = model.checkEditingConstraints();
		if (editingConstraintError != null) {
			flash("error", editingConstraintError);
			return badRequest(form.render(filledForm, sort, rightBind, ChqbllTransRows.build(model)));
		}

		model.workspace = CacheUtils.getWorkspaceId();
		model.right = rightBind.value;
		model.transType = rightBind.value.transType;
		model.transYear = DateUtils.getYear(model.transDate);
		model.transMonth = DateUtils.getYearMonth(model.transDate);
		model.excEquivalent = model.total;

		/*
		 * Cek/Senet ayarlari
		 */
		List<ChqbllPayrollDetail> removeDetailList = new ArrayList<ChqbllPayrollDetail>();

		Ebean.beginTransaction();
		try {
			model.details = new ArrayList<ChqbllTransDetail>();
			for (ChqbllPayrollDetail virtual: model.virtuals) {
				if (virtual.amount == null || virtual.amount.doubleValue() == 0) {
					removeDetailList.add(virtual);
					continue;
				}
			}
			model.virtuals.removeAll(removeDetailList);
			model.rowCount = model.virtuals.size();

			checkSecondConstraints(filledForm);
			if(filledForm.hasErrors()) {
				Ebean.rollbackTransaction();
				return badRequest(form.render(filledForm, sort, rightBind, ChqbllTransRows.build(model)));
			}

			try {
				if (model.id == null) {
					model.save();
				} else {
					model.update();
				}
			} catch (OptimisticLockException e) {
				flash("error", Messages.get("exception.optimistic.lock"));
				return badRequest(form.render(filledForm, sort, rightBind, ChqbllTransRows.build(model)));
			}
			Ebean.commitTransaction();
		} catch (Exception e) {
			log.error("ERROR", e);
			Ebean.rollbackTransaction();
		}

		flash("success", Messages.get("saved", Messages.get(rightBind.value.key)));
		if (Profiles.chosen().gnel_continuouslyRecording)
			return create(sort, rightBind);
		else
			return GO_HOME(sort, rightBind);

	}

	public static Result create(ChqbllSort sort, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Insert, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		ChqbllTrans neu = new ChqbllTrans();
		neu.transType = rightBind.value.transType;
		if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) neu.transNo = DocNoUtils.findLastTransNo(rightBind.value);
		neu.receiptNo = DocNoUtils.findLastReceiptNo(rightBind.value);

		return ok(form.render(dataForm.fill(neu), sort, rightBind, ChqbllTransRows.build(neu)));
	}

	public static Result edit(Integer id, ChqbllSort sort, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			ChqbllTrans model = ChqbllTrans.findById(id);

			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				return ok(form.render(dataForm.fill(model), sort, rightBind, ChqbllTransRows.build(model)));
			}
		}
		return GO_HOME(sort, rightBind);
	}

	public static Result remove(Integer id, ChqbllSort sort, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Delete, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			ChqbllTrans model = ChqbllTrans.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) {
					flash("error", editingConstraintError);
					return badRequest(form.render(dataForm.fill(model), sort, rightBind, ChqbllTransRows.build(model)));
				}
				try {
					model.delete();
					flash("success", Messages.get("deleted", Messages.get(rightBind.value.key)));
				} catch (PersistenceException pe) {
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", Messages.get(rightBind.value.key)));
					return badRequest(form.render(dataForm.fill(model), sort, rightBind, ChqbllTransRows.build(model)));
				}
			}
		}
		return GO_HOME(sort, rightBind);
	}

	private static void checkFirstConstraints(Form<ChqbllTrans> filledForm) {
		ChqbllTrans model = filledForm.get();

		if (model.total == null || model.total.intValue() < 1) {
			filledForm.reject("total", Messages.get("error.min.strict", 0));
		}
		if (model.adat == null || model.adat.intValue() < 1) {
			filledForm.reject("adat", Messages.get("error.min.strict", 0));
		}
	}

	private static void checkSecondConstraints(Form<ChqbllTrans> filledForm) {
		ChqbllTrans model = filledForm.get();

		Module refModule = ChqbllStep.findRefModule(model.fromStep, model.toStep);
		switch (refModule) {
			case safe: {
				model.bank = null;
				model.contact = null;
				if (model.safe.id == null) {
					filledForm.reject("safe.name", Messages.get("is.not.null", Messages.get("safe")));
				}
				break;
			}
			case bank: {
				model.safe = null;
				model.contact = null;
				if (model.bank.id == null) {
					filledForm.reject("bank.name", Messages.get("is.not.null", Messages.get("bank")));
				}
				break;
			}
			case contact: {
				model.safe = null;
				model.bank = null;
				if (model.contact.id == null) {
					filledForm.reject("contact.name", Messages.get("is.not.null", Messages.get("contact")));
				}
				break;
			}

			default: {
				model.bank = null;
				model.safe = null;
				model.contact = null;
			}
		}

		List<ValidationError> veList = new ArrayList<ValidationError>();
		if (model.virtuals != null && model.virtuals.size() < 1) {
			veList.add(new ValidationError("chqblls", Messages.get("table.min.row.alert")));
		} else {
			DateTime transDate =  new DateTime(model.transDate);
			for (int i = 1; i < model.virtuals.size() + 1; i++) {
				ChqbllPayrollDetail std = model.virtuals.get(i-1);
				if (ChqbllStep.Endorsed.equals(model.toStep)) {
					ChqbllPayrollDetail detail = ChqbllPayrollDetail.findById(std.id);
					if (detail.contact.equals(model.contact)) {
						veList.add(new ValidationError("chqblls", Messages.get("contacts.are.same.table", i)));
					}
				}

				DateTime dueDate = new DateTime(std.dueDate);
				Days days = Days.daysBetween(transDate, dueDate);

				if (days.getDays() < 1) {
					veList.add(new ValidationError("chqblls", Messages.get("duedate.close.for.table", i)));
				}
			}
		}

		if (veList.size() > 0) {
			filledForm.errors().put("chqblls", veList);
		}
	}

}

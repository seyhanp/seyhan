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
package controllers.chqbll;

import static play.data.Form.form;
import html.trans_form_rows.ChqbllPayrollRows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import meta.RightBind;
import models.Bank;
import models.ChqbllDetailHistory;
import models.ChqbllPayroll;
import models.ChqbllPayrollDetail;
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
import utils.RefModuleUtil;

import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.DocNoIncType;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Payrolls extends Controller {

	private final static Right[] ACCEPTABLE_RIGHTS = {
		Right.CEK_MUSTERI_ACILIS_ISLEMI,
		Right.CEK_FIRMA_ACILIS_ISLEMI,
		Right.CEK_GIRIS_BORDROSU,
		Right.CEK_CIKIS_BORDROSU,
		Right.SENET_MUSTERI_ACILIS_ISLEMI,
		Right.SENET_FIRMA_ACILIS_ISLEMI,
		Right.SENET_GIRIS_BORDROSU,
		Right.SENET_CIKIS_BORDROSU
	};

	private final static Logger log = LoggerFactory.getLogger(Payrolls.class);
	private final static Form<ChqbllPayroll> dataForm = form(ChqbllPayroll.class);
	private final static Form<ChqbllTransSearchParam> paramForm = form(ChqbllTransSearchParam.class);

	private static List<GridHeader> getHeaderList(Right right) {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("receipt_no"), "6%", "right", null).sortable("receiptNo"));
		if (isNormalPayroll(right)) {
			headerList.add(new GridHeader(Messages.get("contact"), "30%", false, true).sortable("contact.name"));
			headerList.add(new GridHeader(Messages.get("date.avarage"), "8%", "center", null).sortable("avarageDate"));
		}
		headerList.add(new GridHeader(Messages.get("row_count"), "5%", "right", null));
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
	private static PageExtend<ChqbllPayroll> buildPage(ChqbllTransSearchParam searchParam, ChqbllSort sort, Right right) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		searchParam.sort = sort;
		Page<ChqbllPayroll> page = ChqbllPayroll.page(searchParam, right);
		List<ChqbllPayroll> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (ChqbllPayroll model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.receiptNo.toString());
				if (isNormalPayroll(right)) {
					dataMap.put(i++, (model.contact != null ? model.contact.name : ""));
					dataMap.put(i++, DateUtils.formatDateStandart(model.avarageDate));
				}
				dataMap.put(i++, model.rowCount.toString());
				dataMap.put(i++, Format.asMoney(model.total));
				if (Profiles.chosen().gnel_hasExchangeSupport) {
					dataMap.put(i++, model.excCode);
				}
				dataMap.put(i++, model.description);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<ChqbllPayroll>(getHeaderList(right), dataList, page);
	}

	public static Result GO_HOME(ChqbllSort sort, RightBind rightBind) {
		return redirect(
			(ChqbllSort.Cheque.equals(sort)
				? controllers.chqbll.routes.PayrollsForCheque.list(rightBind)
				: controllers.chqbll.routes.PayrollsForBill.list(rightBind)
			)
		);
	}

	public static Result list(ChqbllSort sort, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		Form<ChqbllTransSearchParam> filledParamForm = paramForm.bindFromRequest();

		if (isNormalPayroll(rightBind.value)) {
			return ok(
				views.html.chqblls.payroll.list.render(
					buildPage(filledParamForm.get(), sort, rightBind.value), sort, rightBind, filledParamForm
				)
			);
		} else {
			return ok(
				views.html.chqblls.opening.list.render(
					buildPage(filledParamForm.get(), sort, rightBind.value), sort, rightBind
				)
			);
		}
			
	}

	public static Result save(ChqbllSort sort, RightBind rightBind) {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<ChqbllPayroll> filledForm = dataForm.bindFromRequest();
		ChqbllPayroll model = filledForm.get();

		Result hasProblem = AuthManager.hasProblem(rightBind.value, (model.id == null ? RightLevel.Insert : RightLevel.Update), ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		checkFirstConstraints(filledForm);
		if(filledForm.hasErrors()) {
			if (isNormalPayroll(rightBind.value)) {
				return badRequest(views.html.chqblls.payroll.form.render(filledForm, sort, rightBind, ChqbllPayrollRows.build(model)));
			} else {
				return badRequest(views.html.chqblls.opening.form.render(filledForm, sort, rightBind, ChqbllPayrollRows.build(model)));
			}
		}

		String editingConstraintError = model.checkEditingConstraints();
		if (editingConstraintError != null) {
			flash("error", editingConstraintError);
			if (isNormalPayroll(rightBind.value)) {
				return badRequest(views.html.chqblls.payroll.form.render(filledForm, sort, rightBind, ChqbllPayrollRows.build(model)));
			} else {
				return badRequest(views.html.chqblls.opening.form.render(filledForm, sort, rightBind, ChqbllPayrollRows.build(model)));
			}
		}

		model.workspace = CacheUtils.getWorkspaceId();
		model.right = rightBind.value;
		model.transType = rightBind.value.transType;
		model.transYear = DateUtils.getYear(model.transDate);
		model.transMonth = DateUtils.getYearMonth(model.transDate);
		model.excEquivalent = model.total;

		if (! isNormalPayroll(model.right)) model.contact = null;
		
		/*
		 * Cek/Senet ayarlari
		 */
		int rowNo = 0;
		int portInd = 0;
		int lastPortfolioNo = DocNoUtils.findLastPortfolioNo(sort);

		List<ChqbllPayrollDetail> removeDetailList = new ArrayList<ChqbllPayrollDetail>();
		for (ChqbllPayrollDetail detail: model.details) {
			if (detail.dueDate == null || detail.amount == null || detail.amount.doubleValue() < 1) {
				removeDetailList.add(detail);
				continue;
			}
			detail.rowNo = ++rowNo;
			detail.trans = model;
			detail.workspace = model.workspace;
			detail.sort = model.sort;
			if (isNormalPayroll(model.right)) {
				detail.contact = model.contact;
				detail.contactName = model.contact.name;
				detail.lastContactName = detail.contactName;
				detail.transPoint = model.transPoint;
				detail.privateCode = model.privateCode;
			}
			detail.transSource = model.transSource;
			detail.dueYear = DateUtils.getYear(detail.dueDate);
			detail.dueMonth = DateUtils.getYearMonth(detail.dueDate);
			detail.isCustomer = ChqbllStep.isCustomer(model.right);

			if (detail.bank != null && detail.bank.id != null) {
				Bank bank = Bank.findById(detail.bank.id);
				if (bank != null) {
					detail.bankName = bank.name;
					detail.bankBranch =  bank.branch;
					detail.bankAccountNo =  bank.accountNo;
					detail.paymentPlace = bank.city;
				}
			}

			if (detail.id == null) {
				detail.portfolioNo = lastPortfolioNo + portInd;
				portInd++;

				ChqbllDetailHistory history = new ChqbllDetailHistory();
				history.detail = detail;

				if (isNormalPayroll(model.right)) {
					detail.lastStep = (detail.isCustomer ? ChqbllStep.InPortfolio : ChqbllStep.Issued );
				}

				history.step = detail.lastStep;
				history.sort = detail.sort;
				history.contact = detail.contact;
				history.insertBy = CacheUtils.getUser().username;

				detail.histories = new ArrayList<ChqbllDetailHistory>();
				detail.histories.add(history);
			} else if (! isNormalPayroll(model.right)) { //devir fisi ise
				ChqbllDetailHistory.setStep(detail.id, detail.lastStep);
			}
		}
		model.details.removeAll(removeDetailList);
		model.rowCount = rowNo;

		checkSecondConstraints(filledForm);
		if(filledForm.hasErrors()) {
			if (isNormalPayroll(rightBind.value)) {
				return badRequest(views.html.chqblls.payroll.form.render(filledForm, sort, rightBind, ChqbllPayrollRows.build(model)));
			} else {
				return badRequest(views.html.chqblls.opening.form.render(filledForm, sort, rightBind, ChqbllPayrollRows.build(model)));
			}
		}

		String res = null;
		try {
			if (isNormalPayroll(rightBind.value)) {
				res = RefModuleUtil.save(false, model, rightBind.value.module, model.contact);
			} else {
				if (model.id == null) {
					model.save();
				} else {
					model.update();
				}
			}
		} catch (OptimisticLockException e) {
			res = "exception.optimistic.lock";
		}
		if (res != null) {
			flash("error", Messages.get(res));
			if (isNormalPayroll(rightBind.value)) {
				return badRequest(views.html.chqblls.payroll.form.render(filledForm, sort, rightBind, ChqbllPayrollRows.build(model)));
			} else {
				return badRequest(views.html.chqblls.opening.form.render(filledForm, sort, rightBind, ChqbllPayrollRows.build(model)));
			}
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

		ChqbllPayroll neu = new ChqbllPayroll();
		if (Profiles.chosen().gnel_docNoIncType.equals(DocNoIncType.Full_Automatic)) neu.transNo = DocNoUtils.findLastTransNo(rightBind.value);

		neu.transType = rightBind.value.transType;
		neu.sort = sort;
		neu.right = rightBind.value;
		neu.receiptNo = DocNoUtils.findLastReceiptNo(rightBind.value);

		if (isNormalPayroll(rightBind.value)) {
			return ok(views.html.chqblls.payroll.form.render(dataForm.fill(neu), sort, rightBind, ChqbllPayrollRows.build(neu)));
		} else {
			return ok(views.html.chqblls.opening.form.render(dataForm.fill(neu), sort, rightBind, ChqbllPayrollRows.build(neu)));
		}
	}

	public static Result edit(Integer id, ChqbllSort sort, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			ChqbllPayroll model = ChqbllPayroll.findById(id);

			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				RefModuleUtil.setTransientFields(model);
				if (isNormalPayroll(rightBind.value)) {
					return ok(views.html.chqblls.payroll.form.render(dataForm.fill(model), sort, rightBind, ChqbllPayrollRows.build(model)));
				} else {
					return ok(views.html.chqblls.opening.form.render(dataForm.fill(model), sort, rightBind, ChqbllPayrollRows.build(model)));
				}
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
			ChqbllPayroll model = ChqbllPayroll.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) {
					flash("error", editingConstraintError);
					if (isNormalPayroll(rightBind.value)) {
						return badRequest(views.html.chqblls.payroll.form.render(dataForm.fill(model), sort, rightBind, ChqbllPayrollRows.build(model)));
					} else {
						return badRequest(views.html.chqblls.opening.form.render(dataForm.fill(model), sort, rightBind, ChqbllPayrollRows.build(model)));
					}
				}
				try {
					RefModuleUtil.remove(model);
					flash("success", Messages.get("deleted", Messages.get(rightBind.value.key)));
				} catch (PersistenceException pe) {
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", Messages.get(rightBind.value.key)));
					if (isNormalPayroll(rightBind.value)) {
						return badRequest(views.html.chqblls.payroll.form.render(dataForm.fill(model), sort, rightBind, ChqbllPayrollRows.build(model)));
					} else {
						return badRequest(views.html.chqblls.opening.form.render(dataForm.fill(model), sort, rightBind, ChqbllPayrollRows.build(model)));
					}
				}
			}
		}
		return GO_HOME(sort, rightBind);
	}

	private static boolean isNormalPayroll(Right right) {
		return (right.equals(Right.CEK_GIRIS_BORDROSU)
			 || right.equals(Right.CEK_CIKIS_BORDROSU)
			 || right.equals(Right.SENET_GIRIS_BORDROSU)
			 || right.equals(Right.SENET_CIKIS_BORDROSU));
	}

	private static void checkFirstConstraints(Form<ChqbllPayroll> filledForm) {
		ChqbllPayroll model = filledForm.get();

		if (model.total == null || model.total.intValue() < 1) {
			filledForm.reject("total", Messages.get("error.min.strict", 0));
		}
		
		if (isNormalPayroll(model.right)) {
			if (model.contact.id == null) {
				filledForm.reject("contact.name", Messages.get("is.not.null", Messages.get("contact")));
			}

			if (model.adat == null || model.adat.intValue() < 1) {
				filledForm.reject("adat", Messages.get("error.min.strict", 0));
			}
		}
	}

	private static void checkSecondConstraints(Form<ChqbllPayroll> filledForm) {
		ChqbllPayroll model = filledForm.get();

		List<ValidationError> veList = new ArrayList<ValidationError>();

		if (model.details != null && model.details.size() > 0) {

			DateTime transDate =  new DateTime(model.transDate);
			for (int i = 1; i < model.details.size() + 1; i++) {
				ChqbllPayrollDetail std = model.details.get(i-1);

				if (std.amount == null || std.amount <= 0) {
					veList.add(new ValidationError("chqblls", Messages.get("cannot.be.zero.table", i)));
				}
				if (std.dueDate == null) {
					veList.add(new ValidationError("chqblls", Messages.get("is.not.null.for.table", i, Messages.get("date.maturity"))));
				} else if (isNormalPayroll(model.right)) {
					DateTime dueDate = new DateTime(std.dueDate);
					Days days = Days.daysBetween(transDate, dueDate);

					if (days.getDays() < 1) {
						veList.add(new ValidationError("chqblls", Messages.get("duedate.close.for.table", i)));
					}
				}
			}

		} else {
			veList.add(new ValidationError("chqblls", Messages.get("table.min.row.alert")));
		}

		if (veList.size() > 0) {
			filledForm.errors().put("chqblls", veList);
		}
	}

}

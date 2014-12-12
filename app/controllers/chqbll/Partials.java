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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import meta.GridHeader;
import meta.PageExtend;
import meta.RightBind;
import models.ChqbllDetailHistory;
import models.ChqbllDetailPartial;
import models.ChqbllPayrollDetail;
import models.SafeTrans;
import models.search.ChqbllPartialSearchParam;
import models.temporal.ChqbllPartialList;

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
import utils.Format;
import utils.StringUtils;
import views.html.chqblls.partial.form;
import views.html.chqblls.partial.list;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.ChqbllStep;
import enums.Right;
import enums.RightLevel;
import enums.TransType;

/**
 * @author mdpinar
*/
public class Partials extends Controller {

	private final static Right[] ACCEPTABLE_RIGHTS = {
		Right.CEK_PARCALI_TAHSILAT,
		Right.CEK_PARCALI_ODEME,
		Right.SENET_PARCALI_TAHSILAT,
		Right.SENET_PARCALI_ODEME
	};

	private final static Logger log = LoggerFactory.getLogger(Partials.class);
	private final static Form<ChqbllPartialList> dataForm = form(ChqbllPartialList.class);
	private final static Form<ChqbllPartialSearchParam> paramForm = form(ChqbllPartialSearchParam.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("portfolio.no"), "7%", "right", null).sortable("portfolioNo"));
		headerList.add(new GridHeader(Messages.get("contact.name"), true).sortable("lastContactName"));
		headerList.add(new GridHeader(Messages.get("maturity"), "8%", "center", null).sortable("dueDate"));
		headerList.add(new GridHeader(Messages.get("amount"), "8%", "right", "red"));
		headerList.add(new GridHeader(Messages.get("paid"), "8%", "right", "green"));
		headerList.add(new GridHeader(Messages.get("remaining"), "8%", "right", "blue"));
		if (Profiles.chosen().gnel_hasExchangeSupport) {
			headerList.add(new GridHeader(Messages.get("currency"), "4%", "center", null));
		}
		headerList.add(new GridHeader(Messages.get("status"), "11%").sortable("lastStep"));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<ChqbllPayrollDetail> buildPage(ChqbllPartialSearchParam searchParam, ChqbllSort sort, Right right) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		searchParam.sort = sort;
		Page<ChqbllPayrollDetail> page = ChqbllPayrollDetail.page(searchParam, right);
		List<ChqbllPayrollDetail> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (ChqbllPayrollDetail model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.portfolioNo.toString());
				dataMap.put(i++, model.lastContactName);
				dataMap.put(i++, DateUtils.formatDateStandart(model.dueDate));
				dataMap.put(i++, Format.asMoney(model.amount));
				dataMap.put(i++, Format.asMoney(model.totalPaid));
				dataMap.put(i++, Format.asMoney(model.amount - model.totalPaid));
				if (Profiles.chosen().gnel_hasExchangeSupport) {
					dataMap.put(i++, model.excCode);
				}
				dataMap.put(i++, Messages.get(model.lastStep.key));

				dataList.add(dataMap);
			}
		}

		return new PageExtend<ChqbllPayrollDetail>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME(ChqbllSort sort, RightBind rightBind) {
		return redirect(
			(ChqbllSort.Cheque.equals(sort)
				? controllers.chqbll.routes.PartialsForCheque.list(rightBind)
				: controllers.chqbll.routes.PartialsForBill.list(rightBind)
			)
		);
	}

	public static Result list(ChqbllSort sort, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		Form<ChqbllPartialSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(
			list.render(
				buildPage(filledParamForm.get(), sort, rightBind.value), sort, rightBind, filledParamForm
			)
		);
	}

	public static Result save(ChqbllSort sort, RightBind rightBind) {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<ChqbllPartialList> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, sort, rightBind));
		} else {
			ChqbllPartialList model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Insert, ACCEPTABLE_RIGHTS);
			if (hasProblem != null) return hasProblem;

			double totalPaid = 0d;

			List<ChqbllDetailPartial> removeDetailList = new ArrayList<ChqbllDetailPartial>();
			for (ChqbllDetailPartial detail: model.details) {
				if (detail.transDate == null || detail.amount == null || detail.amount.doubleValue() < 1) {
					removeDetailList.add(detail);
					continue;
				}

				totalPaid += detail.amount;

				if (detail.trans == null || detail.trans.id == null) {
					detail.trans = new SafeTrans(rightBind.value);
					detail.trans.insertBy = CacheUtils.getUser().username;
					detail.trans.insertAt = new Date();
				} else {
					detail.trans.refresh();
				}

				detail.trans.workspace = CacheUtils.getWorkspaceId();
				detail.trans.right = rightBind.value;
				detail.trans.safe = detail.safe;
				detail.trans.amount = detail.amount;
				detail.trans.description = detail.description;
				detail.trans.transYear = DateUtils.getYear(detail.transDate);
				detail.trans.transMonth = DateUtils.getYearMonth(detail.transDate);

				if (model.isCustomer) {
					detail.trans.transType = TransType.Debt;
					detail.trans.debt = detail.amount;
					detail.trans.credit = 0d;
				} else {
					detail.trans.transType = TransType.Credit;
					detail.trans.debt = 0d;
					detail.trans.credit = detail.amount;
				}
				if (detail.trans.id == null) {
					detail.trans.receiptNo = model.detailId.intValue();
					detail.trans.refId = model.detailId;
					detail.trans.refModule = rightBind.value.module;
				}
				detail.trans.excCode = detail.excCode;
				detail.trans.excRate = detail.excRate;
				detail.trans.excEquivalent = detail.excEquivalent;
			}
			model.details.removeAll(removeDetailList);

			checkConstraints(filledForm);

			if(filledForm.hasErrors()) {
				return badRequest(form.render(filledForm, sort, rightBind));
			}

			Ebean.beginTransaction();
			try {

				ChqbllPayrollDetail detail = ChqbllPayrollDetail.findById(model.detailId);
				List<ChqbllDetailPartial> oldList = ChqbllDetailPartial.findList(detail);

				detail.totalPaid = totalPaid;

				ChqbllStep step = (detail.isCustomer ? ChqbllStep.PartCollection : ChqbllStep.PartPayment);
				if (! step.equals(detail.lastStep)) {
					//detay bu kisimda update ediliyor
					ChqbllDetailHistory.goForward(detail, step, null, null, null);
				} else {
					//detay bu kisimda update ediliyor
					detail.update();
				}

				Set<Integer> newIdMap = new HashSet<Integer>();
				for (ChqbllDetailPartial det: model.details) {
					if (det.id == null) {
						det.detail = detail;
						det.insertBy = CacheUtils.getUser().username;
						det.insertAt = new Date();
						det.save();
						detail.trans.receiptNo = det.id.intValue();
					} else {
						det.update();
					}
					newIdMap.add(det.id);
				}

				/*
				 * Silinenler veritabanindan ucurulur
				 */
				if (newIdMap.size() > 0) {
					for (ChqbllDetailPartial part: oldList) {
						if (! newIdMap.contains(part.id)) ChqbllDetailPartial.delById(part.id);
					}
				}

				Ebean.commitTransaction();
			} catch (Exception e) {
				Ebean.rollbackTransaction();
				log.error(e.getMessage(), e);
			}

			flash("success", Messages.get("saved", Messages.get(rightBind.value.key)));
			return GO_HOME(sort, rightBind);
		}

	}

	public static Result edit(Integer id, ChqbllSort sort, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {

			ChqbllPayrollDetail detail = ChqbllPayrollDetail.findById(id);
			ChqbllPartialList model = new ChqbllPartialList();

			model.detailId = detail.id;
			model.sort = detail.sort;
			model.isCustomer = detail.isCustomer;
			model.contactName = detail.lastContactName;
			model.portfolioNo = detail.portfolioNo; 
			model.serialNo = detail.serialNo;
			model.dueDate = DateUtils.formatDateStandart(detail.dueDate);
			model.excCode = detail.excCode;
			model.cbtype = (detail.cbtype !=  null ? detail.cbtype.name : "");
			model.owner = detail.owner;
			model.bankName = detail.bankName;
			model.surety = detail.surety;
			model.paymentPlace = detail.paymentPlace;
			model.description = detail.description;

			model.amount = detail.amount;
			model.paid = detail.totalPaid;
			model.remaining = detail.amount - detail.totalPaid;
			model.details = ChqbllDetailPartial.findList(detail);

			return ok(form.render(dataForm.fill(model), sort, rightBind));
		}
		return GO_HOME(sort, rightBind);
	}

	public static Result remove(Integer id, ChqbllSort sort, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Delete, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {

			ChqbllPartialList model = new ChqbllPartialList();
			Ebean.beginTransaction();
			try {
				ChqbllPayrollDetail detail = ChqbllPayrollDetail.findById(id);
				model.details = ChqbllDetailPartial.findList(detail);

				if (model.details != null && model.details.size() > 0) {
					ChqbllDetailHistory.goBack(detail);
					Ebean.createSqlUpdate("delete from chqbll_detail_partial where detail_id = " + id).execute();
					Ebean.createSqlUpdate("delete from safe_trans where receipt_no = " + id + " and _right = '" + rightBind.value.name() + "'").execute();
					Ebean.commitTransaction();
				} else {
					Ebean.rollbackTransaction();
					flash("error", Messages.get("not.found", StringUtils.getChqbllTitle(detail)));
				}
			} catch (Exception e) {
				Ebean.rollbackTransaction();
				log.error(e.getMessage());
				flash("error", Messages.get("delete.violation", Messages.get(rightBind.value.key)));
				return badRequest(form.render(dataForm.fill(model), sort, rightBind));
			}
		}
		return GO_HOME(sort, rightBind);
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<ChqbllPartialList> filledForm) {
		ChqbllPartialList model = filledForm.get();

		if (model.paid.doubleValue() < 1) {
			filledForm.reject("payment", Messages.get("error.min.strict", 0));
		}

		if (model.remaining.doubleValue() < 0) {
			filledForm.reject("remain", Messages.get("error.min", 0));
		}

		List<ValidationError> veList = new ArrayList<ValidationError>();
		if (model.details != null && model.details.size() > 0) {

			ChqbllPayrollDetail detail = ChqbllPayrollDetail.findById(model.detailId);
			DateTime dueDate =  new DateTime(detail.dueDate);

			for (int i = 1; i < model.details.size() + 1; i++) {
				ChqbllDetailPartial std = model.details.get(i-1);

				if (std.amount == null || std.amount <= 0) {
					veList.add(new ValidationError("partials", Messages.get("cannot.be.zero.table", i)));
				}
				if (std.transDate == null) {
					veList.add(new ValidationError("partials", Messages.get("is.not.null.for.table", i, Messages.get("date.maturity"))));
				} else {
					DateTime transDate = new DateTime(std.transDate);
					Days days = Days.daysBetween(dueDate, transDate);

					if (days.getDays() < 0) {
						veList.add(new ValidationError("partials", Messages.get("date.less.from.due.for.table", i)));
					}
				}
			}

		} else {
			veList.add(new ValidationError("partials", Messages.get("table.min.row.alert")));
		}

		if (veList.size() > 0) {
			filledForm.errors().put("partials", veList);
		}
	}

}

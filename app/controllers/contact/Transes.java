/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.contact;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import meta.RightBind;
import models.Contact;
import models.ContactTrans;
import models.search.ContactTransSearchParam;
import models.temporal.Pair;
import models.temporal.TransMultiplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.CloneUtils;
import utils.DateUtils;
import utils.Format;
import utils.QueryUtils;
import utils.RefModuleUtil;
import views.html.contacts.transaction.form;
import views.html.contacts.transaction.list;
import views.html.tools.components.trans_multiplier;

import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.Module;
import enums.Right;
import enums.RightLevel;
import enums.TransType;

/**
 * @author mdpinar
*/
public class Transes extends Controller {

	private final static Right[] ACCEPTABLE_RIGHTS = {
		Right.CARI_ACILIS_ISLEMI,
		Right.CARI_BORC_DEKONTU,
		Right.CARI_ALACAK_DEKONTU
	};

	private final static Logger log = LoggerFactory.getLogger(Transes.class);
	private final static Form<ContactTrans> dataForm = form(ContactTrans.class);
	private final static Form<ContactTransSearchParam> paramForm = form(ContactTransSearchParam.class);

	private static List<GridHeader> getHeaderList(Right right) {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("receipt_no"), "6%", "right", null).sortable("receiptNo"));
		headerList.add(new GridHeader(Messages.get("contact"), "30%", false, true).sortable("contact.name"));
		headerList.add(new GridHeader(Messages.get("date"), "8%", "center", null).sortable("transDate"));
		headerList.add(new GridHeader(Messages.get("amount"), "8%", "right", "red"));
		if (Profiles.chosen().gnel_hasExchangeSupport) {
			headerList.add(new GridHeader(Messages.get("currency"), "4%", "center", null));
		}
		if (right.equals(Right.CARI_ACILIS_ISLEMI)) {
			headerList.add(new GridHeader(Messages.get("dir"), "6%", "center", null));
		}
		headerList.add(new GridHeader(Messages.get("description")));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<ContactTrans> buildPage(ContactTransSearchParam searchParam, Right right) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<ContactTrans> page = ContactTrans.page(searchParam, right);
		List<ContactTrans> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (ContactTrans model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.receiptNo.toString());
				dataMap.put(i++, (model.contact != null ? model.contact.name : ""));
				dataMap.put(i++, DateUtils.formatDateStandart(model.transDate));
				dataMap.put(i++, Format.asMoney(model.amount));
				if (Profiles.chosen().gnel_hasExchangeSupport) {
					dataMap.put(i++, model.excCode);
				}
				if (right.equals(Right.CARI_ACILIS_ISLEMI)) {
					dataMap.put(i++, Messages.get(model.transType.key));
				}
				dataMap.put(i++, model.description);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<ContactTrans>(getHeaderList(right), dataList, page);
	}

	public static Result GO_HOME(RightBind rightBind) {
		return redirect(
			controllers.contact.routes.Transes.list(rightBind)
		);
	}

	public static Result list(RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		Form<ContactTransSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(
			list.render(
				buildPage(filledParamForm.get(), rightBind.value), rightBind, filledParamForm
			)
		);
	}

	public static Result save(RightBind rightBind) {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<ContactTrans> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, rightBind));
		} else {

			ContactTrans model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(rightBind.value, (model.id == null ? RightLevel.Insert : RightLevel.Update), ACCEPTABLE_RIGHTS);
			if (hasProblem != null) return hasProblem;

			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) {
				flash("error", editingConstraintError);
				return badRequest(form.render(filledForm, rightBind));
			}

			checkConstraints(filledForm);

			if(filledForm.hasErrors()) {
				return badRequest(form.render(filledForm, rightBind));
			}

			model.right = rightBind.value;
			if (! rightBind.value.equals(Right.CARI_ACILIS_ISLEMI)) {
				model.transType = rightBind.value.transType;
			}

			switch (model.transType) {
				case Debt: {
					model.debt   = model.amount;
					model.credit = 0d;
					break;
				}

				case Credit: {
					model.credit = model.amount;
					model.debt   = 0d;
					break;
				}
			}

			String res = RefModuleUtil.save(model, Module.contact);
			if (res != null) {
				flash("error", Messages.get(res));
				return badRequest(form.render(filledForm, rightBind));
			}

			flash("success", Messages.get("saved", Messages.get(rightBind.value.key)));
			if (Profiles.chosen().gnel_continuouslyRecording)
				return create(rightBind);
			else
				return GO_HOME(rightBind);
		}

	}

	public static Result create(RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Insert, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		return ok(form.render(dataForm.fill(new ContactTrans(rightBind.value)), rightBind));
	}

	public static Result edit(Integer id, RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			ContactTrans model = ContactTrans.findById(id);

			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				RefModuleUtil.setTransientFields(model);
				return ok(form.render(dataForm.fill(model), rightBind));
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
			ContactTrans model = ContactTrans.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) {
					flash("error", editingConstraintError);
					return badRequest(form.render(dataForm.fill(model), rightBind));
				}
				try {
					RefModuleUtil.remove(model);
					flash("success", Messages.get("deleted", Messages.get(rightBind.value.key)));
				} catch (PersistenceException pe) {
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", Messages.get(rightBind.value.key)));
					return badRequest(form.render(dataForm.fill(model), rightBind));
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

		ContactTrans source = ContactTrans.findById(id);

		Result hasProblem = AuthManager.hasProblem(source.right, RightLevel.Enable, ACCEPTABLE_RIGHTS);
		if (hasProblem != null) return hasProblem;

		TransMultiplier stm = new TransMultiplier();
		stm.id = id;
		stm.contact =  source.contact;
		stm.transNo = source.transNo;
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
			ContactTrans contactTrans = ContactTrans.findById(stm.id);

			ContactTrans clone = CloneUtils.cloneTransaction(contactTrans);
			clone.contact = stm.contact;
			clone.transDate = stm.transDate;
			clone.transMonth = DateUtils.getYearMonth(stm.transDate);
			clone.transYear = DateUtils.getYear(stm.transDate);
			clone.transNo = stm.transNo;
			clone.description = stm.description;
			RefModuleUtil.save(clone, Module.contact);

			return ok(Messages.get("saved", Messages.get(clone.right.key)));
		}
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<ContactTrans> filledForm) {
		ContactTrans model = filledForm.get();

		if (model.amount == null || model.amount.doubleValue() == 0) {
			filledForm.reject("amount", Messages.get("error.zero", Messages.get("amount")));
		}

		if (model.contact.id == null) {
			filledForm.reject("contact.name", Messages.get("is.not.null", Messages.get("contact")));
		} else {
			if (model.id == null) {
				Contact contact = Contact.findById(model.contact.id);
				if (contact.category != null && Profiles.chosen().cari_hasCategoryControls) {
	
					double limit = 0d;
					double balance = Math.abs(QueryUtils.findBalance(Module.contact, contact.id));
	
					if (model.transType.equals(TransType.Debt)) 
						limit = contact.category.debtLimit;
					else
						limit = contact.category.creditLimit;
	
					if (balance + model.amount > limit) {
						filledForm.reject("amount", Messages.get("warning.balance.limit", limit - balance));
					}
				}
			}

			Pair pair = RefModuleUtil.checkForRefAccounts(model, model.contact);
			if (pair.key != null) {
				filledForm.reject(pair.key, pair.value);
			}
		}

		if (model.maturity != null && model.maturity.before(model.transDate)) {
			filledForm.reject("maturity", Messages.get("error.dateBefore", Messages.get("date.maturity")));
		}

	}

}

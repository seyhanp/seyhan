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
package controllers.novaposhta;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import meta.RightBind;
import models.NovaposhtaCargoTrans;
import models.search.NovaposhtaCargoTransSearchParam;

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
import views.html.novaposhtas.transaction.form;
import views.html.novaposhtas.transaction.list;

import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Transes extends Controller {

	private final static Logger log = LoggerFactory.getLogger(Transes.class);
	private final static Form<NovaposhtaCargoTrans> dataForm = form(NovaposhtaCargoTrans.class);
	private final static Form<NovaposhtaCargoTransSearchParam> paramForm = form(NovaposhtaCargoTransSearchParam.class);

	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("date"), "8%", "center", null).sortable("transDate"));
		headerList.add(new GridHeader(Messages.get("novaposhta.reg_no"), "15%").sortable("regNo"));
		headerList.add(new GridHeader(Messages.get("novaposhta.cargo.value"), "10%", "right", "green"));
		headerList.add(new GridHeader(Messages.get("novaposhta.cargo.money"), "10%", "right", "blue"));
		headerList.add(new GridHeader(Messages.get("novaposhta.cargo.return"), "10%", "right", "red"));
		headerList.add(new GridHeader(Messages.get("novaposhta.cargo.total"), "10%", "right", "brown"));
		headerList.add(new GridHeader(Messages.get("description")));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<NovaposhtaCargoTrans> buildPage(NovaposhtaCargoTransSearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<NovaposhtaCargoTrans> page = NovaposhtaCargoTrans.page(searchParam);
		List<NovaposhtaCargoTrans> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (NovaposhtaCargoTrans model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, DateUtils.formatDateStandart(model.transDate));
				dataMap.put(i++, model.regNo);
				dataMap.put(i++, Format.asMoney(model.cargoValue));
				dataMap.put(i++, Format.asMoney(model.money));
				dataMap.put(i++, Format.asMoney(model.return_));
				dataMap.put(i++, Format.asMoney(model.total));
				dataMap.put(i++, model.description);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<NovaposhtaCargoTrans>(getHeaderList(), dataList, page);
	}

	private static RightBind rightBind = new RightBind(Right.NOVAPOSHTA_KARGO_HAREKETLERI);
	
	public static Result GO_HOME() {
		return redirect(
			controllers.novaposhta.routes.Transes.list(rightBind)
		);
	}
	
	public static Result list(RightBind rightBind) {
		Result hasProblem = AuthManager.hasProblem(rightBind.value, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<NovaposhtaCargoTransSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(
			list.render(buildPage(filledParamForm.get()), rightBind, filledParamForm)
		);
	}

	public static Result save() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<NovaposhtaCargoTrans> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			NovaposhtaCargoTrans model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(right(), (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) {
				flash("error", editingConstraintError);
				return badRequest(form.render(filledForm));
			}

			checkConstraints(filledForm);

			if(filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}
			
			if (model.money == null || model.money.doubleValue() < 0) model.money = 0d;
			if (model.return_ == null || model.return_.doubleValue() < 0) model.return_ = 0d;
			
			model.transYear = DateUtils.getYear(model.transDate);
			model.transMonth = DateUtils.getYearMonth(model.transDate);
			model.total = model.cargoValue.doubleValue() - model.money.doubleValue() - model.return_.doubleValue();

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
			
			flash("success", Messages.get("saved", Messages.get(right().key)));
			if (Profiles.chosen().gnel_continuouslyRecording)
				return create();
			else
				return GO_HOME();
		}

	}

	public static Result create() {
		Result hasProblem = AuthManager.hasProblem(right(), RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		return ok(form.render(dataForm.fill(new NovaposhtaCargoTrans())));
	}

	public static Result edit(Integer id) {
		Result hasProblem = AuthManager.hasProblem(right(), RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			NovaposhtaCargoTrans model = NovaposhtaCargoTrans.findById(id);

			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				return ok(form.render(dataForm.fill(model)));
			}
		}
		return GO_HOME();
	}

	public static Result remove(Integer id) {
		Result hasProblem = AuthManager.hasProblem(right(), RightLevel.Delete);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			flash("error", Messages.get("id.is.null"));
		} else {
			NovaposhtaCargoTrans model = NovaposhtaCargoTrans.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("transaction")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) {
					flash("error", editingConstraintError);
					return badRequest(form.render(dataForm.fill(model)));
				}
				try {
					model.delete();
					flash("success", Messages.get("deleted", Messages.get(right().key)));
				} catch (PersistenceException pe) {
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", Messages.get(right().key)));
					return badRequest(form.render(dataForm.fill(model)));
				}
			}
		}
		return GO_HOME();
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<NovaposhtaCargoTrans> filledForm) {
		NovaposhtaCargoTrans model = filledForm.get();

		if (model.cargo.id == null) {
			filledForm.reject("cargo.name", Messages.get("is.not.null", Messages.get("novaposhta.cargo.company")));
		}

		if (model.cargoValue == null) model.cargoValue = 0d;
		if (model.money == null) model.money = 0d;
		if (model.return_ == null) model.return_ = 0d;
		
		boolean isAllZero = model.cargoValue.doubleValue() == 0
						 && model.money.doubleValue() == 0
						 && model.return_.doubleValue() == 0;
		
		if (isAllZero) {
			filledForm.reject("cargoValue", Messages.get("error.zero", Messages.get("novaposhta.cargo.value")));
		}

		if (NovaposhtaCargoTrans.isUsedForElse("transDate", model.transDate, model.id)) {
			filledForm.reject("transDate", Messages.get("not.unique", model.transDate));
		}

	}
	
	private static Right right() {
		return Right.NOVAPOSHTA_KARGO_HAREKETLERI;
	}

}

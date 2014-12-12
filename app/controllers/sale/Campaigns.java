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
package controllers.sale;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.SaleCampaign;
import models.search.NameOnlySearchParam;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
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
import views.html.sales.campaign.form;
import views.html.sales.campaign.list;

import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Campaigns extends Controller {

	private final static Right RIGHT_SCOPE = Right.SATS_KAMPANYA_TANITIMI;

	private final static Logger log = LoggerFactory.getLogger(Campaigns.class);
	private final static Form<SaleCampaign> dataForm = form(SaleCampaign.class);
	private final static Form<NameOnlySearchParam> paramForm = form(NameOnlySearchParam.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), true).sortable("name"));
		headerList.add(new GridHeader(Messages.get("priority"),  "6%", "right", null));
		headerList.add(new GridHeader(Messages.get("date.start"), "8%", "center", null).sortable("startDate"));
		headerList.add(new GridHeader(Messages.get("date.end"),  "8%", "center", null).sortable("endDate"));
		headerList.add(new GridHeader(Messages.get("stock.discount", "1"),  "6%", "right", null));
		headerList.add(new GridHeader(Messages.get("stock.discount", "2"),  "6%", "right", null));
		headerList.add(new GridHeader(Messages.get("stock.discount", "3"),  "6%", "right", null));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<SaleCampaign> buildPage(NameOnlySearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<SaleCampaign> page = SaleCampaign.page(searchParam);
		List<SaleCampaign> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (SaleCampaign model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);
				dataMap.put(i++, "" + model.priority);
				dataMap.put(i++, DateUtils.formatDateStandart(model.startDate));
				dataMap.put(i++, DateUtils.formatDateStandart(model.endDate));
				dataMap.put(i++, Format.asDecimal(model.discountRate1));
				dataMap.put(i++, Format.asDecimal(model.discountRate2));
				dataMap.put(i++, Format.asDecimal(model.discountRate3));
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<SaleCampaign>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME = redirect(
		controllers.sale.routes.Campaigns.list()
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

		Form<SaleCampaign> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			SaleCampaign model = filledForm.get();

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

			if (model.discountRate1 == null) model.discountRate1 = 0d;
			if (model.discountRate2 == null) model.discountRate2 = 0d;
			if (model.discountRate3 == null) model.discountRate3 = 0d;

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

		return ok(form.render(dataForm.fill(new SaleCampaign())));
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
			SaleCampaign model = SaleCampaign.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("campaign")));
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
			SaleCampaign model = SaleCampaign.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("campaign")));
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

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<SaleCampaign> filledForm) {
		SaleCampaign model = filledForm.get();

		if (SaleCampaign.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}

		if (model.id == null) {
			LocalDate today = new DateTime().toLocalDate();
			LocalDate startDate = new DateTime(model.startDate).toLocalDate();
			LocalDate endDate = new DateTime(model.endDate).toLocalDate();
	
			if (endDate.isBefore(startDate)) {
				filledForm.reject("startDate", Messages.get("error.dateRange"));
			} else if (startDate.isBefore(today)) {
				filledForm.reject("startDate", Messages.get("error.dateRange.x", Messages.get("date.start")));
			}
		}
	}

}

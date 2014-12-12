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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.StockCosting;
import models.search.NameOnlySearchParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.CostingUtils;
import views.html.stocks.costing.form;
import views.html.stocks.costing.list;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Page;

import controllers.Application;
import controllers.global.Profiles;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Costings extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_MALIYET_HESAPLAMALARI;

	private final static Logger log = LoggerFactory.getLogger(Costings.class);
	private final static Form<StockCosting> dataForm = form(StockCosting.class);
	private final static Form<NameOnlySearchParam> paramForm = form(NameOnlySearchParam.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), true).sortable("properties"));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<StockCosting> buildPage(NameOnlySearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<StockCosting> page = StockCosting.page(searchParam);
		List<StockCosting> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (StockCosting model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.properties);
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<StockCosting>(getHeaderList(), dataList, page);
	}

	public static Result GO_HOME = redirect(
		controllers.stock.routes.Costings.list()
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

		Form<StockCosting> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			StockCosting model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) {
				flash("error", editingConstraintError);
				return badRequest(form.render(dataForm.fill(model)));
			}

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}

			Ebean.beginTransaction();
			try {
				model.execDate = new Date();

				if (model.id == null) {
					model.save();
				} else {
					model.update();
				}
				CostingUtils.execute(model);
				Ebean.commitTransaction();

			} catch (Exception e) {
				flash("error", e.getMessage());
				Ebean.rollbackTransaction();
				log.error("ERROR", e);
				return badRequest(form.render(filledForm));
			}

			flash("success", Messages.get("saved", model.properties));
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

		return ok(form.render(dataForm.fill(new StockCosting())));
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
			StockCosting model = StockCosting.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("costing")));
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
			StockCosting model = StockCosting.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("costing")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) {
					flash("error", editingConstraintError);
					return badRequest(form.render(dataForm.fill(model)));
				}
				Ebean.beginTransaction();
				try {
					Ebean.createSqlUpdate("delete from stock_costing_inventory where costing_id = :costing_id")
							.setParameter("costing_id", model.id)
						.execute();
					Ebean.createSqlUpdate("delete from stock_costing_detail where costing_id = :costing_id")
							.setParameter("costing_id", model.id)
						.execute();
					model.delete();
					Ebean.commitTransaction();
					flash("success", Messages.get("deleted", model.properties));

				} catch (PersistenceException pe) {
					Ebean.rollbackTransaction();
					log.error(pe.getMessage());
					flash("error", Messages.get("delete.violation", model.properties));
					return badRequest(form.render(dataForm.fill(model)));
				}
			}
		}
		return GO_HOME;
	}

}

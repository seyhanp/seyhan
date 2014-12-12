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

import java.util.List;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import models.StockCategory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.api.mvc.SimpleResult;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import views.html.stocks.category.form;
import views.html.stocks.category.index;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Categories extends Controller {

	private final static Right RIGHT_SCOPE = Right.STOK_KATEGORI_TANITIMI;

	private final static Logger log = LoggerFactory.getLogger(Categories.class);
	private final static Form<StockCategory> dataForm = form(StockCategory.class);

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(index.render());
	}

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		return ok();
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isLoggedIn()) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		Form<StockCategory> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			StockCategory model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) return badRequest(editingConstraintError);

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

			return ok(model.id.toString());
		}
	}

	public static Result create(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Insert);
		if (hasProblem != null) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		if (id != null) {
			StockCategory model = StockCategory.findById(id);
			if (model != null) {
				if (model.par5Id != null) {
					return badRequest(Messages.get("limit.alert", Messages.get("category"), 6));
				}
			}
		}

		return ok(form.render(dataForm.fill(new StockCategory(id))));
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			StockCategory model = StockCategory.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("category")));
			} else {
				return ok(form.render(dataForm.fill(model)));
			}
		}
	}

	/**
	 * Duzenlemek icin acilmis olan kaydi siler
	 * 
	 * @param id
	 */
	public static Result remove(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Delete);
		if (hasProblem != null) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			StockCategory model = StockCategory.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("category")));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) return badRequest(editingConstraintError);
				try {
					String parId = "1";

					if (model.par1Id == null) 
						;
					else if (model.par2Id == null) 
						parId = "2";
					else if (model.par3Id == null) 
						parId = "3";
					else if (model.par4Id == null) 
						parId = "4";
					else if (model.par5Id == null) 
						parId = "5";

					Ebean.createSqlUpdate("delete from stock_category where id = :id or par" + parId + "Id = :parId ")
							.setParameter("id", id)
							.setParameter("parId", model.id)
						.execute();
					CacheUtils.cleanAll(StockCategory.class, Right.STOK_KATEGORI_TANITIMI);

					return ok(Messages.get("deleted", model.name));
				} catch (PersistenceException pe) {
					flash("error", Messages.get("delete.violation", model.name));
					log.error("ERROR", pe);
					return badRequest(Messages.get("delete.violation", model.name));
				}
			}
		}
	}

	public static Result paste(Integer sourceId, Integer targetId, Integer op) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Update);
		if (hasProblem != null) {
			return badRequest(Messages.get("not.authorized.or.disconnect"));
		}

		if (sourceId == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {

			StockCategory source = StockCategory.findById(sourceId);
			StockCategory parent = StockCategory.findById(targetId);

			if (source == null || parent == null) {
				return badRequest(Messages.get("not.found", Messages.get("category")));
			} else {

				if (parent.par5Id != null) {
					return badRequest(Messages.get("limit.alert", Messages.get("category"), 6));
				}

				if (sourceId.equals(parent.par1Id)
				||  sourceId.equals(parent.par2Id)
				||  sourceId.equals(parent.par3Id)
				||  sourceId.equals(parent.par4Id)
				||  sourceId.equals(parent.par5Id)) { 
					return badRequest(Messages.get("category.sibling.alert"));
				}

				Ebean.beginTransaction();
				try {
					StockCategory target = null;
					if (op == 3) {
						target = parent;
					} else {
						target = new StockCategory(parent);
						target.name = source.name;
						target.save();
					}

					int level = 1;
					if (source.par1Id != null) level++;
					if (source.par2Id != null) level++;
					if (source.par3Id != null) level++;
					if (source.par4Id != null) level++;
					if (source.par5Id != null) level++;

					copy(sourceId, target.id, level, op);

					if (op != null && op.equals(9)) { //for cut op
						Result result = remove(sourceId);
						if (((SimpleResult)result.getWrappedResult()).header().status() != 200) {
							Ebean.rollbackTransaction();
							return result;
						}
					}
					Ebean.commitTransaction();
					return ok();

				} catch (Exception e) {
					Ebean.rollbackTransaction();
					log.error(e.getMessage(), e);
					return badRequest(e.getMessage());
				}
			}
		}
	}

	private static void copy(Integer sourceId, Integer targetId, int level, Integer op) {
		if (level > 5) return;

		StringBuilder query = new StringBuilder();

		query.append("select id from stock_category where par"+level+"id = :id ");
		for (int i = level + 1; i < 6; i++) {
			query.append(" and par"+i+"id is null ");
		}

		List<SqlRow> idList = Ebean.createSqlQuery(query.toString())
									.setParameter("id", sourceId)
									.findList();

		for (SqlRow row : idList) {
			Integer parentId = row.getInteger("id");

			StockCategory old = StockCategory.findById(parentId);
			StockCategory target = null;

			if (op != 2) {
				target = new StockCategory(targetId);
				target.name = old.name;
				target.save();
			} else {
				target = StockCategory.findById(targetId);
			}
			copy(old.id, target.id, ++level, op);
			level--;
		}

	}

}

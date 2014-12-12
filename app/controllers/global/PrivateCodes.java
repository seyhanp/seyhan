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
package controllers.global;

import static play.data.Form.form;

import java.util.List;

import javax.persistence.OptimisticLockException;

import models.GlobalPrivateCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.api.mvc.SimpleResult;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import views.html.globals.private_code.form;
import views.html.globals.private_code.index;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class PrivateCodes extends Controller {

	private final static Right RIGHT_SCOPE = Right.GNEL_OZEL_KODLAR;

	private final static Logger log = LoggerFactory.getLogger(PrivateCodes.class);
	private final static Form<GlobalPrivateCode> dataForm = form(GlobalPrivateCode.class);

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

		Form<GlobalPrivateCode> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			GlobalPrivateCode model = filledForm.get();

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
			GlobalPrivateCode model = GlobalPrivateCode.findById(id);
			if (model != null) {
				if (model.par5Id != null) {
					return badRequest(Messages.get("limit.alert", Messages.get("private_code"), 6));
				}
			}
		}

		return ok(form.render(dataForm.fill(new GlobalPrivateCode(id))));
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
			GlobalPrivateCode model = GlobalPrivateCode.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("private_code")));
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
			GlobalPrivateCode model = GlobalPrivateCode.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("private_code")));
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

					Ebean.createSqlUpdate("delete from global_private_code where id = :id or par" + parId + "Id = :parId ")
							.setParameter("id", id)
							.setParameter("parId", model.id)
						.execute();
					CacheUtils.cleanAll(GlobalPrivateCode.class, Right.GNEL_OZEL_KODLAR);

					return ok(Messages.get("deleted", model.name));
				} catch (Exception pe) {
					log.error("ERROR", pe);
					flash("error", Messages.get("delete.violation", model.name));
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

			GlobalPrivateCode source = GlobalPrivateCode.findById(sourceId);
			GlobalPrivateCode parent = GlobalPrivateCode.findById(targetId);

			if (source == null || parent == null) {
				return badRequest(Messages.get("not.found", Messages.get("private_code")));
			} else {

				if (parent.par5Id != null) {
					return badRequest(Messages.get("limit.alert", Messages.get("private_code"), 6));
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
					GlobalPrivateCode target = null;
					if (op == 3) {
						target = parent;
					} else {
						target = new GlobalPrivateCode(parent);
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

		query.append("select id from global_private_code where par"+level+"id = :id ");
		for (int i = level + 1; i < 6; i++) {
			query.append(" and par"+i+"id is null ");
		}

		List<SqlRow> idList = Ebean.createSqlQuery(query.toString())
									.setParameter("id", sourceId)
									.findList();

		for (SqlRow row : idList) {
			Integer parentId = row.getInteger("id");

			GlobalPrivateCode old = GlobalPrivateCode.findById(parentId);
			GlobalPrivateCode target = null;

			if (op != 2) {
				target = new GlobalPrivateCode(targetId);
				target.name = old.name;
				target.save();
			} else {
				target = GlobalPrivateCode.findById(targetId);
			}
			copy(old.id, target.id, ++level, op);
			level--;
		}

	}

}

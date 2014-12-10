/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.chqbll;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.ChqbllType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.StringUtils;
import views.html.chqblls.types.form;
import views.html.chqblls.types.index;
import views.html.chqblls.types.list;
import controllers.Application;
import controllers.global.Profiles;
import enums.ChqbllSort;
import enums.Right;
import enums.RightLevel;
/**
 * @author mdpinar
*/
class Types extends Controller {

	private final static Logger log = LoggerFactory.getLogger(Types.class);
	private final static Form<ChqbllType> dataForm = form(ChqbllType.class);

	private static String lastSaved;

	static Result index(ChqbllSort sort) {
		Result hasProblem = AuthManager.hasProblem(findRightBySort(sort), RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(
			index.render(buildPage(sort), findRightBySort(sort))
		);
	}

	static Result options(ChqbllSort sort) {
		Result result = ok(StringUtils.buildOptionTag(ChqbllType.options(sort), lastSaved));
		lastSaved = null;

		return result;
	}

	static Result list(ChqbllSort sort) {
		Result hasProblem = AuthManager.hasProblem(findRightBySort(sort), RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(
			list.render(buildPage(sort), findRightBySort(sort))
		);
	}

	static Result save(ChqbllSort sort) {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<ChqbllType> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm, sort));
		} else {

			ChqbllType model = filledForm.get();

			Result hasProblem = AuthManager.hasProblem(findRightBySort(sort), (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			String editingConstraintError = model.checkEditingConstraints();
			if (editingConstraintError != null) return badRequest(editingConstraintError);

			checkConstraints(filledForm);

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm, sort));
			}

			try {
				if (model.id == null) {
					model.save();
				} else {
					model.update();
				}
			} catch (OptimisticLockException e) {
				flash("error", Messages.get("exception.optimistic.lock"));
				return badRequest(form.render(dataForm.fill(model), sort));
			}
			lastSaved = model.name;

			flash("success", Messages.get("saved", model.name));
			if (Profiles.chosen().gnel_continuouslyRecording)
				return create(sort);
			else
				return index(sort);
		}
	}

	static Result create(ChqbllSort sort) {
		Result hasProblem = AuthManager.hasProblem(findRightBySort(sort), RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		return ok(form.render(dataForm.fill(new ChqbllType(sort)), sort));
	}

	static Result edit(Integer id, ChqbllSort sort) {
		Result hasProblem = AuthManager.hasProblem(findRightBySort(sort), RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			ChqbllType model = ChqbllType.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("typeOf", Messages.get(sort.key))));
			} else {
				return ok(form.render(dataForm.fill(model), sort));
			}
		}
	}

	static Result remove(Integer id, ChqbllSort sort) {
		Result hasProblem = AuthManager.hasProblem(findRightBySort(sort), RightLevel.Delete);
		if (hasProblem != null) return hasProblem;

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			ChqbllType model = ChqbllType.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("typeOf", Messages.get(sort.key))));
			} else {
				String editingConstraintError = model.checkEditingConstraints();
				if (editingConstraintError != null) return badRequest(editingConstraintError);
				try {
					model.delete();
					flash("success", Messages.get("deleted", model.name));
				} catch (PersistenceException pe) {
					flash("error", Messages.get("delete.violation", model.name));
					log.error("ERROR", pe);
				}
			}
		}
		return index(sort);
	}

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), true).sortable("name"));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<ChqbllType> buildPage(ChqbllSort sort) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<ChqbllType> modelList = ChqbllType.page(sort);
		if (modelList != null && modelList.size() > 0) {
			for (ChqbllType model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<ChqbllType>(getHeaderList(), dataList, null);
	}

	private static Right findRightBySort(ChqbllSort sort) {
		if (ChqbllSort.Cheque.equals(sort)) {
			return Right.CEK_TURLERI;
		} else {
			return Right.SENET_TURLERI;
		}
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<ChqbllType> filledForm) {
		ChqbllType model = filledForm.get();

		if (ChqbllType.isUsedForElse(model.sort, "name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}

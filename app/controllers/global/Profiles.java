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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.GlobalProfile;
import models.Safe;
import models.StockDepot;
import models.temporal.InfoMultiplier;
import models.temporal.ProfileData;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.CloneUtils;
import utils.StringUtils;
import views.html.globals.profile.form;
import views.html.globals.profile.list;
import views.html.tools.components.info_multiplier;
import controllers.Application;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class Profiles extends Controller {

	private final static Right RIGHT_SCOPE = Right.GNEL_PROFIL_TANITIMI;
	private final static Form<ProfileData> dataForm = form(ProfileData.class);

	private static ProfileData fakePD = new ProfileData();

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), "20%", true, null).sortable("name"));
		headerList.add(new GridHeader(Messages.get("description")));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<GlobalProfile> buildPage() {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<GlobalProfile> modelList = GlobalProfile.page();
		if (modelList != null && modelList.size() > 0) {
			for (GlobalProfile model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);
				dataMap.put(i++, model.description);
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<GlobalProfile>(getHeaderList(), dataList, null);
	}

	public static Result GO_HOME = redirect(
		controllers.global.routes.Profiles.list()
	);

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(list.render(buildPage()));
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<ProfileData> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			ProfileData modelData = filledForm.get();

			GlobalProfile model = new GlobalProfile();
			model.id = modelData.id;
			model.name = modelData.name;
			model.description = modelData.description;
			model.isActive = modelData.isActive;
			model.version = modelData.version;

			model.jsonData = StringUtils.toJson(modelData);

			Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, (model.id == null ? RightLevel.Insert : RightLevel.Update));
			if (hasProblem != null) return hasProblem;

			checkConstraints(filledForm);

			if (filledForm.hasErrors()) {
				return badRequest(form.render(filledForm));
			}

			try {
				if (model.id == null) {
					model.save();
				} else {
					model.update();
				}
			} catch (OptimisticLockException e) {
				flash("error", Messages.get("exception.optimistic.lock"));
				return badRequest(form.render(filledForm));
			}
			if (CacheUtils.getProfile().equals(model.name)) CacheUtils.setProfile(model.name);

			flash("success", Messages.get("saved", model.name));
			return GO_HOME;
		}

	}

	/**
	 * Yeni bir kayit formu olusturur
	 */
	public static Result create() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		return ok(form.render(dataForm.fill(fakePD)));
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
			GlobalProfile model = GlobalProfile.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("profile")));
			} else {
				ProfileData modelData = StringUtils.fromJson(model.jsonData, ProfileData.class);

				modelData.id = model.id;
				modelData.name = model.name;
				modelData.description = model.description;
				modelData.version = model.version;
				modelData.isActive = model.isActive;
				return ok(form.render(dataForm.fill(modelData)));
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
			GlobalProfile model = GlobalProfile.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("profile")));
			} else {
				try {
					model.delete();
					if (CacheUtils.getProfile().equals(model.name)) {
						GlobalProfile firstProfile = GlobalProfile.findFirst();
						if (firstProfile != null) {
							setChoosen(firstProfile.name);
							Application.changeProfile(firstProfile.name);
						} else {
							Application.changeProfile("default");
						}
					}
					flash("success", Messages.get("deleted", model.name));
				} catch (PersistenceException pe) {
					flash("error", Messages.get("delete.violation", model.name));
					ProfileData modelData = StringUtils.fromJson(model.jsonData, ProfileData.class);
					return badRequest(form.render(dataForm.fill(modelData)));
				}
			}
		}
		return GO_HOME;
	}

	/**
	 * Secilen kaydin kopyasini olusturur
	 * 
	 * @param id
	 */
	public static Result createClone(Integer id) {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Insert);
		if (hasProblem != null) return hasProblem;

		GlobalProfile source = GlobalProfile.findById(id);

		InfoMultiplier im = new InfoMultiplier();
		im.id = id;
		im.name =  source.name;
		im.description =  source.description;

		Form<InfoMultiplier> imDataForm = form(InfoMultiplier.class);

		return ok(
			info_multiplier.render(imDataForm.fill(im), controllers.global.routes.Profiles.list().url(), Profiles.class.getSimpleName())
		);
	}

	/**
	 * Yeni kopyayi kaydeder
	 */
	public static Result saveClone() {
		if (! CacheUtils.isLoggedIn()) return Application.login();

		Form<InfoMultiplier> stmDataForm = form(InfoMultiplier.class);
		Form<InfoMultiplier> filledForm = stmDataForm.bindFromRequest();

		InfoMultiplier im = filledForm.get();

		checkCloneConstraints(filledForm);
		if (filledForm.hasErrors()) {
			return badRequest(info_multiplier.render(filledForm, controllers.global.routes.Profiles.list().url(), Profiles.class.getSimpleName()));
		}

		GlobalProfile source = GlobalProfile.findById(im.id);

		GlobalProfile clone = CloneUtils.cloneModel(source);
		clone.id = null;
		clone.name = im.name;
		clone.description = im.description;
		clone.jsonData = source.jsonData;
		clone.version = 0;
		clone.isActive = Boolean.TRUE;
		clone.save();

		return ok(Messages.get("saved", clone.name));
	}

	private static void checkCloneConstraints(Form<InfoMultiplier> filledForm) {
		InfoMultiplier model = filledForm.get();

		if (model.id == null) {
			filledForm.reject("name", Messages.get("id.is.null"));
		}

		if (GlobalProfile.isUsedForElse("name", model.name, null)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

	public static ProfileData chosen() {
		ProfileData result = null;
		if (Http.Context.current.get() == null) return fakePD;

		String name = CacheUtils.getProfile();
		if (name != null) {
			GlobalProfile profile = GlobalProfile.findByName(name);
			if (profile != null) {
				result = StringUtils.fromJson(profile.jsonData, ProfileData.class);
			}
		} else {
			GlobalProfile profile = GlobalProfile.findFirst();
			if (profile != null) {
				CacheUtils.setProfile(profile.name);
				result = StringUtils.fromJson(profile.jsonData, ProfileData.class);
			}
		}
		
		if (result != null) {
			if (result.gnel_safe == null) result.gnel_safe = Safe.findById(1);
			if (result.stok_depot == null) result.stok_depot = StockDepot.findById(1);
			return result;
		} else {
			fakePD.gnel_safe = Safe.findById(1);
			fakePD.stok_depot = StockDepot.findById(1);
		}

		GlobalProfile model = new GlobalProfile();
		model.name = fakePD.name;
		model.description = fakePD.description;
		model.isActive = true;
		model.jsonData = StringUtils.toJson(fakePD);
		model.version = 0;
		model.save();

		CacheUtils.setProfile(model.name);

		return fakePD;
	}

	public static void setChoosen(String name) {
		GlobalProfile profile = null;

		if (name != null) {
			profile = GlobalProfile.findByName(name);
		}

		if (profile != null) CacheUtils.setProfile(profile.name);
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<ProfileData> filledForm) {
		ProfileData model = filledForm.get();

		if (GlobalProfile.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}

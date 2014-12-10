/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.admin;

import static play.data.Form.form;

import javax.persistence.OptimisticLockException;

import models.AdminSetting;
import models.temporal.SettingData;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CacheUtils;
import utils.StringUtils;
import views.html.admins.setting.form;
import controllers.Application;

/**
 * @author mdpinar
*/
public class Settings extends Controller {

	private static SettingData global;
	private final static Form<SettingData> dataForm = form(SettingData.class);

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<SettingData> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			SettingData modelData = filledForm.get();

			AdminSetting model = new AdminSetting();
			model.id = modelData.id;
			model.code = modelData.code;
			model.description = modelData.description;

			model.jsonData = StringUtils.toJson(modelData);

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
			global = null;

			flash("success", Messages.get("saved", model.description));
			return ok(form.render(filledForm));
		}

	}

	public static Result edit() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(form.render(dataForm.fill(getGlobal())));
	}

	public static SettingData getGlobal() {
		if (global == null) {

			AdminSetting setting = AdminSetting.findByCode("global");
			if (setting == null || setting.jsonData == null || setting.jsonData.trim().isEmpty()) {

				setting = new AdminSetting();
				setting.code = "global";
				setting.description = "global settings";
				setting.save();

				global = new SettingData();
				global.id = setting.id;
				global.code = setting.code;
				global.description = setting.description;

				setting.jsonData = StringUtils.toJson(global);
				setting.update();

			} else {
				global = StringUtils.fromJson(setting.jsonData, SettingData.class);
				global.id = setting.id;
			}
		}

		return global;
	}

}

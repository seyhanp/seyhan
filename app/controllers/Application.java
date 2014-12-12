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
package controllers;

import static play.data.Form.form;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import models.AdminUserAudit;
import models.AdminWorkspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import utils.AuthManager;
import utils.CacheUtils;
import utils.CookieUtils;
import utils.GlobalCons;
import views.html.index;
import views.html.login;
import views.html.admins.workspace.selection;
import views.html.tools.errors.bad_request;
import views.html.tools.errors.forbidden;
import views.html.tools.errors.no_workspace;
import controllers.admin.Workspaces;
import controllers.global.Profiles;
import enums.UserLogLevel;

/**
 * @author mdpinar
*/
public class Application extends Controller {

	private final static Logger log = LoggerFactory.getLogger(Application.class);
	private static ResourceBundle bundle;
	
	public static Result index() {
		changeLang(getLang());

		if (CacheUtils.isLoggedIn()) {
			if (! CacheUtils.isSuperUser() && CacheUtils.getWorkspaceId() == null) {
				return ok(selection.render(Workspaces.getAll()));
			} else {
				return ok(index.render());
			}
		} else {
			return ok(login.render(form(Login.class)));
		}
	}

	public static Result login() {
		bundle = null;

		CookieUtils.cleanAll();
		CacheUtils.cleanAll();

		flash("success", Messages.get("please.login"));

		return redirect("/");
	}

	public static Result authenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if(loginForm.hasErrors()) {
			return badRequest(login.render(loginForm));
		} else {
			new AdminUserAudit(loginForm.get().username, new Date(), null, null, UserLogLevel.Login).save();

			return redirect(
				routes.Application.index()
			);
		}
	}

	public static Result logout() {
		if (CacheUtils.isLoggedIn()) {
			flash("success", Messages.get("logged.out"));
			new AdminUserAudit(CacheUtils.getUser().username, new Date(), null, null, UserLogLevel.Logout).save();
		}

		return redirect(
			routes.Application.login()
		);
	}

	public static Result workspaces() {
		return workspaces(null);
	}

	public static Result workspaces(String message) {
		if (CacheUtils.isLoggedIn()) {
			if (message != null) flash("success", message);
			return ok(selection.render(Workspaces.getAll()));
		} else {
			return ok(login.render(form(Login.class)));
		}
	}

	public static Result getNoWorkspaceResult() {
		return forbidden(no_workspace.render());
	}

	public static Result getForbiddenResult() {
		return forbidden(forbidden.render());
	}

	public static Result getBadRequestResult() {
		return badRequest(bad_request.render());
	}

	public static Result getCurrentPageResult() {
		String referer = request().getHeader("referer");
		if (referer.endsWith("login")) {
			return redirect("/");
		} else {
			return redirect(request().getHeader("referer"));
		}
	}

	public static Result changeLanguage(String lang) {
		if (! CacheUtils.isLoggedIn() || lang == null || ! GlobalCons.getLangMap().containsKey(lang)) return login();

		response().setCookie("PLAY_LANG", lang, 200000);
		changeLang(lang);

		bundle = null;
		CacheUtils.removeMenu();

		return getCurrentPageResult();
	}

	public static String getLang() {
		Cookie langCookie = request().cookies().get("PLAY_LANG");

		if (langCookie == null) {
			response().setCookie("PLAY_LANG", GlobalCons.defaultLang, 200000);
			changeLang(GlobalCons.defaultLang);
			return GlobalCons.defaultLang;
		} else {
			return langCookie.value();
		}
	}

	public static Result changeProfile(String profile) {
		if (! CacheUtils.isLoggedIn()) return login();

		Profiles.setChoosen(profile);
		CacheUtils.removeMenu();

		return getCurrentPageResult();
	}

	public static Result changeWorkspace(String name) {
		if (! CacheUtils.isLoggedIn()) return login();

		if (! isActiveWorkspace(name)) {
			AdminWorkspace ws = Workspaces.isRightUserForWS(CacheUtils.getUser(), name);
			if (ws != null) CacheUtils.setWorkspace(ws.id);
		}
		CacheUtils.removeMenu();

		return getCurrentPageResult();
	}

	public static boolean isActiveWorkspace(String name) {
		return (name != null && name.equals(CacheUtils.getWorkspaceName()));
	}

	public static boolean isActiveProfile(String name) {
		return (name != null && name.equals(CacheUtils.getProfile()));
	}

	public static boolean isActiveLang(String name) {
		return (name != null && name.equals(getLang()));
	}

	public static ResourceBundle getResourceBundle() {
		if (bundle != null) {
			return bundle;
		} else {
			FileInputStream fis;
			try {
				fis = new FileInputStream(Play.application().path().getAbsolutePath() + "/conf/messages." + getLang());
				return new PropertyResourceBundle(new InputStreamReader(fis, "UTF-8"));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}

		return null;
	}

	public static class Login {

		public String username;
		public String password;

		public String validate() {

			String result = AuthManager.authenticate(username, password);
			if(result != null) {
				return result;
			}
			return null;
		}

	}

}

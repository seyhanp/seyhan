/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.admin;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import meta.GridHeader;
import meta.PageExtend;
import models.AdminUser;
import models.AdminWorkspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CacheUtils;
import utils.DateUtils;
import views.html.admins.workspace.form;
import views.html.admins.workspace.list;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import controllers.Application;
import controllers.global.Profiles;
import data.transfer.ws2ws.Ws2WsTransferManager;

/**
 * @author mdpinar
*/
public class Workspaces extends Controller {

	private final static Logger log = LoggerFactory.getLogger(Workspaces.class);
	private final static Form<AdminWorkspace> dataForm = form(AdminWorkspace.class);
	private static Map<String, AdminWorkspace> wsMap;
	private static Map<Integer, List<Integer>> wsIdListMap;
	private static Map<Integer, List<AdminWorkspace>> wsMapForGroup;

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("name"), "12%", false, true).sortable("name"));
		headerList.add(new GridHeader(Messages.get("description")));
		headerList.add(new GridHeader(Messages.get("date.start"), "8%", "center", null));
		headerList.add(new GridHeader(Messages.get("date.end"), "8%", "center", null));
		headerList.add(new GridHeader(Messages.get("has_restriction"), "7%", true));
		headerList.add(new GridHeader(Messages.get("is_active"), "7%", true));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<AdminWorkspace> buildPage() {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		List<AdminWorkspace> modelList = AdminWorkspace.page();
		if (modelList != null && modelList.size() > 0) {
			for (AdminWorkspace model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, model.name);
				dataMap.put(i++, model.description);
				dataMap.put(i++, DateUtils.formatDateStandart(model.startDate));
				dataMap.put(i++, DateUtils.formatDateStandart(model.endDate));
				dataMap.put(i++, model.hasDateRestriction.toString());
				dataMap.put(i++, model.isActive.toString());

				dataList.add(dataMap);
			}
		}

		return new PageExtend<AdminWorkspace>(getHeaderList(), dataList, null);
	}

	public static Result GO_HOME = redirect(
		controllers.admin.routes.Workspaces.list()
	);

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(
			list.render(buildPage())
		);
	}

	public static Result list(String message) {
		if (message != null) flash("success", message);
		return list();
	}

	/**
	 * Kayit formundaki bilgileri kaydeder
	 */
	public static Result save() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<AdminWorkspace> filledForm = dataForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(form.render(filledForm));
		} else {

			AdminWorkspace model = filledForm.get();
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
				return badRequest(form.render(dataForm.fill(model)));
			}

			destroyMaps();

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
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		return ok(form.render(dataForm.fill(new AdminWorkspace())));
	}

	/**
	 * Secilen kayit icin duzenleme formunu acar
	 * 
	 * @param id
	 */
	public static Result edit(Integer id) {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			AdminWorkspace model = AdminWorkspace.findById(id);
			if (model == null) {
				flash("error", Messages.get("not.found", Messages.get("user")));
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
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		if (id == null) {
			return badRequest(Messages.get("id.is.null"));
		} else {
			AdminWorkspace model = AdminWorkspace.findById(id);
			if (model == null) {
				return badRequest(Messages.get("not.found", Messages.get("workspace")));
			} else {
				Ebean.beginTransaction();
				try {
					Ws2WsTransferManager.destroy(model.id, true);
					model.delete();
					destroyMaps();
					Ebean.commitTransaction();

					flash("success", Messages.get("deleted", model.name));
				} catch (PersistenceException pe) {
					Ebean.rollbackTransaction();
					flash("error", Messages.get("delete.violation", model.name));
					log.error("ERROR", pe);
				}
			}
		}
		return GO_HOME;
	}

	public static void destroyMaps() {
		if (wsMap != null) wsMap.clear();
		if (wsIdListMap != null) wsIdListMap.clear();
		if (wsMapForGroup != null) wsMapForGroup.clear();
		wsMap = null;
		wsIdListMap = null;
		wsMapForGroup = null;
	}

	public static Map<String, AdminWorkspace> getWsMap() {
		if (wsMap == null) {
			wsMap = new HashMap<String, AdminWorkspace>();
			List<AdminWorkspace> wsList = getAll();
			for (AdminWorkspace ws : wsList) {
				wsMap.put(ws.name, ws);
			}
		}

		return wsMap;
	}

	public static AdminWorkspace isRightUserForWS(AdminUser user, String wsName) {
		if (user == null) return null;

		AdminWorkspace found = Workspaces.getWsMap().get(wsName);

		if (user.id.intValue() == 1 || user.isAdmin) {
			return found;
		} else {
			List<Integer> idList = getWorkspaceIdList(user.userGroup.id);
			if (found != null && idList != null && idList.size() > 0 && idList.contains(found.id)) {
				return found;
			}

			return null;
		}
	}

	public static AdminWorkspace isRightUserForWS(AdminUser user) {
		if (user == null || user.workspace == null) return null;

		AdminWorkspace found = AdminWorkspace.findById(user.workspace);

		if (user.id.intValue() == 1 || user.isAdmin) {
			return found;
		} else {
			List<Integer> idList = getWorkspaceIdList(user.userGroup.id);
			if (found != null && idList != null && idList.size() > 0 && idList.contains(found.id)) {
				return found;
			}

			return null;
		}
	}

	public static List<Integer> getWorkspaceIdList(Integer groupId) {
		List<Integer> result = null;

		if (wsIdListMap == null) {
			wsIdListMap = new HashMap<Integer, List<Integer>>();
		}
		
		boolean isSuperUser = CacheUtils.isSuperUser();
		
		if (isSuperUser) {
			result = wsIdListMap.get(0);
		} else if (groupId != null) {
			result = wsIdListMap.get(groupId);
		}
		if (result == null) result = new ArrayList<Integer>();

		if (result.size() == 0) {
			StringBuilder querySB = new StringBuilder("select workspace_id from admin_user_given_role ");
			querySB.append("inner join admin_workspace w on w.id = workspace_id ");

			if (isSuperUser) {
				querySB.append("where 1=1 ");
			} else if (groupId != null) { 
				querySB.append("where user_group_id = " + groupId);
			} else { //there is a problem!
				querySB.append("where 1=0 ");
			}
			querySB.append(" and is_active = :active order by name");

			List<SqlRow> roleRows = Ebean.createSqlQuery(querySB.toString()).setParameter("active", true).findList();
			if (roleRows != null && roleRows.size() > 0) {
				for(SqlRow roleRow: roleRows) {
					result.add(roleRow.getInteger("workspace_id"));
				}
			}
			wsIdListMap.put((isSuperUser ? 0 : groupId.intValue()), result);
		}

		return result;
	}

	public static List<AdminWorkspace> getAll() {
		return getAll(null);
	}

	public static List<AdminWorkspace> getAll(AdminUser user) {
		List<AdminWorkspace> result = null;

		AdminUser usr = user;
		if (usr == null) usr = CacheUtils.getUser();
		
		if (wsMapForGroup == null) {
			wsMapForGroup = new HashMap<Integer, List<AdminWorkspace>>();
		}

		boolean isSuperUser = CacheUtils.isSuperUser();

		if (isSuperUser) {
			result = wsMapForGroup.get(0);
		} else if (usr != null && usr.userGroup != null) {
			result = wsMapForGroup.get(usr.userGroup.id);
		}

		boolean isExist = false;
		
		if (result == null) {
			result = AdminWorkspace.getAll();
		} else {
			isExist = true;
		}

		if (! isExist) {
			if (isSuperUser) {
				wsMapForGroup.put(0, result);
			} else if (usr != null && usr.userGroup != null) {
				wsMapForGroup.put(usr.userGroup.id, result);
			}
		}

		return result;
	}

	/**
	 * Kayit isleminden once form uzerinde bulunan verilerin uygunlugunu kontrol eder
	 * 
	 * @param filledForm
	 */
	private static void checkConstraints(Form<AdminWorkspace> filledForm) {
		AdminWorkspace model = filledForm.get();

		if (AdminWorkspace.isUsedForElse("name", model.name, model.id)) {
			filledForm.reject("name", Messages.get("not.unique", model.name));
		}
	}

}

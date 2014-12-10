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

import meta.GridHeader;
import meta.PageExtend;
import models.AdminUserAudit;
import models.search.UserAuditSearchParam;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CacheUtils;
import utils.DateUtils;
import views.html.admins.user_audit.list;

import com.avaje.ebean.Page;

import controllers.Application;

/**
 * @author mdpinar
*/
public class UserAudits extends Controller {

	private final static Form<UserAuditSearchParam> paramForm = form(UserAuditSearchParam.class);

	/**
	 * Liste formu basliklarini doner
	 * 
	 * @return List<GridHeader>
	 */
	private static List<GridHeader> getHeaderList() {
		List<GridHeader> headerList = new ArrayList<GridHeader>();
		headerList.add(new GridHeader(Messages.get("date"), "14%", "center", null).sortable("date"));
		headerList.add(new GridHeader(Messages.get("name"), "10%").sortable("username"));
		headerList.add(new GridHeader("IP", "10%").sortable("ip").sortable("ip"));
		headerList.add(new GridHeader(Messages.get("section"), "14%").sortable("right"));
		headerList.add(new GridHeader(Messages.get("action"), "8%", "center", null).sortable("logLevel"));
		headerList.add(new GridHeader(Messages.get("description")));

		return headerList;
	}

	/**
	 * Liste formunda gosterilecek verileri doner
	 * 
	 * @return PageExtend
	 */
	private static PageExtend<AdminUserAudit> buildPage(UserAuditSearchParam searchParam) {
		List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();

		Page<AdminUserAudit> page = AdminUserAudit.page(searchParam);
		List<AdminUserAudit> modelList = page.getList();
		if (modelList != null && modelList.size() > 0) {
			for (AdminUserAudit model : modelList) {
				Map<Integer, String> dataMap = new HashMap<Integer, String>();
				int i = -1;
				dataMap.put(i++, model.id.toString());
				dataMap.put(i++, DateUtils.formatDate(model.date, "yyyy/MM/dd HH:mm:ss"));
				dataMap.put(i++, model.username);
				dataMap.put(i++, model.ip);
				dataMap.put(i++, (model.right != null ? Messages.get(model.right.key) : ""));
				dataMap.put(i++, model.logLevel.name().toUpperCase());
				dataMap.put(i++, model.description);

				dataList.add(dataMap);
			}
		}

		return new PageExtend<AdminUserAudit>(getHeaderList(), dataList, page);
	}

	/**
	 * Uzerinde veri bulunan liste formunu doner
	 */
	public static Result list() {
		if (! CacheUtils.isSuperUser()) return Application.getForbiddenResult();

		Form<UserAuditSearchParam> filledParamForm = paramForm.bindFromRequest();

		return ok(list.render(buildPage(filledParamForm.get()), filledParamForm));
	}

}

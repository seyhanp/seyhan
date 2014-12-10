/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import models.search.UserAuditSearchParam;
import models.temporal.Pair;
import play.db.ebean.Model;
import play.mvc.Http;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import enums.Right;
import enums.UserLogLevel;

@Entity
/**
 * @author mdpinar
*/
public class AdminUserAudit extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	public String workspace;

	public String username;

	@Column(name = "_date")
	public Date date;

	@Column(name = "_right")
	public Right right;

	public UserLogLevel logLevel;

	public String ip;

	public String description;

	private static Model.Finder<Integer, AdminUserAudit> find = new Model.Finder<Integer, AdminUserAudit>(Integer.class, AdminUserAudit.class);

	public AdminUserAudit(String username, Date date, Right right, String description, UserLogLevel logLevel) {
		super();
		this.workspace = CacheUtils.getWorkspaceName();
		this.username = username;
		this.date = date;
		this.right = right;
		this.logLevel = logLevel;
		this.description = description;
		try {
			this.ip = Http.Context.current().request().remoteAddress();
		} catch (Exception e) {
			this.ip = "unknown";
		}
	}

	public static Page<AdminUserAudit> page(UserAuditSearchParam searchParam) {
		ExpressionList<AdminUserAudit> expList = find.where();

		if (searchParam.workspace != null && ! searchParam.workspace.isEmpty()) {
			expList.eq("workspace", searchParam.workspace);
		}
		if (searchParam.username != null && ! searchParam.username.isEmpty()) {
			expList.eq("username", searchParam.username);
		}
		if (searchParam.ip != null && ! searchParam.ip.isEmpty()) {
			expList.like("ip", "%"+searchParam.ip+"%");
		}
		if (searchParam.startDate != null) {
			expList.ge("date", searchParam.startDate);
		}
		if (searchParam.endDate != null) {
			expList.le("date", searchParam.endDate);
		}

		List<String> actions = new ArrayList<String>();
		if (searchParam.loginAction != null && searchParam.loginAction) actions.add("Login");
		if (searchParam.logoutAction != null && searchParam.logoutAction) actions.add("Logout");
		if (searchParam.insertAction != null && searchParam.insertAction) actions.add("Insert");
		if (searchParam.updateAction != null && searchParam.updateAction) actions.add("Update");
		if (searchParam.deleteAction != null && searchParam.deleteAction) actions.add("Delete");
		if (actions.size() > 0) {
			expList.in("logLevel", actions);
		}

		Pair sortInfo = CookieUtils.getSortInfo(Right.KULLANICI_HAREKETLERI, "date", "desc");

		return expList.orderBy(sortInfo.key + " " + sortInfo.value + ", username")
							.findPagingList(50)
							.setFetchAhead(false)
						.getPage(searchParam.pageIndex);
	}

}

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
package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import models.search.NameOnlySearchParam;
import models.temporal.Pair;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;

import controllers.global.Profiles;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class AdminUser extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(20)
	public String username;

	@Constraints.MaxLength(30)
	public String title;

	public Boolean isAdmin = Boolean.FALSE;

	@Constraints.MaxLength(100)
	public String email;

	public String authToken;
	public String passwordHash;

	@Transient
	@Constraints.Required
	@Constraints.MinLength(4)
	@Constraints.MaxLength(30)
	public String password;

	@Transient
	@Constraints.Required
	@Constraints.MinLength(4)
	@Constraints.MaxLength(30)
	public String repeatPassword;

	@ManyToOne
	public AdminUserGroup userGroup;

	public String profile;
	public Integer workspace;

	public Boolean isActive = Boolean.TRUE;

	@Version
	public Integer version;

	private static Model.Finder<Integer, AdminUser> find = new Model.Finder<Integer, AdminUser>(Integer.class, AdminUser.class);

	public AdminUser() {
		super();
	}

	public AdminUser(String username) {
		this();
		this.isActive = true;
		this.username = username;
	}

	public static Page<AdminUser> page(NameOnlySearchParam searchParam) {
		ExpressionList<AdminUser> expList = find.where();

		if (!CacheUtils.isSuperUser()) expList.gt("id", 1);

		if (searchParam.fullText != null && !searchParam.fullText.isEmpty()) {
			expList.or(Expr.like("username", "%" + searchParam.fullText + "%"),
					Expr.like("title", "%" + searchParam.fullText + "%"));
		} else {
			if (searchParam.name != null && !searchParam.name.isEmpty()) {
				expList.like("username", searchParam.name + "%");
			}
		}

		Pair sortInfo = CookieUtils.getSortInfo(Right.KULLANICI_TANITIMI, "username");

		Page<AdminUser> page = expList
				.orderBy(sortInfo.key + " " + sortInfo.value)
				.findPagingList(Profiles.chosen().gnel_pageRowNumber)
				.setFetchAhead(false).getPage(searchParam.pageIndex);

		return page;
	}

	public static List<AdminUser> listAll() {
		List<AdminUser> result = CacheUtils.getListAll(AdminUser.class);
		if (result != null) return result;

		result = find
					.fetch("userGroup")
					.where()
						.eq("isActive", Boolean.TRUE)
					.orderBy("username")
				.findList();

		if (result != null && result.size() > 0) CacheUtils.setListAll(AdminUser.class, result);

		return result;
	}

	public static AdminUser findById(Integer id) {
		AdminUser result = CacheUtils.getById(AdminUser.class, id);

		if (result == null) {
			result = find.fetch("userGroup").where().eq("id", id).findUnique();
			if (result != null) CacheUtils.setById(AdminUser.class, id, result);
		}

		return result;
	}

	public static AdminUser findByUsername(String username) {
		return findByX("username", username);
	}

	public static AdminUser findByAuthToken(String authToken) {
		return findByX("authToken", authToken);
	}

	public static List<String> options() {
		List<String> options = CacheUtils.getListOptions(AdminUser.class);
		if (options != null)
			return options;

		List<AdminUser> userList = find.where().eq("isActive", Boolean.TRUE)
				.orderBy("username").findList();

		options = new ArrayList<String>();
		for (AdminUser model : userList) {
			options.add(model.username);
		}

		if (options.size() > 0)
			CacheUtils.setListOptions(AdminUser.class, options);

		return options;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<AdminUser> el = find.where().eq(field, value);
		if (id != null)
			el.ne("id", id);

		return el.findUnique() != null;
	}

	@Override
	public String toString() {
		return username;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(AdminUser.class, Right.KULLANICI_TANITIMI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(AdminUser.class, Right.KULLANICI_TANITIMI);
		super.update();
	}

	@Override
	public void delete() {
		CacheUtils.cleanAll(AdminUser.class, Right.KULLANICI_TANITIMI);
		super.delete();
	}

	public String createToken() {
		authToken = UUID.randomUUID().toString().replaceAll("\\-", "");
		save();
		return authToken;
	}

	public void deleteAuthToken() {
		authToken = null;
		save();
	}

	private static AdminUser findByX(String key, String value) {
		AdminUser result = CacheUtils.getByKeyValue(AdminUser.class, key, value);

		if (result == null) {
			result = find.fetch("userGroup").where().eq(key, value).eq("isActive", Boolean.TRUE).findUnique();
			if (result != null)CacheUtils.setByKeyValue(AdminUser.class, key, value, result);
		}

		return result;
	}

	public static void setProfileNull(String profile) {
		String query = "update admin_user set profile = null where profile = :profile";
		Ebean.createSqlUpdate(query).setParameter("profile", profile).execute();
	}

	public static void setWorkspaceNull(int workspace) {
		String query = "update admin_user set workspace = null where workspace = :workspace";
		Ebean.createSqlUpdate(query).setParameter("workspace", workspace)
				.execute();
	}
	
}

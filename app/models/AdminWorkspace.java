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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import models.temporal.Pair;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.ExpressionList;

import controllers.admin.Workspaces;
import enums.CacheKeys;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class AdminWorkspace extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	@Constraints.MaxLength(50)
	public String description;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date startDate;

	@DateTime(pattern = "dd/MM/yyyy")
	public Date endDate;

	public Boolean hasDateRestriction = Boolean.FALSE;

	public Boolean isActive = Boolean.TRUE;

	@Version
	public Integer version;

	private static Model.Finder<Integer, AdminWorkspace> find = new Model.Finder<Integer, AdminWorkspace>(Integer.class, AdminWorkspace.class);

	public static Map<String, String> options() {
		Map<String, String> options = CacheUtils.getMapOptions(AdminWorkspace.class);
		if (options != null) return options;

		List<AdminWorkspace> modelList = find.where()
													.eq("isActive", Boolean.TRUE)
												.orderBy("name")
											.findList();

		options = new LinkedHashMap<String, String>();
		for(AdminWorkspace model: modelList) {
			options.put(model.id.toString(), model.name);
		}

		if (options.size() > 0) CacheUtils.setMapOptions(AdminWorkspace.class, options);

		return options;
	}

	public static List<AdminWorkspace> page() {
		Pair sortInfo = CookieUtils.getSortInfo(Right.CALISMA_ALANI, "name");
		return find.where()
					.orderBy(sortInfo.key + " " + sortInfo.value)
				.findList();
	}

	public static List<AdminWorkspace> getAll() {
		List<AdminWorkspace> result = CacheUtils.get(CacheKeys.WORKSPACES);

		if (result == null) {
			result = new ArrayList<AdminWorkspace>();
			List<AdminWorkspace> wholeList = CacheUtils.getListAll(AdminWorkspace.class);
			
			if (wholeList == null) {
				wholeList = find.where()
									.eq("isActive", Boolean.TRUE)
								.orderBy("name")
								.findList();
				if (wholeList != null) CacheUtils.setListAll(AdminWorkspace.class, wholeList);
			}
			
			if (wholeList != null) {
				if (CacheUtils.isSpecialUser()) {
					result = wholeList;
				} else if (wholeList.size() > 0) {
					AdminUser usr = CacheUtils.getUser();
					if (usr != null) {
						List<Integer> idList = Workspaces.getWorkspaceIdList(usr.userGroup.id);
						for (AdminWorkspace ws : wholeList) {
							if (idList.contains(ws.id)) {
								result.add(ws);
							}
						}
					}
					
				}
			}
			CacheUtils.set(CacheKeys.WORKSPACES, result);
		}

		return result;
	}
	
	public static AdminWorkspace findById(Integer id) {
		AdminWorkspace result = CacheUtils.getById(AdminWorkspace.class, id);

		if (result == null) {
			result = find.byId(id);
			if (result != null) CacheUtils.setById(AdminWorkspace.class, id, result);
		}

		return result;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<AdminWorkspace> el = find.where().eq(field, value);
		if (id != null)
			el.ne("id", id);

		return el.findUnique() != null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(AdminWorkspace.class, Right.CALISMA_ALANI, CacheKeys.WORKSPACES);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(AdminWorkspace.class, Right.CALISMA_ALANI, CacheKeys.WORKSPACES);
		super.update();
		if (! isActive) AdminUser.setWorkspaceNull(id);
	}
	
	@Override
	public void delete() {
		CacheUtils.cleanAll(AdminWorkspace.class, Right.CALISMA_ALANI, CacheKeys.WORKSPACES);
		super.delete();
		AdminUser.setWorkspaceNull(id);
	}

}

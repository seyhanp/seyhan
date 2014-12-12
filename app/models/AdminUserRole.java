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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import models.temporal.Pair;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.ExpressionList;

import controllers.admin.UserRights;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class AdminUserRole extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="userRole", orphanRemoval = true)
	public List<AdminUserRight> rights;

	@Version
	public Integer version;

	private static Model.Finder<Integer, AdminUserRole> find = new Model.Finder<Integer, AdminUserRole>(Integer.class, AdminUserRole.class);

	public AdminUserRole(String name) {
		this.name = name;
	}

	public List<AdminUserRight> getRights() {
		List<AdminUserRight> definedRights = UserRights.definedRights();

		for (AdminUserRight or : rights) {
			if (definedRights.contains(or)) {
				definedRights.get(definedRights.indexOf(or)).id = or.id;
				definedRights.get(definedRights.indexOf(or)).rightLevel = or.rightLevel;
			}
		}
		rights = definedRights;

		return rights;
	}

	public static List<AdminUserRole> page() {
		Pair sortInfo = CookieUtils.getSortInfo(Right.KULLANICI_ROLLERI, "name");
		return find.orderBy(sortInfo.key + " " + sortInfo.value).findList();
	}

	public static AdminUserRole findById(Integer id) {
		AdminUserRole result = CacheUtils.getById(AdminUserRole.class, id);

		if (result == null) {
			result = find.byId(id);
			if (result != null) CacheUtils.setById(AdminUserRole.class, id, result);
		}

		return result;
	}

	public static AdminUserRole findByName(String name) {
		AdminUserRole result = CacheUtils.getByKeyValue(AdminUserRole.class, "name", name);

		if (result == null) {
			result = find
						.where()
						.eq("name", name)
					.findUnique();
			if (result != null) CacheUtils.setByKeyValue(AdminUserRole.class, "name", name, result);
		}

		return result;
	}

	public static List<AdminUserRole> getAll() {
		List<AdminUserRole> result = CacheUtils.getListAll(AdminUserRole.class);
		if (result != null) return result;

		result = find
					.where()
					.orderBy("name")
				.findList();

		if (result != null && result.size() > 0) CacheUtils.setListAll(AdminUserRole.class, result);

		return result;
	}

	public static Map<String, String> options() {
		Map<String, String> options = CacheUtils.getMapOptions(AdminUserRole.class);
		if (options != null) return options;

		List<AdminUserRole> modelList = find.where().orderBy("name").findList();

		options = new LinkedHashMap<String, String>();
		for(AdminUserRole model: modelList) {
			options.put(model.id.toString(), model.name);
		}

		if (options.size() > 0) CacheUtils.setMapOptions(AdminUserRole.class, options);

		return options;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<AdminUserRole> el = find.where().eq(field, value);
		if (id != null) el.ne("id", id);

		return el.findUnique() != null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(AdminUserRole.class, Right.KULLANICI_ROLLERI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(AdminUserRole.class, Right.KULLANICI_ROLLERI);
		super.update();
	}
	
	@Override
	public void delete() {
		CacheUtils.cleanAll(AdminUserRole.class, Right.KULLANICI_ROLLERI);
		super.delete();
	}

}

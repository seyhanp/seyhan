/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import controllers.admin.Workspaces;
import enums.Right;
import enums.UserEditingLimit;

@Entity
/**
 * @author mdpinar
*/
public class AdminUserGroup extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(20)
	public String name;

	@Constraints.MaxLength(50)
	public String description;

	public Integer editingTimeout = 0;
	public UserEditingLimit editingLimit = UserEditingLimit.Free;
	public Boolean hasEditDifDate = Boolean.FALSE;

	@OneToMany(cascade = CascadeType.ALL, mappedBy ="userGroup", orphanRemoval = true)
	public List<AdminUserGivenRole> roles = new ArrayList<AdminUserGivenRole>();

	@Version
	public Integer version;

	private static Model.Finder<Integer, AdminUserGroup> find = new Model.Finder<Integer, AdminUserGroup>(Integer.class, AdminUserGroup.class);

	public static Map<String, String> options() {
		Map<String, String> options = CacheUtils.getMapOptions(AdminUserGroup.class);
		if (options != null) return options;

		List<AdminUserGroup> modelList = find.where().orderBy("name").findList();

		options = new LinkedHashMap<String, String>();
		for(AdminUserGroup model: modelList) {
			options.put(model.id.toString(), model.name);
		}

		if (options.size() > 0) CacheUtils.setMapOptions(AdminUserGroup.class, options);

		return options;
	}

	public static List<AdminUserGroup> page() {
		Pair sortInfo = CookieUtils.getSortInfo(Right.KULLANICI_GURUPLARI, "name");
		return find.orderBy(sortInfo.key + " " + sortInfo.value).findList();
	}

	public static AdminUserGroup findById(Integer id) {
		AdminUserGroup result = CacheUtils.getById(AdminUserGroup.class, id);

		if (result == null) {
			result = find.byId(id);
			if (result != null) CacheUtils.setById(AdminUserGroup.class, id, result);
		}

		return result;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<AdminUserGroup> el = find.where().eq(field, value);
		if (id != null) el.ne("id", id);

		return el.findUnique() != null;
	}

	public void loadMissingRoles() {
		Set<String> nameSet = new HashSet<String>();
		for (AdminUserGivenRole role: roles) {
			nameSet.add(role.workspace.name);
		}

		List<AdminWorkspace> wsList = Workspaces.getAll();
		for (AdminWorkspace ws : wsList) {
			if (! nameSet.contains(ws.name)) roles.add(new AdminUserGivenRole(ws));
		}
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(AdminUserGroup.class, Right.KULLANICI_GURUPLARI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(AdminUserGroup.class, Right.KULLANICI_GURUPLARI);
		super.update();
	}
	
	@Override
	public void delete() {
		CacheUtils.cleanAll(AdminUserGroup.class, Right.KULLANICI_GURUPLARI);
		super.delete();
	}

}

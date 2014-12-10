/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

import models.temporal.Pair;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.ExpressionList;

import enums.CacheKeys;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class GlobalProfile extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(20)
	public String name;

	@Constraints.MaxLength(30)
	public String description;

	public Boolean isActive = Boolean.TRUE;

	@Lob
	public String jsonData;

	@Version
	public Integer version;

	private static Model.Finder<Integer, GlobalProfile> find = new Model.Finder<Integer, GlobalProfile>(Integer.class, GlobalProfile.class);

	public static List<GlobalProfile> page() {
		Pair sortInfo = CookieUtils.getSortInfo(Right.GNEL_PROFIL_TANITIMI, "name");
		return find.where()
					.orderBy(sortInfo.key + " " + sortInfo.value)
				.findList();
	}

	public static List<String> getNames() {
		List<String> result = CacheUtils.get(true, Right.GNEL_PROFIL_TANITIMI.name() + CacheKeys.LIST_ALL.value);

		if (result == null) {
			List<GlobalProfile> modelList = find.select("name")
												.where()
													.eq("isActive", Boolean.TRUE)
												.orderBy("name")
											.findList();
			result = new ArrayList<String>();
			for (GlobalProfile gp : modelList) {
				result.add(gp.name);
			}
			if (result.size() == 0) {
				result.add("default");
				CacheUtils.setProfile("default");
			}
			CacheUtils.set(true, Right.GNEL_PROFIL_TANITIMI.name() + CacheKeys.LIST_ALL.value, result);
		}

		return result;
	}

	public static GlobalProfile findById(Integer id) {
		GlobalProfile result = CacheUtils.getById(GlobalProfile.class, id);

		if (result == null) {
			result = find.byId(new Integer(id));
			if (result != null) CacheUtils.setById(GlobalProfile.class, id, result);
		}

		return result;
	}

	public static GlobalProfile findByName(String name) {
		GlobalProfile result = CacheUtils.getByKeyValue(GlobalProfile.class, "name", name);

		if (result == null) {
			result = find.where()
							.eq("isActive", Boolean.TRUE)
							.eq("name", name)
						.findUnique();
			if (result != null) CacheUtils.setByKeyValue(GlobalProfile.class, "name", name, result);
		}

		return result;
	}

	public static GlobalProfile findFirst() {
		List<GlobalProfile> result = CacheUtils.get(true, Right.GNEL_PROFIL_TANITIMI.name() + CacheKeys.LIST_ALL.value);

		if (result == null) {
			result = find.where()
							.eq("isActive", Boolean.TRUE)
						.orderBy("name")
					.findList();
			CacheUtils.set(true, Right.GNEL_PROFIL_TANITIMI.name() + CacheKeys.LIST_ALL.value, result);
		}

		if (result != null && result.size() > 0)
			return result.get(0);
		else
			return null;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<GlobalProfile> el = find.where().eq(field, value);
		if (id != null)
			el.ne("id", id);

		return el.findUnique() != null;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(GlobalProfile.class, Right.GNEL_PROFIL_TANITIMI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(GlobalProfile.class, Right.GNEL_PROFIL_TANITIMI);
		super.update();
		if (! isActive) AdminUser.setProfileNull(name);
	}

	@Override
	public void delete() {
		CacheUtils.cleanAll(GlobalProfile.class, Right.GNEL_PROFIL_TANITIMI);
		super.delete();
		AdminUser.setProfileNull(name);
	}

	@Override
	public String toString() {
		return name;
	}

}

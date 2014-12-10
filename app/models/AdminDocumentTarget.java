/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import models.temporal.Pair;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.ExpressionList;

import enums.CacheKeys;
import enums.DocTargetType;
import enums.DocViewType;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class AdminDocumentTarget extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Integer id;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	public Boolean isLocal = Boolean.TRUE;
	public DocTargetType targetType = DocTargetType.FILE;
	public DocViewType viewType = DocViewType.PORTRAIT;

	public String path;
	public Boolean isCompressed = Boolean.TRUE;

	@Constraints.MaxLength(30)
	public String description;

	public Boolean isActive = Boolean.TRUE;

	@Version
	public Integer version;

	private static Model.Finder<Integer, AdminDocumentTarget> find = new Model.Finder<Integer, AdminDocumentTarget>(Integer.class, AdminDocumentTarget.class);

	public static Map<String, String> options() {
		Map<String, String> result = CacheUtils.get(true, Right.BELGE_HEDEFLERI.name() + CacheKeys.OPTIONS.value);
		
		if (result == null) {
			result = new LinkedHashMap<String, String>();
		}

		List<AdminDocumentTarget> modelList = find.where()
												.eq("isActive", Boolean.TRUE)
											.orderBy("name, path")
										.findList();
		StringBuilder sb = new StringBuilder();
		for (AdminDocumentTarget gd : modelList) {
			sb.setLength(0);
			sb.append(gd.id);
			sb.append("|");
			sb.append(gd.isLocal);
			sb.append("|");
			sb.append(gd.targetType);
			sb.append("|");
			sb.append(gd.path);
			sb.append("|");
			sb.append(gd.description);
			result.put(sb.toString(), gd.toString());
		}
		CacheUtils.set(true, Right.BELGE_HEDEFLERI.name() + CacheKeys.OPTIONS.value, result);

		return result;
	}

	public static List<AdminDocumentTarget> page() {
		Pair sortInfo = CookieUtils.getSortInfo(Right.BELGE_HEDEFLERI, "name");
		return find.where()
					.orderBy(sortInfo.key + " " + sortInfo.value)
				.findList();
	}

	public static AdminDocumentTarget findById(Integer id) {
		return find.byId(id);
	}

	public static AdminDocumentTarget findByName(String name) {
		AdminDocumentTarget result = CacheUtils.getByKeyValue(AdminDocumentTarget.class, "name", name);

		if (result == null) {
			result = find.where()
							.eq("isActive", Boolean.TRUE)
							.eq("name", name)
						.findUnique();
			if (result != null) CacheUtils.setByKeyValue(AdminDocumentTarget.class, "name", name, result);
		}

		return result;
	}

	public static boolean isUsedForElse(String field, Object value, Integer id) {
		ExpressionList<AdminDocumentTarget> el = find.where().eq(field, value);
		if (id != null)
			el.ne("id", id);

		return el.findUnique() != null;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(AdminDocumentTarget.class, Right.BELGE_HEDEFLERI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(AdminDocumentTarget.class, Right.BELGE_HEDEFLERI);
		super.update();
	}

	@Override
	public void delete() {
		CacheUtils.cleanAll(AdminDocumentTarget.class, Right.BELGE_HEDEFLERI);
		super.delete();
	}

	@Override
	public String toString() {
		return name;
	}

}

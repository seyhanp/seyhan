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

import models.temporal.Pair;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.i18n.Messages;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.ExpressionList;

import enums.ChqbllSort;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class ChqbllType extends BaseModel {

	private static final long serialVersionUID = 1L;
	
	/*
	 * Cek mi / Senet mi?
	 */
	@Constraints.Required
	public ChqbllSort sort;

	@Constraints.Required
	@Constraints.MinLength(3)
	@Constraints.MaxLength(30)
	public String name;

	public Boolean isActive = Boolean.TRUE;

	private static Model.Finder<Integer, ChqbllType> find = new Model.Finder<Integer, ChqbllType>(Integer.class, ChqbllType.class);

	public ChqbllType(ChqbllSort sort) {
		super();
		this.sort = sort;
		this.name = "";
	}

	public static Map<String, String> options(ChqbllSort sort) {
		Map<String, String> options = CacheUtils.getMapOptions(ChqbllType.class, sort.name());
		if (options != null) return options;

		List<ChqbllType> modelList = find.where()
										.eq("workspace", CacheUtils.getWorkspaceId())
										.eq("sort", sort)
										.eq("isActive", Boolean.TRUE)
										.orderBy("name")
									.findList();
		options = new LinkedHashMap<String, String>();
		for(ChqbllType model: modelList) {
			options.put(model.id.toString(), model.name);
		}

		if (options.size() > 0) CacheUtils.setMapOptions(ChqbllType.class, options, sort.name());

		return options;
	}

	public static List<ChqbllType> page(ChqbllSort sort) {
		Pair sortInfo = CookieUtils.getSortInfo(sort.equals(ChqbllSort.Cheque) ? Right.CEK_TURLERI : Right.SENET_TURLERI , "name");
		return find.where()
					.eq("workspace", CacheUtils.getWorkspaceId())
					.eq("sort", sort)
					.orderBy(sortInfo.key + " " + sortInfo.value)
				.findList();
	}

	public static ChqbllType findById(Integer id) {
		ChqbllType result = CacheUtils.getById(ChqbllType.class, id);

		if (result == null) {
			result = find.byId(id);
			if (result != null) CacheUtils.setById(ChqbllType.class, id, result);
		}

		return result;
	}

	public static boolean isUsedForElse(ChqbllSort sort, String field, Object value, Integer id) {
		ExpressionList<ChqbllType> el = 
				find.where()
						.eq("workspace", CacheUtils.getWorkspaceId())
						.eq("sort", sort)
						.eq(field, value);
		if (id != null) el.ne("id", id);

		return el.findUnique() != null;
	}

	@Override
	public Right getAuditRight() {
		return RIGHT(sort);
	}

	public static Right RIGHT(ChqbllSort sort) {
		if (ChqbllSort.Cheque.equals(sort))
			return Right.CEK_TURLERI;
		else
			return Right.SENET_TURLERI;
	}

	@Override
	public String getAuditDescription() {
		return Messages.get("audit.name") + this.name + " [" + this.sort + "]";
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(ChqbllType.class, Right.CEK_TURLERI);
		CacheUtils.cleanAll(ChqbllType.class, Right.SENET_TURLERI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(ChqbllType.class, Right.CEK_TURLERI);
		CacheUtils.cleanAll(ChqbllType.class, Right.SENET_TURLERI);
		super.update();
	}
	
	@Override
	public void delete() {
		CacheUtils.cleanAll(ChqbllType.class, Right.CEK_TURLERI);
		CacheUtils.cleanAll(ChqbllType.class, Right.SENET_TURLERI);
		super.delete();
	}

}

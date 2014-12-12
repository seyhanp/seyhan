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

import javax.persistence.Entity;

import models.temporal.Pair;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.i18n.Messages;
import utils.CacheUtils;
import utils.CookieUtils;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;

import enums.ChqbllSort;
import enums.Right;

@Entity
/**
 * @author mdpinar
*/
public class ChqbllPayrollSource extends BaseModel {

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

	public Right suitableRight;

	public Boolean isActive = Boolean.TRUE;

	private static Model.Finder<Integer, ChqbllPayrollSource> find = new Model.Finder<Integer, ChqbllPayrollSource>(Integer.class, ChqbllPayrollSource.class);

	public ChqbllPayrollSource(ChqbllSort sort) {
		super();
		this.sort = sort;
	}

	public static Map<String, String> options(ChqbllSort sort, Right right) {
		Map<String, String> options = CacheUtils.getMapOptions(ChqbllPayrollSource.class, sort.name());
		if (options != null) return options;

		ExpressionList<ChqbllPayrollSource> elList = find.where()
															.eq("workspace", CacheUtils.getWorkspaceId())
															.eq("sort", sort)
															.eq("isActive", Boolean.TRUE);
		options = new LinkedHashMap<String, String>();
		if (right != null) {
			elList.or(
				Expr.isNull("suitableRight"),
				Expr.eq("suitableRight", right)
			);
		}

		List<ChqbllPayrollSource> modelList = elList.orderBy("name").findList();

		for(ChqbllPayrollSource model: modelList) {
			options.put(model.id.toString(), model.name);
		}

		if (options.size() > 0) CacheUtils.setMapOptions(ChqbllPayrollSource.class, options, sort.name());

		return options;
	}

	public static List<ChqbllPayrollSource> page(ChqbllSort sort) {
		Pair sortInfo = CookieUtils.getSortInfo(sort.equals(ChqbllSort.Cheque) ? Right.CEK_BORDRO_KAYNAKLARI : Right.SENET_BORDRO_KAYNAKLARI , "name");
		return find.where()
				.eq("workspace", CacheUtils.getWorkspaceId())
				.eq("sort", sort)
				.orderBy(sortInfo.key + " " + sortInfo.value)
			.findList();
	}

	public static ChqbllPayrollSource findById(Integer id) {
		ChqbllPayrollSource result = CacheUtils.getById(ChqbllPayrollSource.class, id);

		if (result == null) {
			result = find.byId(id);
			if (result != null) CacheUtils.setById(ChqbllPayrollSource.class, id, result);
		}

		return result;
	}

	public static boolean isUsedForElse(ChqbllSort sort, String field, Object value, Integer id) {
		ExpressionList<ChqbllPayrollSource> el =
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
			return Right.CEK_BORDRO_KAYNAKLARI;
		else
			return Right.SENET_BORDRO_KAYNAKLARI;
	}

	@Override
	public String getAuditDescription() {
		return Messages.get("audit.name") + this.name;
	}

	@Override
	public void save() {
		CacheUtils.cleanAll(ChqbllPayrollSource.class, Right.CEK_BORDRO_KAYNAKLARI);
		CacheUtils.cleanAll(ChqbllPayrollSource.class, Right.SENET_BORDRO_KAYNAKLARI);
		super.save();
	}

	@Override
	public void update() {
		CacheUtils.cleanAll(ChqbllPayrollSource.class, Right.CEK_BORDRO_KAYNAKLARI);
		CacheUtils.cleanAll(ChqbllPayrollSource.class, Right.SENET_BORDRO_KAYNAKLARI);
		super.update();
	}
	
	@Override
	public void delete() {
		CacheUtils.cleanAll(ChqbllPayrollSource.class, Right.CEK_BORDRO_KAYNAKLARI);
		CacheUtils.cleanAll(ChqbllPayrollSource.class, Right.SENET_BORDRO_KAYNAKLARI);
		super.delete();
	}

}
